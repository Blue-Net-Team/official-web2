package com.bluenet.web.application.result.achievement;

import com.bluenet.web.domain.model.enumerate.AchievementType;
import com.bluenet.web.domain.model.enumerate.AwardLevel;

import java.time.LocalDate;

/**
 * 成就聚合的应用层结果对象。
 * <p>
 * 封装了成就相关操作返回给 API 层的数据。
 * </p>
 */
public record AchievementResult(
        /** 唯一标识 */
        Long id,
        /** 标题 */
        String title,
        /** 类型 */
        AchievementType type,
        /** 关联对象 */
        String relateTo,
        /** 获得时间 */
        LocalDate achieveAt,
        /** 奖项等级 */
        AwardLevel awardLevel,
        /** 奖项等级名称 */
        String awardLevelName,
        /** 奖项名称 */
        String awardName,
        /** 竞赛名称 */
        String competitionName,
        /** 竞赛简称 */
        String competitionShortName,
        /** 竞赛Logo文件ID */
        Long competitionLogoFileId,
        /** 文件ID */
        Long fileId,
        /** 文件URL地址 */
        String fileUrl) {
}
