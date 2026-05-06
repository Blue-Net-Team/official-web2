package com.bluenet.web.domain.model.vo;

import com.bluenet.web.domain.model.enumerate.QuestionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 考生在单题上的提交与评分状态视图。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssessmentCandidateQuestionScoreVO {
    private Long questionId;
    private Integer questionNo;
    private String questionTitle;
    private QuestionType questionType;
    private BigDecimal maxScore;
    private Long answerId;
    private Boolean submitted;
    private LocalDateTime submitTime;
    private BigDecimal score;
    private Boolean judged;
    private AssessmentJudgementVO latestJudgement;
}
