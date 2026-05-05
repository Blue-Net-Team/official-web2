package com.bluenet.web.api.dto.qrcode;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 更新考核群二维码请求 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "更新考核群二维码请求")
public class UpdateAssessmentQrcodeRequestDTO {

    @Schema(description = "文件ID")
    private Long fileId;

    @Schema(description = "方向")
    private String direction;

    @Schema(description = "考核轮次")
    private Integer epoch;

    @Schema(description = "是否三方向共用")
    private Boolean isShared;
}
