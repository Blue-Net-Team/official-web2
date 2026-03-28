package com.bluenet.web.application.service;

import com.bluenet.web.api.dto.PageDTO;
import com.bluenet.web.api.dto.achievement.AchievementDTO;
import com.bluenet.web.api.dto.achievement.AchievementStatsDTO;

public interface AchievementService {
    PageDTO<AchievementDTO> getAchievements(Integer page, Integer size, String type, String awardLevel, Integer year);

    AchievementStatsDTO getAchievementStats();
}
