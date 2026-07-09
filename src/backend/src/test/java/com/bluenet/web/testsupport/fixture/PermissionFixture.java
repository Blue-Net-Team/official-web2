package com.bluenet.web.testsupport.fixture;

import com.bluenet.web.domain.model.entity.Permission;
import com.bluenet.web.domain.repository.PermissionRepository;

/**
 * 权限测试夹具。
 */
public final class PermissionFixture {

    private PermissionFixture() {
    }

    public static Permission create(String value) {
        return create(value, value, "GET", "PROTECTED");
    }

    public static Permission create(String name, String value) {
        return create(name, value, "GET", "PROTECTED");
    }

    public static Permission create(String name, String value, String method) {
        return create(name, value, method, "PROTECTED");
    }

    public static Permission create(String name, String value, String method, String accessLevel) {
        return Permission.create(name, value, "/api/v1/test/" + value.replace(':', '-'), method, accessLevel);
    }

    public static Permission save(PermissionRepository permissionRepository, String value) {
        Permission permission = create(value);
        permissionRepository.save(permission);
        return permission;
    }

    public static Permission save(PermissionRepository permissionRepository, String name, String value) {
        Permission permission = create(name, value);
        permissionRepository.save(permission);
        return permission;
    }
}
