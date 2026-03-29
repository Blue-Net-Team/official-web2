package com.bluenet.web.api.dto.venue;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 更新场地请求DTO
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "更新场地请求")
public class UpdateVenueRequestDTO {
    @Size(max = 100, message = "场地名称不能超过100个字符")
    @Schema(description = "场地名称", example = "办公区域")
    private String name;

    @Size(max = 100, message = "副标题不能超过100个字符")
    @Schema(description = "场地副标题", example = "团队协作空间")
    private String subtitle;

    @Schema(description = "场地描述", example = "宽敞明亮的办公区域...")
    private String description;

    @Schema(description = "图片文件ID")
    private Long imageFileId;

    @Schema(description = "排序权重，越大越靠前", example = "0")
    private Integer sortOrder;
}
