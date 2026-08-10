package com.bluenet.web.api.converter.equipment;

import com.bluenet.web.api.dto.equipment.EquipmentDTO;
import com.bluenet.web.application.result.equipment.EquipmentResult;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 设备响应转换器
 * <p>
 * 负责将应用层结果转换为 API 响应 DTO
 * </p>
 */
@Component
public class EquipmentResponseConverter {

    /**
     * 将应用层结果转换为 API 响应 DTO
     */
    public EquipmentDTO toDTO(EquipmentResult result) {
        return EquipmentDTO.builder()
                .id(result.id())
                .name(result.name())
                .brand(result.brand())
                .description(result.description())
                .imageUrl(result.imageUrl())
                .imageFileId(result.imageFileId())
                .build();
    }

    /**
     * 将应用层结果列表转换为 API 响应 DTO 列表
     */
    public List<EquipmentDTO> toDTOList(List<EquipmentResult> results) {
        return results.stream()
                .map(this::toDTO)
                .toList();
    }
}
