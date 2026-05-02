package com.bluenet.judge.infrastructure.repository.mapper;

import com.bluenet.judge.infrastructure.repository.dataobject.GeneratedTestCaseWrite;
import com.bluenet.judge.infrastructure.repository.dataobject.JudgeLanguageLimitRecord;
import com.bluenet.judge.infrastructure.repository.dataobject.JudgeProblemConfigRecord;
import com.bluenet.judge.infrastructure.repository.dataobject.JudgeStandardSolutionRecord;
import com.bluenet.judge.infrastructure.repository.dataobject.JudgeTestCaseRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface JudgeMetadataMapper {
    /**
     * 查询判题配置。
     *
     * @param configId
     *            判题配置主键。
     * @return 判题配置记录；不存在时为 null。
     */
    JudgeProblemConfigRecord selectConfig(@Param("configId") Long configId);

    /**
     * 查询配置下的标准解文件记录。
     *
     * @param configId
     *            判题配置主键。
     * @return 标准解文件记录列表。
     */
    List<JudgeStandardSolutionRecord> selectStandardSolutions(@Param("configId") Long configId);

    /**
     * 查询题目当前可用正式测试用例。
     *
     * @param questionId
     *            算法题目主键。
     * @return 当前正式测试用例列表。
     */
    List<JudgeTestCaseRecord> selectCurrentTestCases(@Param("questionId") Long questionId);

    /**
     * 查询已确认的语言资源限制。
     *
     * @param questionId
     *            算法题目主键。
     * @param language
     *            编程语言值。
     * @return 已确认资源限制；不存在时为 null。
     */
    JudgeLanguageLimitRecord selectConfirmedLimit(@Param("questionId") Long questionId,
            @Param("language") String language);

    /**
     * 更新判题配置状态。
     *
     * @param configId
     *            判题配置主键。
     * @param status
     *            新状态。
     * @return 无返回值。
     */
    void updateConfigStatus(@Param("configId") Long configId, @Param("status") String status);

    /**
     * 删除指定配置下已生成的测试用例索引。
     *
     * @param configId
     *            判题配置主键。
     * @return 无返回值。
     */
    void deleteGeneratedTestCases(@Param("configId") Long configId);

    /**
     * 插入生成后的测试用例索引。
     *
     * @param testcase
     *            生成后的测试用例写入对象。
     * @return 无返回值。
     */
    void insertGeneratedTestCase(@Param("testcase") GeneratedTestCaseWrite testcase);

    /**
     * 更新标准解 benchmark 结果。
     *
     * @param solutionId
     *            标准解主键。
     * @param status
     *            benchmark 状态。
     * @param p95TimeMs
     *            p95 耗时毫秒。
     * @param maxTimeMs
     *            最大耗时毫秒。
     * @param peakMemoryKb
     *            峰值内存 KB。
     * @param suggestedTimeLimitMs
     *            建议限时毫秒。
     * @param message
     *            结果说明。
     * @return 无返回值。
     */
    void updateBenchmarkResult(@Param("solutionId") Long solutionId,
            @Param("status") String status,
            @Param("p95TimeMs") Integer p95TimeMs,
            @Param("maxTimeMs") Integer maxTimeMs,
            @Param("peakMemoryKb") Integer peakMemoryKb,
            @Param("suggestedTimeLimitMs") Integer suggestedTimeLimitMs,
            @Param("message") String message);

    /**
     * 按配置主键查询测试用例（不限制配置状态）。
     *
     * @param configId
     *            判题配置主键。
     * @return 测试用例列表。
     */
    List<JudgeTestCaseRecord> selectTestCasesByConfigId(@Param("configId") Long configId);

    /**
     * 查询题目内容 JSON。
     *
     * @param questionId
     *            算法题目主键。
     * @return 题目 content 字段 JSON 字符串；不存在时为 null。
     */
    String selectQuestionContent(@Param("questionId") Long questionId);
}
