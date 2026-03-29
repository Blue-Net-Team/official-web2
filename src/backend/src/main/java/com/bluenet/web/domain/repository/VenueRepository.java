package com.bluenet.web.domain.repository;

import com.bluenet.web.domain.model.vo.VenueVO;

import java.util.List;
import java.util.Optional;

/**
 * 场地仓库接口
 * <p>
 * 负责场地数据的持久化操作
 * </p>
 */
public interface VenueRepository {
    /**
     * 查询所有场地（按排序号降序）
     *
     * @return 场地列表
     */
    List<VenueVO> findAllOrderBySortOrderDesc();

    /**
     * 根据ID查询场地
     *
     * @param id
     *            场地ID
     * @return 场地信息，如果不存在则返回Optional.empty()
     */
    Optional<VenueVO> findById(Long id);

    /**
     * 保存场地
     *
     * @param venue
     *            场地实体
     * @return 保存后的场地ID
     */
    Long save(com.bluenet.web.domain.model.entity.Venue venue);

    /**
     * 更新场地
     *
     * @param venue
     *            场地实体
     */
    void update(com.bluenet.web.domain.model.entity.Venue venue);

    /**
     * 删除场地
     *
     * @param id
     *            场地ID
     */
    void deleteById(Long id);

    /**
     * 检查场地是否存在
     *
     * @param id
     *            场地ID
     * @return 如果存在返回true，否则返回false
     */
    boolean existsById(Long id);

    /**
     * 更新场地图片
     *
     * @param id
     *            场地ID
     * @param imageFileId
     *            图片文件ID
     */
    void updateImage(Long id, Long imageFileId);
}
