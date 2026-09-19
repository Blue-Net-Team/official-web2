package com.bluenet.web.api.converter.learningpath;

import com.bluenet.web.api.dto.learningpath.BatchSortRequestDTO;
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
        return new LearningPathCommands.CreateLearningStepCommand(slug, dto.getTitle(), dto.getRelatedLink());
    }

    /**
     * 将更新请求 DTO 转换为命令
     */
    public LearningPathCommands.UpdateLearningStepCommand toCommand(Long id, UpdateLearningStepRequestDTO dto) {
        return new LearningPathCommands.UpdateLearningStepCommand(id, dto.getTitle(), dto.getRelatedLink());
    }

    /**
     * 将批量排序请求 DTO 转换为命令
     */
    public LearningPathCommands.BatchUpdateSortOrderCommand toCommand(String slug, BatchSortRequestDTO dto) {
        return new LearningPathCommands.BatchUpdateSortOrderCommand(
                slug,
                dto.getItems()
                        .stream()
                        .map(item -> new LearningPathCommands.SortItemCommand(item.getId(), item.getSortOrder()))
                        .toList());
    }
}
