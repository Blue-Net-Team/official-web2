package com.bluenet.web.application.service;

import com.bluenet.web.application.PermissionResult;
import com.bluenet.web.application.command.permission.PermissionCommands;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * 权限应用服务接口。
 * <p>
 * 定义了权限聚合在应用层的所有业务操作。
 * </p>
 */
public interface PermissionAppService {

    /**
     * 分页查询权限列表
     *
     * @param command
     *            查询命令
     * @return 分页权限结果
     */
    Page<PermissionResult> getPermissions(PermissionCommands.GetPermissionsCommand command);

    /**
     * 获取权限详情
     *
     * @param id
     *            权限ID
     * @return 权限详情结果
     */
    PermissionResult getPermissionDetail(Long id);

    /**
     * 获取权限树形结构
     *
     * @return 权限结果列表（用于构建树形结构）
     */
    List<PermissionResult> getPermissionTree();
}
