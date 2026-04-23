package com.bluenet.web.application.service;

import com.bluenet.web.application.EquipmentResult;
import com.bluenet.web.application.command.equipment.EquipmentCommands;

import java.util.List;

/**
 * 设备应用服务接口。
 * <p>
 * 定义了设备聚合在应用层的所有业务操作。
 * </p>
 */
public interface EquipmentAppService {

    /**
     * 获取所有设备列表
     *
     * @return 设备结果列表
     */
    List<EquipmentResult> getAllEquipments();

    /**
     * 获取设备详情
     *
     * @param id
     *            设备ID
     * @return 设备结果
     */
    EquipmentResult getEquipmentDetail(Long id);

    /**
     * 创建设备
     *
     * @param command
     *            创建命令
     * @return 创建后的设备结果
     */
    EquipmentResult createEquipment(EquipmentCommands.CreateEquipmentCommand command);

    /**
     * 更新设备
     *
     * @param command
     *            更新命令
     * @return 更新后的设备结果
     */
    EquipmentResult updateEquipment(EquipmentCommands.UpdateEquipmentCommand command);

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
