package com.bluenet.web.domain.service.impl;

import com.bluenet.web.domain.model.entity.DirectionLearningStep;
import com.bluenet.web.domain.model.enumerate.Direction;
import com.bluenet.web.domain.model.vo.LearningStepVO;
import com.bluenet.web.domain.repository.LearningPathRepository;
import com.bluenet.web.domain.service.LearningPathDomainService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * 学习路径领域服务实现
 * <p>
 * 实现学习路径相关的业务逻辑
 * </p>
 */
@Service
@RequiredArgsConstructor
public class LearningPathDomainServiceImpl implements LearningPathDomainService {
    private final LearningPathRepository learningPathRepository;

    @Override
    public List<LearningStepVO> getLearningPath(Direction direction) {
        return learningPathRepository.findByDirection(direction);
    }

    @Override
    public Optional<LearningStepVO> getStepById(Long id) {
        return learningPathRepository.findById(id);
    }

    @Override
    public Long createStep(Direction direction, Integer stepNumber, String title, String videoUrl) {
        DirectionLearningStep step = new DirectionLearningStep();
        step.setDirection(direction);
        step.setStepNumber(stepNumber);
        step.setTitle(title);
        step.setVideoUrl(videoUrl);
        return learningPathRepository.save(step);
    }

    @Override
    public void updateStep(Long id, Integer stepNumber, String title, String videoUrl) {
        DirectionLearningStep step = new DirectionLearningStep();
        step.setId(id);
        step.setStepNumber(stepNumber);
        step.setTitle(title);
        step.setVideoUrl(videoUrl);
        learningPathRepository.update(step);
    }

    @Override
    public void deleteStep(Long id) {
        learningPathRepository.deleteById(id);
    }

    @Override
    public boolean existsById(Long id) {
        return learningPathRepository.existsById(id);
    }

    @Override
    public boolean existsByDirectionAndStepNumber(Direction direction, Integer stepNumber, Long excludeId) {
        return learningPathRepository.existsByDirectionAndStepNumber(direction, stepNumber, excludeId);
    }
}
