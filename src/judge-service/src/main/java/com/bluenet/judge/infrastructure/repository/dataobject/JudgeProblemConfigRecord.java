package com.bluenet.judge.infrastructure.repository.dataobject;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 判题配置记录。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class JudgeProblemConfigRecord {
    /** 判题配置主键。 */
    private Long id;
    /** 算法题目主键。 */
    private Long questionId;
    /** generator 源码语言。 */
    private String generatorLanguage;
    /** generator 对象键。 */
    private String generatorObjectKey;
    /** manifest 对象键。 */
    private String manifestObjectKey;
    /** 用于生成标准输出的主标准解语言。 */
    private String primaryStandardLanguage;
    /** 配置状态。 */
    private String status;
    /** 标准解 benchmark 重复次数。 */
    private Integer benchmarkRepeatTimes;
    /** 建议限时倍率。 */
    private java.math.BigDecimal marginMultiplier;
    /** 建议限时最小额外毫秒。 */
    private Integer minExtraMs;
    /** 建议限时向上取整粒度。 */
    private Integer roundToMs;
}
