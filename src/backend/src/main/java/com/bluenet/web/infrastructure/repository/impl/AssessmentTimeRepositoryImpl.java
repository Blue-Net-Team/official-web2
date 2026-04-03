package com.bluenet.web.infrastructure.repository.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bluenet.web.domain.model.entity.AssessmentQuestion;
import com.bluenet.web.domain.model.entity.AssessmentTime;
import com.bluenet.web.domain.model.enumerate.Direction;
import com.bluenet.web.domain.model.vo.AssessmentTimeVO;
import com.bluenet.web.domain.repository.AssessmentTimeRepository;
import com.bluenet.web.infrastructure.repository.mapper.AssessmentQuestionMapper;
import com.bluenet.web.infrastructure.repository.mapper.AssessmentTimeMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageImpl;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 考核时间仓库实现类
 * <p>
 * 实现考核时间数据的持久化操作
 * </p>
 */
@Repository
@RequiredArgsConstructor
public class AssessmentTimeRepositoryImpl implements AssessmentTimeRepository {
    private final AssessmentTimeMapper assessmentTimeMapper;
    private final AssessmentQuestionMapper assessmentQuestionMapper;

    @Override
    public Optional<AssessmentTimeVO> findById(Long id) {
        AssessmentTime entity = assessmentTimeMapper.selectById(id);
        if (entity == null) {
            return Optional.empty();
        }
        return Optional.of(convertToVO(entity));
    }

    @Override
    public Long save(AssessmentTimeVO assessmentTime) {
        AssessmentTime entity = convertToEntity(assessmentTime);
        assessmentTimeMapper.insert(entity);
        return entity.getId();
    }

    @Override
    public void update(AssessmentTimeVO assessmentTime) {
        AssessmentTime entity = new AssessmentTime();
        entity.setId(assessmentTime.getId());
        if (assessmentTime.getDirection() != null) {
            entity.setDirection(assessmentTime.getDirection());
        }
        if (assessmentTime.getEpoch() != null) {
            entity.setEpoch(assessmentTime.getEpoch());
        }
        if (assessmentTime.getGrade() != null) {
            entity.setGrade(assessmentTime.getGrade());
        }
        if (assessmentTime.getStartTime() != null) {
            entity.setStartTime(assessmentTime.getStartTime());
        }
        if (assessmentTime.getEndTime() != null) {
            entity.setEndTime(assessmentTime.getEndTime());
        }
        if (assessmentTime.getTimeLimit() != null) {
            entity.setTimeLimit(assessmentTime.getTimeLimit());
        }
        if (assessmentTime.getTimeLimitMinutes() != null) {
            entity.setTimeLimitMinutes(assessmentTime.getTimeLimitMinutes());
        }
        assessmentTimeMapper.updateById(entity);
    }

    @Override
    public void deleteById(Long id) {
        assessmentTimeMapper.deleteById(id);
    }

    @Override
    public boolean existsById(Long id) {
        return assessmentTimeMapper.selectById(id) != null;
    }

    @Override
    public boolean existsByDirectionAndEpochAndGrade(Direction direction, Integer epoch, Integer grade) {
        LambdaQueryWrapper<AssessmentTime> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AssessmentTime::getDirection, direction)
                .eq(AssessmentTime::getEpoch, epoch)
                .eq(AssessmentTime::getGrade, grade);
        return assessmentTimeMapper.selectCount(wrapper) > 0;
    }

    @Override
    public boolean existsByDirectionAndEpochAndGradeAndIdNot(Direction direction, Integer epoch, Integer grade,
            Long excludeId) {
        LambdaQueryWrapper<AssessmentTime> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AssessmentTime::getDirection, direction)
                .eq(AssessmentTime::getEpoch, epoch)
                .eq(AssessmentTime::getGrade, grade)
                .ne(AssessmentTime::getId, excludeId);
        return assessmentTimeMapper.selectCount(wrapper) > 0;
    }

    @Override
    public boolean hasAssociatedQuestions(Long assessmentTimeId) {
        LambdaQueryWrapper<AssessmentQuestion> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AssessmentQuestion::getAssessmentTimeId, assessmentTimeId);
        return assessmentQuestionMapper.selectCount(wrapper) > 0;
    }

    @Override
    public org.springframework.data.domain.Page<AssessmentTimeVO> findByFilters(
            Direction direction, Integer grade, org.springframework.data.domain.Pageable pageable) {
        Page<AssessmentTime> page = new Page<>(pageable.getPageNumber() + 1, pageable.getPageSize());
        LambdaQueryWrapper<AssessmentTime> wrapper = new LambdaQueryWrapper<>();
        if (direction != null) {
            wrapper.eq(AssessmentTime::getDirection, direction);
        }
        if (grade != null) {
            wrapper.eq(AssessmentTime::getGrade, grade);
        }
        wrapper.orderByDesc(AssessmentTime::getId);
        IPage<AssessmentTime> result = assessmentTimeMapper.selectPage(page, wrapper);

        List<AssessmentTimeVO> content = result.getRecords()
                .stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
        return new PageImpl<>(content, pageable, result.getTotal());
    }

    private AssessmentTimeVO convertToVO(AssessmentTime entity) {
        return AssessmentTimeVO.builder()
                .id(entity.getId())
                .direction(entity.getDirection())
                .epoch(entity.getEpoch())
                .grade(entity.getGrade())
                .startTime(entity.getStartTime())
                .endTime(entity.getEndTime())
                .timeLimit(entity.getTimeLimit())
                .timeLimitMinutes(entity.getTimeLimitMinutes())
                .build();
    }

    private AssessmentTime convertToEntity(AssessmentTimeVO vo) {
        AssessmentTime entity = new AssessmentTime();
        entity.setDirection(vo.getDirection());
        entity.setEpoch(vo.getEpoch());
        entity.setGrade(vo.getGrade());
        entity.setStartTime(vo.getStartTime());
        entity.setEndTime(vo.getEndTime());
        entity.setTimeLimit(vo.getTimeLimit());
        entity.setTimeLimitMinutes(vo.getTimeLimitMinutes());
        return entity;
    }
}
