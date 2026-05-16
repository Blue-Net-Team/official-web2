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
@TableName("tb_assessment_team")
public class AssessmentTeamDO {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long assessmentTimeId;

    private Long leaderId;

    private String name;

    private String inviteCode;

    private String status;

    private LocalDateTime createdAt;
}
