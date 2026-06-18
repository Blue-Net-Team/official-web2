package com.bluenet.web.api.converter.softwareresource;

import com.bluenet.web.api.dto.softwareresource.CreateSoftwareResourceRequestDTO;
import com.bluenet.web.api.dto.softwareresource.SoftwareResourceListRequestDTO;
import com.bluenet.web.api.dto.softwareresource.UpdateSoftwareResourceRequestDTO;
import com.bluenet.web.application.command.softwareresource.SoftwareResourceCommands;
import com.bluenet.web.domain.model.enumerate.SoftwareResourceDirection;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

/**
 * 软件资源请求转换器。
 */
@Component
public class SoftwareResourceRequestConverter {

    /**
     * 将列表查询 DTO 转换为方向枚举。
     */
    public SoftwareResourceDirection toDirection(SoftwareResourceListRequestDTO dto) {
        if (dto == null || dto.getDirection() == null || dto.getDirection().isBlank()) {
            return null;
        }
        try {
            return SoftwareResourceDirection.valueOf(dto.getDirection().toUpperCase(java.util.Locale.ROOT));
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /**
     * 将列表查询 DTO 转换为分页参数。
     */
    public Pageable toPageable(SoftwareResourceListRequestDTO dto) {
        if (dto == null) {
            return PageRequest.of(0, 20);
        }
        int page = dto.getPage() != null ? dto.getPage() : 0;
        int size = dto.getSize() != null ? dto.getSize() : 20;
        return PageRequest.of(page, size);
    }

    /**
     * 将创建 DTO 转换为创建命令。
     */
    public SoftwareResourceCommands.CreateSoftwareResourceCommand toCreateCommand(
            CreateSoftwareResourceRequestDTO dto) {
        return new SoftwareResourceCommands.CreateSoftwareResourceCommand(
                dto.getName(),
                dto.getDirection(),
                dto.getCategory(),
                dto.getDescription(),
                dto.getExternalUrl(),
                dto.getSortOrder());
    }

    /**
     * 将更新 DTO 转换为更新命令。
     */
    public SoftwareResourceCommands.UpdateSoftwareResourceCommand toUpdateCommand(
            Long id, UpdateSoftwareResourceRequestDTO dto) {
        return new SoftwareResourceCommands.UpdateSoftwareResourceCommand(
                id,
                dto.getName(),
                dto.getDirection(),
                dto.getCategory(),
                dto.getDescription(),
                dto.getExternalUrl(),
                dto.getSortOrder(),
                dto.getStatus());
    }
}
