package com.bluenet.web.domain.model.vo;

import com.bluenet.web.domain.model.enumerate.QuestionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Domain view of question-level scoreboard statistics.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssessmentQuestionScoreboardVO {
    private Long questionId;
    private Long assessmentTimeId;
    private Integer questionNo;
    private QuestionType questionType;
    private String title;
    private BigDecimal maxScore;
    private Long submittedCount;
    private Long judgedCount;
    private Long pendingCount;
    private BigDecimal averageScore;
}
