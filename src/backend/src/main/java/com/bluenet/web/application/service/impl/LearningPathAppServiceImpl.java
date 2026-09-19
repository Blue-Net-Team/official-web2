package com.bluenet.web.application.service.impl;

import com.bluenet.web.application.result.learningpath.LearningPathResult;
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
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 学习路径应用服务实现。
 * <p>
 * 实现学习路径聚合在应用层的业务逻辑编排。顺序值由系统负责：创建时追加到方向末尾， 拖拽排序时整体覆盖。
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
     * <p>
     * 顺序值由系统分配为当前方向排序值上界 + 1，即追加到末尾。
     * </p>
     *
     * @param command
     *            创建学习步骤命令
     * @return 创建后的学习路径结果
     */
    @Override
    @Transactional
    public LearningPathResult createStep(LearningPathCommands.CreateLearningStepCommand command) {
        Direction direction = DirectionSlugConverter.fromSlug(command.slug());

        Integer maxSortOrder = learningPathRepository.findMaxSortOrder(direction);
        int nextSortOrder = maxSortOrder == null ? 1 : maxSortOrder + 1;

        DirectionLearningStep step = DirectionLearningStep
                .create(direction, nextSortOrder, command.title(), command.relatedUrl());
        learningPathRepository.save(step);
        return toResult(step);
    }

    /**
     * 更新学习步骤。
     * <p>
     * 只更新内容，不改变展示顺序。
     * </p>
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

        step.updateTitle(command.title());
        step.updateRelatedUrl(command.relatedUrl());
        learningPathRepository.save(step);
        return toResult(step);
    }

    /**
     * 删除学习步骤。
     * <p>
     * 不重排剩余步骤的顺序值——展示编号由前端按位置派生，删除后天然连续。
     * </p>
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

    /**
     * 批量更新学习步骤顺序。
     * <p>
     * 全量覆盖该方向的排序值，并校验所有步骤均属于该方向；任一不合法则整体回滚。
     * </p>
     *
     * @param command
     *            批量排序命令
     */
    @Override
    @Transactional
    public void batchUpdateSortOrder(LearningPathCommands.BatchUpdateSortOrderCommand command) {
        Direction direction = DirectionSlugConverter.fromSlug(command.slug());

        Set<Long> ownedStepIds = learningPathRepository.findByDirection(direction)
                .stream()
                .map(DirectionLearningStep::getId)
                .collect(Collectors.toSet());

        List<LearningPathRepository.SortItem> sortItems = command.items()
                .stream()
                .map(item -> {
                    if (item.id() == null || !ownedStepIds.contains(item.id())) {
                        throw new IllegalArgumentException("学习步骤不存在或不属于该方向: " + item.id());
                    }
                    if (item.sortOrder() == null) {
                        throw new IllegalArgumentException("排序值不能为空");
                    }
                    return new LearningPathRepository.SortItem(item.id(), item.sortOrder());
                })
                .toList();

        learningPathRepository.batchUpdateSortOrder(direction, sortItems);
    }

    private LearningPathResult toResult(DirectionLearningStep step) {
        return new LearningPathResult(
                step.getId(),
                step.getDirection(),
                step.getSortOrder(),
                step.getTitle(),
                step.getRelatedUrl());
    }
}
