package com.bluenet.web.application.result.user;

import org.springframework.data.domain.Page;
import com.bluenet.web.application.result.assessment.AssessmentQuestionResult;

/**
 * 用户题目列表聚合的应用层结果对象。
 * <p>
 * 封装了用户题目列表相关操作返回给 API 层的数据。
 * </p>
 */
public record UserQuestionListResult(
        /** 题目列表 */
        Page<AssessmentQuestionResult> questions,
        /** 截止时间 */
        String deadline,
        /** 考核是否已结束 */
        Boolean ended) {
}
