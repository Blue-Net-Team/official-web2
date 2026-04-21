package com.bluenet.web.infrastructure.repository.impl;

import com.bluenet.web.infrastructure.repository.dataobject.*;

import com.bluenet.web.domain.model.entity.DirectionLearningStep;
import com.bluenet.web.domain.model.enumerate.Direction;
import com.bluenet.web.domain.model.vo.LearningStepVO;
import com.bluenet.web.domain.repository.LearningPathRepository;
import com.bluenet.web.infrastructure.repository.mapper.LearningPathMapper;
import com.bluenet.web.infrastructure.repository.support.RepositoryObjectConverter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 学习路径仓库实现
 * <p>
 * 实现学习路径数据的持久化操作
 * </p>
 */
@Repository
@RequiredArgsConstructor
public class LearningPathRepositoryImpl implements LearningPathRepository {
    private final LearningPathMapper learningPathMapper;

    /**
     * 按技术方向查询学习路径 记录。
     *
     * @param direction
     *            技术方向过滤条件。
     * @return 满足条件的学习路径 结果集合。
     */
    @Override
    public List<LearningStepVO> findByDirection(Direction direction) {
        return learningPathMapper.selectByDirection(direction)
                .stream()
                .map(step -> RepositoryObjectConverter.copy(step, LearningStepVO.class))
                .toList();
    }

    /**
     * 按主键查询学习路径 记录。
     *
     * @param id
     *            业务记录主键。
     * @return 查询到的学习路径 结果；不存在时为空。
     */
    @Override
    public Optional<LearningStepVO> findById(Long id) {
        return Optional.ofNullable(learningPathMapper.selectLearningStepById(id))
                .map(step -> RepositoryObjectConverter.copy(step, LearningStepVO.class));
    }

    /**
     * 保存新的学习路径 记录。
     *
     * @param step
     *            学习路径步骤领域对象。
     * @return 新记录的主键。
     */
    @Override
    public Long save(DirectionLearningStep step) {
        // Mapper 只接收学习步骤 DO，保存后把自增 id 回填到领域对象。
        RepositoryObjectConverter.insert(learningPathMapper, step, DirectionLearningStepDO.class);
        return step.getId();
    }

    /**
     * 更新已有学习路径 记录。
     *
     * @param step
     *            学习路径步骤领域对象。
     */
    @Override
    public void update(DirectionLearningStep step) {
        RepositoryObjectConverter.updateById(learningPathMapper, step, DirectionLearningStepDO.class);
    }

    /**
     * 删除指定学习路径 记录。
     *
     * @param id
     *            业务记录主键。
     */
    @Override
    public void deleteById(Long id) {
        learningPathMapper.deleteById(id);
    }

    /**
     * 判断是否存在满足条件的学习路径 记录。
     *
     * @param id
     *            业务记录主键。
     * @return 满足条件时返回 true，否则返回 false。
     */
    @Override
    public boolean existsById(Long id) {
        return learningPathMapper.selectById(id) != null;
    }

    /**
     * 判断是否存在满足条件的学习路径 记录。
     *
     * @param direction
     *            技术方向过滤条件。
     * @param stepNumber
     *            学习路径步骤序号。
     * @param excludeId
     *            需要排除的当前记录主键。
     * @return 满足条件时返回 true，否则返回 false。
     */
    @Override
    public boolean existsByDirectionAndStepNumber(Direction direction, Integer stepNumber, Long excludeId) {
        return learningPathMapper.existsByDirectionAndStepNumber(direction, stepNumber, excludeId);
    }
}
