package com.bluenet.web.api.dto.qrcode;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 创建考核群二维码请求 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "创建考核群二维码请求")
public class CreateAssessmentQrcodeRequestDTO {

    @Schema(description = "文件ID", required = true)
    @NotNull(message = "文件ID不能为空")
    private Long fileId;

    @Schema(description = "方向")
    private String direction;

    @Schema(description = "考核轮次", required = true)
    @NotNull(message = "考核轮次不能为空")
    private Integer epoch;

    @Schema(description = "是否三方向共用")
    private Boolean isShared;
}
