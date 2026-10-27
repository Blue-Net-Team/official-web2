package com.bluenet.web.domain.model.entity;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 算法题判题配置领域实体。
 */
@Data
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class JudgeProblemConfig {
    /**
     * 判题配置主键。
     */
    private Long id;
    /**
     * 算法题目主键。
     */
    private Long questionId;
    /**
     * 生成器语言。
     */
    private String generatorLanguage;
    /**
     * 生成器 OSS 对象键。
     */
    private String generatorObjectKey;
    /**
     * 生成器 SHA-256 哈希。
     */
    private String generatorObjectHash;
    /**
     * manifest OSS 对象键。
     */
    private String manifestObjectKey;
    /**
     * manifest SHA-256 哈希。
     */
    private String manifestObjectHash;
    /**
     * 用于生成标准输出的主标准解语言。
     */
    private String primaryStandardLanguage;
    /**
     * 判题配置状态。
     */
    private String status;
    /**
     * 标准解 benchmark 重复次数。
     */
    private Integer benchmarkRepeatTimes;
    /**
     * 建议限时倍率。
     */
    private BigDecimal marginMultiplier;
    /**
     * 建议限时最小额外毫秒。
     */
    private Integer minExtraMs;
    /**
     * 建议限时向上取整粒度。
     */
    private Integer roundToMs;

    /**
     * 新建判题配置，状态默认置为草稿。
     */
    public static JudgeProblemConfig create(
            Long questionId,
            String generatorLanguage,
            String generatorObjectKey,
            String generatorObjectHash,
            String primaryStandardLanguage,
            Integer benchmarkRepeatTimes,
            BigDecimal marginMultiplier,
            Integer minExtraMs,
            Integer roundToMs) {
        return create(
                questionId,
                generatorLanguage,
                generatorObjectKey,
                generatorObjectHash,
                primaryStandardLanguage,
                benchmarkRepeatTimes,
                marginMultiplier,
                minExtraMs,
                roundToMs,
                "DRAFT");
    }

    /**
     * 新建判题配置，并指定保存后的状态。
     *
     * <p>
     * 仅当本次保存未改动任何判题相关内容时才允许沿用既有状态，避免把已生成测试数据的配置打回草稿。
     * </p>
     */
    public static JudgeProblemConfig create(
            Long questionId,
            String generatorLanguage,
            String generatorObjectKey,
            String generatorObjectHash,
            String primaryStandardLanguage,
            Integer benchmarkRepeatTimes,
            BigDecimal marginMultiplier,
            Integer minExtraMs,
            Integer roundToMs,
            String status) {
        return new JudgeProblemConfig(
                null,
                questionId,
                generatorLanguage,
                generatorObjectKey,
                generatorObjectHash,
                null,
                null,
                primaryStandardLanguage,
                status,
                benchmarkRepeatTimes,
                marginMultiplier,
                minExtraMs,
                roundToMs);
    }

    public static JudgeProblemConfig reconstruct(
            Long id,
            Long questionId,
            String generatorLanguage,
            String generatorObjectKey,
            String generatorObjectHash,
            String manifestObjectKey,
            String manifestObjectHash,
            String primaryStandardLanguage,
            String status,
            Integer benchmarkRepeatTimes,
            BigDecimal marginMultiplier,
            Integer minExtraMs,
            Integer roundToMs) {
        return new JudgeProblemConfig(
                id,
                questionId,
                generatorLanguage,
                generatorObjectKey,
                generatorObjectHash,
                manifestObjectKey,
                manifestObjectHash,
                primaryStandardLanguage,
                status,
                benchmarkRepeatTimes,
                marginMultiplier,
                minExtraMs,
                roundToMs);
    }
}
