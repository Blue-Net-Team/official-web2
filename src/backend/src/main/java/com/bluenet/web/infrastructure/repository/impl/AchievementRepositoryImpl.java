package com.bluenet.web.infrastructure.repository.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bluenet.web.domain.model.vo.AchievementStatsVO;
import com.bluenet.web.domain.model.vo.AchievementVO;
import com.bluenet.web.domain.repository.AchievementRepository;
import com.bluenet.web.infrastructure.repository.mapper.AchievementMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageImpl;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class AchievementRepositoryImpl implements AchievementRepository {
    private final AchievementMapper achievementMapper;

    @Override
    public org.springframework.data.domain.Page<AchievementVO> findAchievementsWithFilter(String type,
            String awardLevel, Integer year, org.springframework.data.domain.Pageable pageable) {
        Page<AchievementVO> page = new Page<>(pageable.getPageNumber() + 1, pageable.getPageSize());
        IPage<AchievementVO> result = achievementMapper.selectAchievementsWithFilter(type, awardLevel, year, page);

        List<AchievementVO> content = result.getRecords();
        return new PageImpl<>(content, pageable, result.getTotal());
    }

    @Override
    public AchievementStatsVO findAchievementStats() {
        return achievementMapper.selectAchievementStats();
    }
}
