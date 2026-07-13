package com.bluenet.web.domain.model.entity;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 算法题语言资源限制领域实体。
 */
@Data
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class JudgeLanguageLimit {
    /**
     * 主键。
     */
    private Long id;
    /**
     * 算法题目主键。
     */
    private Long questionId;
    /**
     * 编程语言值。
     */
    private String language;
    /**
     * 时间限制，单位毫秒。
     */
    private Integer timeLimitMs;
    /**
     * 内存限制，单位 KB。
     */
    private Integer memoryLimitKb;
    /**
     * 输出限制，单位 KB。
     */
    private Integer outputLimitKb;
    /**
     * 是否已确认。
     */
    private Boolean confirmed;
    /**
     * 确认时间。
     */
    private LocalDateTime confirmedAt;
    /**
     * 来源判题配置主键。
     */
    private Long sourceConfigId;

    public static JudgeLanguageLimit createConfirmed(
            Long questionId,
            String language,
            Integer timeLimitMs,
            Integer memoryLimitKb,
            Integer outputLimitKb,
            Long sourceConfigId) {
        return new JudgeLanguageLimit(
                null,
                questionId,
                language,
                timeLimitMs,
                memoryLimitKb,
                outputLimitKb,
                Boolean.TRUE,
                LocalDateTime.now(),
                sourceConfigId);
    }

    public static JudgeLanguageLimit reconstruct(
            Long id,
            Long questionId,
            String language,
            Integer timeLimitMs,
            Integer memoryLimitKb,
            Integer outputLimitKb,
            Boolean confirmed,
            LocalDateTime confirmedAt,
            Long sourceConfigId) {
        return new JudgeLanguageLimit(
                id,
                questionId,
                language,
                timeLimitMs,
                memoryLimitKb,
                outputLimitKb,
                confirmed,
                confirmedAt,
                sourceConfigId);
    }
}
