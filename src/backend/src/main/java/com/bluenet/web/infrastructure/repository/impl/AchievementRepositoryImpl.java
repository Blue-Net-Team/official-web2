package com.bluenet.web.infrastructure.repository.impl;

import com.bluenet.web.infrastructure.repository.support.RepositoryObjectConverter;

import com.bluenet.web.infrastructure.repository.dataobject.*;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bluenet.web.domain.model.entity.Achievement;
import com.bluenet.web.domain.model.enumerate.AchievementType;
import com.bluenet.web.domain.model.enumerate.AwardLevel;
import com.bluenet.web.domain.model.vo.AchievementStatsVO;
import com.bluenet.web.domain.model.vo.AchievementVO;
import com.bluenet.web.domain.repository.AchievementRepository;
import com.bluenet.web.infrastructure.repository.mapper.AchievementMapper;
import com.bluenet.web.infrastructure.repository.mapper.CompetitionMapper;
import com.bluenet.web.infrastructure.repository.mapper.FileMapper;
import com.bluenet.web.infrastructure.repository.dataobject.query.AchievementStatsQueryDO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class AchievementRepositoryImpl implements AchievementRepository {
    private final AchievementMapper achievementMapper;
    private final FileMapper fileMapper;
    private final CompetitionMapper competitionMapper;

    /**
     * 按成果类型和奖项级别分页查询成果视图。
     *
     * @param type
     *            业务类型或枚举类型。
     * @param awardLevel
     *            成果奖项级别过滤条件。
     * @param year
     *            成果取得年份过滤条件。
     * @param pageable
     *            Spring 分页请求对象。
     * @return 分页后的成果 结果。
     */
    @Override
    public org.springframework.data.domain.Page<AchievementVO> findAchievementsWithFilter(AchievementType type,
            AwardLevel awardLevel, Integer year, Pageable pageable) {
        Page<AchievementDO> page = new Page<>(pageable.getPageNumber() + 1, pageable.getPageSize());
        IPage<AchievementDO> result = achievementMapper.selectAchievementsWithFilter(type, awardLevel, year, page);

        List<AchievementVO> content = buildAchievementVOs(result.getRecords());
        return new PageImpl<>(content, pageable, result.getTotal());
    }

    /**
     * 统计各级别成果数量和成果总数。
     *
     * @return 查询或处理得到的成果 结果。
     */
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

    /**
     * 保存新的成果 记录。
     *
     * @param achievement
     *            成果领域对象。
     * @return 查询或处理得到的成果 结果。
     */
    @Override
    public AchievementVO save(Achievement achievement) {
        AchievementDO dataObject = RepositoryObjectConverter.copy(achievement, AchievementDO.class);
        RepositoryObjectConverter.insert(achievementMapper, dataObject, AchievementDO.class);
        RepositoryObjectConverter.copyInto(dataObject, achievement);

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

    /**
     * 按主键查询成果 记录。
     *
     * @param id
     *            业务记录主键。
     * @return 查询或处理得到的成果 结果。
     */
    @Override
    public AchievementVO findById(Long id) {
        AchievementDO entity = achievementMapper.selectById(id);
        if (entity == null) {
            return null;
        }
        return convertToVO(entity);
    }

    /**
     * 更新已有成果 记录。
     *
     * @param achievement
     *            成果领域对象。
     * @return 数据库受影响行数。
     */
    @Override
    public AchievementVO update(Achievement achievement) {
        int rows = RepositoryObjectConverter.updateById(
                achievementMapper,
                RepositoryObjectConverter.copy(achievement, AchievementDO.class),
                AchievementDO.class);
        if (rows == 0) {
            throw new com.bluenet.web.domain.exception.GlobalException("成就更新失败，ID: " + achievement.getId());
        }

        return findById(achievement.getId());
    }

    /**
     * 删除指定成果 记录。
     *
     * @param id
     *            业务记录主键。
     */
    @Override
    public void delete(Long id) {
        int rows = achievementMapper.deleteById(id);
        if (rows == 0) {
            throw new com.bluenet.web.domain.exception.GlobalException("成就删除失败，ID: " + id);
        }
    }

    /**
     * 在成果 的持久层对象、领域对象和视图对象之间转换。
     *
     * @param entity
     *            领域实体。
     * @return 转换后的目标模型对象。
     */
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

    /**
     * 组合成果主表、文件和竞赛数据，构造成果展示视图列表。
     *
     * @param achievements
     *            成果数据行集合。
     * @return 满足条件的成果 结果集合。
     */
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

    /**
     * 将成果 及其关联数据组装为领域视图对象。
     *
     * @param achievement
     *            成果领域对象。
     * @param file
     *            文件领域对象或文件视图对象。
     * @param competition
     *            竞赛领域对象。
     * @return 转换后的目标模型对象。
     */
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
