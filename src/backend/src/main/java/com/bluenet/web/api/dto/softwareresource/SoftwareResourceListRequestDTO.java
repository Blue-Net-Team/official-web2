package com.bluenet.web.api.dto.softwareresource;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 软件资源列表查询请求 DTO。
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "软件资源列表查询参数")
public class SoftwareResourceListRequestDTO {

    @Schema(description = "方向值；为空时查询全部")
    private String direction;

    @Schema(description = "页码，从 0 开始", example = "0")
    @Min(0)
    private Integer page;

    @Schema(description = "每页大小", example = "20")
    @Min(1)
    @Max(100)
    private Integer size;
}
