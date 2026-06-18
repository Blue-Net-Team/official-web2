package com.bluenet.web.api.dto.softwareresource;

import com.bluenet.web.domain.model.enumerate.Direction;
import com.bluenet.web.domain.model.enumerate.SoftwareResourceStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 软件资源响应 DTO。
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "软件资源信息")
public class SoftwareResourceDTO {

    @Schema(description = "资源ID")
    private Long id;

    @Schema(description = "软件名称")
    private String name;

    @Schema(description = "所属方向")
    private Direction direction;

    @Schema(description = "分类")
    private String category;

    @Schema(description = "描述")
    private String description;

    @Schema(description = "外部下载链接")
    private String externalUrl;

    @Schema(description = "排序权重")
    private Integer sortOrder;

    @Schema(description = "状态")
    private SoftwareResourceStatus status;
}
