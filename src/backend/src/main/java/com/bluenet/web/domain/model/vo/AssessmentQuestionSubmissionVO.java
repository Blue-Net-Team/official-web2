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
    /**
     * 考核作答记录标识。
     */
    private Long answerId;
    /**
     * 考核题目标识。
     */
    private Long questionId;
    /**
     * 所属考核场次或考核时间配置标识。
     */
    private Long assessmentTimeId;
    /**
     * 题目在考核场次中的序号。
     */
    private Integer questionNo;
    /**
     * 题目标题。
     */
    private String questionTitle;
    /**
     * 题目类型，例如算法题、单选题或多选题。
     */
    private QuestionType questionType;
    /**
     * 题目或评审项可获得的最高分。
     */
    private BigDecimal maxScore;
    /**
     * 参加考核的候选用户标识。
     */
    private Long candidateUserId;
    /**
     * 学生学号。
     */
    private String studentId;
    /**
     * 用户真实姓名或登录用户名。
     */
    private String username;
    /**
     * 用户昵称或展示名。
     */
    private String nickname;
    /**
     * 关联文件记录标识。
     */
    private Long fileId;
    /**
     * 正文内容、题目内容或结构化配置内容。
     */
    private String content;
    /**
     * 提交代码使用的编程语言。
     */
    private ProgrammingLanguage language;
    /**
     * 答案提交时间。
     */
    private LocalDateTime submitTime;
    /**
     * 评审记录标识。
     */
    private Long judgementId;
    /**
     * 本次评审给出的得分。
     */
    private BigDecimal judgementScore;
    /**
     * 本次评审可给出的最高分。
     */
    private BigDecimal judgementMaxScore;
    /**
     * 评审处理状态。
     */
    private JudgementStatus judgementStatus;
    /**
     * 算法评测或评审结果编码。
     */
    private ObjectiveResultCode resultCode;
    /**
     * 评审结果来源。
     */
    private JudgementSource source;
    /**
     * 执行评审的用户或系统标识。
     */
    private Long reviewerId;
    /**
     * 评审来源类型，例如人工评审或自动评测。
     */
    private ReviewerType reviewerType;
    /**
     * 评审意见或扣分说明。
     */
    private String judgementComment;
    /**
     * 评审完成时间。
     */
    private LocalDateTime judgedAt;
    /**
     * 该提交是否被选为当前题目的最佳或最终采用提交。
     */
    private Boolean selectedBest;
}
