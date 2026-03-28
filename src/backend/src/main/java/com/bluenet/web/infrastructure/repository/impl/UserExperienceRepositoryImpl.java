package com.bluenet.web.infrastructure.repository.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.bluenet.web.domain.model.entity.UserExperience;
import com.bluenet.web.domain.model.enumerate.ExperienceType;
import com.bluenet.web.domain.model.vo.ExperienceVO;
import com.bluenet.web.domain.repository.UserExperienceRepository;
import com.bluenet.web.infrastructure.repository.mapper.UserExperienceMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

/**
 * 用户经历仓储实现
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class UserExperienceRepositoryImpl implements UserExperienceRepository {
    private final UserExperienceMapper userExperienceMapper;
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public Optional<ExperienceVO> findById(Long id) {
        UserExperience experience = userExperienceMapper.selectById(id);
        if (experience == null) {
            log.warn("Experience not found: {}", id);
            return Optional.empty();
        }
        return Optional.of(convertToVO(experience));
    }

    @Override
    public List<ExperienceVO> findByUserId(Long userId) {
        List<UserExperience> experiences = userExperienceMapper.selectList(
                new LambdaQueryWrapper<UserExperience>()
                        .eq(UserExperience::getUserId, userId)
                        .orderByDesc(UserExperience::getStartTime));

        return experiences.stream()
                .map(this::convertToVO)
                .toList();
    }

    @Override
    public List<ExperienceVO> findByUserIdAndType(Long userId, ExperienceType type) {
        List<UserExperience> experiences = userExperienceMapper.selectList(
                new LambdaQueryWrapper<UserExperience>()
                        .eq(UserExperience::getUserId, userId)
                        .eq(UserExperience::getType, type)
                        .orderByDesc(UserExperience::getStartTime));

        return experiences.stream()
                .map(this::convertToVO)
                .toList();
    }

    @Override
    public ExperienceVO save(Long userId, ExperienceType type, String title,
            String startTime, String endTime, String content) {
        UserExperience experience = new UserExperience();
        experience.setUserId(userId);
        experience.setType(type);
        experience.setTitle(title);
        experience.setContent(content);
        experience.setStartTime(parseDateTime(startTime));
        experience.setEndTime(endTime != null ? parseDateTime(endTime) : null);

        userExperienceMapper.insert(experience);
        log.info("Created experience: id={}, userId={}, type={}", experience.getId(), userId, type);

        return convertToVO(experience);
    }

    @Override
    public ExperienceVO update(Long id, String title, String startTime, String endTime, String content) {
        UserExperience experience = userExperienceMapper.selectById(id);
        if (experience == null) {
            throw new IllegalArgumentException("Experience not found: " + id);
        }

        experience.setTitle(title);
        experience.setContent(content);
        experience.setStartTime(parseDateTime(startTime));
        experience.setEndTime(endTime != null ? parseDateTime(endTime) : null);

        userExperienceMapper.updateById(experience);
        log.info("Updated experience: id={}", id);

        return convertToVO(experience);
    }

    @Override
    public boolean deleteById(Long id) {
        int rows = userExperienceMapper.deleteById(id);
        if (rows > 0) {
            log.info("Deleted experience: id={}", id);
            return true;
        }
        log.warn("Failed to delete experience: id={}", id);
        return false;
    }

    @Override
    public int countByUserIdAndType(Long userId, ExperienceType type) {
        return Math.toIntExact(
                userExperienceMapper.selectCount(
                        new LambdaQueryWrapper<UserExperience>()
                                .eq(UserExperience::getUserId, userId)
                                .eq(UserExperience::getType, type)));
    }

    @Override
    public boolean checkOwner(Long experienceId, Long userId) {
        UserExperience experience = userExperienceMapper.selectById(experienceId);
        return experience != null && experience.getUserId().equals(userId);
    }

    /**
     * 转换为值对象
     */
    private ExperienceVO convertToVO(UserExperience experience) {
        return ExperienceVO.builder()
                .id(experience.getId())
                .type(experience.getType())
                .title(experience.getTitle())
                .startTime(formatDateTime(experience.getStartTime()))
                .endTime(formatDateTime(experience.getEndTime()))
                .content(experience.getContent())
                .build();
    }

    /**
     * 解析日期时间字符串 支持格式：yyyy.MM、yyyy-MM、yyyy.MM.dd、yyyy-MM-dd
     */
    private LocalDateTime parseDateTime(String dateStr) {
        if (dateStr == null || dateStr.isBlank()) {
            return null;
        }

        // 尝试不同格式
        try {
            if (dateStr.matches("\\d{4}\\.\\d{2}")) {
                // yyyy.MM 格式
                return LocalDateTime.parse(
                        dateStr + ".01 00:00:00",
                        DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm:ss"));
            } else if (dateStr.matches("\\d{4}-\\d{2}")) {
                // yyyy-MM 格式
                return LocalDateTime.parse(
                        dateStr + "-01 00:00:00",
                        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            } else if (dateStr.matches("\\d{4}\\.\\d{2}\\.\\d{2}")) {
                // yyyy.MM.dd 格式
                return LocalDateTime.parse(
                        dateStr + " 00:00:00",
                        DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm:ss"));
            } else if (dateStr.matches("\\d{4}-\\d{2}-\\d{2}")) {
                // yyyy-MM-dd 格式
                return LocalDateTime.parse(
                        dateStr + " 00:00:00",
                        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            } else if (dateStr.matches(".*\\d{4}年\\d{1,2}月.*")) {
                // 包含 "2024年8月" 格式，提取年月
                String normalized = dateStr.replaceAll("[年月]", ".");
                // 提取年月
                String[] parts = normalized.split("\\.");
                if (parts.length >= 2) {
                    String year = parts[0].replaceAll("[^0-9]", "");
                    String month = String.format("%02d", Integer.parseInt(parts[1].replaceAll("[^0-9]", "")));
                    return LocalDateTime.parse(
                            year + "." + month + ".01 00:00:00",
                            DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm:ss"));
                }
            }
            // 默认尝试完整格式
            return LocalDateTime.parse(dateStr, DATE_TIME_FORMATTER);
        } catch (Exception e) {
            log.warn("Failed to parse date: {}", dateStr, e);
            return null;
        }
    }

    /**
     * 格式化日期时间 返回 yyyy.MM 格式
     */
    private String formatDateTime(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.format(DateTimeFormatter.ofPattern("yyyy.MM"));
    }
}
