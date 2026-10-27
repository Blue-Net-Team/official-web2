package com.bluenet.web.infrastructure.repository.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 算法题语言资源限制数据对象。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("tb_judge_language_limit")
public class JudgeLanguageLimitDO {
    /** 主键。 */
    @TableId(type = IdType.AUTO)
    private Long id;
    /** 算法题目主键。 */
    private Long questionId;
    /** 编程语言值。 */
    private String language;
    /** 时间限制，单位毫秒。 */
    private Integer timeLimitMs;
    /** 内存限制，单位 KB。 */
    private Integer memoryLimitKb;
    /** 输出限制，单位 KB。 */
    private Integer outputLimitKb;
    /** 是否已确认。 */
    private Boolean confirmed;
    /** 确认时间。 */
    private LocalDateTime confirmedAt;
    /** 来源判题配置主键。 */
    private Long sourceConfigId;
}
