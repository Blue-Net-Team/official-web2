package com.bluenet.web.domain.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.bluenet.web.domain.model.enumerate.AchievementType;
import com.bluenet.web.domain.model.enumerate.AwardLevel;
import lombok.Data;

import java.time.LocalDate;

@Data
@TableName("tb_achievement")
public class Achievement {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String title;
    private AchievementType type;
    private String relateTo;
    private LocalDate achieveAt;
    private AwardLevel awardLevel;
    private String awardName;
    private Long fileId;

    /**
     * 创建成就实体
     *
     * @param title
     *            成就标题
     * @param type
     *            成就类型
     * @param relateTo
     *            关联项（竞赛名称/期刊名称）
     * @param achieveAt
     *            获奖日期
     * @param awardLevel
     *            奖项级别
     * @param awardName
     *            奖项名称
     * @param fileId
     *            文件ID
     * @return 成就实体
     */
    public static Achievement create(String title, AchievementType type, String relateTo, LocalDate achieveAt,
            AwardLevel awardLevel, String awardName, Long fileId) {
        Achievement achievement = new Achievement();
        achievement.setTitle(title);
        achievement.setType(type);
        achievement.setRelateTo(relateTo);
        achievement.setAchieveAt(achieveAt);
        achievement.setAwardLevel(awardLevel);
        achievement.setAwardName(awardName);
        achievement.setFileId(fileId);
        return achievement;
    }

    /**
     * 更新成就实体
     *
     * @param title
     *            新标题
     * @param type
     *            新类型
     * @param relateTo
     *            新关联项
     * @param achieveAt
     *            新获奖日期
     * @param awardLevel
     *            新奖项级别
     * @param awardName
     *            新奖项名称
     * @param fileId
     *            新文件ID
     */
    public void update(String title, AchievementType type, String relateTo, LocalDate achieveAt,
            AwardLevel awardLevel, String awardName, Long fileId) {
        this.setTitle(title);
        this.setType(type);
        this.setRelateTo(relateTo);
        this.setAchieveAt(achieveAt);
        this.setAwardLevel(awardLevel);
        this.setAwardName(awardName);
        this.setFileId(fileId);
    }
}
