package com.bluenet.web.application;

import com.bluenet.web.api.dto.assessment_judgement.AssessmentJudgementDTO;
import com.bluenet.web.domain.model.enumerate.ProgrammingLanguage;

import java.time.LocalDateTime;

/**
 * 评测答案聚合的应用层结果对象。
 * <p>
 * 封装了评测答案相关操作返回给 API 层的数据。
 * </p>
 */
public record AssessmentAnswerResult(
        /** 答案唯一标识 */
        Long id,
        /** 题目ID */
        Long questionId,
        /** 附件文件ID */
        Long fileId,
        /** 答案内容 */
        String content,
        /** 编程语言 */
        ProgrammingLanguage language,
        /** 提交时间 */
        LocalDateTime submitTime,
        /** 评测结果 */
        AssessmentJudgementDTO judgement) {

    /**
     * 通过已有的对象创建一个新实例，将评测结果设为 null。
     *
     * @return 新的评测答案结果对象
     */
    public AssessmentAnswerResult withJudgementErased() {
        return new AssessmentAnswerResult(
                id, questionId, fileId, content, language, submitTime, null);
    }
}
