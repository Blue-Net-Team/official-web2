package com.bluenet.web.infrastructure.repository.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bluenet.web.domain.model.entity.Achievement;
import com.bluenet.web.domain.model.enumerate.AchievementType;
import com.bluenet.web.domain.model.enumerate.AwardLevel;
import com.bluenet.web.domain.model.vo.AchievementStatsVO;
import com.bluenet.web.domain.model.vo.AchievementVO;
import com.bluenet.web.domain.repository.AchievementRepository;
import com.bluenet.web.infrastructure.repository.converter.AchievementRepositoryConverter;
import com.bluenet.web.infrastructure.repository.dataobject.AchievementDO;
import com.bluenet.web.infrastructure.repository.dataobject.CompetitionDO;
import com.bluenet.web.infrastructure.repository.dataobject.FileDO;
import com.bluenet.web.infrastructure.repository.dataobject.query.AchievementStatsQueryDO;
import com.bluenet.web.infrastructure.repository.mapper.AchievementMapper;
import com.bluenet.web.infrastructure.repository.mapper.CompetitionMapper;
import com.bluenet.web.infrastructure.repository.mapper.FileMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class AchievementRepositoryImpl implements AchievementRepository {
    private final AchievementMapper achievementMapper;
    private final FileMapper fileMapper;
    private final CompetitionMapper competitionMapper;
    private final AchievementRepositoryConverter converter;

    @Override
    public org.springframework.data.domain.Page<AchievementVO> findAchievementsWithFilter(AchievementType type,
            AwardLevel awardLevel, Integer year, Pageable pageable) {
        Page<AchievementDO> page = new Page<>(pageable.getPageNumber() + 1, pageable.getPageSize());
        IPage<AchievementDO> result = achievementMapper.selectAchievementsWithFilter(type, awardLevel, year, page);

        List<AchievementVO> content = buildAchievementVOs(result.getRecords());
        return new PageImpl<>(content, pageable, result.getTotal());
    }

    @Override
    public AchievementStatsVO findAchievementStats() {
        AchievementStatsQueryDO stats = achievementMapper.selectAchievementStats();
        return AchievementStatsVO.builder()
                .totalAchievements(stats.getTotalAchievements())
                .nationalCount(stats.getNationalCount())
                .provincialCount(stats.getProvincialCount())
                .schoolCount(stats.getSchoolCount())
                .build();
    }

    @Override
    public void save(Achievement achievement) {
        AchievementDO dataObject = converter.toDataObject(achievement);
        if (dataObject.getId() == null) {
            achievementMapper.insert(dataObject);
            achievement.setId(dataObject.getId());
        } else {
            achievementMapper.updateById(dataObject);
        }
    }

    @Override
    public Optional<Achievement> findById(Long id) {
        AchievementDO dataObject = achievementMapper.selectById(id);
        return Optional.ofNullable(converter.toEntity(dataObject));
    }
    @Override
    public void deleteById(Long id) {
        achievementMapper.deleteById(id);
    }

    private AchievementVO convertToVO(AchievementDO entity) {
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

    private List<AchievementVO> buildAchievementVOs(List<AchievementDO> achievements) {
        List<Long> fileIds = achievements.stream()
                .map(AchievementDO::getFileId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        Map<Long, FileDO> files = fileIds.isEmpty()
                ? Collections.emptyMap()
                : fileMapper.selectBatchIds(fileIds)
                        .stream()
                        .collect(Collectors.toMap(FileDO::getId, Function.identity()));
        List<String> competitionNames = achievements.stream()
                .map(AchievementDO::getRelateTo)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        Map<String, CompetitionDO> competitions = competitionNames.isEmpty()
                ? Collections.emptyMap()
                : competitionMapper.selectByNames(competitionNames)
                        .stream()
                        .collect(Collectors.toMap(CompetitionDO::getName, Function.identity(), (left, right) -> left));
        return achievements.stream()
                .map(
                        achievement -> toVO(
                                achievement,
                                files.get(achievement.getFileId()),
                                competitions.get(achievement.getRelateTo())))
                .toList();
    }

    private AchievementVO toVO(AchievementDO achievement, FileDO file, CompetitionDO competition) {
        return AchievementVO.builder()
                .id(achievement.getId())
                .title(achievement.getTitle())
                .type(achievement.getType())
                .relateTo(achievement.getRelateTo())
                .achieveAt(achievement.getAchieveAt())
                .awardLevel(achievement.getAwardLevel())
                .awardName(achievement.getAwardName())
                .fileId(achievement.getFileId())
                .fileUrl(file == null ? null : file.getUrl())
                .competitionName(competition == null ? null : competition.getName())
                .competitionShortName(competition == null ? null : competition.getShortName())
                .competitionLogoFileId(competition == null ? null : competition.getLogoFileId())
                .build();
    }
}
