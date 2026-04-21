package com.bluenet.web.infrastructure.repository.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.bluenet.web.domain.model.enumerate.AchievementType;
import com.bluenet.web.domain.model.enumerate.AwardLevel;
import java.time.LocalDate;
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
@TableName("tb_achievement")
public class AchievementDO {
    /**
     * 当前对象在系统中的唯一标识。
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 标题或名称，用于列表和详情展示。
     */
    private String title;
    /**
     * 业务分类或枚举类型。
     */
    private AchievementType type;

    /**
     * 成果关联的竞赛、项目或业务对象名称。
     */
    private String relateTo;
    /**
     * 成果取得或获奖发生的时间。
     */
    private LocalDate achieveAt;

    /**
     * 奖项级别，例如国家级、省级或校级。
     */
    private AwardLevel awardLevel;
    /**
     * 奖项名称或获奖名次。
     */
    private String awardName;

    /**
     * 关联文件记录标识。
     */
    private Long fileId;
}
