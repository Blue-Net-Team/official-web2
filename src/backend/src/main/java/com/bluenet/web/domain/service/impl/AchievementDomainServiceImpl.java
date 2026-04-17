package com.bluenet.web.domain.service.impl;

import com.bluenet.web.domain.exception.BadRequest;
import com.bluenet.web.domain.model.entity.Achievement;
import com.bluenet.web.domain.model.enumerate.AchievementType;
import com.bluenet.web.domain.model.enumerate.AwardLevel;
import com.bluenet.web.domain.repository.AchievementRepository;
import com.bluenet.web.domain.service.AchievementDomainService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class AchievementDomainServiceImpl implements AchievementDomainService {
    private final AchievementRepository achievementRepository;

    @Override
    public Achievement createAchievement(String title, AchievementType type, String relateTo, LocalDate achieveAt,
            AwardLevel awardLevel, String awardName, Long fileId) {
        if (type == AchievementType.COMPETITION && awardLevel == null) {
            throw new BadRequest("竞赛成就必须指定奖项级别");
        }
        return Achievement.create(title, type, relateTo, achieveAt, awardLevel, awardName, fileId);
    }

    @Override
    public Achievement updateAchievement(Achievement achievement, String title, AchievementType type, String relateTo,
            LocalDate achieveAt, AwardLevel awardLevel, String awardName, Long fileId) {
        if (type == AchievementType.COMPETITION && awardLevel == null) {
            throw new BadRequest("竞赛成就必须指定奖项级别");
        }
        achievement.update(title, type, relateTo, achieveAt, awardLevel, awardName, fileId);
        return achievement;
    }
}
