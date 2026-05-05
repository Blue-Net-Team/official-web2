package com.bluenet.web.api.dto.qrcode;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 更新咨询群二维码请求 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "更新咨询群二维码请求")
public class UpdateConsultationQrcodeRequestDTO {

    @Schema(description = "文件ID", required = true)
    @NotNull(message = "文件ID不能为空")
    private Long fileId;
}
