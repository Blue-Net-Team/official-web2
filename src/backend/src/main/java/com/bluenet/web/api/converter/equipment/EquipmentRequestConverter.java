package com.bluenet.web.api.converter.equipment;

import com.bluenet.web.api.dto.equipment.CreateEquipmentRequestDTO;
import com.bluenet.web.api.dto.equipment.UpdateEquipmentRequestDTO;
import com.bluenet.web.application.command.equipment.EquipmentCommands;
import org.springframework.stereotype.Component;

/**
 * 设备请求转换器
 * <p>
 * 负责将 API 层的 RequestDTO 转换为应用层的 Command
 * </p>
 */
@Component
public class EquipmentRequestConverter {

    /**
     * 将创建请求 DTO 转换为命令
     */
    public EquipmentCommands.CreateEquipmentCommand toCommand(CreateEquipmentRequestDTO dto) {
        return new EquipmentCommands.CreateEquipmentCommand(
                dto.getName(),
                dto.getBrand(),
                dto.getDescription(),
                dto.getImageFileId(),
                dto.getSortOrder());
    }

    /**
     * 将更新请求 DTO 转换为命令
     */
    public EquipmentCommands.UpdateEquipmentCommand toCommand(Long id, UpdateEquipmentRequestDTO dto) {
        return new EquipmentCommands.UpdateEquipmentCommand(
                id,
                dto.getName(),
                dto.getBrand(),
                dto.getDescription(),
                dto.getImageFileId(),
                dto.getSortOrder());
    }
}
