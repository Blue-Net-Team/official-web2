package com.bluenet.web.domain.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.bluenet.web.domain.model.enumerate.QuestionType;
import com.bluenet.web.domain.model.vo.evaluation.QuestionContent;
import lombok.Data;

import java.math.BigDecimal;

@Data
@TableName("tb_assessment_question")
public class AssessmentQuestion {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long assessmentTimeId;
    private Integer questionNo;
    private QuestionType questionType;
    private String title;
    private QuestionContent content;
    private Long attachmentId;
    private BigDecimal score;
}
