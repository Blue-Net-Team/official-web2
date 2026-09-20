package com.bluenet.web;

import java.sql.ResultSetMetaData;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import javax.sql.DataSource;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.bluenet.web.domain.model.enumerate.RoleType;
import com.bluenet.web.testcontainers.TestcontainersConfiguration;
import com.bluenet.web.testsupport.fixture.RoleFixture;

/**
 * 数据库集成测试基类。
 * <p>
 * 适用于需要真实数据库的测试：Application Service 的用例编排与事务边界、RepositoryImpl 的自定义 SQL
 * 与分页聚合、以及断言数据确实持久化到数据库的测试。提供真实 PostgreSQL 实例与完整的 Flyway 迁移。
 * </p>
 *
 * <h2>迁移时机：按数据源只执行一次，而不是每个用例执行一次</h2>
 * <p>
 * 表结构在每个用例中完全相同，逐用例 {@code flyway.clean() + flyway.migrate()} 属于重复劳动（实测约 1.6
 * 秒/用例，其中 {@code clean} 约 0.25 秒、{@code migrate} 约 1.10 秒）。本基类改为按
 * {@code DataSource} 实例身份维护迁移状态：某个数据源首次出现时迁移一次并做种子快照，之后只做数据清空与回填。
 * </p>
 * <p>
 * 迁移状态必须按数据源隔离，不能用 JVM 全局布尔值：不同测试类因 {@code @MockitoBean} 组合不同会产生不同的 Spring
 * 上下文，进而是不同的 Testcontainers 实例与数据库。也正因如此，<b>迁移次数等于不同数据源的数量</b>， 而不是测试类数量——共享同一
 * Spring 上下文的多个测试类只会在第一个类开始时迁移一次。
 * </p>
 *
 * <h2>数据隔离：TRUNCATE 而不是重建 schema</h2>
 * <p>
 * 用例之间的隔离通过 {@code TRUNCATE ... RESTART IDENTITY CASCADE} 清空全部 {@code tb_} 表实现。
 * 仅有的 4 个外键集中于 judge 表并带 {@code ON DELETE CASCADE}，可由 {@code CASCADE} 一并处理。
 * </p>
 *
 * <h2>{@code tb_role}：只读参考表，不参与清空</h2>
 * <p>
 * 生产代码从不写 {@code tb_role}（{@code RoleRepository} 没有
 * {@code save}，{@code RoleMapper} 的生产侧调用全是读），唯一写入方是迁移。因此它不参与清空，从而无需恢复机制；角色 ID
 * 也由迁移保证稳定。 为守住这一约定，迁移完成后会对该表做内容快照，并在<b>每个用例结束时</b>比对，不一致即判定违规用例失败——
 * 放在结束时而非开始时，是为了把失败归因到真正写入的用例。
 * </p>
 * <p>
 * 注意：{@code tb_role.id} 由 {@code SERIAL} 生成，迁移只插入角色名，因此 ID 不可假定为固定值。 测试需要通过
 * {@code roleMapper.selectByName(...)}（或
 * {@link com.bluenet.web.testsupport.fixture.RoleFixture} 的按名称解析）获取角色 ID，不得硬编码。
 * </p>
 *
 * <h2>{@code tb_direction_learning_step}：可变种子表，用迁移快照回填</h2>
 * <p>
 * 该表在生产中可写（{@code LearningPathAppService.createStep}），因此必须参与清空；其种子数据又被 V26
 * 改标题、V28 写入 {@code sort_order}，最终态不等于 V1 初始态。为避免在测试代码中维护一份会静默漂移的
 * 重复数据，回填内容来自迁移完成后的内存快照；回填后按 {@code MAX(id)} 修正自增序列，避免后续插入主键冲突。
 * </p>
 *
 * <h2>为什么不能关闭 Flyway</h2>
 * <p>
 * 本层测试的断言依赖真实 schema 与迁移插入的种子数据（例如 {@code tb_role} 中的角色记录，测试需要通过
 * {@code roleMapper.selectByName("MEMBER")} 获取角色 ID）。关闭迁移会导致这些断言失败。
 * </p>
 * <p>
 * 仅验证 HTTP 契约、不需要数据库的测试请继承 {@link APIIntegrationTest}，该基类跳过了 Flyway 迁移，可显著缩短耗时。
 * </p>
 *
 * @see APIIntegrationTest
 */
