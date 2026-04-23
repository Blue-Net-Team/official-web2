package com.bluenet.web.application.converter;

import com.bluenet.web.api.dto.venue.VenueDTO;
import com.bluenet.web.application.VenueResult;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 场地应用层转换器
 * <p>
 * 负责应用层 Result 与 API 层 DTO 之间的转换
 * </p>
 */
@Component
public class VenueAppConverter {

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
