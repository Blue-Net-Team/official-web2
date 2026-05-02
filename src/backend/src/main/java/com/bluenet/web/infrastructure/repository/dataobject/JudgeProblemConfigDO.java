package com.bluenet.web.infrastructure.repository.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 算法题判题配置数据对象。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("tb_judge_problem_config")
public class JudgeProblemConfigDO {
    /**
     * 判题配置主键。
     */
    @TableId(type = IdType.AUTO)
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
}
