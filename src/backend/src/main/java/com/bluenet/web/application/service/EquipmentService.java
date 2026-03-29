package com.bluenet.web.application.service;

import com.bluenet.web.api.dto.equipment.CreateEquipmentRequestDTO;
import com.bluenet.web.api.dto.equipment.EquipmentDTO;
import com.bluenet.web.api.dto.equipment.UpdateEquipmentRequestDTO;

import java.util.List;

/**
 * 设备应用服务接口
 * <p>
 * 提供设备相关的应用层服务
 * </p>
 */
public interface EquipmentService {
    /**
     * 获取所有设备列表
     *
     * @return 设备列表
     */
    List<EquipmentDTO> getEquipmentList();

    /**
     * 获取设备详情
     *
     * @param id
     *            设备ID
     * @return 设备详情
     */
    EquipmentDTO getEquipmentDetail(Long id);

    /**
     * 创建设备
     *
     * @param request
     *            创建请求
     * @return 创建后的设备
     */
    EquipmentDTO createEquipment(CreateEquipmentRequestDTO request);

    /**
     * 更新设备
     *
     * @param id
     *            设备ID
     * @param request
     *            更新请求
     * @return 更新后的设备
     */
    EquipmentDTO updateEquipment(Long id, UpdateEquipmentRequestDTO request);

    /**
     * 删除设备
     *
     * @param id
     *            设备ID
     */
    void deleteEquipment(Long id);

    /**
     * 更新设备图片
     *
     * @param id
     *            设备ID
     * @param imageFileId
     *            图片文件ID
     */
    void updateEquipmentImage(Long id, Long imageFileId);
}
