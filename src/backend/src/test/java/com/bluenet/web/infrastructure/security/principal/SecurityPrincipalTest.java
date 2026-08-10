package com.bluenet.web.infrastructure.security.principal;

import com.bluenet.web.domain.model.entity.User;
import com.bluenet.web.domain.model.enumerate.RoleType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * SecurityPrincipal 单元测试。
 */
@DisplayName("SecurityPrincipal 测试")
class SecurityPrincipalTest {

    @Test
    @DisplayName("权限集合应被包装为不可变")
    void permissions_shouldBeImmutable() {
        Set<String> permissions = Set.of("user:read");
        SecurityPrincipal principal = new SecurityPrincipal(
                User.reconstruct(1L, "pwd"), RoleType.MEMBER, permissions);

        assertThrows(
                UnsupportedOperationException.class,
                () -> principal.permissions().add("user:write"));
    }

    @Test
    @DisplayName("userId 应返回用户ID")
    void userId_shouldReturnUserId() {
        User user = User.reconstruct(42L, "pwd");
        SecurityPrincipal principal = new SecurityPrincipal(user, RoleType.CANDIDATE, Set.of());

        assertEquals(42L, principal.userId());
    }

    @Test
    @DisplayName("用户为空时 userId 返回 null")
    void userId_withNullUser_shouldReturnNull() {
        SecurityPrincipal principal = new SecurityPrincipal(null, RoleType.MEMBER, Set.of());

        assertNull(principal.userId());
    }

    @Test
    @DisplayName("hasPermission 应正确判断权限")
    void hasPermission_shouldCheckPermission() {
        SecurityPrincipal principal = new SecurityPrincipal(
                User.reconstruct(1L, "pwd"), RoleType.MEMBER, Set.of("user:read", "user:write"));

        assertTrue(principal.hasPermission("user:read"));
        assertTrue(principal.hasPermission("user:write"));
        assertFalse(principal.hasPermission("admin:delete"));
    }

    @Test
    @DisplayName("null 权限集合应被转换为空集合")
    void nullPermissions_shouldBecomeEmptySet() {
        SecurityPrincipal principal = new SecurityPrincipal(
                User.reconstruct(1L, "pwd"), RoleType.MEMBER, null);

        assertNotNull(principal.permissions());
        assertTrue(principal.permissions().isEmpty());
    }
}
