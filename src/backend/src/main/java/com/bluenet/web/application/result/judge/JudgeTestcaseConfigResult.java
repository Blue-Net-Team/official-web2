package com.bluenet.web.application.result.judge;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * 判题测试用例配置应用层结果。
 *
 * @param caseNo
 *            测试用例序号
 * @param category
 *            测试用例分类
 * @param generatorArgs
 *            generator 参数
 * @param weight
 *            权重
 * @param hidden
 *            是否隐藏
 * @param sample
 *            是否为样例
 * @param description
 *            说明
 */
public record JudgeTestcaseConfigResult(
        Integer caseNo,
        String category,
        JsonNode generatorArgs,
        java.math.BigDecimal weight,
        Boolean hidden,
        Boolean sample,
        String description) {
}
