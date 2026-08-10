package com.bluenet.web.api.converter.venue;

import com.bluenet.web.api.dto.venue.VenueDTO;
import com.bluenet.web.application.result.venue.VenueResult;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 场地响应转换器
 * <p>
 * 负责将应用层结果转换为 API 响应 DTO
 * </p>
 */
@Component
public class VenueResponseConverter {

    /**
     * 将应用层结果转换为 API 响应 DTO
     */
    public VenueDTO toDTO(VenueResult result) {
        return VenueDTO.builder()
                .id(result.id())
                .name(result.name())
                .subtitle(result.subtitle())
                .description(result.description())
                .imageUrl(result.imageUrl())
                .imageFileId(result.imageFileId())
                .build();
    }

    /**
     * 将应用层结果列表转换为 API 响应 DTO 列表
     */
    public List<VenueDTO> toDTOList(List<VenueResult> results) {
        return results.stream()
                .map(this::toDTO)
                .toList();
    }
}
