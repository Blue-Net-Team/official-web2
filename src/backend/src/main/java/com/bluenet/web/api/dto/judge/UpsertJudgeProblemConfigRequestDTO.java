package com.bluenet.web.api.dto.judge;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.List;

/**
 * 管理员新增或替换算法题当前判题配置的请求。
 *
 * @param generatorLanguage
 *            generator 源码语言。
 * @param generatorSource
 *            generator 源码内容。
 * @param primaryStandardLanguage
 *            用于生成标准输出的主标准解语言。
 * @param benchmarkRepeatTimes
 *            每个语言标准解 benchmark 的重复运行次数。
 * @param marginMultiplier
 *            根据标准解 p95 耗时推导建议时限时使用的倍率。
 * @param minExtraMs
 *            建议时限相对标准解 p95 耗时至少增加的毫秒数。
 * @param roundToMs
 *            建议时限向上取整的毫秒粒度。
 * @param standardSolutions
 *            各支持语言标准解源码。
 * @param testcases
 *            测试用例生成配置。
 */
public record UpsertJudgeProblemConfigRequestDTO(
        @NotBlank(message = "生成器语言不能为空") String generatorLanguage,
        @NotBlank(message = "生成器源码不能为空") String generatorSource,
        @NotBlank(message = "主标准解语言不能为空") String primaryStandardLanguage,
        @NotNull(message = "基准测试重复次数不能为空") @Min(value = 1, message = "基准测试重复次数必须大于等于1") Integer benchmarkRepeatTimes,
        @NotNull(message = "时限倍率不能为空") @DecimalMin(value = "1.0", message = "时限倍率必须大于等于1.0") BigDecimal marginMultiplier,
        @NotNull(message = "最小额外时间不能为空") @Min(value = 0, message = "最小额外时间必须大于等于0毫秒") Integer minExtraMs,
        @NotNull(message = "取整粒度不能为空") @Min(value = 1, message = "取整粒度必须大于等于1毫秒") Integer roundToMs,
        @Valid @NotEmpty(message = "标准解列表不能为空") List<StandardSolutionRequest> standardSolutions,
        @Valid @NotEmpty(message = "测试用例列表不能为空") List<TestcaseConfigRequest> testcases) {

    /**
     * 管理员提交的单个语言标准解源码。
     *
     * @param language
     *            标准解语言。
     * @param source
     *            标准解源码内容。
     * @param primarySolution
     *            是否为用于生成标准输出的主标准解。
     */
    public record StandardSolutionRequest(
            @NotBlank(message = "标准解语言不能为空") String language,
            @NotBlank(message = "标准解源码不能为空") String source,
            Boolean primarySolution) {
    }

    /**
     * 管理员提交的单个测试用例生成配置。
     *
     * @param caseNo
     *            测试用例序号。
     * @param category
     *            测试用例分类，例如 SAMPLE、NORMAL、EDGE、WORST_CASE。
     * @param generatorArgs
     *            传给 generator 的结构化 JSON 参数。
     * @param weight
     *            测试用例权重。
     * @param hidden
     *            是否对候选人隐藏该用例详情。
     * @param sample
     *            是否作为样例用例。
     * @param description
     *            测试用例说明。
     */
    public record TestcaseConfigRequest(
            @NotNull(message = "测试用例序号不能为空") @Min(value = 1, message = "测试用例序号必须大于等于1") Integer caseNo,
            @NotBlank(message = "测试用例分类不能为空") String category,
            JsonNode generatorArgs,
            @NotNull(message = "测试用例权重不能为空") @DecimalMin(value = "0.0", message = "测试用例权重必须大于等于0.0") BigDecimal weight,
            Boolean hidden,
            Boolean sample,
            String description) {
    }
}
