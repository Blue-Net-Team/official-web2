package com.bluenet.web.domain.repository;

import com.bluenet.web.domain.model.entity.SoftwareResource;
import com.bluenet.web.domain.model.enumerate.SoftwareResourceDirection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

/**
 * 软件资源仓库接口。
 * <p>
 * 负责软件资源数据的持久化操作，只操作 Entity，不暴露 VO 或 DTO。
 * </p>
 */
public interface SoftwareResourceRepository {

    /**
     * 按主键查询软件资源。
     *
     * @param id
     *            业务记录主键。
     * @return 查询到的软件资源实体；不存在时为 Optional.empty()。
     */
    Optional<SoftwareResource> findById(Long id);

    /**
     * 分页查询指定方向下已启用的软件资源，支持关键字搜索。
     *
     * @param direction
     *            方向；为 null 时查询所有方向。
     * @param keyword
     *            搜索关键词；为 null 或空时忽略。
     * @param pageable
     *            分页参数。
     * @return 分页的软件资源实体。
     */
    Page<SoftwareResource> findActiveByDirection(SoftwareResourceDirection direction, String keyword,
            Pageable pageable);

    /**
     * 保存新的软件资源记录。
     *
     * @param softwareResource
     *            软件资源实体。
     * @return 新记录的主键。
     */
    Long save(SoftwareResource softwareResource);

    /**
     * 更新已有软件资源记录。
     *
     * @param softwareResource
     *            软件资源实体（id 必须非空）。
     */
    void update(SoftwareResource softwareResource);

    /**
     * 删除指定软件资源记录。
     *
     * @param id
     *            业务记录主键。
     */
    void deleteById(Long id);

    /**
     * 分页查询所有软件资源（管理后台）。
     *
     * @param pageable
     *            分页参数。
     * @return 分页的软件资源实体。
     */
    Page<SoftwareResource> findAllForAdmin(Pageable pageable);

    /**
     * 判断指定主键的软件资源是否存在。
     *
     * @param id
     *            业务记录主键。
     * @return 存在返回 true，否则 false。
     */
    boolean existsById(Long id);

    /**
     * 查询当前最大的排序号。
     *
     * @return 最大排序号；无记录时返回 null。
     */
    Integer findMaxSortOrder();

    /**
     * 批量更新软件资源排序号。
     *
     * @param sortItems
     *            排序项列表（id 与目标排序号）。
     */
    void batchUpdateSortOrder(List<SortItem> sortItems);

    /**
     * 排序项：软件资源主键及其目标排序号。
     */
    record SortItem(Long id, Integer sortOrder) {
    }
}