@SpringBootTest
@ActiveProfiles("test")
@Testcontainers
@Import(TestcontainersConfiguration.class)
public abstract class DBIntegrationTest {

    /** 只读参考表：内容来自迁移，不参与清空，并在每个用例结束时校验未被修改。 */
    private static final String READ_ONLY_TABLE = "tb_role";

    /** 可变种子表：参与清空，清空后按迁移快照回填。 */
    private static final String SEEDED_TABLE = "tb_direction_learning_step";

    /** 业务表统一前缀（LIKE 模式，反斜杠转义下划线）。 */
    private static final String TABLE_PREFIX_PATTERN = "tb\\_%";

    /** 已完成迁移的数据源（以实例身份区分，因为 equals 未被重写的数据源本身就是身份比较）。 */
    private static final Set<DataSource> MIGRATED_DATA_SOURCES = ConcurrentHashMap.newKeySet();

    /** 各数据源在迁移完成时 {@code tb_role} 的内容快照，用作只读保护基线。 */
    private static final ConcurrentHashMap<DataSource, TableSnapshot> READ_ONLY_SNAPSHOTS = new ConcurrentHashMap<>();

    /** 各数据源在迁移完成时 {@code tb_direction_learning_step} 的种子快照，用于回填。 */
    private static final ConcurrentHashMap<DataSource, TableSnapshot> SEEDED_SNAPSHOTS = new ConcurrentHashMap<>();

    /** 保护「首次迁移 + 快照」的竞态。 */
    private static final Object MIGRATION_MONITOR = new Object();

    @Autowired
    private Flyway flyway;

    @Autowired
    private DataSource dataSource;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * 在每个集成测试用例开始前保证数据源已迁移，并把数据重置到「迁移完成、无业务数据」的基线状态。
     * <p>
     * 基类的 {@code @BeforeEach} 会先于子类的 {@code @BeforeEach} 执行，因此子类可以在此之后安全地准备测试数据。
     * </p>
     *
     * @return 无返回值。
     */
    @BeforeEach
    void prepareDatabase() {
        ensureMigrated();
        truncateBusinessTables();
        restoreSeededTable();
        bindRoleIdsForCurrentDataSource();
    }

    /**
     * 在每个集成测试用例结束时校验只读参考表未被修改。
     * <p>
     * 校验放在用例结束时，是为了把失败归因到真正执行写入的用例；若放在开始时，违规用例本身会通过，失败会出现在 下一个无关用例上，排查成本高。
     * </p>
     *
     * @return 无返回值。
     */
    @AfterEach
    void verifyReadOnlyReferenceTableUnchanged() {
        try {
            TableSnapshot expected = READ_ONLY_SNAPSHOTS.get(dataSource);
            if (expected == null) {
                // 迁移尚未成功（例如 @BeforeEach 已失败），此时无从比对。
                return;
            }
            if (!expected.equals(snapshot(READ_ONLY_TABLE))) {
                throw new AssertionError("测试写入了只读参考表 " + READ_ONLY_TABLE
                        + "：该表内容由 Flyway 迁移提供，运行期与测试均不得修改。"
                        + "请改用 RoleFixture.roleId(roleMapper, RoleType.X) 按名称解析角色，而不是新增或修改角色行。");
            }
        } finally {
            // 避免角色映射泄漏到后续不访问数据库的单元测试。
            RoleFixture.unbindRoleIds();
        }
    }

    /**
     * 把当前数据源 {@code tb_role} 中的「角色名 → 真实 ID」映射注入 {@link RoleFixture}，使测试夹具能够按名称解析角色
     * 而无需硬编码角色 ID。
     *
     * @return 无返回值。
     */
    private void bindRoleIdsForCurrentDataSource() {
        Map<RoleType, Long> roleIds = new EnumMap<>(RoleType.class);
        jdbcTemplate.query("SELECT id, name FROM " + READ_ONLY_TABLE, resultSet -> {
            RoleType roleType = RoleType.fromName(resultSet.getString("name"));
            if (roleType != null) {
                roleIds.put(roleType, resultSet.getLong("id"));
            }
        });
        RoleFixture.bindRoleIds(roleIds);
    }

