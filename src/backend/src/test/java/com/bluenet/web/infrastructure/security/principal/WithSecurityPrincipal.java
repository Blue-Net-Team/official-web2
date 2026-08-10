package com.bluenet.web.infrastructure.security.principal;

import com.bluenet.web.domain.model.enumerate.Direction;
import org.springframework.security.test.context.support.WithSecurityContext;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * 测试用：以指定安全主体身份运行，同时填充 Spring Security 的 SecurityContext 和 UserCTX。
 * <p>
 * 使用方式示例：
 * </p>
 *
 * <pre>
 * &#64;SpringBootTest
 * &#64;AutoConfigureMockMvc
 * class MyControllerTest {
 *     &#64;Autowired
 *     MockMvc mockMvc;
 *
 *     &#64;AfterEach
 *     void tearDown() {
 *         UserCTX.clear(); // 必须清理，避免 ThreadLocal 污染后续测试
 *     }
 *
 *     &#64;Test
 *     &#64;WithSecurityPrincipal(userId = 1L, roleId = 2L, roleType = "MEMBER")
 *     void getMe_returnsOk() throws Exception {
 *         mockMvc.perform(get("/api/v1/user/me")).andExpect(status().isOk());
 *     }
 * }
 * </pre>
 */
@Retention(RetentionPolicy.RUNTIME)
@WithSecurityContext(factory = WithSecurityPrincipalContextFactory.class)
public @interface WithSecurityPrincipal {

    long userId() default 1L;

    long roleId() default 2L;

    String studentId() default "2024001001";

    String username() default "测试用户";

    String roleType() default "MEMBER";

    Direction direction() default Direction.COMPUTER_VISION;

    boolean noDirection() default false;

    /** 权限标识，如 user:info:me */
    String[] permissions() default {};
}
