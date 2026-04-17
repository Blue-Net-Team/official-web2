package com.bluenet.web.infrastructure.repository.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bluenet.web.domain.model.entity.RolePermission;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface RolePermissionMapper extends BaseMapper<RolePermission> {

    /**
     * 批量插入角色权限关联
     */
    int batchInsert(@Param("list") List<RolePermission> rolePermissions);

    /**
     * 批量删除角色权限关联
     */
    int batchDelete(@Param("roleId") Long roleId, @Param("permissionIds") List<Long> permissionIds);

    /**
     * 根据权限ID批量删除角色权限关联
     */
    int batchDeleteByPermissionId(@Param("permissionId") Long permissionId, @Param("roleIds") List<Long> roleIds);

    /**
     * 查询角色拥有的权限ID列表
     */
    List<Long> selectPermissionIdsByRoleId(@Param("roleId") Long roleId);

    /**
     * 查询拥有权限的角色ID列表
     */
    List<Long> selectRoleIdsByPermissionId(@Param("permissionId") Long permissionId);

    /**
     * 检查角色是否拥有某个权限
     */
    boolean existsByRoleIdAndPermissionId(@Param("roleId") Long roleId, @Param("permissionId") Long permissionId);
}
