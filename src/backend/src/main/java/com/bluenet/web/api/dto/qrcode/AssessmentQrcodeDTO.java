package com.bluenet.web.api.dto.qrcode;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 考核群二维码 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "考核群二维码")
public class AssessmentQrcodeDTO {

    @Schema(description = "二维码ID")
    private Long id;

    @Schema(description = "文件ID，用于下载二维码图片")
    private Long fileId;

    @Schema(description = "方向")
    private String direction;

    @Schema(description = "考核轮次")
    private Integer epoch;

    @Schema(description = "是否三方向共用")
    private Boolean isShared;
}
