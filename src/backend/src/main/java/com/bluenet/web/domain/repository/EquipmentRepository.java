package com.bluenet.web.domain.repository;

import com.bluenet.web.domain.model.vo.EquipmentVO;

import java.util.List;
import java.util.Optional;

/**
 * 设备仓库接口
 * <p>
 * 负责设备数据的持久化操作
 * </p>
 */
public interface EquipmentRepository {
    /**
     * 按展示排序倒序查询全部设备 视图。
     *
     * @return 满足条件的设备 结果集合。
     */
    List<EquipmentVO> findAllOrderBySortOrderDesc();

    /**
     * 处理设备 仓储职责中的业务数据访问逻辑。
     *
     * @param id
     *            业务记录主键。
     * @return 设备信息，如果不存在则返回Optional.empty()
     */
    Optional<EquipmentVO> findById(Long id);

    /**
     * 保存新的设备 记录。
     *
     * @param equipment
     *            设备领域对象。
     * @return 新记录的主键。
     */
    Long save(com.bluenet.web.domain.model.entity.Equipment equipment);

    /**
     * 更新已有设备 记录。
     *
     * @param equipment
     *            设备领域对象。
     */
    void update(com.bluenet.web.domain.model.entity.Equipment equipment);

    /**
     * 删除指定设备 记录。
     *
     * @param id
     *            业务记录主键。
     */
    void deleteById(Long id);

    /**
     * 判断是否存在满足条件的设备 记录。
     *
     * @param id
     *            业务记录主键。
     * @return 满足条件时返回 true，否则返回 false。
     */
    boolean existsById(Long id);

    /**
     * 更新设备 展示图片文件关联。
     *
     * @param id
     *            业务记录主键。
     * @param imageFileId
     *            展示图片文件主键。
     */
    void updateImage(Long id, Long imageFileId);
}
