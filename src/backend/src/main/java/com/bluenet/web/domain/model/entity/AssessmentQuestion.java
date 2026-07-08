package com.bluenet.web.domain.model.entity;

import com.bluenet.web.domain.model.enumerate.QuestionType;
import com.bluenet.web.domain.model.vo.question_content.QuestionContent;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class AssessmentQuestion {
    /**
     * 当前对象在系统中的唯一标识。
     */
    private Long id;
    /**
     * 所属考核场次或考核时间配置标识。
     */
    private Long assessmentTimeId;
    /**
     * 题目在考核场次中的序号。
     */
    private Integer questionNo;
    /**
     * 题目类型，例如算法题、单选题或多选题。
     */
    private QuestionType questionType;
    /**
     * 标题或名称，用于列表和详情展示。
     */
    private String title;
    /**
     * 正文内容、题目内容或结构化配置内容。
     */
    private QuestionContent content;
    /**
     * 题目或记录绑定的附件文件标识。
     */
    private Long attachmentId;
    /**
     * 答案、题目或评审记录在考核中的得分。
     */
    private BigDecimal score;

    private AssessmentQuestion(Long id, Long assessmentTimeId, Integer questionNo,
            QuestionType questionType, String title, QuestionContent content,
            Long attachmentId, BigDecimal score) {
        this.id = id;
        this.assessmentTimeId = assessmentTimeId;
        this.questionNo = questionNo;
        this.questionType = questionType;
        this.title = title;
        this.content = content;
        this.attachmentId = attachmentId;
        this.score = score;
    }

    public static AssessmentQuestion create(Long assessmentTimeId, Integer questionNo,
            QuestionType questionType, String title,
            QuestionContent content, Long attachmentId,
            BigDecimal score) {
        return new AssessmentQuestion(null, assessmentTimeId, questionNo, questionType,
                title, content, attachmentId, score);
    }

    public static AssessmentQuestion reconstruct(Long id, Long assessmentTimeId,
            Integer questionNo, QuestionType questionType,
            String title, QuestionContent content,
            Long attachmentId, BigDecimal score) {
        return new AssessmentQuestion(id, assessmentTimeId, questionNo, questionType,
                title, content, attachmentId, score);
    }

    public void update(Integer questionNo, QuestionType questionType, String title,
            QuestionContent content, Long attachmentId, BigDecimal score) {
        if (questionNo != null) {
            this.questionNo = questionNo;
        }
        if (questionType != null) {
            this.questionType = questionType;
        }
        if (title != null) {
            this.title = title;
        }
        if (content != null) {
            this.content = content;
        }
        if (attachmentId != null) {
            this.attachmentId = attachmentId;
        }
        if (score != null) {
            this.score = score;
        }
    }
}
