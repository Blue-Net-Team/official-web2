package com.bluenet.web.domain.repository;

import com.bluenet.web.domain.model.entity.Venue;

import java.util.List;
import java.util.Optional;

/**
 * 场地仓库接口
 * <p>
 * 负责场地数据的持久化操作，只操作 Entity，不暴露 VO 或 DTO
 * </p>
 */
public interface VenueRepository {
    /**
     * 按展示排序倒序查询全部场地。
     *
     */
    List<Venue> findAllOrderBySortOrderDesc();

    /**
     * 按主键查询场地记录。
     *
     * @param id
     *            业务记录主键。
     */
    Optional<Venue> findById(Long id);

    /**
     * 保存新的场地记录。
     *
     * @param venue
     *            场地实体。
     */
    void save(Venue venue);

    /**
     * 删除指定场地记录。
     *
     * @param id
     *            业务记录主键。
     */
    void deleteById(Long id);

    /**
     * 判断是否存在满足条件的场地记录。
     *
     * @param id
     *            业务记录主键。
     */
    boolean existsById(Long id);

}
