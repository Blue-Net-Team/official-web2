package com.bluenet.web.api.dto.softwareresource;

import com.bluenet.web.domain.model.enumerate.Direction;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 创建软件资源请求 DTO。
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "创建软件资源请求")
public class CreateSoftwareResourceRequestDTO {

    @Schema(description = "软件名称")
    @NotBlank(message = "软件名称不能为空")
    private String name;

    @Schema(description = "所属方向")
    @NotNull(message = "方向不能为空")
    private Direction direction;

    @Schema(description = "分类")
    private String category;

    @Schema(description = "描述")
    private String description;

    @Schema(description = "外部下载链接")
    @NotBlank(message = "外部下载链接不能为空")
    private String externalUrl;

    @Schema(description = "排序权重")
    private Integer sortOrder;
}
