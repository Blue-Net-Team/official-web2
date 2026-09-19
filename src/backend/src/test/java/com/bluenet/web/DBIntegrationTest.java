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

@SpringBootTest
@ActiveProfiles("test")
@Testcontainers
@Import(TestcontainersConfiguration.class)
public abstract class BaseIntegrationTest {
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
