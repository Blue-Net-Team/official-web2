package com.bluenet.web.infrastructure.repository.impl;

import com.bluenet.web.infrastructure.repository.support.RepositoryObjectConverter;

import com.bluenet.web.infrastructure.repository.dataobject.*;

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

    /**
     * 按主键查询用户经历 记录。
     *
     * @param id
     *            业务记录主键。
     * @return 查询到的用户经历 结果；不存在时为空。
     */
    @Override
    public Optional<ExperienceVO> findById(Long id) {
        UserExperience experience = RepositoryObjectConverter
                .toDomain(userExperienceMapper.selectById(id), UserExperience.class);
        if (experience == null) {
            log.warn("Experience not found: {}", id);
            return Optional.empty();
        }
        return Optional.of(convertToVO(experience));
    }

    /**
     * 查询指定用户关联的用户经历 记录。
     *
     * @param userId
     *            用户主键，用于限定用户范围。
     * @return 满足条件的用户经历 结果集合。
     */
    @Override
    public List<ExperienceVO> findByUserId(Long userId) {
        List<UserExperience> experiences = RepositoryObjectConverter.toDomainList(
                userExperienceMapper.selectByUserId(userId),
                UserExperience.class);

        return experiences.stream()
                .map(this::convertToVO)
                .toList();
    }

    /**
     * 查询用户指定类型的经历列表。
     *
     * @param userId
     *            用户主键，用于限定用户范围。
     * @param type
     *            业务类型或枚举类型。
     * @return 满足条件的用户经历 结果集合。
     */
    @Override
    public List<ExperienceVO> findByUserIdAndType(Long userId, ExperienceType type) {
        List<UserExperience> experiences = RepositoryObjectConverter.toDomainList(
                userExperienceMapper.selectByUserIdAndType(userId, type),
                UserExperience.class);

        return experiences.stream()
                .map(this::convertToVO)
                .toList();
    }

    /**
     * 保存新的用户经历 记录。
     *
     * @param userId
     *            用户主键，用于限定用户范围。
     * @param type
     *            业务类型或枚举类型。
     * @param title
     *            经历或展示项标题。
     * @param startTime
     *            经历开始时间。
     * @param endTime
     *            经历结束时间。
     * @param content
     *            作答内容、经历内容或题目内容。
     * @return 查询或处理得到的用户经历 结果。
     */
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

        RepositoryObjectConverter.insert(userExperienceMapper, experience, UserExperienceDO.class);
        log.info("Created experience: id={}, userId={}, type={}", experience.getId(), userId, type);

        return convertToVO(experience);
    }

    /**
     * 更新已有用户经历 记录。
     *
     * @param id
     *            业务记录主键。
     * @param title
     *            经历或展示项标题。
     * @param startTime
     *            经历开始时间。
     * @param endTime
     *            经历结束时间。
     * @param content
     *            作答内容、经历内容或题目内容。
     * @return 数据库受影响行数。
     */
    @Override
    public ExperienceVO update(Long id, String title, String startTime, String endTime, String content) {
        UserExperience experience = RepositoryObjectConverter
                .toDomain(userExperienceMapper.selectById(id), UserExperience.class);
        if (experience == null) {
            throw new IllegalArgumentException("Experience not found: " + id);
        }

        experience.setTitle(title);
        experience.setContent(content);
        experience.setStartTime(parseDateTime(startTime));
        experience.setEndTime(endTime != null ? parseDateTime(endTime) : null);

        RepositoryObjectConverter.updateById(userExperienceMapper, experience, UserExperienceDO.class);
        log.info("Updated experience: id={}", id);

        return convertToVO(experience);
    }

    /**
     * 删除指定用户经历 记录。
     *
     * @param id
     *            业务记录主键。
     * @return 数据库受影响行数。
     */
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

    /**
     * 统计用户指定类型的经历数量。
     *
     * @param userId
     *            用户主键，用于限定用户范围。
     * @param type
     *            业务类型或枚举类型。
     * @return 满足条件的记录数量。
     */
    @Override
    public int countByUserIdAndType(Long userId, ExperienceType type) {
        return Math.toIntExact(userExperienceMapper.countByUserIdAndType(userId, type));
    }

    /**
     * 校验用户是否拥有指定经历记录。
     *
     * @param experienceId
     *            用户经历记录主键。
     * @param userId
     *            用户主键，用于限定用户范围。
     * @return 满足条件时返回 true，否则返回 false。
     */
    @Override
    public boolean checkOwner(Long experienceId, Long userId) {
        UserExperience experience = RepositoryObjectConverter
                .toDomain(userExperienceMapper.selectById(experienceId), UserExperience.class);
        return experience != null && experience.getUserId().equals(userId);
    }

    /**
     * 在用户经历 的持久层对象、领域对象和视图对象之间转换。
     *
     * @param experience
     *            用户经历领域对象。
     * @return 转换后的目标模型对象。
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
     * 处理用户经历 仓储职责中的业务数据访问逻辑。
     *
     * @param dateStr
     *            待解析的日期字符串。
     * @return 查询或处理得到的用户经历 结果。
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
     * 处理用户经历 仓储职责中的业务数据访问逻辑。
     *
     * @param dateTime
     *            待格式化的日期时间。
     * @return 查询或处理得到的用户经历 结果。
     */
    private String formatDateTime(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.format(DateTimeFormatter.ofPattern("yyyy.MM"));
    }
}
