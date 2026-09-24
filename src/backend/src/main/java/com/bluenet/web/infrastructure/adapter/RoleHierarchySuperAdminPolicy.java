package com.bluenet.web.infrastructure.adapter;

import io.github.ivencn.infra.rbac.spi.SuperAdminPolicy;
import io.github.ivencn.infra.rbac.role.Role;

import com.bluenet.web.domain.model.enumerate.RoleType;
import com.bluenet.web.domain.model.policy.RoleHierarchy;

/**
 * iven 框架 {@link SuperAdminPolicy} 的应用侧实现：基于领域角色层级规则短路超级管理员。
 */
public class RoleHierarchySuperAdminPolicy implements SuperAdminPolicy {

    @Override
    public boolean isSuperAdmin(Role role) {
        if (!(role instanceof RoleType roleType)) {
            return false;
        }
        return RoleHierarchy.isSuperAdmin(roleType);
    }
}
