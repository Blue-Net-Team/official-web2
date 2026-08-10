package com.bluenet.web.domain.model.entity;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 算法题测试用例生成配置领域实体。
 */
@Data
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class JudgeTestcaseConfig {
    /**
     * 测试用例配置主键。
     */
    private Long id;
    /**
     * 判题配置主键。
     */
    private Long configId;
    /**
     * 测试用例序号。
     */
    private Integer caseNo;
    /**
     * 测试用例分类。
     */
    private String category;
    /**
     * 传给 generator 的 JSON 参数字符串。
     */
    private String generatorArgs;
    /**
     * 用例权重。
     */
    private BigDecimal weight;
    /**
     * 是否隐藏用例详情。
     */
    private Boolean hidden;
    /**
     * 是否作为样例展示。
     */
    private Boolean sample;
    /**
     * 用例说明。
     */
    private String description;

    public static JudgeTestcaseConfig create(
            Long configId,
            Integer caseNo,
            String category,
            String generatorArgs,
            BigDecimal weight,
            Boolean hidden,
            Boolean sample,
            String description) {
        return new JudgeTestcaseConfig(
                null,
                configId,
                caseNo,
                category,
                generatorArgs,
                weight,
                hidden,
                sample,
                description);
    }

    public static JudgeTestcaseConfig reconstruct(
            Long id,
            Long configId,
            Integer caseNo,
            String category,
            String generatorArgs,
            BigDecimal weight,
            Boolean hidden,
            Boolean sample,
            String description) {
        return new JudgeTestcaseConfig(
                id,
                configId,
                caseNo,
                category,
                generatorArgs,
                weight,
                hidden,
                sample,
                description);
    }
}
