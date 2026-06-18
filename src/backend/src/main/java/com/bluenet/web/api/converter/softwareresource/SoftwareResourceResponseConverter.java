package com.bluenet.web.api.converter.softwareresource;

import com.bluenet.web.api.dto.PageDTO;
import com.bluenet.web.api.dto.softwareresource.SoftwareResourceDTO;
import com.bluenet.web.application.SoftwareResourceResult;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

/**
 * 软件资源响应转换器。
 */
@Component
public class SoftwareResourceResponseConverter {

    /**
     * 将应用层结果转换为 DTO。
     */
    public SoftwareResourceDTO toDTO(SoftwareResourceResult result) {
        if (result == null) {
            return null;
        }
        return SoftwareResourceDTO.builder()
                .id(result.id())
                .name(result.name())
                .direction(result.direction())
                .category(result.category())
                .description(result.description())
                .externalUrl(result.externalUrl())
                .sortOrder(result.sortOrder())
                .status(result.status())
                .build();
    }

    /**
     * 将分页结果转换为 PageDTO。
     */
    public PageDTO<SoftwareResourceDTO> toPageDTO(Page<SoftwareResourceResult> page) {
        Page<SoftwareResourceDTO> dtoPage = page.map(this::toDTO);
        return PageDTO.from(dtoPage);
    }
}
