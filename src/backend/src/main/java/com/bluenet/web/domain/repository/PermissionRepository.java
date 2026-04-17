package com.bluenet.web.domain.repository;

import com.bluenet.web.domain.model.vo.PermissionVO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

/**
 * 权限仓储接口
 * <p>
 * 提供权限相关的数据访问抽象
 * </p>
 */
public interface PermissionRepository {
    /**
     * 根据ID查找权限
     *
     * @param id
     *            权限ID
     * @return 权限值对象，未找到返回 empty
     */
    Optional<PermissionVO> findById(Long id);

    /**
     * 查找所有权限（用于构建权限树）
     *
     * @return 所有权限列表
     */
    List<PermissionVO> findAll();

    /**
     * 分页查询权限列表，支持关键词搜索和格式筛选
     *
     * @param keyword
     *            搜索关键词（权限标识符或名称）
     * @param format
     *            格式筛选（resource:action, resource:subresource:action,
     *            resource-action:action）
     * @param pageable
     *            分页参数
     * @return 分页权限列表
     */
    Page<PermissionVO> findAll(String keyword, String format, Pageable pageable);

    /**
     * 批量查找权限
     *
     * @param ids
     *            权限ID列表
     * @return 权限列表（不保证顺序，不包含未找到的权限）
     */
    List<PermissionVO> findAllByIds(List<Long> ids);
}
