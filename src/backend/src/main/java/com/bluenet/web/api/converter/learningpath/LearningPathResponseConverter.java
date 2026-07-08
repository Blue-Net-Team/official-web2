package com.bluenet.web.api.converter.learningpath;

import com.bluenet.web.api.dto.learningpath.DirectionLearningPathDTO;
import com.bluenet.web.api.dto.learningpath.LearningStepDTO;
import com.bluenet.web.application.result.learningpath.LearningPathResult;
import com.bluenet.web.domain.model.enumerate.Direction;
import com.bluenet.web.infrastructure.util.DirectionSlugConverter;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 学习路径响应转换器
 * <p>
 * 负责将学习路径 Result 转换为接口 DTO
 * </p>
 */
@Component
public class LearningPathResponseConverter {

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
