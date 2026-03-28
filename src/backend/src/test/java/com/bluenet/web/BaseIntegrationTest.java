package com.bluenet.web;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

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

import com.bluenet.web.testcontainers.TestcontainersConfiguration;

@SpringBootTest
@ActiveProfiles("test")
@Testcontainers
@Import(TestcontainersConfiguration.class)
public abstract class BaseIntegrationTest {

    @Autowired
    private DataSource dataSource;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private Flyway flyway;

    @BeforeEach
    void setUp() throws SQLException {
        // 清理所有表数据
        cleanAllTables();

        // 重新执行 Flyway 迁移
        flyway.clean();
        flyway.migrate();
    }

    @AfterEach
    void tearDown() throws SQLException {
        // 测试结束后清理所有数据
        cleanAllTables();
    }

    /**
     * 清理所有表数据
     */
    private void cleanAllTables() throws SQLException {
        List<String> tables = getAllTables();

        if (tables.isEmpty()) {
            return;
        }

        // 截断所有表（无级联，因为无外键）
        for (String table : tables) {
            try {
                jdbcTemplate.execute("TRUNCATE TABLE " + table);
            } catch (Exception e) {
                // 忽略不存在的表
            }
        }
    }

    /**
     * 获取所有表名
     */
    private List<String> getAllTables() throws SQLException {
        List<String> tables = new ArrayList<>();

        try (Connection conn = dataSource.getConnection();
                ResultSet rs = conn.getMetaData().getTables(null, "public", "tb_%", new String[] { "TABLE" })) {
            while (rs.next()) {
                tables.add(rs.getString("TABLE_NAME"));
            }
        }

        return tables;
    }
}
