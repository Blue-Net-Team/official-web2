package com.bluenet.judge.infrastructure.repository;

import com.bluenet.judge.infrastructure.repository.dataobject.GeneratedTestCaseWrite;
import com.bluenet.judge.infrastructure.repository.dataobject.JudgeLanguageLimitRecord;
import com.bluenet.judge.infrastructure.repository.dataobject.JudgeProblemConfigRecord;
import com.bluenet.judge.infrastructure.repository.dataobject.JudgeStandardSolutionRecord;
import com.bluenet.judge.infrastructure.repository.dataobject.JudgeTestCaseRecord;
import com.bluenet.judge.infrastructure.repository.mapper.JudgeMetadataMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 判题元数据持久化访问入口。
 */
@Repository
@RequiredArgsConstructor
public class JudgeMetadataRepository {
    /** 判题元数据 MyBatis mapper。 */
    private final JudgeMetadataMapper judgeMetadataMapper;

    /**
     * 查询判题配置。
     *
     * @param configId
     *            判题配置主键。
     * @return 判题配置；不存在时返回空。
     */
    public Optional<JudgeProblemConfigRecord> findConfig(Long configId) {
        return Optional.ofNullable(judgeMetadataMapper.selectConfig(configId));
    }

    /**
     * 查询判题配置下的标准解文件记录。
     *
     * @param configId
     *            判题配置主键。
     * @return 标准解文件记录列表。
     */
    public List<JudgeStandardSolutionRecord> findStandardSolutions(Long configId) {
        return judgeMetadataMapper.selectStandardSolutions(configId);
    }

    /**
     * 查询题目当前可用正式测试用例。
     *
     * @param questionId
     *            算法题目主键。
     * @return 正式测试用例列表。
     */
    public List<JudgeTestCaseRecord> findCurrentTestCases(Long questionId) {
        // A question has one current config; old testcase versions are intentionally
        // not retained.
        return judgeMetadataMapper.selectCurrentTestCases(questionId);
    }

    /**
     * 查询题目指定语言已确认资源限制。
     *
     * @param questionId
     *            算法题目主键。
     * @param language
     *            编程语言值。
     * @return 已确认资源限制；不存在时返回空。
     */
    public Optional<JudgeLanguageLimitRecord> findConfirmedLimit(Long questionId, String language) {
        return Optional.ofNullable(judgeMetadataMapper.selectConfirmedLimit(questionId, language));
    }

    /**
     * 标记判题配置状态。
     *
     * @param configId
     *            判题配置主键。
     * @param status
     *            新状态。
     * @return 无返回值。
     */
    public void markConfigStatus(Long configId, String status) {
        judgeMetadataMapper.updateConfigStatus(configId, status);
    }

    /**
     * 替换指定配置下的当前生成测试用例索引。
     *
     * @param configId
     *            判题配置主键。
     * @param testcases
     *            生成后的测试用例写入对象列表。
     * @return 无返回值。
     */
    public void replaceGeneratedTestCases(Long configId, List<GeneratedTestCaseWrite> testcases) {
        judgeMetadataMapper.deleteGeneratedTestCases(configId);
        for (GeneratedTestCaseWrite testcase : testcases) {
            judgeMetadataMapper.insertGeneratedTestCase(testcase);
        }
    }

    /**
     * 更新标准解 benchmark 结果。
     *
     * @param solutionId
     *            标准解记录主键。
     * @param status
     *            benchmark 状态（PENDING / RUNNING / SUCCEEDED / FAILED）。
     * @param p95TimeMs
     *            p95 耗时，单位毫秒。
     * @param maxTimeMs
     *            最大耗时，单位毫秒。
     * @param peakMemoryKb
     *            峰值内存，单位 KB。
     * @param suggestedTimeLimitMs
     *            建议时间限制，单位毫秒。
     * @param message
     *            状态说明或失败原因。
     */
    public void updateBenchmarkResult(Long solutionId, String status, Integer p95TimeMs,
            Integer maxTimeMs, Integer peakMemoryKb,
            Integer suggestedTimeLimitMs, String message) {
        judgeMetadataMapper.updateBenchmarkResult(
                solutionId,
                status,
                p95TimeMs,
                maxTimeMs,
                peakMemoryKb,
                suggestedTimeLimitMs,
                message);
    }

    /**
     * 按判题配置主键查询其下所有已生成的测试用例。
     *
     * @param configId
     *            判题配置主键。
     * @return 测试用例记录列表。
     */
    public List<JudgeTestCaseRecord> findTestCasesByConfigId(Long configId) {
        return judgeMetadataMapper.selectTestCasesByConfigId(configId);
    }

    /**
     * 查询算法题题面内容。
     *
     * @param questionId
     *            算法题目主键。
     * @return 题面内容字符串。
     */
    public String findQuestionContent(Long questionId) {
        return judgeMetadataMapper.selectQuestionContent(questionId);
    }
}
