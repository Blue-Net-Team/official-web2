package com.bluenet.web.infrastructure.security.aspect;

import com.bluenet.web.BaseIntegrationTest;
import com.bluenet.web.infrastructure.security.annotation.AccessLevel;
import com.bluenet.web.infrastructure.security.annotation.RequiresPermission;
import com.bluenet.web.infrastructure.security.principal.WithSecurityPrincipal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.context.annotation.Import;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * PermissionAspect 集成测试。
 * <p>
 * 验证 @RequiresPermission 在不同访问级别和角色上下文下的拦截行为。
 * </p>
 */
@DisplayName("PermissionAspect 集成测试")
@AutoConfigureMockMvc
@Import(PermissionAspectIntegrationTest.PermissionTestController.class)
class PermissionAspectIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("PUBLIC 接口应允许匿名访问")
    @WithAnonymousUser
    void publicEndpoint_shouldAllowAnonymous() throws Exception {
        mockMvc.perform(get("/api/v1/test/permission/public"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("AUTHENTICATED 接口未登录应返回 401")
    @WithAnonymousUser
    void authenticatedEndpoint_anonymous_shouldReturn401() throws Exception {
        mockMvc.perform(get("/api/v1/test/permission/authenticated"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("AUTHENTICATED 接口已登录应允许访问")
    @WithSecurityPrincipal(roleType = "MEMBER")
    void authenticatedEndpoint_authenticated_shouldAllow() throws Exception {
        mockMvc.perform(get("/api/v1/test/permission/authenticated"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("PROTECTED 接口未登录应返回 401")
    @WithAnonymousUser
    void protectedEndpoint_anonymous_shouldReturn401() throws Exception {
        mockMvc.perform(get("/api/v1/test/permission/protected"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("PROTECTED 接口无权限应返回 403")
    @WithSecurityPrincipal(roleType = "MEMBER", permissions = {})
    void protectedEndpoint_noPermission_shouldReturn403() throws Exception {
        mockMvc.perform(get("/api/v1/test/permission/protected"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("PROTECTED 接口有权限应允许访问")
    @WithSecurityPrincipal(roleType = "MEMBER", permissions = "test:read")
    void protectedEndpoint_withPermission_shouldAllow() throws Exception {
        mockMvc.perform(get("/api/v1/test/permission/protected"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("PROTECTED 接口超级管理员应直接放行")
    @WithSecurityPrincipal(roleType = "SUPER_ADMIN", permissions = {})
    void protectedEndpoint_superAdmin_shouldAllow() throws Exception {
        mockMvc.perform(get("/api/v1/test/permission/protected"))
                .andExpect(status().isOk());
    }

    /**
     * 测试用的内部 Controller。
     */
    @RestController
    static class PermissionTestController {

        @GetMapping("/api/v1/test/permission/public")
        @RequiresPermission(value = "test:public", access = AccessLevel.PUBLIC)
        public java.util.Map<String, String> publicEndpoint() {
            return java.util.Map.of("endpoint", "public");
        }

        @GetMapping("/api/v1/test/permission/authenticated")
        @RequiresPermission(value = "test:authenticated", access = AccessLevel.AUTHENTICATED)
        public java.util.Map<String, String> authenticatedEndpoint() {
            return java.util.Map.of("endpoint", "authenticated");
        }

        @GetMapping("/api/v1/test/permission/protected")
        @RequiresPermission(value = "test:read", access = AccessLevel.PROTECTED)
        public java.util.Map<String, String> protectedEndpoint() {
            return java.util.Map.of("endpoint", "protected");
        }
    }
}
