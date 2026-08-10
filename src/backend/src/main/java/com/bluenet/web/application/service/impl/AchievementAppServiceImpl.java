package com.bluenet.web.application.service.impl;

import com.bluenet.web.application.result.achievement.AchievementMemberResult;
import com.bluenet.web.application.result.achievement.AchievementResult;
import com.bluenet.web.application.command.achievement.AchievementCommands;
import com.bluenet.web.application.service.AchievementAppService;
import com.bluenet.web.domain.exception.BadRequest;
import com.bluenet.web.domain.exception.DataNotFound;
import com.bluenet.web.domain.model.entity.Achievement;
import com.bluenet.web.domain.model.entity.File;
import com.bluenet.web.domain.model.enumerate.AchievementType;
import com.bluenet.web.domain.model.enumerate.AwardLevel;
import com.bluenet.web.domain.model.enumerate.FileType;
import com.bluenet.web.application.result.achievement.AchievementStatistics;
import com.bluenet.web.domain.model.readmodel.AchievementMemberReadModel;
import com.bluenet.web.domain.model.readmodel.AchievementReadModel;
import com.bluenet.web.domain.repository.AchievementRepository;
import com.bluenet.web.domain.repository.UserRepository;
import com.bluenet.web.domain.service.FileDomainService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 成就应用服务实现。
 * <p>
 * 实现成就聚合在应用层的业务逻辑编排。
 * </p>
 */
@Service
@RequiredArgsConstructor
public class AchievementAppServiceImpl implements AchievementAppService {
    private final AchievementRepository achievementRepository;
    private final FileDomainService fileDomainService;
    private final UserRepository userRepository;

    /**
     * 分页查询成就列表。
     *
     * @param page
     *            页码
     * @param size
     *            每页大小
     * @param type
     *            成就类型
     * @param awardLevel
     *            奖项级别
     * @param year
     *            年份
     * @return 成就分页结果
     */
    @Override
    public Page<AchievementResult> getAchievements(Integer page, Integer size, AchievementType type,
            AwardLevel awardLevel, Integer year) {
        int pageNum = page != null ? page : 0;
        int pageSize = size != null ? size : 12;
        Pageable pageable = PageRequest.of(pageNum, pageSize);

        Page<AchievementReadModel> voPage = achievementRepository
                .findAchievementsWithFilter(type, awardLevel, year, pageable);
        return voPage.map(this::toResult);
    }

    /**
     * 获取统计信息。
     *
     * @return 成就统计结果
     */
    @Override
    public AchievementStatistics getAchievementStats() {
        return achievementRepository.findAchievementStats();
    }

    /**
     * 创建成就。
     *
     * @param command
     *            创建成就命令
     * @return 创建后的成就结果
     */
    @Override
    @Transactional
    public AchievementResult createAchievement(AchievementCommands.CreateAchievementCommand command) {
        validateFile(command.fileId());
        validateMemberIds(command.userIds());

        Achievement achievement = Achievement.create(
                command.title(),
                command.type(),
                command.relateTo(),
                command.achieveAt(),
                command.awardLevel(),
                command.awardName(),
                command.fileId());
        achievement.assignMembers(command.userIds(), command.externalMembers());

        achievementRepository.save(achievement);
        return toResult(achievementRepository.findById(achievement.getId()).orElseThrow());
    }

    /**
     * 更新成就。
     *
     * @param command
     *            更新成就命令
     * @return 更新后的成就结果
     */
    @Override
    @Transactional
    public AchievementResult updateAchievement(AchievementCommands.UpdateAchievementCommand command) {
        validateFile(command.fileId());
        validateMemberIds(command.userIds());

        Achievement achievement = achievementRepository.findById(command.id())
                .orElseThrow(() -> new DataNotFound("成就不存在"));

        achievement.update(
                command.title(),
                command.type(),
                command.relateTo(),
                command.achieveAt(),
                command.awardLevel(),
                command.awardName(),
                command.fileId());
        achievement.assignMembers(command.userIds(), command.externalMembers());

        achievementRepository.save(achievement);
        return toResult(achievementRepository.findById(achievement.getId()).orElseThrow());
    }

    /**
     * 删除成就。
     * <p>
     * 仓储层级联清理成员关联与外部协作者。
     * </p>
     *
     * @param id
     *            成就ID
     */
    @Override
    @Transactional
    public void deleteAchievement(Long id) {
        Achievement achievement = achievementRepository.findById(id)
                .orElseThrow(() -> new DataNotFound("成就不存在"));
        achievementRepository.deleteById(id);
    }

    /**
     * 查询指定成员关联的成就列表，按获奖日期倒序。
     *
     * @param memberId
     *            成员用户ID
     * @return 成就结果列表
     */
    @Override
    public List<AchievementResult> getMemberAchievements(Long memberId) {
        userRepository.findById(memberId)
                .orElseThrow(() -> new DataNotFound("成员不存在"));
        return achievementRepository.findByUserId(memberId)
                .stream()
                .map(this::toResult)
                .toList();
    }

    private void validateFile(Long fileId) {
        File file = fileDomainService.getFileById(fileId);
        if (file == null) {
            throw new DataNotFound("文件不存在");
        }
        if (file.getType() != FileType.NORMAL_IMG) {
            throw new BadRequest("文件类型不匹配，期望 NORMAL_IMG");
        }
    }

    private void validateMemberIds(List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return;
        }
        List<Long> distinctIds = userIds.stream().filter(id -> id != null).distinct().toList();
        Set<Long> existingIds = new HashSet<>(userRepository.findExistingUserIds(distinctIds));
        if (existingIds.size() != distinctIds.size()) {
            throw new BadRequest("存在无效的成员用户");
        }
    }

    private AchievementResult toResult(Achievement achievement) {
        List<AchievementMemberReadModel> members = achievementRepository
                .findMembersByAchievementIds(List.of(achievement.getId()))
                .getOrDefault(achievement.getId(), List.of());
        return new AchievementResult(
                achievement.getId(),
                achievement.getTitle(),
                achievement.getType(),
                achievement.getRelateTo(),
                achievement.getAchieveAt(),
                achievement.getAwardLevel(),
                achievement.getAwardLevel() != null ? achievement.getAwardLevel().getDescription() : null,
                achievement.getAwardName(),
                null,
                null,
                null,
                achievement.getFileId(),
                null,
                members.stream().map(this::toMemberResult).toList(),
                achievement.getExternalMembers());
    }

    private AchievementResult toResult(AchievementReadModel vo) {
        return new AchievementResult(
                vo.getId(),
                vo.getTitle(),
                vo.getType(),
                vo.getRelateTo(),
                vo.getAchieveAt(),
                vo.getAwardLevel(),
                vo.getAwardLevel() != null ? vo.getAwardLevel().getDescription() : null,
                vo.getAwardName(),
                vo.getCompetitionName(),
                vo.getCompetitionShortName(),
                vo.getCompetitionLogoFileId(),
                vo.getFileId(),
                vo.getFileUrl(),
                vo.getMembers() == null
                        ? List.of()
                        : vo.getMembers().stream().map(this::toMemberResult).toList(),
                vo.getExternalMembers() == null ? List.of() : vo.getExternalMembers());
    }

    private AchievementMemberResult toMemberResult(AchievementMemberReadModel member) {
        return new AchievementMemberResult(member.getUserId(), member.getUsername(), member.getAvatarFileId());
    }
}
