package com.bluenet.web.infrastructure.repository.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
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
@TableName("tb_assessment_decision")
public class AssessmentDecisionDO {
    /**
     * 当前对象在系统中的唯一标识。
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 关联用户标识。
     */
    private Long userId;
    /**
     * 所属考核场次或考核时间配置标识。
     */
    private Long assessmentTimeId;

    /**
     * 候选人最终是否通过考核。
     */
    private Boolean passed;
    /**
     * 做出最终决策的管理员用户标识。
     */
    private Long decidedBy;

    /**
     * 最终决策的说明或原因。
     */
    private String decisionComment;
    /**
     * 最终录用或通过决策生成时间。
     */
    private LocalDateTime decidedAt;

    /**
     * 记录最后更新时间。
     */
    private LocalDateTime updatedAt;
}
