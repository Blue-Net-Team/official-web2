package com.bluenet.web.infrastructure.security.jwt;
import io.github.ivencn.infra.security.jwt.JwtService;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bluenet.web.DBIntegrationTest;
import com.bluenet.web.domain.model.entity.Permission;
import com.bluenet.web.domain.model.entity.User;
import com.bluenet.web.domain.model.enumerate.RoleType;
import com.bluenet.web.domain.repository.PermissionRepository;
import com.bluenet.web.domain.repository.RolePermissionRepository;
import com.bluenet.web.domain.repository.UserRepository;
import com.bluenet.web.infrastructure.repository.mapper.RoleMapper;
import io.github.ivencn.infra.security.principal.AccessLevel;
import io.github.ivencn.infra.security.annotation.RequiresPermission;
import io.github.ivencn.infra.security.auth.AuthTokenService;
import io.github.ivencn.infra.security.cookie.CookieProperties;
import com.bluenet.web.testconfig.TestSecurityConfig;
import com.bluenet.web.testsupport.fixture.PermissionFixture;
import com.bluenet.web.testsupport.fixture.RoleFixture;
import com.bluenet.web.testsupport.fixture.RolePermissionFixture;
import com.bluenet.web.testsupport.fixture.UserFixture;

import jakarta.servlet.http.Cookie;

/**
 * JwtAuthenticationFilter 端到端集成测试。
 * <p>
 * 与 {@code PermissionAspectIntegrationTest} 的区别在于认证方式：后者用
 * {@code @WithSecurityPrincipal} 直接把权限注入安全上下文，**绕过了权限的真实加载路径**； 本类走完整链路——真实 JWT
 * Cookie → {@link JwtAuthenticationFilter} → 持久层查询角色权限 → 构造 SecurityPrincipal →
 * PermissionAspect 判定。
 * </p>
 * <p>
 * 之所以需要这个类：权限曾经由启动期预加载的内存缓存提供，而该缓存在 Bean 初始化顺序上早于
 * 权限写入方，导致启动后缓存为空、非超管角色的权限校验全部失效。只注入权限的测试无法发现 这类缺陷，因为它们从不触碰加载路径。
 * </p>
 */
@DisplayName("JwtAuthenticationFilter 端到端集成测试")
@AutoConfigureMockMvc
@Import({ TestSecurityConfig.class, JwtAuthenticationFilterIntegrationTest.PermissionProbeController.class })
class JwtAuthenticationFilterIntegrationTest extends DBIntegrationTest {

    private static final String PROBE_PERMISSION = "it-auth-probe:protected";
    private static final String PROBE_ENDPOINT = "/api/v1/test/auth-probe/protected";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private AuthTokenService authTokenService;

    @Autowired
    private CookieProperties cookieProperties;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private RoleMapper roleMapper;

    @Autowired
    private PermissionRepository permissionRepository;

    @Autowired
    private RolePermissionRepository rolePermissionRepository;

    @Test
    @DisplayName("真实认证链路：角色已绑定权限时应放行受保护接口")
    void realAuthentication_withBoundPermission_shouldAllowAccess() throws Exception {
        Long roleId = RoleFixture.roleId(roleMapper, RoleType.MEMBER);
        Permission permission = PermissionFixture.save(permissionRepository, "认证链路探针权限", PROBE_PERMISSION);
        RolePermissionFixture.grant(rolePermissionRepository, roleId, permission.getId());
        Long userId = createUserWithRole(roleId);

        mockMvc.perform(get(PROBE_ENDPOINT).cookie(authCookie(issueToken(userId))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.endpoint").value("protected"));
    }

    @Test
    @DisplayName("真实认证链路：角色未绑定该权限时应拒绝访问")
    void realAuthentication_withoutBoundPermission_shouldForbidAccess() throws Exception {
        Long roleId = RoleFixture.roleId(roleMapper, RoleType.CANDIDATE);
        // 权限存在于权限表，但不与该角色绑定
        PermissionFixture.save(permissionRepository, "认证链路探针权限", PROBE_PERMISSION);
        Long userId = createUserWithRole(roleId);

        mockMvc.perform(get(PROBE_ENDPOINT).cookie(authCookie(issueToken(userId))))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("真实认证链路：未携带令牌时应按未认证处理，不授予任何权限")
    void realAuthentication_withoutToken_shouldNotAuthenticate() throws Exception {
        Long roleId = RoleFixture.roleId(roleMapper, RoleType.MEMBER);
        Permission permission = PermissionFixture.save(permissionRepository, "认证链路探针权限", PROBE_PERMISSION);
        RolePermissionFixture.grant(rolePermissionRepository, roleId, permission.getId());
        createUserWithRole(roleId);

        mockMvc.perform(get(PROBE_ENDPOINT))
                .andExpect(status().isUnauthorized());
    }

    /**
     * 为用户签发真实 JWT 并写入白名单，使 JwtAuthenticationFilter 能够完成认证。
     *
     * @param userId
     *            用户主键
     * @return JWT 字符串
     */
    private String issueToken(Long userId) {
        String token = jwtService.issue(userId);
        authTokenService.storeToken(jwtService.getJti(token), userId);
        return token;
    }

    /**
     * 构造认证 Cookie（过滤器优先从 Cookie 读取令牌）。
     *
     * @param token
     *            JWT 字符串
     * @return 认证 Cookie
     */
    private Cookie authCookie(String token) {
        return new Cookie(cookieProperties.getAuthCookieName(), token);
    }

    /**
     * 创建指定角色的用户。
     *
     * @param roleId
     *            角色主键
     * @return 用户主键
     */
    private Long createUserWithRole(Long roleId) {
        User user = UserFixture.member("2024009901").withRoleId(roleId).build();
        return UserFixture.save(userRepository, passwordEncoder, user).getId();
    }

    /**
     * 测试用的内部 Controller，提供一个受保护接口以驱动真实权限判定。
     */
    @RestController
    static class PermissionProbeController {

        @GetMapping(PROBE_ENDPOINT)
        @RequiresPermission(value = PROBE_PERMISSION, name = "认证链路探针权限", access = AccessLevel.PROTECTED, audit = false)
        public Map<String, String> protectedEndpoint() {
            return Map.of("endpoint", "protected");
        }
    }
}
