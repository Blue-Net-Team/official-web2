package com.bluenet.web.api.converter.venue;

import com.bluenet.web.api.dto.venue.CreateVenueRequestDTO;
import com.bluenet.web.api.dto.venue.UpdateVenueRequestDTO;
import com.bluenet.web.application.command.venue.VenueCommands;
import org.springframework.stereotype.Component;

/**
 * 场地请求转换器
 * <p>
 * 负责将 API 层的 RequestDTO 转换为应用层的 Command
 * </p>
 */
@Component
public class VenueRequestConverter {

    /**
     * 将创建请求 DTO 转换为命令
     */
    public VenueCommands.CreateVenueCommand toCommand(CreateVenueRequestDTO dto) {
        return new VenueCommands.CreateVenueCommand(
                dto.getName(),
                dto.getSubtitle(),
                dto.getDescription(),
                dto.getImageFileId(),
                dto.getSortOrder());
    }

    /**
     * 将更新请求 DTO 转换为命令
     */
    public VenueCommands.UpdateVenueCommand toCommand(Long id, UpdateVenueRequestDTO dto) {
        return new VenueCommands.UpdateVenueCommand(
                id,
                dto.getName(),
                dto.getSubtitle(),
                dto.getDescription(),
                dto.getImageFileId(),
                dto.getSortOrder());
    }
}
