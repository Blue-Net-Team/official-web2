package com.bluenet.web.application.converter;

import com.bluenet.web.api.dto.equipment.EquipmentDTO;
import com.bluenet.web.domain.model.vo.EquipmentVO;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 设备转换器
 * <p>
 * 负责设备相关的VO与DTO之间的转换
 * </p>
 */
@Component
public class EquipmentConverter {
    /**
     * 将设备VO转换为DTO
     *
     * @param vo
     *            设备VO
     * @return 设备DTO
     */
    public EquipmentDTO convertToDTO(EquipmentVO vo) {
        return EquipmentDTO.builder()
                .id(vo.getId())
                .name(vo.getName())
                .brand(vo.getBrand())
                .description(vo.getDescription())
                .imageUrl(vo.getImageUrl())
                .imageFileId(vo.getImageFileId())
                .build();
    }

    /**
     * 将设备VO列表转换为DTO列表
     *
     * @param voList
     *            设备VO列表
     * @return 设备DTO列表
     */
    public List<EquipmentDTO> convertToDTOList(List<EquipmentVO> voList) {
        return voList.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
}
