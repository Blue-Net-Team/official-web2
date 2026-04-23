package com.bluenet.web.api.converter.learningpath;

import com.bluenet.web.api.dto.learningpath.CreateLearningStepRequestDTO;
import com.bluenet.web.api.dto.learningpath.UpdateLearningStepRequestDTO;
import com.bluenet.web.application.command.learningpath.LearningPathCommands;
import org.springframework.stereotype.Component;

/**
 * 学习路径请求转换器
 * <p>
 * 负责将 API 层的 RequestDTO 转换为应用层的 Command
 * </p>
 */
@Component
public class LearningPathRequestConverter {

    /**
     * 将创建请求 DTO 转换为命令
     */
    public LearningPathCommands.CreateLearningStepCommand toCommand(String slug, CreateLearningStepRequestDTO dto) {
        return new LearningPathCommands.CreateLearningStepCommand(slug, dto.getStepNumber(), dto.getTitle(),
                dto.getVideoUrl());
    }

    /**
     * 将更新请求 DTO 转换为命令
     */
    public LearningPathCommands.UpdateLearningStepCommand toCommand(Long id, UpdateLearningStepRequestDTO dto) {
        return new LearningPathCommands.UpdateLearningStepCommand(id, dto.getStepNumber(), dto.getTitle(),
                dto.getVideoUrl());
    }
}
