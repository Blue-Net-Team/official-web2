package com.bluenet.web.infrastructure.repository.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 算法题标准解数据对象。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("tb_judge_standard_solution")
public class JudgeStandardSolutionDO {
    /**
     * 标准解主键。
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    /**
     * 判题配置主键。
     */
    private Long configId;
    /**
     * 算法题目主键。
     */
    private Long questionId;
    /**
     * 标准解语言。
     */
    private String language;
    /**
     * 标准解 OSS 对象键。
     */
    private String objectKey;
    /**
     * 标准解 SHA-256 哈希。
     */
    private String objectHash;
    /**
     * 是否为生成标准输出的主标准解。
     */
    private Boolean primarySolution;
    /**
     * benchmark 状态。
     */
    private String benchmarkStatus;
    /**
     * benchmark p95 耗时。
     */
    private Integer p95TimeMs;
    /**
     * benchmark 最大耗时。
     */
    private Integer maxTimeMs;
    /**
     * benchmark 峰值内存。
     */
    private Integer peakMemoryKb;
    /**
     * 建议正式判题限时。
     */
    private Integer suggestedTimeLimitMs;
    /**
     * benchmark 说明。
     */
    private String benchmarkMessage;
}
