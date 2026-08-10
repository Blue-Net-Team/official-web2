package com.bluenet.web.application.result.algorithm_judge;

import com.bluenet.web.domain.model.enumerate.AlgorithmTestcaseType;
import com.bluenet.web.domain.model.enumerate.JudgeCaseStatus;
import com.bluenet.web.domain.model.enumerate.JudgeJobStatus;
import com.bluenet.web.domain.model.enumerate.JudgementSource;
import com.bluenet.web.domain.model.enumerate.JudgementStatus;
import com.bluenet.web.domain.model.enumerate.ObjectiveResultCode;
import com.bluenet.web.domain.model.enumerate.ReviewerType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 算法判题聚合的应用层结果对象。
 * <p>
 * 封装了算法判题相关操作返回给 API 层的数据。
 * </p>
 */
public class AlgorithmJudgeResult {

    private AlgorithmJudgeResult() {
        // 工具类，禁止实例化
    }

    /**
     * 提交算法题结果。
     */
    public record SubmitResult(
            /** 判题任务ID */
            Long judgeJobId,
            /** 答案ID */
            Long answerId,
            /** 测试用例类型 */
            AlgorithmTestcaseType testcaseType) {
    }

    /**
     * 轮询判题任务结果。
     */
    public record PollResult(
            /** 判题任务ID */
            Long judgeJobId,
            /** 测试用例类型 */
            AlgorithmTestcaseType testcaseType,
            /** 任务状态 */
            JudgeJobStatus status,
            /** 状态消息 */
            String statusMessage,
            /** 用例结果列表 */
            List<CaseResult> caseResults,
            /** 评判信息 */
            JudgementInfo judgement) {
    }

    /**
     * 用例结果。
     */
    public record CaseResult(
            /** 用例编号 */
            Integer caseNo,
            /** 测试用例类型 */
            AlgorithmTestcaseType testcaseType,
            /** 用例状态 */
            JudgeCaseStatus status,
            /** 输入数据 */
            String input,
            /** 期望输出 */
            String expectedOutput,
            /** 实际输出 */
            String actualOutput,
            /** 标准输出 */
            String stdout,
            /** 标准错误 */
            String stderr,
            /** 耗时（毫秒） */
            Integer timeUsedMs,
            /** 内存使用（KB） */
            Integer memoryUsedKb,
            /** 消息 */
            String message) {
    }

    /**
     * 评判信息。
     */
    public record JudgementInfo(
            /** 唯一标识 */
            Long id,
            /** 答案ID */
            Long answerId,
            /** 题目ID */
            Long questionId,
            /** 考核时间ID */
            Long assessmentTimeId,
            /** 用户ID */
            Long userId,
            /** 得分 */
            BigDecimal score,
            /** 满分 */
            BigDecimal maxScore,
            /** 评判状态 */
            JudgementStatus status,
            /** 结果代码 */
            ObjectiveResultCode resultCode,
            /** 评判来源 */
            JudgementSource source,
            /** 评审人ID */
            Long reviewerId,
            /** 评审人类型 */
            ReviewerType reviewerType,
            /** 评判时间 */
            LocalDateTime judgedAt) {
    }
}
