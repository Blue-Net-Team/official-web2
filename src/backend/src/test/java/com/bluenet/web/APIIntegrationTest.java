package com.bluenet.web;

import org.junit.jupiter.api.AfterEach;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.bluenet.web.application.message.MessageTemplateRegistry;
import com.bluenet.web.infrastructure.init.SystemUserInitializer;
import com.bluenet.web.infrastructure.security.util.UserCTX;
import com.bluenet.web.testconfig.TestSecurityConfig;
import com.bluenet.web.testcontainers.TestcontainersConfiguration;

/**
 * 接口层集成测试基类。
 * <p>
 * 适用于只验证 HTTP 契约的测试：{@code @MockitoBean} 替换 {@code XxxAppService} 与
 * {@code RequestConverter}/{@code ResponseConverter}，全部断言止步于 Controller 层。
 * 不适用于需要真实数据库的测试，那类测试请继承 {@link DBIntegrationTest}。
 * </p>
 *
 * <h2>为什么关闭 Flyway 迁移</h2>
 * <p>
 * 接口层测试已 mock 掉应用层，不会执行任何 SQL，Flyway 建立的 43 张表对被测逻辑没有意义。 原先每个用例执行 2 次
 * {@code flyway.clean()} 加 1 次 28 个迁移脚本，实测约 3 秒/用例， 对本层是纯开销，故通过
 * {@code spring.flyway.enabled=false} 跳过。
 * </p>
 *
 * <h2>为什么仍然保留 Testcontainers</h2>
 * <p>
 * {@code @SpringBootTest} 会实例化全部 {@code RepositoryImpl}，每个都需要
 * {@code SqlSessionFactory} 到 {@code DataSource} 的链路；{@code @MockitoBean} 只替换单个
 * Bean，不会阻止其他 {@code RepositoryImpl} 被构造。移除容器会导致上下文启动失败
 * （{@code No qualifying bean of type '...Mapper'}），因此容器必须保留，仅跳过迁移。
 * </p>
 *
 * <h2>维护约定：新增启动期访问数据库的组件时需同步补充替身</h2>
 * <p>
 * 迁移被跳过意味着数据库 schema 为空。任何在 Spring 上下文启动阶段访问数据库的组件都会
 * 导致启动失败，因此必须在下方声明对应的测试替身。当前已识别的启动期查库组件为：
 * </p>
 * <ul>
 * <li>{@link MessageTemplateRegistry}：{@code @PostConstruct} 查询消息模板表</li>
 * <li>{@link SystemUserInitializer}：{@code CommandLineRunner} 访问用户表</li>
 * </ul>
 * <p>
 * 新增此类组件时，上下文会显式启动失败并指向该组件，按失败信息补充替身即可。
 * </p>
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Testcontainers
@Import({ TestcontainersConfiguration.class, TestSecurityConfig.class })
@TestPropertySource(properties = "spring.flyway.enabled=false")
public abstract class APIIntegrationTest {

    /**
     * 启动期 {@code @PostConstruct} 会查询消息模板表；跳过迁移后 schema 不存在，故以替身替换。
     */
    @MockitoBean
    private MessageTemplateRegistry messageTemplateRegistry;

    /**
     * 启动期 {@code CommandLineRunner} 会访问用户表；跳过迁移后 schema 不存在，故以替身替换。
     */
    @MockitoBean
    private SystemUserInitializer systemUserInitializer;

    /**
     * 清理安全上下文，避免用例之间泄漏用户身份与权限。
     * <p>
     * 方法名刻意不与子类常见的 {@code tearDown()} 相同，以免子类覆盖后丢失清理逻辑； 与子类自身的 {@code @AfterEach}
     * 方法并行执行。
     * </p>
     *
     * @return 无返回值。
     */
    @AfterEach
    void clearUserContext() {
        UserCTX.clear();
    }
}
