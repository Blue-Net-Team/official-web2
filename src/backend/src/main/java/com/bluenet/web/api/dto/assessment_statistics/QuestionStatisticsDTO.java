package com.bluenet.web.api.dto.assessment_statistics;

import com.bluenet.web.domain.model.enumerate.ObjectiveResultCode;
import com.bluenet.web.domain.model.enumerate.QuestionType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Aggregated objective-question statistics for one assessment question.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "题目通过率统计")
public class QuestionStatisticsDTO {
    @Schema(description = "题目ID")
    private Long questionId;
    @Schema(description = "题型")
    private QuestionType questionType;
    @Schema(description = "提交人数")
    private Long submittedCount;
    @Schema(description = "通过人数")
    private Long acceptedCount;
    @Schema(description = "通过率")
    private BigDecimal passRate;
    @Schema(description = "结果码分布")
    private Map<ObjectiveResultCode, Long> resultDistribution;
}
