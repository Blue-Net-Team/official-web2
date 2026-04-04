package com.bluenet.web.api.dto.assessment_time;

import com.bluenet.web.domain.model.enumerate.Direction;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 考核时间数据传输对象
 * <p>
 * 用于API响应中返回考核时间信息
 * </p>
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "考核时间信息")
public class AssessmentTimeDTO {
    @Schema(description = "考核时间ID")
    private Long id;

    @Schema(description = "方向")
    private Direction direction;

    @Schema(description = "届次")
    private Integer epoch;

    @Schema(description = "年级（1=大一, 2=大二, 3=大三）")
    private Integer grade;

    @Schema(description = "开始时间")
    private LocalDateTime startTime;

    @Schema(description = "结束时间")
    private LocalDateTime endTime;

    @Schema(description = "是否限制时间")
    private Boolean timeLimit;

    @Schema(description = "限时分钟数")
    private Integer timeLimitMinutes;

    @Schema(description = "题目总数")
    private Integer totalQuestions;

    @Schema(description = "已完成题目数")
    private Integer completedQuestions;
}
