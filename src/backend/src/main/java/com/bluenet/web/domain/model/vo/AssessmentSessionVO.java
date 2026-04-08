package com.bluenet.web.domain.model.vo;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class AssessmentSessionVO {
    private Long id;
    private Long userId;
    private Long assessmentTimeId;
    private LocalDateTime startTime;
    private LocalDateTime deadline;
}
