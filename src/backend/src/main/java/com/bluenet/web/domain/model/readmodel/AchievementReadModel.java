package com.bluenet.web.domain.model.readmodel;

import com.bluenet.web.domain.model.enumerate.AchievementType;
import com.bluenet.web.domain.model.enumerate.AwardLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@Builder
public class AchievementReadModel {
    /**
     * 当前对象在系统中的唯一标识。
     */
    private Long id;
    /**
     * 标题或名称，用于列表和详情展示。
     */
    private String title;
    /**
     * 成果关联的竞赛、项目或业务对象名称。
     */
    private String relateTo;
    /**
     * 业务分类或枚举类型。
     */
    private AchievementType type;
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
     * 竞赛名称。
     */
    private String competitionName;
    /**
     * 竞赛简称，用于列表或首页紧凑展示。
     */
    private String competitionShortName;
    /**
     * 竞赛 Logo 对应的文件标识。
     */
    private Long competitionLogoFileId;
    /**
     * 关联文件记录标识。
     */
    private Long fileId;
    /**
     * 文件访问地址。
     */
    private String fileUrl;
}
