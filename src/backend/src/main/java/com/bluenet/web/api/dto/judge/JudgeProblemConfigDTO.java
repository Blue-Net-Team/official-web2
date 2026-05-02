package com.bluenet.web.api.dto.judge;

import java.util.List;

/**
 * 管理端算法题当前判题配置响应。
 *
 * @param id
 *            判题配置主键。
 * @param questionId
 *            关联的算法题目主键。
 * @param generatorLanguage
 *            generator 源码语言。
 * @param generatorObjectKey
 *            generator 在判题 bucket 中的 OSS 对象键。
 * @param manifestObjectKey
 *            后端根据配置生成的 manifest OSS 对象键。
 * @param primaryStandardLanguage
 *            用于生成标准输出的主标准解语言。
 * @param status
 *            当前配置状态，例如 DRAFT、GENERATING、READY、FAILED。
 * @param benchmarkRepeatTimes
 *            每个语言标准解 benchmark 的重复运行次数。
 * @param marginMultiplier
 *            根据标准解 p95 耗时推导建议时限时使用的倍率。
 * @param minExtraMs
 *            建议时限相对标准解 p95 耗时至少增加的毫秒数。
 * @param roundToMs
 *            建议时限向上取整的毫秒粒度。
 * @param standardSolutions
 *            已配置的各语言标准解元数据和 benchmark 结果。
 * @param testcases
 *            已配置的测试用例生成参数。
 */
public record JudgeProblemConfigDTO(
        Long id,
        Long questionId,
        String generatorLanguage,
        String generatorObjectKey,
        String generatorSource,
        String manifestObjectKey,
        String primaryStandardLanguage,
        String status,
        Integer benchmarkRepeatTimes,
        Double marginMultiplier,
        Integer minExtraMs,
        Integer roundToMs,
        List<JudgeStandardSolutionDTO> standardSolutions,
        List<JudgeTestcaseConfigDTO> testcases) {
}
