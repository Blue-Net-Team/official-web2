package com.bluenet.web.application.service.impl;

import com.bluenet.web.application.LearningPathResult;
import com.bluenet.web.application.command.learningpath.LearningPathCommands;
import com.bluenet.web.application.service.LearningPathAppService;
import com.bluenet.web.domain.model.entity.DirectionLearningStep;
import com.bluenet.web.domain.model.enumerate.Direction;
import com.bluenet.web.domain.repository.LearningPathRepository;
import com.bluenet.web.infrastructure.util.DirectionSlugConverter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 学习路径应用服务实现。
 * <p>
 * 实现学习路径聚合在应用层的业务逻辑编排。
 * </p>
 */
@Service
@RequiredArgsConstructor
public class LearningPathAppServiceImpl implements LearningPathAppService {
    private final LearningPathRepository learningPathRepository;

    /**
     * 查询学习路径。
     *
     * @param slug
     *            方向标识
     * @return 学习路径结果列表
     */
    @Override
    public List<LearningPathResult> getLearningPath(String slug) {
        Direction direction = DirectionSlugConverter.fromSlug(slug);
        List<DirectionLearningStep> steps = learningPathRepository.findByDirection(direction);
        return steps.stream()
                .map(this::toResult)
                .toList();
    }

    /**
     * 创建学习步骤。
     *
     * @param command
     *            创建学习步骤命令
     * @return 创建后的学习路径结果
     */
    @Override
    @Transactional
    public LearningPathResult createStep(LearningPathCommands.CreateLearningStepCommand command) {
        Direction direction = DirectionSlugConverter.fromSlug(command.slug());

        if (learningPathRepository.existsByDirectionAndStepNumber(direction, command.stepNumber(), null)) {
            throw new IllegalArgumentException("该方向的步骤序号已存在");
        }

        DirectionLearningStep step = DirectionLearningStep
                .create(direction, command.stepNumber(), command.title(), command.videoUrl());
        learningPathRepository.save(step);
        return toResult(step);
    }

    /**
     * 更新学习步骤。
     *
     * @param command
     *            更新学习步骤命令
     * @return 更新后的学习路径结果
     */
    @Override
    @Transactional
    public LearningPathResult updateStep(LearningPathCommands.UpdateLearningStepCommand command) {
        DirectionLearningStep step = learningPathRepository.findById(command.id())
                .orElseThrow(() -> new IllegalArgumentException("学习步骤不存在"));

        if (learningPathRepository
                .existsByDirectionAndStepNumber(step.getDirection(), command.stepNumber(), command.id())) {
            throw new IllegalArgumentException("该方向的步骤序号已存在");
        }

        step.updateStepNumber(command.stepNumber());
        step.updateTitle(command.title());
        step.updateVideoUrl(command.videoUrl());
        learningPathRepository.save(step);
        return toResult(step);
    }

    /**
     * 删除学习步骤。
     *
     * @param id
     *            学习步骤ID
     */
    @Override
    @Transactional
    public void deleteStep(Long id) {
        if (!learningPathRepository.existsById(id)) {
            throw new IllegalArgumentException("学习步骤不存在");
        }
        learningPathRepository.deleteById(id);
    }

    private LearningPathResult toResult(DirectionLearningStep step) {
        return new LearningPathResult(
                step.getId(),
                step.getDirection(),
                step.getStepNumber(),
                step.getTitle(),
                step.getVideoUrl());
    }
}
