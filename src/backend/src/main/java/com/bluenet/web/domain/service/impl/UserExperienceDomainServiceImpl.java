package com.bluenet.web.domain.service.impl;

import com.bluenet.web.domain.exception.DataNotFound;
import com.bluenet.web.domain.exception.Forbidden;
import com.bluenet.web.domain.model.enumerate.ExperienceType;
import com.bluenet.web.domain.model.vo.ExperienceVO;
import com.bluenet.web.domain.repository.UserExperienceRepository;
import com.bluenet.web.domain.service.UserExperienceDomainService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * 用户经历领域服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserExperienceDomainServiceImpl implements UserExperienceDomainService {
    private final UserExperienceRepository userExperienceRepository;

    @Override
    public List<ExperienceVO> getExperiences(Long userId) {
        return userExperienceRepository.findByUserId(userId);
    }

    @Override
    public List<ExperienceVO> getExperiencesByType(Long userId, ExperienceType type) {
        return userExperienceRepository.findByUserIdAndType(userId, type);
    }

    @Override
    public Optional<ExperienceVO> getExperienceById(Long experienceId, Long userId) {
        Optional<ExperienceVO> experience = userExperienceRepository.findById(experienceId);

        if (experience.isPresent()) {
            if (!userExperienceRepository.checkOwner(experienceId, userId)) {
                log.warn("用户 {} 尝试访问不属于自己的经历 {}", userId, experienceId);
                return Optional.empty();
            }
        }

        return experience;
    }

    @Override
    @Transactional
    public ExperienceVO createExperience(Long userId, ExperienceType type, String title,
            String startTime, String endTime, String content) {
        log.info("用户 {} 创建经历: type={}, title={}", userId, type, title);
        return userExperienceRepository.save(userId, type, title, startTime, endTime, content);
    }

    @Override
    @Transactional
    public ExperienceVO updateExperience(Long experienceId, Long userId, String title,
            String startTime, String endTime, String content) {
        // 验证经历存在且属于当前用户
        ExperienceVO existing = userExperienceRepository.findById(experienceId)
                .orElseThrow(() -> new DataNotFound("经历不存在"));

        // 权限校验：只能更新自己的经历
        if (!isOwner(experienceId, userId)) {
            log.warn("用户 {} 尝试更新不属于自己的经历 {}", userId, experienceId);
            throw new Forbidden("无权修改此经历");
        }

        log.info("用户 {} 更新经历 {}", userId, experienceId);
        return userExperienceRepository.update(experienceId, title, startTime, endTime, content);
    }

    @Override
    @Transactional
    public boolean deleteExperience(Long experienceId, Long userId) {
        // 验证经历存在
        ExperienceVO existing = userExperienceRepository.findById(experienceId)
                .orElseThrow(() -> new DataNotFound("经历不存在"));

        // 权限校验：只能删除自己的经历
        if (!isOwner(experienceId, userId)) {
            log.warn("用户 {} 尝试删除不属于自己的经历 {}", userId, experienceId);
            throw new Forbidden("无权删除此经历");
        }

        log.info("用户 {} 删除经历 {}", userId, experienceId);
        return userExperienceRepository.deleteById(experienceId);
    }

    @Override
    public TabCounts getTabCounts(Long userId) {
        int projects = userExperienceRepository.countByUserIdAndType(userId, ExperienceType.PROJECT);
        int competitions = userExperienceRepository.countByUserIdAndType(userId, ExperienceType.COMPETITION);
        int internships = userExperienceRepository.countByUserIdAndType(userId, ExperienceType.INTERNSHIP);

        return new TabCounts(projects, competitions, internships);
    }

    /**
     * 检查经历是否属于指定用户
     */
    private boolean isOwner(Long experienceId, Long userId) {
        return userExperienceRepository.findById(experienceId)
                .map(exp -> userExperienceRepository.checkOwner(experienceId, userId))
                .orElse(false);
    }
}
