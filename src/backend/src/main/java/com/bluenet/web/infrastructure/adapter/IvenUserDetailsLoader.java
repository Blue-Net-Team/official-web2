package com.bluenet.web.infrastructure.adapter;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;

import io.github.ivencn.infra.security.principal.SecurityPrincipal;
import io.github.ivencn.infra.security.spi.UserDetailsLoader;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.bluenet.web.domain.model.entity.User;
import com.bluenet.web.domain.model.enumerate.RoleType;
import com.bluenet.web.domain.repository.PermissionRepository;
import com.bluenet.web.domain.repository.UserRepository;
import com.bluenet.web.infrastructure.security.principal.RoleTypeResolver;

/**
 * iven 框架 {@link UserDetailsLoader} 的应用侧实现：将 subjectId 解析为安全主体 （角色经
 * {@link RoleTypeResolver} 解析，权限按最小权限原则加载）。
 */
@Slf4j
@RequiredArgsConstructor
public class IvenUserDetailsLoader implements UserDetailsLoader {

    private final UserRepository userRepository;
    private final RoleTypeResolver roleTypeResolver;
    private final PermissionRepository permissionRepository;

    @Override
    public Optional<SecurityPrincipal> loadById(Long subjectId) {
        Optional<User> userOpt = userRepository.findById(subjectId);
        if (userOpt.isEmpty()) {
            return Optional.empty();
        }
        User user = userOpt.get();
        RoleType roleType = roleTypeResolver.resolve(user.getRoleId());
        // 供应用侧 LoginContext 访问领域用户实体
        LoginContext.set(user, roleType);
        return Optional.of(
                new SecurityPrincipal(
                        user.getId(),
                        roleType,
                        loadPermissionValues(user.getRoleId())));
    }

    /**
     * 加载角色已绑定的权限值集合。查询异常时按最小权限原则返回空集合，不向外抛出。
     */
    private Set<String> loadPermissionValues(Long roleId) {
        try {
            return permissionRepository.findValuesByRoleId(roleId);
        } catch (RuntimeException e) {
            log.error("加载角色权限失败，按最小权限处理。roleId={}", roleId, e);
            return Collections.emptySet();
        }
    }
}
