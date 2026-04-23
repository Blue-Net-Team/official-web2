package com.bluenet.web.api.converter.achievement;

import com.bluenet.web.api.dto.achievement.CreateAchievementRequestDTO;
import com.bluenet.web.api.dto.achievement.UpdateAchievementRequestDTO;
import com.bluenet.web.application.command.achievement.AchievementCommands;
import org.springframework.stereotype.Component;

/**
 * 成就请求转换器
 * <p>
 * 负责将 API 层的 RequestDTO 转换为应用层的 Command
 * </p>
 */
@Component
public class AchievementRequestConverter {

    /**
     * 将创建请求 DTO 转换为命令
     */
    public AchievementCommands.CreateAchievementCommand toCommand(CreateAchievementRequestDTO dto) {
        return new AchievementCommands.CreateAchievementCommand(
                dto.getTitle(),
                dto.getType(),
                dto.getRelateTo(),
                dto.getAchieveAt(),
                dto.getAwardLevel(),
                dto.getAwardName(),
                dto.getFileId());
    }

    /**
     * 将更新请求 DTO 转换为命令
     */
    public AchievementCommands.UpdateAchievementCommand toCommand(Long id, UpdateAchievementRequestDTO dto) {
        return new AchievementCommands.UpdateAchievementCommand(
                id,
                dto.getTitle(),
                dto.getType(),
                dto.getRelateTo(),
                dto.getAchieveAt(),
                dto.getAwardLevel(),
                dto.getAwardName(),
                dto.getFileId());
    }
}
