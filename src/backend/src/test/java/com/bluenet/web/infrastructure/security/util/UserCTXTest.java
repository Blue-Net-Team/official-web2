package com.bluenet.web.infrastructure.security.util;

import com.bluenet.web.domain.model.entity.User;
import com.bluenet.web.domain.model.enumerate.RoleType;
import com.bluenet.web.infrastructure.security.principal.SecurityPrincipal;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * UserCTX 单元测试。
 */
@DisplayName("UserCTX 测试")
class UserCTXTest {

    @BeforeEach
    @AfterEach
    void cleanup() {
        UserCTX.clear();
    }

    @Test
    @DisplayName("设置和获取主体应一致")
    void setPrincipal_andGetPrincipal_shouldMatch() {
        SecurityPrincipal principal = new SecurityPrincipal(
                User.reconstruct(1L, "pwd"), RoleType.MEMBER, Set.of("user:read"));

        UserCTX.setPrincipal(principal);

        assertSame(principal, UserCTX.getPrincipal());
        assertNotNull(UserCTX.getCurrentUser());
        assertEquals(1L, UserCTX.getCurrentUserId());
        assertEquals(RoleType.MEMBER, UserCTX.getCurrentRoleType());
        assertEquals(Set.of("user:read"), UserCTX.getCurrentPermissions());
        assertTrue(UserCTX.isAuthenticated());
    }

    @Test
    @DisplayName("未设置主体时应返回空值")
    void noPrincipal_shouldReturnEmptyValues() {
        assertNull(UserCTX.getPrincipal());
        assertNull(UserCTX.getCurrentUser());
        assertNull(UserCTX.getCurrentUserId());
        assertNull(UserCTX.getCurrentRoleType());
        assertTrue(UserCTX.getCurrentPermissions().isEmpty());
        assertFalse(UserCTX.isAuthenticated());
    }

    @Test
    @DisplayName("clear 应清理当前线程上下文")
    void clear_shouldRemovePrincipal() {
        UserCTX.setPrincipal(
                new SecurityPrincipal(
                        User.reconstruct(1L, "pwd"), RoleType.MEMBER, Collections.emptySet()));

        UserCTX.clear();

        assertNull(UserCTX.getPrincipal());
        assertFalse(UserCTX.isAuthenticated());
    }
}
