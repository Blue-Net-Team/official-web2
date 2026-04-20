package com.bluenet.web.domain.model.vo;

import com.bluenet.web.domain.model.enumerate.JudgementSource;
import com.bluenet.web.domain.model.enumerate.JudgementStatus;
import com.bluenet.web.domain.model.enumerate.ObjectiveResultCode;
import com.bluenet.web.domain.model.enumerate.ProgrammingLanguage;
import com.bluenet.web.domain.model.enumerate.QuestionType;
import com.bluenet.web.domain.model.enumerate.ReviewerType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Domain view of a flattened answer+question+user+judgement submission row.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssessmentQuestionSubmissionVO {
    private Long answerId;
    private Long questionId;
    private Long assessmentTimeId;
    private Integer questionNo;
    private String questionTitle;
    private QuestionType questionType;
    private BigDecimal maxScore;
    private Long candidateUserId;
    private String studentId;
    private String username;
    private String nickname;
    private Long fileId;
    private String content;
    private ProgrammingLanguage language;
    private LocalDateTime submitTime;
    private Long judgementId;
    private BigDecimal judgementScore;
    private BigDecimal judgementMaxScore;
    private JudgementStatus judgementStatus;
    private ObjectiveResultCode resultCode;
    private JudgementSource source;
    private Long reviewerId;
    private ReviewerType reviewerType;
    private String judgementComment;
    private LocalDateTime judgedAt;
    private Boolean selectedBest;
}
