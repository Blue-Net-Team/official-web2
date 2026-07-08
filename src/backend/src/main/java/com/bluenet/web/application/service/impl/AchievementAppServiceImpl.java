package com.bluenet.web.application.service.impl;

import com.bluenet.web.application.AchievementResult;
import com.bluenet.web.application.command.achievement.AchievementCommands;
import com.bluenet.web.application.service.AchievementAppService;
import com.bluenet.web.domain.exception.BadRequest;
import com.bluenet.web.domain.exception.DataNotFound;
import com.bluenet.web.domain.model.entity.Achievement;
import com.bluenet.web.domain.model.enumerate.AchievementType;
import com.bluenet.web.domain.model.enumerate.AwardLevel;
import com.bluenet.web.domain.model.enumerate.FileType;
import com.bluenet.web.domain.model.vo.AchievementStatsVO;
import com.bluenet.web.domain.model.vo.AchievementVO;
import com.bluenet.web.domain.model.vo.FileVO;
import com.bluenet.web.domain.repository.AchievementRepository;
import com.bluenet.web.domain.service.FileDomainService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

        Page<AchievementVO> voPage = achievementRepository.findAchievementsWithFilter(type, awardLevel, year, pageable);
        return voPage.map(this::toResult);
    }

    /**
     * 获取统计信息。
     *
     * @return 成就统计结果
     */
    @Override
    public AchievementStatsVO getAchievementStats() {
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

        Achievement achievement = Achievement.create(
                command.title(),
                command.type(),
                command.relateTo(),
                command.achieveAt(),
                command.awardLevel(),
                command.awardName(),
                command.fileId());

        achievementRepository.save(achievement);
        return toResult(achievement);
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

        achievementRepository.save(achievement);
        return toResult(achievement);
    }

    /**
     * 删除成就。
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

    private void validateFile(Long fileId) {
        FileVO file = fileDomainService.getFileById(fileId);
        if (file == null) {
            throw new DataNotFound("文件不存在");
        }
        if (file.getType() != FileType.NORMAL_IMG) {
            throw new BadRequest("文件类型不匹配，期望 NORMAL_IMG");
        }
    }

    private AchievementResult toResult(Achievement achievement) {
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
                null);
    }

    private AchievementResult toResult(AchievementVO vo) {
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
                vo.getFileUrl());
    }
}
