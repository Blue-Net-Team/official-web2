package com.bluenet.web.infrastructure.repository.impl;

import com.bluenet.web.domain.model.entity.DirectionLearningStep;
import com.bluenet.web.domain.model.enumerate.Direction;
import com.bluenet.web.domain.model.vo.LearningStepVO;
import com.bluenet.web.domain.repository.LearningPathRepository;
import com.bluenet.web.infrastructure.repository.mapper.LearningPathMapper;
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

    @Override
    public List<LearningStepVO> findByDirection(Direction direction) {
        return learningPathMapper.selectByDirection(direction);
    }

    @Override
    public Optional<LearningStepVO> findById(Long id) {
        return learningPathMapper.selectById(id);
    }

    @Override
    public Long save(DirectionLearningStep step) {
        learningPathMapper.insert(step);
        return step.getId();
    }

    @Override
    public void update(DirectionLearningStep step) {
        learningPathMapper.updateById(step);
    }

    @Override
    public void deleteById(Long id) {
        learningPathMapper.deleteById(id);
    }

    @Override
    public boolean existsById(Long id) {
        return learningPathMapper.selectById(id) != null;
    }

    @Override
    public boolean existsByDirectionAndStepNumber(Direction direction, Integer stepNumber, Long excludeId) {
        return learningPathMapper.existsByDirectionAndStepNumber(direction, stepNumber, excludeId);
    }
}
