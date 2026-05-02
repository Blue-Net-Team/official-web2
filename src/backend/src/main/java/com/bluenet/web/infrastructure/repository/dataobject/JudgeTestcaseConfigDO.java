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
 * 算法题测试用例生成配置数据对象。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("tb_judge_testcase_config")
public class JudgeTestcaseConfigDO {
    /**
     * 测试用例配置主键。
     */
    @TableId(type = IdType.AUTO)
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
}
