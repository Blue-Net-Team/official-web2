package com.bluenet.web.application.converter;

import com.bluenet.web.api.dto.venue.VenueDTO;
import com.bluenet.web.domain.model.vo.VenueVO;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 场地转换器
 * <p>
 * 负责场地相关的VO与DTO之间的转换
 * </p>
 */
@Component
public class VenueConverter {
    /**
     * 将场地VO转换为DTO
     *
     * @param vo
     *            场地VO
     * @return 场地DTO
     */
    public VenueDTO convertToDTO(VenueVO vo) {
        return VenueDTO.builder()
                .id(vo.getId())
                .name(vo.getName())
                .subtitle(vo.getSubtitle())
                .description(vo.getDescription())
                .imageUrl(vo.getImageUrl())
                .imageFileId(vo.getImageFileId())
                .build();
    }

    /**
     * 将场地VO列表转换为DTO列表
     *
     * @param voList
     *            场地VO列表
     * @return 场地DTO列表
     */
    public List<VenueDTO> convertToDTOList(List<VenueVO> voList) {
        return voList.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
}
