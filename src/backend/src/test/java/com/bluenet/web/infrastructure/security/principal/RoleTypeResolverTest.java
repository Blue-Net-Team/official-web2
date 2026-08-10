package com.bluenet.web.infrastructure.security.principal;

import com.bluenet.web.domain.model.entity.Role;
import com.bluenet.web.domain.model.enumerate.RoleType;
import com.bluenet.web.domain.repository.RoleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * RoleTypeResolver 单元测试。
 */
@DisplayName("RoleTypeResolver 测试")
@ExtendWith(MockitoExtension.class)
class RoleTypeResolverTest {

    @Mock
    private RoleRepository roleRepository;

    private RoleTypeResolver resolver;

    @BeforeEach
    void setUp() {
        resolver = new RoleTypeResolver(roleRepository);
    }

    @Test
    @DisplayName("resolve: 应缓存角色解析结果")
    void resolve_shouldCacheResult() {
        when(roleRepository.findById(1L)).thenReturn(Optional.of(Role.reconstruct(1L, "MEMBER")));

        RoleType first = resolver.resolve(1L);
        RoleType second = resolver.resolve(1L);

        assertEquals(RoleType.MEMBER, first);
        assertSame(first, second);
        verify(roleRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("resolve: null roleId 返回 null")
    void resolve_withNullRoleId_shouldReturnNull() {
        assertNull(resolver.resolve(null));
        verifyNoInteractions(roleRepository);
    }

    @Test
    @DisplayName("resolve: 未知角色返回 null 并记录警告")
    void resolve_withUnknownRole_shouldReturnNull() {
        when(roleRepository.findById(2L)).thenReturn(Optional.of(Role.reconstruct(2L, "UNKNOWN_ROLE")));

        assertNull(resolver.resolve(2L));
    }

    @Test
    @DisplayName("resolve: 角色不存在返回 null")
    void resolve_withRoleNotFound_shouldReturnNull() {
        when(roleRepository.findById(3L)).thenReturn(Optional.empty());

        assertNull(resolver.resolve(3L));
    }

    @Test
    @DisplayName("evict: 应清除缓存")
    void evict_shouldClearCache() {
        when(roleRepository.findById(1L)).thenReturn(Optional.of(Role.reconstruct(1L, "MEMBER")));

        resolver.resolve(1L);
        resolver.evict(1L);
        resolver.resolve(1L);

        verify(roleRepository, times(2)).findById(1L);
    }
}
