package com.bluenet.web.domain.repository;

import com.bluenet.web.domain.model.entity.Permission;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

/**
 * 权限仓储接口
 * <p>
 * 负责权限数据的持久化操作，只操作 Entity，不暴露 VO 或 DTO
 * </p>
 */
public interface PermissionRepository {
    /**
     * 按主键查询权限记录。
     *
     * @param id
     *            业务记录主键。
     * @return 查询到的权限实体；不存在时为空。
     */
    Optional<Permission> findById(Long id);

    /**
     * 查询全部权限记录。
     *
     * @return 满足条件的权限实体集合。
     */
    List<Permission> findAll();

    /**
     * 分页查询权限记录。
     *
     * @param keyword
     *            搜索关键字。
     * @param format
     *            权限返回格式或展示格式。
     * @param pageable
     *            Spring 分页请求对象。
     * @return 分页后的权限实体。
     */
    Page<Permission> findAll(String keyword, String format, Pageable pageable);

    /**
     * 按主键集合批量查询权限记录。
     *
     * @param ids
     *            业务记录主键集合。
     * @return 满足条件的权限实体集合。
     */
    List<Permission> findAllByIds(List<Long> ids);

    /**
     * 保存权限记录。
     *
     * @param permission
     *            权限实体
     */
    void save(Permission permission);

    /**
     * 删除指定权限记录。
     *
     * @param id
     *            业务记录主键。
     */
    void deleteById(Long id);

    /**
     * 判断是否存在满足条件的权限记录。
     *
     * @param id
     *            业务记录主键。
     * @return 满足条件时返回 true，否则返回 false。
     */
    boolean existsById(Long id);
}
