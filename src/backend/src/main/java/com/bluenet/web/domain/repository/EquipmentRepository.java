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
     * 查询所有设备（按排序号降序）
     *
     * @return 设备列表
     */
    List<EquipmentVO> findAllOrderBySortOrderDesc();

    /**
     * 根据ID查询设备
     *
     * @param id
     *            设备ID
     * @return 设备信息，如果不存在则返回Optional.empty()
     */
    Optional<EquipmentVO> findById(Long id);

    /**
     * 保存设备
     *
     * @param equipment
     *            设备实体
     * @return 保存后的设备ID
     */
    Long save(com.bluenet.web.domain.model.entity.Equipment equipment);

    /**
     * 更新设备
     *
     * @param equipment
     *            设备实体
     */
    void update(com.bluenet.web.domain.model.entity.Equipment equipment);

    /**
     * 删除设备
     *
     * @param id
     *            设备ID
     */
    void deleteById(Long id);

    /**
     * 检查设备是否存在
     *
     * @param id
     *            设备ID
     * @return 如果存在返回true，否则返回false
     */
    boolean existsById(Long id);

    /**
     * 更新设备图片
     *
     * @param id
     *            设备ID
     * @param imageFileId
     *            图片文件ID
     */
    void updateImage(Long id, Long imageFileId);
}
