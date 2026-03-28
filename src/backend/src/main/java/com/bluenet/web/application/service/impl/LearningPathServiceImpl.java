package com.bluenet.web.application.service.impl;

import com.bluenet.web.api.dto.learningpath.CreateLearningStepRequestDTO;
import com.bluenet.web.api.dto.learningpath.DirectionLearningPathDTO;
import com.bluenet.web.api.dto.learningpath.LearningStepDTO;
import com.bluenet.web.api.dto.learningpath.UpdateLearningStepRequestDTO;
import com.bluenet.web.application.converter.LearningPathConverter;
import com.bluenet.web.application.service.LearningPathService;
import com.bluenet.web.domain.model.enumerate.Direction;
import com.bluenet.web.domain.model.vo.LearningStepVO;
import com.bluenet.web.domain.service.LearningPathDomainService;
import com.bluenet.web.infrastructure.util.DirectionSlugConverter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * 学习路径应用服务实现
 * <p>
 * 实现学习路径相关的应用层逻辑
 * </p>
 */
@Service
@RequiredArgsConstructor
public class LearningPathServiceImpl implements LearningPathService {
    private final LearningPathDomainService learningPathDomainService;
    private final LearningPathConverter learningPathConverter;

    @Override
    public DirectionLearningPathDTO getLearningPath(String slug) {
        Direction direction = DirectionSlugConverter.fromSlug(slug);
        List<LearningStepVO> steps = learningPathDomainService.getLearningPath(direction);
        return learningPathConverter.convertToDirectionLearningPathDTO(direction, steps);
    }

    @Override
    @Transactional
    public LearningStepDTO createStep(String slug, CreateLearningStepRequestDTO request) {
        Direction direction = DirectionSlugConverter.fromSlug(slug);

        // 检查步骤序号是否已存在
        if (learningPathDomainService.existsByDirectionAndStepNumber(direction, request.getStepNumber(), null)) {
            throw new IllegalArgumentException("该方向的步骤序号已存在");
        }

        Long id = learningPathDomainService.createStep(
                direction,
                request.getStepNumber(),
                request.getTitle(),
                request.getVideoUrl());

        Optional<LearningStepVO> created = learningPathDomainService.getStepById(id);
        if (created.isEmpty()) {
            throw new IllegalStateException("创建学习步骤失败");
        }

        return learningPathConverter.convertToDTO(created.get());
    }

    @Override
    @Transactional
    public LearningStepDTO updateStep(Long id, UpdateLearningStepRequestDTO request) {
        if (!learningPathDomainService.existsById(id)) {
            throw new IllegalArgumentException("学习步骤不存在");
        }

        Optional<LearningStepVO> existingStep = learningPathDomainService.getStepById(id);
        if (existingStep.isEmpty()) {
            throw new IllegalStateException("获取学习步骤失败");
        }

        // 检查步骤序号是否与其他步骤冲突
        if (learningPathDomainService.existsByDirectionAndStepNumber(
                existingStep.get().getDirection(),
                request.getStepNumber(),
                id)) {
            throw new IllegalArgumentException("该方向的步骤序号已存在");
        }

        learningPathDomainService.updateStep(
                id,
                request.getStepNumber(),
                request.getTitle(),
                request.getVideoUrl());

        Optional<LearningStepVO> updated = learningPathDomainService.getStepById(id);
        if (updated.isEmpty()) {
            throw new IllegalStateException("更新学习步骤失败");
        }

        return learningPathConverter.convertToDTO(updated.get());
    }

    @Override
    @Transactional
    public void deleteStep(Long id) {
        if (!learningPathDomainService.existsById(id)) {
            throw new IllegalArgumentException("学习步骤不存在");
        }

        learningPathDomainService.deleteStep(id);
    }
}
