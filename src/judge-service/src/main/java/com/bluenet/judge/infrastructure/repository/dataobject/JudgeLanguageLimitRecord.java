package com.bluenet.judge.infrastructure.repository.dataobject;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 已确认的语言资源限制记录。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class JudgeLanguageLimitRecord {
    /** 资源限制主键。 */
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
    /** 是否已由管理员确认。 */
    private Boolean confirmed;
}
