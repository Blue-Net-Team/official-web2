package com.bluenet.web.domain.repository;

import com.bluenet.web.domain.model.vo.AchievementStatsVO;
import com.bluenet.web.domain.model.vo.AchievementVO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AchievementRepository {
    Page<AchievementVO> findAchievementsWithFilter(String type, String awardLevel, Integer year, Pageable pageable);

    AchievementStatsVO findAchievementStats();
}
