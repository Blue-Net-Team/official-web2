package com.bluenet.web.api.dto.assessment_session;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
@Builder
@Schema(description = "考核会话信息")
public class AssessmentSessionDTO {
    @Schema(description = "会话ID")
    private Long id;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "考核时间ID")
    private Long assessmentTimeId;

    @Schema(description = "开始时间")
    private LocalDateTime startTime;

    @Schema(description = "截止时间")
    private LocalDateTime deadline;
}
