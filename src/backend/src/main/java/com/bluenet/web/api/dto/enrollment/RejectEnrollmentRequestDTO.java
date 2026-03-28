package com.bluenet.web.api.dto.enrollment;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "拒绝报名请求")
public class RejectEnrollmentRequestDTO {
    @Size(max = 200, message = "拒绝原因最多200个字符")
    @Schema(description = "拒绝原因（可选）", example = "面试未通过")
    private String reason;
}
