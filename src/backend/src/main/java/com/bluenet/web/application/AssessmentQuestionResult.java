package com.bluenet.web.application;

import com.bluenet.web.domain.model.enumerate.QuestionType;
import com.bluenet.web.domain.model.vo.evaluation.QuestionContent;

import java.math.BigDecimal;

/**
 * 评测题目聚合的应用层结果对象。
 * <p>
 * 封装了评测题目相关操作返回给 API 层的数据。
 * </p>
 */
public record AssessmentQuestionResult(
        /** 题目唯一标识 */
        Long id,
        /** 评测场次ID */
        Long assessmentTimeId,
        /** 题号 */
        Integer questionNo,
        /** 题目类型 */
        QuestionType questionType,
        /** 题目标题 */
        String title,
        /** 题目内容 */
        QuestionContent content,
        /** 附件文件ID */
        Long attachmentId,
        /** 分值 */
        BigDecimal score,
        /** 是否已作答 */
        Boolean answered) {
}
