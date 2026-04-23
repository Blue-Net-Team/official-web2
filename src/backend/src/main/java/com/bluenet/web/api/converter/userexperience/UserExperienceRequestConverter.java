package com.bluenet.web.api.converter.userexperience;

import com.bluenet.web.api.dto.experience.CreateExperienceRequestDTO;
import com.bluenet.web.api.dto.experience.UpdateExperienceRequestDTO;
import com.bluenet.web.application.command.userexperience.UserExperienceCommands;
import org.springframework.stereotype.Component;

/**
 * 用户经历请求转换器
 * <p>
 * 负责将 API 层的 RequestDTO 转换为应用层的 Command
 * </p>
 */
@Component
public class UserExperienceRequestConverter {

    public UserExperienceCommands.CreateExperienceCommand toCommand(CreateExperienceRequestDTO dto) {
        return new UserExperienceCommands.CreateExperienceCommand(
                dto.getType(),
                dto.getName(),
                dto.getRole(),
                dto.getStartDate(),
                dto.getEndDate(),
                dto.getDescription(),
                dto.getTechStack(),
                dto.getDemoUrl(),
                dto.getDate(),
                dto.getLevel(),
                dto.getAward(),
                dto.getTeamSize(),
                dto.getCertificateUrl(),
                dto.getCompany(),
                dto.getPosition(),
                dto.getStatus(),
                dto.getAchievements());
    }

    public UserExperienceCommands.UpdateExperienceCommand toCommand(Long id, UpdateExperienceRequestDTO dto) {
        return new UserExperienceCommands.UpdateExperienceCommand(
                id,
                dto.getName(),
                dto.getRole(),
                dto.getStartDate(),
                dto.getEndDate(),
                dto.getDescription(),
                dto.getTechStack(),
                dto.getDemoUrl(),
                dto.getDate(),
                dto.getLevel(),
                dto.getAward(),
                dto.getTeamSize(),
                dto.getCertificateUrl(),
                dto.getPosition(),
                dto.getStatus(),
                dto.getAchievements());
    }
}
