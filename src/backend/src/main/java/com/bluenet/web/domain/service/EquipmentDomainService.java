package com.bluenet.web.domain.service;

import com.bluenet.web.domain.model.vo.EquipmentVO;

import java.util.List;
import java.util.Optional;

/**
 * 设备领域服务接口
 * <p>
 * 提供设备相关的领域业务操作
 * </p>
 */
public interface EquipmentDomainService {
    /**
     * 获取所有设备（按排序号降序）
     *
     * @return 设备列表
     */
    List<EquipmentVO> getAllEquipments();

    /**
     * 根据ID获取设备
     *
     * @param id
     *            设备ID
     * @return 设备信息
     */
    Optional<EquipmentVO> getEquipmentById(Long id);

    /**
     * 创建设备
     *
     * @param name
     *            设备名称
     * @param brand
     *            设备品牌
     * @param description
     *            设备描述
     * @param imageFileId
     *            图片文件ID
     * @param sortOrder
     *            排序权重
     * @return 创建后的设备ID
     */
    Long createEquipment(String name, String brand, String description, Long imageFileId, Integer sortOrder);

    /**
     * 更新设备
     *
     * @param id
     *            设备ID
     * @param name
     *            设备名称
     * @param brand
     *            设备品牌
     * @param description
     *            设备描述
     * @param imageFileId
     *            图片文件ID
     * @param sortOrder
     *            排序权重
     */
    void updateEquipment(Long id, String name, String brand, String description, Long imageFileId, Integer sortOrder);

    /**
     * 删除设备
     *
     * @param id
     *            设备ID
     */
    void deleteEquipment(Long id);

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
