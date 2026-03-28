package com.bluenet.web.application.converter;

import com.bluenet.web.api.dto.learningpath.DirectionLearningPathDTO;
import com.bluenet.web.api.dto.learningpath.LearningStepDTO;
import com.bluenet.web.domain.model.enumerate.Direction;
import com.bluenet.web.domain.model.vo.LearningStepVO;
import com.bluenet.web.infrastructure.util.DirectionSlugConverter;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 学习路径转换器
 * <p>
 * 负责学习路径相关的VO与DTO之间的转换
 * </p>
 */
@Component
public class LearningPathConverter {
    /**
     * 将学习步骤VO转换为DTO
     *
     * @param vo
     *            学习步骤VO
     * @return 学习步骤DTO
     */
    public LearningStepDTO convertToDTO(LearningStepVO vo) {
        return LearningStepDTO.builder()
                .id(vo.getId())
                .stepNumber(vo.getStepNumber())
                .title(vo.getTitle())
                .videoLink(vo.getVideoUrl())
                .build();
    }

    /**
     * 将学习步骤VO列表转换为DTO列表
     *
     * @param voList
     *            学习步骤VO列表
     * @return 学习步骤DTO列表
     */
    public List<LearningStepDTO> convertToDTOList(List<LearningStepVO> voList) {
        return voList.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * 将方向和学习步骤列表转换为方向学习路径DTO
     *
     * @param direction
     *            方向
     * @param steps
     *            学习步骤列表
     * @return 方向学习路径DTO
     */
    public DirectionLearningPathDTO convertToDirectionLearningPathDTO(Direction direction, List<LearningStepVO> steps) {
        return DirectionLearningPathDTO.builder()
                .direction(DirectionSlugConverter.toSlug(direction))
                .directionName(direction.getDescription())
                .steps(convertToDTOList(steps))
                .build();
    }
}
