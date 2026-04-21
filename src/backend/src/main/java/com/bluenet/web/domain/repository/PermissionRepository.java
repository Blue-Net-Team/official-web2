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
     * 按主键查询权限 记录。
     *
     * @param id
     *            业务记录主键。
     * @return 查询到的权限 结果；不存在时为空。
     */
    Optional<PermissionVO> findById(Long id);

    /**
     * 查询全部权限 记录。
     *
     * @return 满足条件的权限 结果集合。
     */
    List<PermissionVO> findAll();

    /**
     * 查询全部权限 记录。
     *
     * @param keyword
     *            搜索关键字。
     * @param format
     *            权限返回格式或展示格式。
     * @param pageable
     *            Spring 分页请求对象。
     * @return 分页后的权限 结果。
     */
    Page<PermissionVO> findAll(String keyword, String format, Pageable pageable);

    /**
     * 按主键集合批量查询权限 记录。
     *
     * @param ids
     *            业务记录主键集合。
     * @return 满足条件的权限 结果集合。
     */
    List<PermissionVO> findAllByIds(List<Long> ids);
}
