package com.bluenet.web.domain.service.impl;

import com.bluenet.web.domain.exception.DataNotFound;
import com.bluenet.web.domain.exception.Forbidden;
import com.bluenet.web.domain.model.entity.UserExperience;
import com.bluenet.web.domain.model.enumerate.ExperienceType;
import com.bluenet.web.domain.model.vo.ExperienceVO;
import com.bluenet.web.domain.repository.UserExperienceRepository;
import com.bluenet.web.domain.service.UserExperienceDomainService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

/**
 * 用户经历领域服务实现
 * <p>
 * 提供用户经历的领域逻辑，直接协调 Repository 与 Entity。
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserExperienceDomainServiceImpl implements UserExperienceDomainService {

    private final UserExperienceRepository userExperienceRepository;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy.MM");

    @Override
    public List<ExperienceVO> getExperiences(Long userId) {
        return userExperienceRepository.findByUserId(userId)
                .stream()
                .map(this::toVO)
                .toList();
    }

    @Override
    public List<ExperienceVO> getExperiencesByType(Long userId, ExperienceType type) {
        return userExperienceRepository.findByUserIdAndType(userId, type)
                .stream()
                .map(this::toVO)
                .toList();
    }

    @Override
    public Optional<ExperienceVO> getExperienceById(Long experienceId, Long userId) {
        return userExperienceRepository.findById(experienceId)
                .filter(e -> e.getUserId().equals(userId))
                .map(this::toVO);
    }

    @Override
    @Transactional
    public ExperienceVO createExperience(Long userId, ExperienceType type, String title,
            String startTime, String endTime, String content) {
        UserExperience experience = UserExperience.create(
                userId,
                type,
                title,
                content,
                parseDateTime(startTime),
                parseDateTime(endTime));
        userExperienceRepository.save(experience);
        return toVO(experience);
    }

    @Override
    @Transactional
    public ExperienceVO updateExperience(Long experienceId, Long userId, String title,
            String startTime, String endTime, String content) {
        UserExperience experience = userExperienceRepository.findById(experienceId)
                .orElseThrow(() -> new DataNotFound("经历不存在"));

        if (!experience.getUserId().equals(userId)) {
            throw new Forbidden("无权修改此经历");
        }

        experience.updateDetails(title, content, parseDateTime(startTime), parseDateTime(endTime));
        userExperienceRepository.update(experience);
        return toVO(experience);
    }

    @Override
    @Transactional
    public boolean deleteExperience(Long experienceId, Long userId) {
        UserExperience experience = userExperienceRepository.findById(experienceId)
                .orElseThrow(() -> new DataNotFound("经历不存在"));

        if (!experience.getUserId().equals(userId)) {
            throw new Forbidden("无权删除此经历");
        }

        userExperienceRepository.deleteById(experienceId);
        return true;
    }

    @Override
    public TabCounts getTabCounts(Long userId) {
        int projects = userExperienceRepository.countByUserIdAndType(userId, ExperienceType.PROJECT);
        int competitions = userExperienceRepository.countByUserIdAndType(userId, ExperienceType.COMPETITION);
        int internships = userExperienceRepository.countByUserIdAndType(userId, ExperienceType.INTERNSHIP);
        return new TabCounts(projects, competitions, internships);
    }

    private ExperienceVO toVO(UserExperience experience) {
        return ExperienceVO.builder()
                .id(experience.getId())
                .type(experience.getType())
                .title(experience.getTitle())
                .startTime(formatDateTime(experience.getStartTime()))
                .endTime(formatDateTime(experience.getEndTime()))
                .content(experience.getContent())
                .build();
    }

    private String formatDateTime(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.format(DATE_FORMATTER);
    }

    private LocalDateTime parseDateTime(String dateStr) {
        if (dateStr == null || dateStr.isBlank()) {
            return null;
        }
        try {
            if (dateStr.matches("\\d{4}\\.\\d{2}")) {
                return LocalDateTime.parse(
                        dateStr + ".01 00:00:00",
                        DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm:ss"));
            } else if (dateStr.matches("\\d{4}-\\d{2}")) {
                return LocalDateTime.parse(
                        dateStr + "-01 00:00:00",
                        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            } else if (dateStr.matches("\\d{4}\\.\\d{2}\\.\\d{2}")) {
                return LocalDateTime.parse(
                        dateStr + " 00:00:00",
                        DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm:ss"));
            } else if (dateStr.matches("\\d{4}-\\d{2}-\\d{2}")) {
                return LocalDateTime.parse(
                        dateStr + " 00:00:00",
                        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            }
            return LocalDateTime.parse(
                    dateStr,
                    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        } catch (Exception e) {
            log.warn("Failed to parse date: {}", dateStr, e);
            return null;
        }
    }
}
