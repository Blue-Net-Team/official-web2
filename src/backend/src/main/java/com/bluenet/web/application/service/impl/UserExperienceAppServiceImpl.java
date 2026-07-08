package com.bluenet.web.application.service.impl;

import com.bluenet.web.application.UserExperienceResult;
import com.bluenet.web.application.command.userexperience.UserExperienceCommands;
import com.bluenet.web.application.service.UserExperienceAppService;
import com.bluenet.web.domain.exception.DataNotFound;
import com.bluenet.web.domain.exception.Forbidden;
import com.bluenet.web.domain.exception.Unauthorized;
import com.bluenet.web.domain.model.entity.UserExperience;
import com.bluenet.web.domain.model.enumerate.ExperienceType;
import com.bluenet.web.domain.model.entity.User;
import com.bluenet.web.domain.model.vo.content.CompetitionContent;
import com.bluenet.web.domain.model.vo.content.InternshipContent;
import com.bluenet.web.domain.model.vo.content.ProjectContent;
import com.bluenet.web.domain.repository.UserExperienceRepository;
import com.bluenet.web.infrastructure.security.util.UserCTX;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 用户经历应用服务实现。
 * <p>
 * 实现用户经历聚合在应用层的业务逻辑编排。
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserExperienceAppServiceImpl implements UserExperienceAppService {

    private final UserExperienceRepository userExperienceRepository;
    private final ObjectMapper objectMapper;
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * 查询经历列表。
     *
     * @param type
     *            经历类型
     * @return 用户经历结果列表
     */
    @Override
    public List<UserExperienceResult> getExperiences(String type) {
        Long userId = getCurrentUserId();
        List<UserExperience> experiences;
        if (type != null && !type.isBlank()) {
            ExperienceType experienceType = parseExperienceType(type);
            experiences = userExperienceRepository.findByUserIdAndType(userId, experienceType);
        } else {
            experiences = userExperienceRepository.findByUserId(userId);
        }
        return experiences.stream()
                .map(this::toResult)
                .toList();
    }

    /**
     * 创建经历。
     *
     * @param command
     *            创建经历命令
     * @return 创建后的用户经历结果
     */
    @Override
    @Transactional
    public UserExperienceResult createExperience(UserExperienceCommands.CreateExperienceCommand command) {
        Long userId = getCurrentUserId();
        ExperienceType type = parseExperienceType(command.type());
        String title = extractTitle(command, type);
        String content = buildContent(command, type);
        LocalDateTime startTime = parseDateTime(command.startDate());
        LocalDateTime endTime = parseDateTime(command.endDate());

        UserExperience experience = UserExperience.create(userId, type, title, content, startTime, endTime);
        userExperienceRepository.save(experience);
        return toResult(experience);
    }

    /**
     * 更新经历。
     *
     * @param command
     *            更新经历命令
     * @return 更新后的用户经历结果
     */
    @Override
    @Transactional
    public UserExperienceResult updateExperience(UserExperienceCommands.UpdateExperienceCommand command) {
        Long userId = getCurrentUserId();
        Long experienceId = command.id();

        UserExperience experience = userExperienceRepository.findById(experienceId)
                .orElseThrow(() -> new DataNotFound("经历不存在"));

        if (!userExperienceRepository.checkOwner(experienceId, userId)) {
            log.warn("用户 {} 尝试更新不属于自己的经历 {}", userId, experienceId);
            throw new Forbidden("无权修改此经历");
        }

        String title = command.name();
        String content = buildUpdateContent(command, experience.getType());
        LocalDateTime startTime = parseDateTime(command.startDate());
        LocalDateTime endTime = parseDateTime(command.endDate());

        experience.updateDetails(title, content, startTime, endTime);
        userExperienceRepository.save(experience);
        return toResult(experience);
    }

    /**
     * 删除经历。
     *
     * @param experienceId
     *            经历ID
     */
    @Override
    @Transactional
    public void deleteExperience(Long experienceId) {
        Long userId = getCurrentUserId();

        UserExperience experience = userExperienceRepository.findById(experienceId)
                .orElseThrow(() -> new DataNotFound("经历不存在"));

        if (!userExperienceRepository.checkOwner(experienceId, userId)) {
            log.warn("用户 {} 尝试删除不属于自己的经历 {}", userId, experienceId);
            throw new Forbidden("无权删除此经历");
        }

        userExperienceRepository.deleteById(experienceId);
        log.info("用户 {} 删除经历 {}", userId, experienceId);
    }

    private Long getCurrentUserId() {
        User user = UserCTX.getCurrentUser();
        if (user == null) {
            throw new Unauthorized("未认证");
        }
        return user.getId();
    }

    private ExperienceType parseExperienceType(String type) {
        try {
            return ExperienceType.valueOf(type);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("无效的经历类型: " + type);
        }
    }

    private String extractTitle(UserExperienceCommands.CreateExperienceCommand command, ExperienceType type) {
        return switch (type) {
            case PROJECT -> command.name();
            case COMPETITION -> command.name();
            case INTERNSHIP -> command.company();
        };
    }

    private String buildContent(UserExperienceCommands.CreateExperienceCommand command, ExperienceType type) {
        try {
            return switch (type) {
                case PROJECT -> objectMapper.writeValueAsString(
                        ProjectContent.builder()
                                .role(command.role())
                                .description(command.description())
                                .techStack(command.techStack())
                                .demoUrl(command.demoUrl())
                                .build());
                case COMPETITION -> objectMapper.writeValueAsString(
                        CompetitionContent.builder()
                                .role(command.role())
                                .date(command.date())
                                .level(command.level())
                                .award(command.award())
                                .teamSize(command.teamSize())
                                .description(command.description())
                                .certificateUrl(command.certificateUrl())
                                .build());
                case INTERNSHIP -> objectMapper.writeValueAsString(
                        InternshipContent.builder()
                                .position(command.position())
                                .description(command.description())
                                .achievements(command.achievements())
                                .status(command.status())
                                .build());
            };
        } catch (JsonProcessingException e) {
            log.error("序列化经历内容失败", e);
            throw new RuntimeException("序列化经历内容失败", e);
        }
    }

    private String buildUpdateContent(UserExperienceCommands.UpdateExperienceCommand command, ExperienceType type) {
        try {
            return switch (type) {
                case PROJECT -> objectMapper.writeValueAsString(
                        ProjectContent.builder()
                                .role(command.role())
                                .description(command.description())
                                .techStack(command.techStack())
                                .demoUrl(command.demoUrl())
                                .build());
                case COMPETITION -> objectMapper.writeValueAsString(
                        CompetitionContent.builder()
                                .role(command.role())
                                .date(command.date())
                                .level(command.level())
                                .award(command.award())
                                .teamSize(command.teamSize())
                                .description(command.description())
                                .certificateUrl(command.certificateUrl())
                                .build());
                case INTERNSHIP -> objectMapper.writeValueAsString(
                        InternshipContent.builder()
                                .position(command.position())
                                .description(command.description())
                                .achievements(command.achievements())
                                .status(command.status())
                                .build());
            };
        } catch (JsonProcessingException e) {
            log.error("序列化经历内容失败", e);
            throw new RuntimeException("序列化经历内容失败", e);
        }
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
            } else if (dateStr.matches(".*\\d{4}年\\d{1,2}月.*")) {
                String normalized = dateStr.replaceAll("[年月]", ".");
                String[] parts = normalized.split("\\.");
                if (parts.length >= 2) {
                    String year = parts[0].replaceAll("[^0-9]", "");
                    String month = String.format("%02d", Integer.parseInt(parts[1].replaceAll("[^0-9]", "")));
                    return LocalDateTime.parse(
                            year + "." + month + ".01 00:00:00",
                            DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm:ss"));
                }
            }
            return LocalDateTime.parse(dateStr, DATE_TIME_FORMATTER);
        } catch (Exception e) {
            log.warn("Failed to parse date: {}", dateStr, e);
            return null;
        }
    }

    private String formatDateTime(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.format(DateTimeFormatter.ofPattern("yyyy.MM"));
    }

    private UserExperienceResult toResult(UserExperience experience) {
        return new UserExperienceResult(
                experience.getId(),
                experience.getType(),
                experience.getTitle(),
                formatDateTime(experience.getStartTime()),
                formatDateTime(experience.getEndTime()),
                experience.getContent());
    }
}
