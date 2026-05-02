package com.bluenet.web.api.dto.judge;

import com.fasterxml.jackson.databind.JsonNode;

import java.math.BigDecimal;

/**
 * 管理端测试用例生成配置响应。
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
public record JudgeTestcaseConfigDTO(
        Integer caseNo,
        String category,
        JsonNode generatorArgs,
        BigDecimal weight,
        Boolean hidden,
        Boolean sample,
        String description) {
}
