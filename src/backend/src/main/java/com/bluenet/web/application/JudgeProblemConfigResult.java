package com.bluenet.web.application;

import java.util.List;

/**
 * 判题配置应用层结果。
 *
 * @param id
 *            判题配置主键
 * @param questionId
 *            关联的算法题目主键
 * @param generatorLanguage
 *            generator 源码语言
 * @param generatorObjectKey
 *            generator 在判题 bucket 中的 OSS 对象键
 * @param generatorSource
 *            generator 源码内容
 * @param manifestObjectKey
 *            后端根据配置生成的 manifest OSS 对象键
 * @param primaryStandardLanguage
 *            用于生成标准输出的主标准解语言
 * @param status
 *            当前配置状态
 * @param benchmarkRepeatTimes
 *            benchmark 重复运行次数
 * @param marginMultiplier
 *            时限倍率
 * @param minExtraMs
 *            最少增加毫秒数
 * @param roundToMs
 *            向上取整粒度
 * @param standardSolutions
 *            标准解列表
 * @param testcases
 *            测试用例配置列表
 */
public record JudgeProblemConfigResult(
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
        List<JudgeStandardSolutionResult> standardSolutions,
        List<JudgeTestcaseConfigResult> testcases) {
}
