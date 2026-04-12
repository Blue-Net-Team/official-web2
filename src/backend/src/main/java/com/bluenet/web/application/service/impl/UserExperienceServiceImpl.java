package com.bluenet.web.application.service.impl;

import com.bluenet.web.api.dto.experience.*;
import com.bluenet.web.application.service.UserExperienceService;
import com.bluenet.web.domain.exception.Unauthorized;
import com.bluenet.web.domain.model.enumerate.ExperienceType;
import com.bluenet.web.domain.model.vo.ExperienceVO;
import com.bluenet.web.domain.model.vo.UserVO;
import com.bluenet.web.domain.model.vo.content.CompetitionContent;
import com.bluenet.web.domain.model.vo.content.InternshipContent;
import com.bluenet.web.domain.model.vo.content.ProjectContent;
import com.bluenet.web.domain.service.UserExperienceDomainService;
import com.bluenet.web.infrastructure.security.util.UserCTX;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 用户经历应用服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserExperienceServiceImpl implements UserExperienceService {
    private final UserExperienceDomainService userExperienceDomainService;
    private final ObjectMapper objectMapper;

    @Override
    public List<ExperienceDTO> getExperiences(String type) {
        Long userId = getCurrentUserId();

        List<ExperienceVO> experiences;
        if (type != null && !type.isBlank()) {
            ExperienceType experienceType = parseExperienceType(type);
            experiences = userExperienceDomainService.getExperiencesByType(userId, experienceType);
        } else {
            experiences = userExperienceDomainService.getExperiences(userId);
        }

        return experiences.stream()
                .map(this::convertToDTO)
                .toList();
    }

    @Override
    public ExperienceDTO createExperience(CreateExperienceRequestDTO request) {
        Long userId = getCurrentUserId();
        ExperienceType type = parseExperienceType(request.getType());

        String title = extractTitle(request, type);
        String content = buildContent(request, type);
        String startTime = extractStartTime(request, type);
        String endTime = request.getEndDate();

        ExperienceVO experience = userExperienceDomainService.createExperience(
                userId,
                type,
                title,
                startTime,
                endTime,
                content);

        return convertToDTO(experience);
    }

    @Override
    public ExperienceDTO updateExperience(Long experienceId, UpdateExperienceRequestDTO request) {
        Long userId = getCurrentUserId();

        // 获取现有经历以确定类型
        ExperienceVO existing = userExperienceDomainService.getExperienceById(experienceId, userId)
                .orElseThrow(() -> new IllegalArgumentException("经历不存在"));

        String title = request.getName();
        String content = buildUpdateContent(request, existing.getType());
        String startTime = request.getStartDate();
        String endTime = request.getEndDate();

        ExperienceVO experience = userExperienceDomainService.updateExperience(
                experienceId,
                userId,
                title,
                startTime,
                endTime,
                content);

        return convertToDTO(experience);
    }

    @Override
    public void deleteExperience(Long experienceId) {
        Long userId = getCurrentUserId();
        userExperienceDomainService.deleteExperience(experienceId, userId);
    }

    private Long getCurrentUserId() {
        UserVO userVO = UserCTX.getCurrentUser();
        if (userVO == null) {
            throw new Unauthorized("未认证");
        }
        return userVO.getId();
    }

    private ExperienceType parseExperienceType(String type) {
        try {
            return ExperienceType.valueOf(type);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("无效的经历类型: " + type);
        }
    }

    private String extractTitle(CreateExperienceRequestDTO request, ExperienceType type) {
        return switch (type) {
            case PROJECT -> request.getName();
            case COMPETITION -> request.getName();
            case INTERNSHIP -> request.getCompany();
        };
    }

    private String extractStartTime(CreateExperienceRequestDTO request, ExperienceType type) {
        if (type == ExperienceType.COMPETITION) {
            return request.getDate();
        }
        return request.getStartDate();
    }

    private String buildContent(CreateExperienceRequestDTO request, ExperienceType type) {
        try {
            return switch (type) {
                case PROJECT -> {
                    ProjectContent content = ProjectContent.builder()
                            .role(request.getRole())
                            .description(request.getDescription())
                            .techStack(request.getTechStack())
                            .demoUrl(request.getDemoUrl())
                            .build();
                    yield objectMapper.writeValueAsString(content);
                }
                case COMPETITION -> {
                    CompetitionContent content = CompetitionContent.builder()
                            .role(request.getRole())
                            .date(request.getDate())
                            .level(request.getLevel())
                            .award(request.getAward())
                            .teamSize(request.getTeamSize())
                            .description(request.getDescription())
                            .certificateUrl(request.getCertificateUrl())
                            .build();
                    yield objectMapper.writeValueAsString(content);
                }
                case INTERNSHIP -> {
                    InternshipContent content = InternshipContent.builder()
                            .position(request.getPosition())
                            .description(request.getDescription())
                            .achievements(request.getAchievements())
                            .status(request.getStatus())
                            .build();
                    yield objectMapper.writeValueAsString(content);
                }
            };
        } catch (JsonProcessingException e) {
            log.error("序列化经历内容失败", e);
            throw new RuntimeException("序列化经历内容失败", e);
        }
    }

    private String buildUpdateContent(UpdateExperienceRequestDTO request, ExperienceType type) {
        try {
            return switch (type) {
                case PROJECT -> {
                    ProjectContent content = ProjectContent.builder()
                            .role(request.getRole())
                            .description(request.getDescription())
                            .techStack(request.getTechStack())
                            .demoUrl(request.getDemoUrl())
                            .build();
                    yield objectMapper.writeValueAsString(content);
                }
                case COMPETITION -> {
                    CompetitionContent content = CompetitionContent.builder()
                            .role(request.getRole())
                            .date(request.getDate())
                            .level(request.getLevel())
                            .award(request.getAward())
                            .teamSize(request.getTeamSize())
                            .description(request.getDescription())
                            .certificateUrl(request.getCertificateUrl())
                            .build();
                    yield objectMapper.writeValueAsString(content);
                }
                case INTERNSHIP -> {
                    InternshipContent content = InternshipContent.builder()
                            .position(request.getPosition())
                            .description(request.getDescription())
                            .achievements(request.getAchievements())
                            .status(request.getStatus())
                            .build();
                    yield objectMapper.writeValueAsString(content);
                }
            };
        } catch (JsonProcessingException e) {
            log.error("序列化经历内容失败", e);
            throw new RuntimeException("序列化经历内容失败", e);
        }
    }

    private ExperienceDTO convertToDTO(ExperienceVO vo) {
        ExperienceDTO dto = ExperienceDTO.builder()
                .id(String.valueOf(vo.getId()))
                .type(vo.getType().name())
                .startDate(vo.getStartTime())
                .endDate(vo.getEndTime())
                .build();

        // 设置名称
        dto.setNameByType(vo.getType().name(), vo.getTitle());

        // 解析内容
        try {
            switch (vo.getType()) {
                case PROJECT -> {
                    ProjectContent content = objectMapper.readValue(vo.getContent(), ProjectContent.class);
                    dto.setRole(content.getRole());
                    dto.setDescription(content.getDescription());
                    dto.setTechStack(content.getTechStack());
                    dto.setDemoUrl(content.getDemoUrl());
                }
                case COMPETITION -> {
                    CompetitionContent content = objectMapper.readValue(vo.getContent(), CompetitionContent.class);
                    dto.setRole(content.getRole());
                    dto.setDate(content.getDate());
                    dto.setLevel(content.getLevel());
                    dto.setAward(content.getAward());
                    dto.setTeamSize(content.getTeamSize());
                    dto.setDescription(content.getDescription());
                    dto.setCertificateUrl(content.getCertificateUrl());
                }
                case INTERNSHIP -> {
                    InternshipContent content = objectMapper.readValue(vo.getContent(), InternshipContent.class);
                    dto.setPosition(content.getPosition());
                    dto.setDescription(content.getDescription());
                    dto.setAchievements(content.getAchievements());
                    dto.setStatus(content.getStatus());
                }
            }
        } catch (JsonProcessingException e) {
            log.error("解析经历内容失败: id={}", vo.getId(), e);
        }

        return dto;
    }
}
