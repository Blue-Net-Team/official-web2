package com.bluenet.web.infrastructure.repository.impl;

import com.bluenet.web.domain.model.entity.DirectionLearningStep;
import com.bluenet.web.domain.model.enumerate.Direction;
import com.bluenet.web.domain.repository.LearningPathRepository;
import com.bluenet.web.infrastructure.repository.converter.LearningPathRepositoryConverter;
import com.bluenet.web.infrastructure.repository.dataobject.DirectionLearningStepDO;
import com.bluenet.web.infrastructure.repository.mapper.LearningPathMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 学习路径仓库实现
 * <p>
 * 实现学习路径数据的持久化操作，使用显式转换器替代 BeanUtils
 * </p>
 */
@Repository
@RequiredArgsConstructor
public class LearningPathRepositoryImpl implements LearningPathRepository {
    private final LearningPathMapper learningPathMapper;
    private final LearningPathRepositoryConverter converter;

    @Override
    public List<DirectionLearningStep> findByDirection(Direction direction) {
        List<DirectionLearningStepDO> dataObjects = learningPathMapper.selectByDirection(direction);
        return converter.toEntityList(dataObjects);
    }

    @Override
    public Optional<DirectionLearningStep> findById(Long id) {
        DirectionLearningStepDO dataObject = learningPathMapper.selectLearningStepById(id);
        return Optional.ofNullable(converter.toEntity(dataObject));
    }

    @Override
    public void save(DirectionLearningStep step) {
        DirectionLearningStepDO dataObject = converter.toDataObject(step);
        learningPathMapper.insert(dataObject);
        step.setId(dataObject.getId());
    }

    @Override
    public void update(DirectionLearningStep step) {
        DirectionLearningStepDO dataObject = converter.toDataObject(step);
        learningPathMapper.updateById(dataObject);
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
