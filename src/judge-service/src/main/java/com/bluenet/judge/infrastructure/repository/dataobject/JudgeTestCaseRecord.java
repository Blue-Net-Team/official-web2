package com.bluenet.judge.infrastructure.repository.dataobject;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 当前正式测试用例索引记录。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class JudgeTestCaseRecord {
    /** 测试用例主键。 */
    private Long id;
    /** 判题配置主键。 */
    private Long configId;
    /** 算法题目主键。 */
    private Long questionId;
    /** 测试用例序号。 */
    private Integer caseNo;
    /** 测试用例分类。 */
    private String category;
    /** 输入文件对象键。 */
    private String inputObjectKey;
    /** 输入文件 SHA-256 哈希。 */
    private String inputObjectHash;
    /** 期望输出文件对象键。 */
    private String outputObjectKey;
    /** 期望输出文件 SHA-256 哈希。 */
    private String outputObjectHash;
    /** 测试用例权重。 */
    private BigDecimal weight;
    /** 是否隐藏详情。 */
    private Boolean hidden;
    /** 是否为样例。 */
    private Boolean sample;
}
