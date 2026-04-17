package com.bluenet.web.application.service;

import com.bluenet.web.api.dto.PageDTO;
import com.bluenet.web.api.dto.permission.PermissionDTO;
import com.bluenet.web.api.dto.permission.PermissionQueryDTO;
import com.bluenet.web.api.dto.permission.PermissionTreeDTO;

import java.util.List;

/**
 * 权限应用服务接口
 * <p>
 * 提供权限相关的应用层服务，负责权限查询和树形结构构建
 * </p>
 */
public interface PermissionService {
    /**
     * 分页查询权限列表
     *
     * @param query
     *            查询参数（分页、关键词搜索、格式筛选）
     * @return 分页权限DTO
     */
    PageDTO<PermissionDTO> getPermissions(PermissionQueryDTO query);

    /**
     * 获取权限详情
     *
     * @param id
     *            权限ID
     * @return 权限详情DTO，包含已分配的角色
     * @throws IllegalArgumentException
     *             权限不存在时抛出
     */
    PermissionDTO getPermissionDetail(Long id);

    /**
     * 获取权限树形结构
     *
     * @return 权限树节点列表
     */
    List<PermissionTreeDTO> getPermissionTree();
}
