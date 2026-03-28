package com.bluenet.web.domain.model.vo;

import com.bluenet.web.domain.model.enumerate.QuestionType;
import com.bluenet.web.domain.model.vo.evaluation.QuestionContent;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class AssessmentQuestionVO {
    private Long id;
    private Long assessmentTimeId;
    private Integer questionNo;
    private QuestionType questionType;
    private String title;
    private QuestionContent content;
    private Long attachmentId;
    private BigDecimal score;
}
