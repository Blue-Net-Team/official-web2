package com.bluenet.web.infrastructure.repository.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.bluenet.web.domain.model.enumerate.AlgorithmTestcaseType;
import com.bluenet.web.domain.model.enumerate.JudgeCaseStatus;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Mapper 专用数据对象，只承载数据库表字段，避免持久层依赖领域实体行为。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("tb_algorithm_judge_case_result")
public class AlgorithmJudgeCaseResultDO {
    /**
     * 当前对象在系统中的唯一标识。
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 算法评测任务标识。
     */
    private Long judgeJobId;
    /**
     * 算法评测用例序号。
     */
    private Integer caseNo;

    /**
     * 评测用例类型，例如示例、自测或正式用例。
     */
    private AlgorithmTestcaseType testcaseType;
    /**
     * 当前业务流程、任务或记录的状态。
     */
    private JudgeCaseStatus status;

    /**
     * 算法示例或评测用例输入内容。
     */
    private String input;
    /**
     * 算法评测用例期望输出内容。
     */
    private String expectedOutput;

    /**
     * 算法评测运行后得到的实际输出内容。
     */
    private String actualOutput;
    /**
     * 程序运行产生的标准输出。
     */
    private String stdout;

    /**
     * 程序运行产生的标准错误输出。
     */
    private String stderr;
    /**
     * 评测用例运行耗时，单位毫秒。
     */
    private Integer timeUsedMs;

    /**
     * 评测用例运行消耗内存，单位 KB。
     */
    private Integer memoryUsedKb;
    /**
     * 任务状态、评测结果或业务处理的提示信息。
     */
    private String message;

    /**
     * 评测用例结果是否对候选人可见。
     */
    private Boolean visibleToCandidate;
    /**
     * 记录创建时间。
     */
    private LocalDateTime createdAt;
}
