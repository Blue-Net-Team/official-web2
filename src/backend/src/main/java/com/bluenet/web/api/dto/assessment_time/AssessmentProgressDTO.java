package com.bluenet.web.api.dto.assessment_time;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 考核进度数据传输对象
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "考核进度信息")
public class AssessmentProgressDTO {
    @Schema(description = "考核时间ID")
    private Long assessmentTimeId;

    @Schema(description = "题目总数")
    private Integer totalQuestions;

    @Schema(description = "已完成题目数")
    private Integer completedQuestions;
}
