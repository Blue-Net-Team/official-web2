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
     * 按展示排序倒序查询全部场地 视图。
     *
     * @return 满足条件的场地 结果集合。
     */
    List<VenueVO> findAllOrderBySortOrderDesc();

    /**
     * 处理场地 仓储职责中的业务数据访问逻辑。
     *
     * @param id
     *            业务记录主键。
     * @return 查询或处理得到的场地 结果。
     */
    Optional<VenueVO> findById(Long id);

    /**
     * 保存新的场地 记录。
     *
     * @param venue
     *            场地领域对象。
     * @return 新记录的主键。
     */
    Long save(com.bluenet.web.domain.model.entity.Venue venue);

    /**
     * 更新已有场地 记录。
     *
     * @param venue
     *            场地领域对象。
     */
    void update(com.bluenet.web.domain.model.entity.Venue venue);

    /**
     * 删除指定场地 记录。
     *
     * @param id
     *            业务记录主键。
     */
    void deleteById(Long id);

    /**
     * 判断是否存在满足条件的场地 记录。
     *
     * @param id
     *            业务记录主键。
     * @return 满足条件时返回 true，否则返回 false。
     */
    boolean existsById(Long id);

    /**
     * 更新场地 展示图片文件关联。
     *
     * @param id
     *            业务记录主键。
     * @param imageFileId
     *            展示图片文件主键。
     */
    void updateImage(Long id, Long imageFileId);
}
