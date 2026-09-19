package com.bluenet.web;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.bluenet.web.testcontainers.TestcontainersConfiguration;

/**
 * 数据库集成测试基类。
 * <p>
 * 适用于需要真实数据库的测试：Application Service 的用例编排与事务边界、RepositoryImpl 的 自定义 SQL
 * 与分页聚合、以及断言数据确实持久化到数据库的测试。提供真实 PostgreSQL 实例与 完整的 Flyway 迁移，并在每个用例前后重建 schema
 * 保证数据隔离。
 * </p>
 *
 * <h2>为什么不能关闭 Flyway</h2>
 * <p>
 * 本层测试的断言依赖真实 schema 与迁移插入的种子数据（例如 {@code tb_role} 中的角色记录， 测试需要通过
 * {@code roleMapper.selectByName("MEMBER")} 获取角色 ID）。关闭迁移会导致 这些断言失败。
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
    @Autowired
    private Flyway flyway;

    /**
     * 在每个集成测试用例开始前重建数据库 schema。
     *
     * @return 无返回值。
     */
    @BeforeEach
    void setUp() {
        // 每个集成测试用例都从 Flyway 重建后的空 schema 开始。
        flyway.clean();
        flyway.migrate();
    }

    /**
     * 在每个集成测试用例结束后清理数据库 schema。
     *
     * @return 无返回值。
     */
    @AfterEach
    void tearDown() {
        // Flyway clean 能正确处理新增外键表，避免逐表清理的依赖顺序问题。
        flyway.clean();
    }
}
