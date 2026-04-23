package com.bluenet.web.application.converter;

import com.bluenet.web.api.dto.learningpath.DirectionLearningPathDTO;
import com.bluenet.web.api.dto.learningpath.LearningStepDTO;
import com.bluenet.web.application.LearningPathResult;
import com.bluenet.web.domain.model.enumerate.Direction;
import com.bluenet.web.infrastructure.util.DirectionSlugConverter;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 学习路径应用层转换器
 * <p>
 * 负责应用层 Result 与 API 层 DTO 之间的转换
 * </p>
 */
@Component
public class LearningPathAppConverter {

    /**
     * 将学习路径结果转换为 DTO
     */
    public LearningStepDTO toDTO(LearningPathResult result) {
        return LearningStepDTO.builder()
                .id(result.id())
                .stepNumber(result.stepNumber())
                .title(result.title())
                .videoLink(result.videoUrl())
                .build();
    }

    /**
     * 将学习路径结果列表转换为 DTO 列表
     */
    public List<LearningStepDTO> toDTOList(List<LearningPathResult> results) {
        return results.stream()
                .map(this::toDTO)
                .toList();
    }

    /**
     * 将方向标识和学习步骤结果列表转换为方向学习路径 DTO
     */
    public DirectionLearningPathDTO toDirectionLearningPathDTO(String slug, List<LearningPathResult> results) {
        Direction direction = DirectionSlugConverter.fromSlug(slug);
        return DirectionLearningPathDTO.builder()
                .direction(DirectionSlugConverter.toSlug(direction))
                .directionName(direction.getDescription())
                .steps(toDTOList(results))
                .build();
    }
}
