package com.bluenet.web.api.dto.qrcode;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 咨询群二维码 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "咨询群二维码")
public class ConsultationQrcodeDTO {

    @Schema(description = "二维码ID")
    private Long id;

    @Schema(description = "文件ID，用于下载二维码图片")
    private Long fileId;
}
