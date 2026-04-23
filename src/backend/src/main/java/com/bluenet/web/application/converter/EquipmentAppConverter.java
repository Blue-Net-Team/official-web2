package com.bluenet.web.application.converter;

import com.bluenet.web.api.dto.equipment.EquipmentDTO;
import com.bluenet.web.application.EquipmentResult;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 设备应用层转换器
 * <p>
 * 负责应用层 Result 与 API 层 DTO 之间的转换
 * </p>
 */
@Component
public class EquipmentAppConverter {

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
