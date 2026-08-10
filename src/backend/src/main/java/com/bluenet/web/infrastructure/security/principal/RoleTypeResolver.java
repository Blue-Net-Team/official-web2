package com.bluenet.web.infrastructure.security.principal;

import com.bluenet.web.domain.model.enumerate.RoleType;
import com.bluenet.web.domain.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 角色类型解析器，根据用户绑定的 {@code roleId} 解析为领域 {@link RoleType}。
 * <p>
 * 职责单一：将数据库中的角色标识映射为类型安全的枚举，供安全上下文和权限切面使用。 角色表数据量极小且变更不频繁，使用本地内存缓存避免每个请求重复查库。
 * </p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RoleTypeResolver {

    private final RoleRepository roleRepository;
    private final Map<Long, RoleType> cache = new ConcurrentHashMap<>();

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
        RoleType cached = cache.get(roleId);
        if (cached != null) {
            return cached;
        }
        return roleRepository.findById(roleId)
                .map(role -> {
                    RoleType roleType = RoleType.fromName(role.getName());
                    if (roleType != null) {
                        cache.put(roleId, roleType);
                    }
                    return roleType;
                })
                .orElseGet(() -> {
                    log.warn("Role not found for roleId: {}", roleId);
                    return null;
                });
    }

    /**
     * 清除指定角色ID的缓存。角色信息变更后可调用此方法保证解析结果及时刷新。
     *
     * @param roleId
     *            角色ID
     */
    public void evict(Long roleId) {
        if (roleId != null) {
            cache.remove(roleId);
        }
    }
}
