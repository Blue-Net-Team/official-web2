package com.bluenet.web.domain.repository;

import com.bluenet.web.domain.model.entity.Achievement;
import com.bluenet.web.domain.model.enumerate.AchievementType;
import com.bluenet.web.domain.model.enumerate.AwardLevel;
import com.bluenet.web.domain.model.vo.AchievementStatsVO;
import com.bluenet.web.domain.model.vo.AchievementVO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AchievementRepository {
    Page<AchievementVO> findAchievementsWithFilter(AchievementType type, AwardLevel awardLevel, Integer year,
            Pageable pageable);

    AchievementStatsVO findAchievementStats();

    AchievementVO save(Achievement achievement);

    AchievementVO findById(Long id);

    AchievementVO update(Achievement achievement);

    void delete(Long id);
}
