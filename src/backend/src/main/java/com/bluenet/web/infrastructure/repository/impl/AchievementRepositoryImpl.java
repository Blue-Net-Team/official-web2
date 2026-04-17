package com.bluenet.web.infrastructure.repository.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bluenet.web.domain.model.entity.Achievement;
import com.bluenet.web.domain.model.enumerate.AchievementType;
import com.bluenet.web.domain.model.enumerate.AwardLevel;
import com.bluenet.web.domain.model.vo.AchievementStatsVO;
import com.bluenet.web.domain.model.vo.AchievementVO;
import com.bluenet.web.domain.repository.AchievementRepository;
import com.bluenet.web.infrastructure.repository.mapper.AchievementMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class AchievementRepositoryImpl implements AchievementRepository {
    private final AchievementMapper achievementMapper;

    @Override
    public org.springframework.data.domain.Page<AchievementVO> findAchievementsWithFilter(AchievementType type,
            AwardLevel awardLevel, Integer year, Pageable pageable) {
        Page<AchievementVO> page = new Page<>(pageable.getPageNumber() + 1, pageable.getPageSize());
        IPage<AchievementVO> result = achievementMapper.selectAchievementsWithFilter(type, awardLevel, year, page);

        List<AchievementVO> content = result.getRecords();
        return new PageImpl<>(content, pageable, result.getTotal());
    }

    @Override
    public AchievementStatsVO findAchievementStats() {
        return achievementMapper.selectAchievementStats();
    }

    @Override
    public AchievementVO save(Achievement achievement) {
        achievementMapper.insert(achievement);

        Long id = achievement.getId();

        return AchievementVO.builder()
                .id(id)
                .title(achievement.getTitle())
                .type(achievement.getType())
                .relateTo(achievement.getRelateTo())
                .achieveAt(achievement.getAchieveAt())
                .awardLevel(achievement.getAwardLevel())
                .awardName(achievement.getAwardName())
                .fileId(achievement.getFileId())
                .build();
    }

    @Override
    public AchievementVO findById(Long id) {
        Achievement entity = achievementMapper.selectById(id);
        if (entity == null) {
            return null;
        }
        return convertToVO(entity);
    }

    @Override
    public AchievementVO update(Achievement achievement) {
        int rows = achievementMapper.updateById(achievement);
        if (rows == 0) {
            throw new com.bluenet.web.domain.exception.GlobalException("成就更新失败，ID: " + achievement.getId());
        }

        return findById(achievement.getId());
    }

    @Override
    public void delete(Long id) {
        int rows = achievementMapper.deleteById(id);
        if (rows == 0) {
            throw new com.bluenet.web.domain.exception.GlobalException("成就删除失败，ID: " + id);
        }
    }

    private AchievementVO convertToVO(Achievement entity) {
        if (entity == null) {
            return null;
        }
        return AchievementVO.builder()
                .id(entity.getId())
                .title(entity.getTitle())
                .type(entity.getType())
                .relateTo(entity.getRelateTo())
                .achieveAt(entity.getAchieveAt())
                .awardLevel(entity.getAwardLevel())
                .awardName(entity.getAwardName())
                .fileId(entity.getFileId())
                .build();
    }
}
