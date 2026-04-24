package com.bluenet.web.infrastructure.repository.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bluenet.web.domain.model.entity.AssessmentTime;
import com.bluenet.web.domain.model.enumerate.Direction;
import com.bluenet.web.domain.repository.AssessmentTimeRepository;
import com.bluenet.web.infrastructure.repository.converter.AssessmentTimeRepositoryConverter;
import com.bluenet.web.infrastructure.repository.dataobject.AssessmentTimeDO;
import com.bluenet.web.infrastructure.repository.mapper.AssessmentQuestionMapper;
import com.bluenet.web.infrastructure.repository.mapper.AssessmentTimeMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageImpl;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 考核时间仓库实现类
 * <p>
 * 实现考核时间数据的持久化操作，使用显式转换器替代 BeanUtils
 * </p>
 */
@Repository
@RequiredArgsConstructor
public class AssessmentTimeRepositoryImpl implements AssessmentTimeRepository {
    private final AssessmentTimeMapper assessmentTimeMapper;
    private final AssessmentQuestionMapper assessmentQuestionMapper;
    private final AssessmentTimeRepositoryConverter converter;

    @Override
    public Optional<AssessmentTime> findById(Long id) {
        AssessmentTimeDO dataObject = assessmentTimeMapper.selectById(id);
        return Optional.ofNullable(converter.toEntity(dataObject));
    }

    @Override
    public void save(AssessmentTime assessmentTime) {
        AssessmentTimeDO dataObject = converter.toDataObject(assessmentTime);
        assessmentTimeMapper.insert(dataObject);
        assessmentTime.setId(dataObject.getId());
    }

    @Override
    public void update(AssessmentTime assessmentTime) {
        AssessmentTimeDO dataObject = converter.toDataObject(assessmentTime);
        assessmentTimeMapper.updateById(dataObject);
        if (Boolean.FALSE.equals(assessmentTime.getTimeLimit())) {
            assessmentTimeMapper.clearTimeLimitMinutesById(assessmentTime.getId());
        }
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
        return assessmentTimeMapper.countByDirectionEpochGrade(direction, epoch, grade) > 0;
    }

    @Override
    public boolean existsByDirectionAndEpochAndGradeAndIdNot(Direction direction, Integer epoch, Integer grade,
            Long excludeId) {
        return assessmentTimeMapper.countByDirectionEpochGradeAndIdNot(direction, epoch, grade, excludeId) > 0;
    }

    @Override
    public boolean hasAssociatedQuestions(Long assessmentTimeId) {
        return assessmentQuestionMapper.countByAssessmentTimeId(assessmentTimeId) > 0;
    }

    @Override
    public org.springframework.data.domain.Page<AssessmentTime> findByFilters(
            Direction direction, Integer grade, org.springframework.data.domain.Pageable pageable) {
        Page<AssessmentTimeDO> page = new Page<>(pageable.getPageNumber() + 1, pageable.getPageSize());
        IPage<AssessmentTimeDO> result = assessmentTimeMapper.selectPageByFilters(page, direction, grade);

        List<AssessmentTime> content = converter.toEntityList(result.getRecords());
        return new PageImpl<>(content, pageable, result.getTotal());
    }

    @Override
    public org.springframework.data.domain.Page<AssessmentTime> findByUserParticipation(
            Long userId, Direction direction, Integer enrollmentYear,
            org.springframework.data.domain.Pageable pageable) {
        Page<AssessmentTimeDO> page = new Page<>(pageable.getPageNumber() + 1, pageable.getPageSize());
        IPage<AssessmentTimeDO> result = assessmentTimeMapper.selectPageByUserParticipation(
                page,
                userId,
                direction,
                enrollmentYear);

        List<AssessmentTime> content = converter.toEntityList(result.getRecords());
        return new PageImpl<>(content, pageable, result.getTotal());
    }

    @Override
    public Optional<Integer> findMaxEpochByDirectionAndGrade(Direction direction, Integer grade) {
        Integer maxEpoch = assessmentTimeMapper.selectMaxEpochByDirectionAndGrade(direction, grade);
        return Optional.ofNullable(maxEpoch);
    }
}
