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
     */
    List<Equipment> findAllOrderBySortOrderDesc();

    /**
     * 按主键查询设备记录。
     *
     * @param id
     *            业务记录主键。
     */
    Optional<Equipment> findById(Long id);

    /**
     * 保存新的设备记录。
     *
     * @param equipment
     *            设备实体。
     */
    void save(Equipment equipment);

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
     */
    boolean existsById(Long id);

}
