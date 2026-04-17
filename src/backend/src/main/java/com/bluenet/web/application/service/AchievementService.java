package com.bluenet.web.application.service;

import com.bluenet.web.api.dto.PageDTO;
import com.bluenet.web.api.dto.achievement.AchievementDTO;
import com.bluenet.web.api.dto.achievement.AchievementStatsDTO;
import com.bluenet.web.api.dto.achievement.CreateAchievementRequestDTO;
import com.bluenet.web.api.dto.achievement.UpdateAchievementRequestDTO;
import com.bluenet.web.domain.model.enumerate.AchievementType;
import com.bluenet.web.domain.model.enumerate.AwardLevel;

public interface AchievementService {
    PageDTO<AchievementDTO> getAchievements(Integer page, Integer size, AchievementType type, AwardLevel awardLevel,
            Integer year);

    AchievementStatsDTO getAchievementStats();

    AchievementDTO createAchievement(CreateAchievementRequestDTO request);

    AchievementDTO updateAchievement(Long id, UpdateAchievementRequestDTO request);

    void deleteAchievement(Long id);
}
