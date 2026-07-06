package com.bluenet.web.infrastructure.security.principal;

import com.bluenet.web.domain.model.enumerate.RoleType;
import com.bluenet.web.domain.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 角色类型解析器，根据用户绑定的 {@code roleId} 解析为领域 {@link RoleType}。
 * <p>
 * 职责单一：将数据库中的角色标识映射为类型安全的枚举，供安全上下文和权限切面使用。 若角色不存在或解析失败，返回
 * {@code null}，由调用方决定后续处理。
 * </p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RoleTypeResolver {

    private final RoleRepository roleRepository;

    /**
     * 根据角色ID解析角色类型。
     *
     * @param roleId
     *            角色ID，可能为 null
     * @return 对应的 RoleType，找不到或 roleId 为 null 时返回 null
     */
    public RoleType resolve(Long roleId) {
        if (roleId == null) {
            return null;
        }
        return roleRepository.findById(roleId)
                .map(role -> RoleType.fromName(role.getName()))
                .orElseGet(() -> {
                    log.warn("Role not found for roleId: {}", roleId);
                    return null;
                });
    }
}