    /**
     * 保证当前数据源恰好迁移一次，并在迁移完成后立即建立两份快照。
     *
     * @return 无返回值。
     */
    private void ensureMigrated() {
        if (MIGRATED_DATA_SOURCES.contains(dataSource)) {
            return;
        }
        synchronized (MIGRATION_MONITOR) {
            if (MIGRATED_DATA_SOURCES.contains(dataSource)) {
                return;
            }
            flyway.clean();
            flyway.migrate();
            READ_ONLY_SNAPSHOTS.put(dataSource, snapshot(READ_ONLY_TABLE));
            SEEDED_SNAPSHOTS.put(dataSource, snapshot(SEEDED_TABLE));
            MIGRATED_DATA_SOURCES.add(dataSource);
        }
    }

    /**
     * 清空全部 {@code tb_} 业务表，重置自增序列，并保留只读参考表。
     *
     * @return 无返回值。
     */
    private void truncateBusinessTables() {
        List<String> tables = jdbcTemplate.queryForList(
                "SELECT tablename FROM pg_tables "
                        + "WHERE schemaname = 'public' AND tablename LIKE '" + TABLE_PREFIX_PATTERN + "'",
                String.class);
        List<String> truncatable = tables.stream()
                .filter(table -> !READ_ONLY_TABLE.equals(table))
                .sorted()
                .collect(Collectors.toList());
        if (truncatable.isEmpty()) {
            return;
        }
        jdbcTemplate.execute("TRUNCATE TABLE " + String.join(", ", truncatable) + " RESTART IDENTITY CASCADE");
    }

    /**
     * 按迁移快照回填可变种子表，并修正其自增序列。
     *
     * @return 无返回值。
     */
    private void restoreSeededTable() {
        TableSnapshot snapshot = SEEDED_SNAPSHOTS.get(dataSource);
        if (snapshot == null || snapshot.rows().isEmpty()) {
            return;
        }
        String columns = String.join(", ", snapshot.columns());
        String placeholders = snapshot.columns().stream().map(column -> "?").collect(Collectors.joining(", "));
        String insertSql = "INSERT INTO " + SEEDED_TABLE + " (" + columns + ") VALUES (" + placeholders + ")";
        for (List<Object> row : snapshot.rows()) {
            jdbcTemplate.update(insertSql, row.toArray());
        }
        realignSeededTableSequence();
    }

    /**
     * 把种子表的自增序列推进到当前最大主键，避免回填（显式主键）之后的新插入发生主键冲突。
     *
     * @return 无返回值。
     */
    private void realignSeededTableSequence() {
        Long maxId = jdbcTemplate.queryForObject("SELECT COALESCE(MAX(id), 1) FROM " + SEEDED_TABLE, Long.class);
        jdbcTemplate.queryForObject(
                "SELECT setval(pg_get_serial_sequence(?, 'id'), ?)",
                Long.class,
                SEEDED_TABLE,
                maxId);
    }

    /**
     * 读取整张表的列顺序与全部行数据。
     *
     * @param table
     *            表名，仅接受代码内定义的常量。
     * @return 该表的内容快照。
     */
    private TableSnapshot snapshot(String table) {
        return jdbcTemplate.query("SELECT * FROM " + table, resultSet -> {
            ResultSetMetaData metaData = resultSet.getMetaData();
            int columnCount = metaData.getColumnCount();
            List<String> columns = new ArrayList<>(columnCount);
            for (int index = 1; index <= columnCount; index++) {
                columns.add(metaData.getColumnName(index));
            }
            List<List<Object>> rows = new ArrayList<>();
            while (resultSet.next()) {
                List<Object> row = new ArrayList<>(columnCount);
                for (int index = 1; index <= columnCount; index++) {
                    row.add(resultSet.getObject(index));
                }
                rows.add(row);
            }
            return new TableSnapshot(columns, rows);
        });
    }

    /**
     * 表内容快照：列顺序与全部行数据。
     *
     * @param columns
     *            列名（按查询返回顺序）。
     * @param rows
     *            行数据，每行按列顺序排列。
     */
    private record TableSnapshot(List<String> columns, List<List<Object>> rows) {
    }
}
