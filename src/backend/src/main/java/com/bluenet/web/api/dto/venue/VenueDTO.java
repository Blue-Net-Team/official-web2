package com.bluenet.web.api.dto.venue;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 场地响应DTO
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "场地信息")
public class VenueDTO {
    @Schema(description = "场地ID")
    private Long id;

    @Schema(description = "场地名称")
    private String name;

    @Schema(description = "场地副标题")
    private String subtitle;

    @Schema(description = "场地描述")
    private String description;

    @Schema(description = "图片URL")
    private String imageUrl;

    @Schema(description = "图片文件ID")
    private Long imageFileId;
}
