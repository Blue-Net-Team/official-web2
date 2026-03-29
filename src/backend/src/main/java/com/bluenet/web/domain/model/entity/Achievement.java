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
}
