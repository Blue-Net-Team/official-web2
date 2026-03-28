package com.bluenet.web.api.dto.enrollment;

import com.bluenet.web.domain.model.enumerate.EnrollStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "审核结果")
public class EnrollmentApprovalResultDTO {
    @Schema(description = "报名ID", example = "123")
    private Long id;

    @Schema(description = "报名状态", example = "approved")
    private EnrollStatus status;

    @Schema(description = "新创建的用户ID（仅审核通过时返回）", example = "456")
    private Long createdUserId;
}
