package com.bluenet.web.infrastructure.repository.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bluenet.web.domain.model.entity.Achievement;
import com.bluenet.web.domain.model.entity.AchievementExternalMember;
import com.bluenet.web.domain.model.enumerate.AchievementType;
import com.bluenet.web.domain.model.enumerate.AwardLevel;
import com.bluenet.web.application.result.achievement.AchievementStatistics;
import com.bluenet.web.domain.model.readmodel.AchievementMemberReadModel;
import com.bluenet.web.domain.model.readmodel.AchievementReadModel;
import com.bluenet.web.domain.repository.AchievementRepository;
import com.bluenet.web.infrastructure.repository.converter.AchievementRepositoryConverter;
import com.bluenet.web.infrastructure.repository.dataobject.AchievementDO;
import com.bluenet.web.infrastructure.repository.dataobject.AchievementExternalMemberDO;
import com.bluenet.web.infrastructure.repository.dataobject.CompetitionDO;
import com.bluenet.web.infrastructure.repository.dataobject.FileDO;
import com.bluenet.web.infrastructure.repository.dataobject.UserAchievementDO;
import com.bluenet.web.infrastructure.repository.dataobject.UserDO;
import com.bluenet.web.infrastructure.repository.dataobject.query.AchievementStatsQueryDO;
import com.bluenet.web.infrastructure.repository.mapper.AchievementExternalMemberMapper;
import com.bluenet.web.infrastructure.repository.mapper.AchievementMapper;
import com.bluenet.web.infrastructure.repository.mapper.CompetitionMapper;
import com.bluenet.web.infrastructure.repository.mapper.FileMapper;
import com.bluenet.web.infrastructure.repository.mapper.UserAchievementMapper;
import com.bluenet.web.infrastructure.repository.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
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
    private final UserAchievementMapper userAchievementMapper;
    private final AchievementExternalMemberMapper externalMemberMapper;
    private final UserMapper userMapper;
    private final AchievementRepositoryConverter converter;

    @Override
    public org.springframework.data.domain.Page<AchievementReadModel> findAchievementsWithFilter(AchievementType type,
            AwardLevel awardLevel, Integer year, Pageable pageable) {
        Page<AchievementDO> page = new Page<>(pageable.getPageNumber() + 1, pageable.getPageSize());
        IPage<AchievementDO> result = achievementMapper.selectAchievementsWithFilter(type, awardLevel, year, page);

        List<AchievementReadModel> content = buildAchievementReadModels(result.getRecords());
        return new PageImpl<>(content, pageable, result.getTotal());
    }

    @Override
    public AchievementStatistics findAchievementStats() {
        AchievementStatsQueryDO stats = achievementMapper.selectAchievementStats();
        return AchievementStatistics.builder()
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
        saveMemberAssociations(achievement.getId(), achievement.getMemberIds());
        saveExternalMembers(achievement.getId(), achievement.getExternalMembers());
    }

    @Override
    public Optional<Achievement> findById(Long id) {
        AchievementDO dataObject = achievementMapper.selectById(id);
        if (dataObject == null) {
            return Optional.empty();
        }
        Achievement achievement = converter.toEntity(dataObject);
        achievement.setMemberIds(findMemberIds(id));
        achievement.setExternalMembers(findExternalMemberNames(id));
        return Optional.of(achievement);
    }

    @Override
    public void deleteById(Long id) {
        achievementMapper.deleteById(id);
        // 级联清理成员关联与外部协作者
        userAchievementMapper.delete(new QueryWrapper<UserAchievementDO>().eq("achievement_id", id));
        externalMemberMapper.delete(new QueryWrapper<AchievementExternalMemberDO>().eq("achievement_id", id));
    }

    @Override
    public void saveMemberAssociations(Long achievementId, List<Long> userIds) {
        userAchievementMapper.delete(new QueryWrapper<UserAchievementDO>().eq("achievement_id", achievementId));
        if (userIds == null || userIds.isEmpty()) {
            return;
        }
        for (Long userId : userIds) {
            userAchievementMapper.insert(
                    UserAchievementDO.builder()
                            .achievementId(achievementId)
                            .userId(userId)
                            .build());
        }
    }

    @Override
    public void saveExternalMembers(Long achievementId, List<String> names) {
        externalMemberMapper
                .delete(new QueryWrapper<AchievementExternalMemberDO>().eq("achievement_id", achievementId));
        if (names == null || names.isEmpty()) {
            return;
        }
        int order = 0;
        for (String name : names) {
            AchievementExternalMember member = AchievementExternalMember.create(achievementId, name, order++);
            externalMemberMapper.insert(converter.toExternalMemberDataObject(member));
        }
    }

    @Override
    public Map<Long, List<AchievementMemberReadModel>> findMembersByAchievementIds(List<Long> achievementIds) {
        if (achievementIds == null || achievementIds.isEmpty()) {
            return Collections.emptyMap();
        }
        List<UserAchievementDO> associations = userAchievementMapper.selectList(
                new QueryWrapper<UserAchievementDO>().in("achievement_id", achievementIds));
        if (associations.isEmpty()) {
            return Collections.emptyMap();
        }
        List<Long> userIds = associations.stream()
                .map(UserAchievementDO::getUserId)
                .distinct()
                .toList();
        Map<Long, UserDO> users = userMapper.selectBatchIds(userIds)
                .stream()
                .collect(Collectors.toMap(UserDO::getId, Function.identity()));
        Map<Long, List<AchievementMemberReadModel>> result = new HashMap<>();
        for (UserAchievementDO association : associations) {
            UserDO user = users.get(association.getUserId());
            if (user == null) {
                continue;
            }
            result.computeIfAbsent(association.getAchievementId(), key -> new ArrayList<>())
                    .add(
                            AchievementMemberReadModel.builder()
                                    .userId(user.getId())
                                    .username(user.getUsername())
                                    .avatarFileId(user.getAvatarId())
                                    .build());
        }
        return result;
    }

    @Override
    public Map<Long, List<String>> findExternalMembersByAchievementIds(List<Long> achievementIds) {
        if (achievementIds == null || achievementIds.isEmpty()) {
            return Collections.emptyMap();
        }
        List<AchievementExternalMemberDO> members = externalMemberMapper.selectByAchievementIds(achievementIds);
        Map<Long, List<String>> result = new HashMap<>();
        for (AchievementExternalMemberDO member : members) {
            result.computeIfAbsent(member.getAchievementId(), key -> new ArrayList<>())
                    .add(member.getName());
        }
        return result;
    }

    @Override
    public List<AchievementReadModel> findByUserId(Long userId) {
        List<UserAchievementDO> associations = userAchievementMapper.selectList(
                new QueryWrapper<UserAchievementDO>().eq("user_id", userId));
        if (associations.isEmpty()) {
            return List.of();
        }
        List<Long> achievementIds = associations.stream()
                .map(UserAchievementDO::getAchievementId)
                .distinct()
                .toList();
        List<AchievementDO> achievements = new ArrayList<>(achievementMapper.selectBatchIds(achievementIds));
        // 按获奖日期降序，空日期排最后
        achievements.sort(
                Comparator.comparing(
                        AchievementDO::getAchieveAt,
                        Comparator.nullsLast(Comparator.reverseOrder())));
        return buildAchievementReadModels(achievements);
    }

    private List<Long> findMemberIds(Long achievementId) {
        return userAchievementMapper.selectList(
                new QueryWrapper<UserAchievementDO>().eq("achievement_id", achievementId))
                .stream()
                .map(UserAchievementDO::getUserId)
                .toList();
    }

    private List<String> findExternalMemberNames(Long achievementId) {
        return externalMemberMapper.selectByAchievementIds(List.of(achievementId))
                .stream()
                .map(AchievementExternalMemberDO::getName)
                .toList();
    }

    private List<AchievementReadModel> buildAchievementReadModels(List<AchievementDO> achievements) {
        if (achievements.isEmpty()) {
            return List.of();
        }
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
                .map(String::trim)
                .distinct()
                .toList();
        Map<String, CompetitionDO> competitions = competitionNames.isEmpty()
                ? Collections.emptyMap()
                : competitionMapper.selectByNames(competitionNames)
                        .stream()
                        .collect(Collectors.toMap(CompetitionDO::getName, Function.identity(), (left, right) -> left));
        List<Long> achievementIds = achievements.stream()
                .map(AchievementDO::getId)
                .filter(Objects::nonNull)
                .toList();
        Map<Long, List<AchievementMemberReadModel>> membersMap = findMembersByAchievementIds(achievementIds);
        Map<Long, List<String>> externalMembersMap = findExternalMembersByAchievementIds(achievementIds);
        return achievements.stream()
                .map(
                        achievement -> toVO(
                                achievement,
                                files.get(achievement.getFileId()),
                                competitions.get(
                                        achievement.getRelateTo() == null ? null : achievement.getRelateTo().trim()),
                                membersMap.get(achievement.getId()),
                                externalMembersMap.get(achievement.getId())))
                .toList();
    }

    private AchievementReadModel toVO(AchievementDO achievement, FileDO file, CompetitionDO competition,
            List<AchievementMemberReadModel> members, List<String> externalMembers) {
        return AchievementReadModel.builder()
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
                .members(members == null ? List.of() : members)
                .externalMembers(externalMembers == null ? List.of() : externalMembers)
                .build();
    }
}
