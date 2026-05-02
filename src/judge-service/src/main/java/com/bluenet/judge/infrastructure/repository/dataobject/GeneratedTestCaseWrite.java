package com.bluenet.judge.infrastructure.repository.dataobject;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 生成后的测试用例写入对象。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GeneratedTestCaseWrite {
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
    /** 输出文件对象键。 */
    private String outputObjectKey;
    /** 输出文件 SHA-256 哈希。 */
    private String outputObjectHash;
    /** 输入文件大小，单位字节。 */
    private Long inputSizeBytes;
    /** 输出文件大小，单位字节。 */
    private Long outputSizeBytes;
    /** 测试用例权重。 */
    private BigDecimal weight;
    /** 是否隐藏详情。 */
    private Boolean hidden;
    /** 是否为样例。 */
    private Boolean sample;
}
