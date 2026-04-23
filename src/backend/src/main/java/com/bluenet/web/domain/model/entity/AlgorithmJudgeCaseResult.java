package com.bluenet.web.domain.model.entity;

import com.bluenet.web.domain.model.enumerate.AlgorithmTestcaseType;
import com.bluenet.web.domain.model.enumerate.JudgeCaseStatus;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Stores the execution result for a single algorithm testcase.
 */
@Data
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class AlgorithmJudgeCaseResult {
    /**
     * 当前对象在系统中的唯一标识。
     */
    private Long id;
    /**
     * 算法评测任务标识。
     */
    private Long judgeJobId;
    /**
     * 算法评测用例序号。
     */
    private Integer caseNo;
    /**
     * 评测用例类型，例如示例、自测或正式用例。
     */
    private AlgorithmTestcaseType testcaseType;
    /**
     * 当前业务流程、任务或记录的状态。
     */
    private JudgeCaseStatus status;
    /**
     * 算法示例或评测用例输入内容。
     */
    private String input;
    /**
     * 算法评测用例期望输出内容。
     */
    private String expectedOutput;
    /**
     * 算法评测运行后得到的实际输出内容。
     */
    private String actualOutput;
    /**
     * 程序运行产生的标准输出。
     */
    private String stdout;
    /**
     * 程序运行产生的标准错误输出。
     */
    private String stderr;
    /**
     * 评测用例运行耗时，单位毫秒。
     */
    private Integer timeUsedMs;
    /**
     * 评测用例运行消耗内存，单位 KB。
     */
    private Integer memoryUsedKb;
    /**
     * 任务状态、评测结果或业务处理的提示信息。
     */
    private String message;
    /**
     * 评测用例结果是否对候选人可见。
     */
    private Boolean visibleToCandidate;
    /**
     * 记录创建时间。
     */
    private LocalDateTime createdAt;

    private AlgorithmJudgeCaseResult(
            Long id,
            Long judgeJobId,
            Integer caseNo,
            AlgorithmTestcaseType testcaseType,
            JudgeCaseStatus status,
            String input,
            String expectedOutput,
            String actualOutput,
            String stdout,
            String stderr,
            Integer timeUsedMs,
            Integer memoryUsedKb,
            String message,
            Boolean visibleToCandidate,
            LocalDateTime createdAt) {
        this.id = id;
        this.judgeJobId = judgeJobId;
        this.caseNo = caseNo;
        this.testcaseType = testcaseType;
        this.status = status;
        this.input = input;
        this.expectedOutput = expectedOutput;
        this.actualOutput = actualOutput;
        this.stdout = stdout;
        this.stderr = stderr;
        this.timeUsedMs = timeUsedMs;
        this.memoryUsedKb = memoryUsedKb;
        this.message = message;
        this.visibleToCandidate = visibleToCandidate;
        this.createdAt = createdAt;
    }

    /**
     * 构造新评测用例结果
     */
    public static AlgorithmJudgeCaseResult create(
            Long judgeJobId,
            Integer caseNo,
            AlgorithmTestcaseType testcaseType,
            JudgeCaseStatus status,
            String input,
            String expectedOutput,
            String actualOutput,
            String stdout,
            String stderr,
            Integer timeUsedMs,
            Integer memoryUsedKb,
            String message,
            Boolean visibleToCandidate) {
        return new AlgorithmJudgeCaseResult(
                null,
                judgeJobId,
                caseNo,
                testcaseType,
                status,
                input,
                expectedOutput,
                actualOutput,
                stdout,
                stderr,
                timeUsedMs,
                memoryUsedKb,
                message,
                visibleToCandidate,
                LocalDateTime.now());
    }

    /**
     * 从数据库重建 —— 跳过创建校验
     */
    public static AlgorithmJudgeCaseResult reconstruct(
            Long id,
            Long judgeJobId,
            Integer caseNo,
            AlgorithmTestcaseType testcaseType,
            JudgeCaseStatus status,
            String input,
            String expectedOutput,
            String actualOutput,
            String stdout,
            String stderr,
            Integer timeUsedMs,
            Integer memoryUsedKb,
            String message,
            Boolean visibleToCandidate,
            LocalDateTime createdAt) {
        return new AlgorithmJudgeCaseResult(
                id,
                judgeJobId,
                caseNo,
                testcaseType,
                status,
                input,
                expectedOutput,
                actualOutput,
                stdout,
                stderr,
                timeUsedMs,
                memoryUsedKb,
                message,
                visibleToCandidate,
                createdAt);
    }
}
