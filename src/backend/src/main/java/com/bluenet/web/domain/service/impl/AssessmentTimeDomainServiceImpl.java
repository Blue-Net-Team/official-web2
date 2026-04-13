package com.bluenet.web.domain.service.impl;

import com.bluenet.web.domain.exception.DataConflict;
import com.bluenet.web.domain.model.vo.AssessmentTimeVO;
import com.bluenet.web.domain.repository.AssessmentTimeRepository;
import com.bluenet.web.domain.service.AssessmentTimeDomainService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * 考核时间领域服务实现类
 * <p>
 * 实现考核时间相关的业务逻辑操作，包括创建校验、更新校验、删除校验
 * </p>
 */
@Service
@RequiredArgsConstructor
public class AssessmentTimeDomainServiceImpl implements AssessmentTimeDomainService {
    private final AssessmentTimeRepository assessmentTimeRepository;

    @Override
    public Optional<AssessmentTimeVO> getById(Long id) {
        return assessmentTimeRepository.findById(id);
    }

    @Override
    public Long create(AssessmentTimeVO assessmentTime) {
        // 校验开始时间早于结束时间
        if (assessmentTime.getStartTime() != null && assessmentTime.getEndTime() != null
                && !assessmentTime.getStartTime().isBefore(assessmentTime.getEndTime())) {
            throw new IllegalArgumentException("开始时间必须早于结束时间");
        }

        // 校验限时考核必须设置限时分钟数
        if (Boolean.TRUE.equals(assessmentTime.getTimeLimit())
                && (assessmentTime.getTimeLimitMinutes() == null || assessmentTime.getTimeLimitMinutes() <= 0)) {
            throw new IllegalArgumentException("限时考核必须设置有效的限时分钟数");
        }

        // 校验唯一性：(direction, epoch, grade) 组合唯一，grade为入学年份
        if (assessmentTime.getDirection() != null && assessmentTime.getEpoch() != null
                && assessmentTime.getGrade() != null
                && assessmentTimeRepository.existsByDirectionAndEpochAndGrade(
                        assessmentTime.getDirection(),
                        assessmentTime.getEpoch(),
                        assessmentTime.getGrade())) {
            throw new IllegalArgumentException("该方向轮次年级的考核时间已存在");
        }

        return assessmentTimeRepository.save(assessmentTime);
    }

    @Override
    public void update(AssessmentTimeVO assessmentTime) {
        // 检查考核时间是否存在
        Optional<AssessmentTimeVO> existingOpt = assessmentTimeRepository.findById(assessmentTime.getId());
        if (existingOpt.isEmpty()) {
            throw new IllegalArgumentException("考核时间不存在");
        }

        AssessmentTimeVO existing = existingOpt.get();
        LocalDateTime now = LocalDateTime.now();

        // 校验已开始的考核不允许修改开始时间
        if (existing.getStartTime() != null && !existing.getStartTime().isAfter(now)) {
            if (assessmentTime.getStartTime() != null
                    && !assessmentTime.getStartTime().equals(existing.getStartTime())) {
                throw new IllegalArgumentException("已开始的考核不允许修改开始时间");
            }
        }

        // 校验开始时间早于结束时间
        LocalDateTime startTime = assessmentTime.getStartTime() != null
                ? assessmentTime.getStartTime()
                : existing.getStartTime();
        LocalDateTime endTime = assessmentTime.getEndTime() != null
                ? assessmentTime.getEndTime()
                : existing.getEndTime();
        if (startTime != null && endTime != null && !startTime.isBefore(endTime)) {
            throw new IllegalArgumentException("开始时间必须早于结束时间");
        }

        // 校验限时考核必须设置限时分钟数
        Boolean timeLimit = assessmentTime.getTimeLimit() != null
                ? assessmentTime.getTimeLimit()
                : existing.getTimeLimit();
        Integer timeLimitMinutes = assessmentTime.getTimeLimitMinutes() != null
                ? assessmentTime.getTimeLimitMinutes()
                : existing.getTimeLimitMinutes();
        if (Boolean.TRUE.equals(timeLimit) && (timeLimitMinutes == null || timeLimitMinutes <= 0)) {
            throw new IllegalArgumentException("限时考核必须设置有效的限时分钟数");
        }

        // 校验唯一性（排除自身）
        if (assessmentTime.getDirection() != null || assessmentTime.getEpoch() != null
                || assessmentTime.getGrade() != null) {
            com.bluenet.web.domain.model.enumerate.Direction direction = assessmentTime.getDirection() != null
                    ? assessmentTime.getDirection()
                    : existing.getDirection();
            Integer epoch = assessmentTime.getEpoch() != null ? assessmentTime.getEpoch() : existing.getEpoch();
            Integer grade = assessmentTime.getGrade() != null ? assessmentTime.getGrade() : existing.getGrade();
            if (assessmentTimeRepository
                    .existsByDirectionAndEpochAndGradeAndIdNot(direction, epoch, grade, assessmentTime.getId())) {
                throw new IllegalArgumentException("该方向轮次年级的考核时间已存在");
            }
        }

        assessmentTimeRepository.update(assessmentTime);
    }

    @Override
    public void delete(Long id) {
        // 检查考核时间是否存在
        if (!assessmentTimeRepository.existsById(id)) {
            throw new IllegalArgumentException("考核时间不存在");
        }

        // 检查是否有关联的考核题目
        if (assessmentTimeRepository.hasAssociatedQuestions(id)) {
            throw new DataConflict("存在关联的考核题目，需先删除相关题目");
        }

        assessmentTimeRepository.deleteById(id);
    }
}
