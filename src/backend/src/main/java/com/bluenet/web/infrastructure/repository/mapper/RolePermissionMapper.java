package com.bluenet.web.infrastructure.repository.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bluenet.web.infrastructure.repository.dataobject.RolePermissionDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface RolePermissionMapper extends BaseMapper<RolePermissionDO> {

    /**
     * 处理角色权限关系 仓储职责中的业务数据访问逻辑。
     *
     * @param list
     *            待批量插入的角色权限关系集合。
     * @return 数据库受影响行数。
     */
    int batchInsert(@Param("list") List<RolePermissionDO> rolePermissions);

    /**
     * 处理角色权限关系 仓储职责中的业务数据访问逻辑。
     *
     * @param roleId
     *            角色主键。
     * @param permissionIds
     *            权限主键集合。
     * @return 数据库受影响行数。
     */
    int batchDelete(@Param("roleId") Long roleId, @Param("permissionIds") List<Long> permissionIds);

    /**
     * 处理角色权限关系 仓储职责中的业务数据访问逻辑。
     *
     * @param permissionId
     *            权限主键。
     * @param roleIds
     *            角色主键集合。
     * @return 数据库受影响行数。
     */
    int batchDeleteByPermissionId(@Param("permissionId") Long permissionId, @Param("roleIds") List<Long> roleIds);

    /**
     * 查询角色权限关系 数据行。
     *
     * @param roleId
     *            角色主键。
     * @return 满足条件的角色权限关系 结果集合。
     */
    List<Long> selectPermissionIdsByRoleId(@Param("roleId") Long roleId);

    /**
     * 查询角色权限关系 数据行。
     *
     * @param permissionId
     *            权限主键。
     * @return 满足条件的角色权限关系 结果集合。
     */
    List<Long> selectRoleIdsByPermissionId(@Param("permissionId") Long permissionId);

    /**
     * 判断角色与权限是否已经存在授权关系。
     *
     * @param roleId
     *            角色主键。
     * @param permissionId
     *            权限主键。
     * @return 满足条件时返回 true，否则返回 false。
     */
    boolean existsByRoleIdAndPermissionId(@Param("roleId") Long roleId, @Param("permissionId") Long permissionId);

    /**
     * 按权限主键集合查询角色权限关系数据行。
     *
     * @param permissionIds
     *            权限主键集合。
     * @return 满足条件的角色权限关系 结果集合。
     */
    List<RolePermissionDO> selectByPermissionIds(@Param("permissionIds") List<Long> permissionIds);

    /**
     * 查询角色在指定权限集合中已存在的授权关系。
     *
     * @param roleId
     *            角色主键。
     * @param permissionIds
     *            权限主键集合。
     * @return 满足条件的角色权限关系 结果集合。
     */
    List<RolePermissionDO> selectByRoleIdAndPermissionIds(@Param("roleId") Long roleId,
            @Param("permissionIds") List<Long> permissionIds);
}
