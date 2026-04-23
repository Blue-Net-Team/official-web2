package com.bluenet.web.domain.repository;

import com.bluenet.web.domain.model.entity.Equipment;

import java.util.List;
import java.util.Optional;

/**
 * 设备仓库接口
 * <p>
 * 负责设备数据的持久化操作，只操作 Entity，不暴露 VO 或 DTO
 * </p>
 */
public interface EquipmentRepository {
    /**
     * 按展示排序倒序查询全部设备。
     *
     * @return 设备实体集合。
     */
    List<Equipment> findAllOrderBySortOrderDesc();

    /**
     * 按主键查询设备记录。
     *
     * @param id
     *            业务记录主键。
     * @return 查询到的设备实体；不存在时为 Optional.empty()。
     */
    Optional<Equipment> findById(Long id);

    /**
     * 保存新的设备记录。
     *
     * @param equipment
     *            设备实体。
     * @return 新记录的主键。
     */
    Long save(Equipment equipment);

    /**
     * 更新已有设备记录。
     *
     * @param equipment
     *            设备实体（id 必须非空）。
     */
    void update(Equipment equipment);

    /**
     * 删除指定设备记录。
     *
     * @param id
     *            业务记录主键。
     */
    void deleteById(Long id);

    /**
     * 判断是否存在满足条件的设备记录。
     *
     * @param id
     *            业务记录主键。
     * @return 满足条件时返回 true，否则返回 false。
     */
    boolean existsById(Long id);

    /**
     * 更新设备展示图片文件关联。
     *
     * @param id
     *            业务记录主键。
     * @param imageFileId
     *            展示图片文件主键。
     */
    void updateImage(Long id, Long imageFileId);
}
