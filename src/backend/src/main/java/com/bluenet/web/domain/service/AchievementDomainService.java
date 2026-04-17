package com.bluenet.web.domain.service;

import com.bluenet.web.domain.model.entity.Achievement;
import com.bluenet.web.domain.model.enumerate.AchievementType;
import com.bluenet.web.domain.model.enumerate.AwardLevel;

import java.time.LocalDate;

public interface AchievementDomainService {
    Achievement createAchievement(String title, AchievementType type, String relateTo, LocalDate achieveAt,
            AwardLevel awardLevel, String awardName, Long fileId);

    Achievement updateAchievement(Achievement achievement, String title, AchievementType type, String relateTo,
            LocalDate achieveAt, AwardLevel awardLevel, String awardName, Long fileId);
}
