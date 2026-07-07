package com.bluenet.web.api.converter.userexperience;

import com.bluenet.web.api.dto.experience.ExperienceDTO;
import com.bluenet.web.application.UserExperienceResult;
import com.bluenet.web.domain.model.vo.content.CompetitionContent;
import com.bluenet.web.domain.model.vo.content.InternshipContent;
import com.bluenet.web.domain.model.vo.content.ProjectContent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 用户经历响应转换器
 * <p>
 * 负责将用户经历 Result 转换为接口 DTO
 * </p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UserExperienceResponseConverter {

    private final ObjectMapper objectMapper;

    public ExperienceDTO toDTO(UserExperienceResult result) {
        ExperienceDTO dto = ExperienceDTO.builder()
                .id(String.valueOf(result.id()))
                .type(result.type().name())
                .startDate(result.startTime())
                .endDate(result.endTime())
                .build();

        dto.setNameByType(result.type().name(), result.title());

        if (result.content() != null) {
            try {
                switch (result.type()) {
                    case PROJECT -> {
                        ProjectContent content = objectMapper.readValue(result.content(), ProjectContent.class);
                        dto.setRole(content.getRole());
                        dto.setDescription(content.getDescription());
                        dto.setTechStack(content.getTechStack());
                        dto.setDemoUrl(content.getDemoUrl());
                    }
                    case COMPETITION -> {
                        CompetitionContent content = objectMapper.readValue(result.content(), CompetitionContent.class);
                        dto.setRole(content.getRole());
                        dto.setDate(content.getDate());
                        dto.setLevel(content.getLevel());
                        dto.setAward(content.getAward());
                        dto.setTeamSize(content.getTeamSize());
                        dto.setDescription(content.getDescription());
                        dto.setCertificateUrl(content.getCertificateUrl());
                    }
                    case INTERNSHIP -> {
                        InternshipContent content = objectMapper.readValue(result.content(), InternshipContent.class);
                        dto.setPosition(content.getPosition());
                        dto.setDescription(content.getDescription());
                        dto.setAchievements(content.getAchievements());
                        dto.setStatus(content.getStatus());
                    }
                }
            } catch (JsonProcessingException e) {
                log.error("解析经历内容失败: id={}", result.id(), e);
            }
        }

        return dto;
    }

    public List<ExperienceDTO> toDTOList(List<UserExperienceResult> results) {
        return results.stream()
                .map(this::toDTO)
                .toList();
    }
}
