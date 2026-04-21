package com.bluenet.web.infrastructure.repository.impl;

import com.bluenet.web.infrastructure.repository.support.RepositoryObjectConverter;

import com.bluenet.web.infrastructure.repository.dataobject.*;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
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

    /**
     * 按主键查询考核场次 记录。
     *
     * @param id
     *            业务记录主键。
     * @return 查询到的考核场次 结果；不存在时为空。
     */
    @Override
    public Optional<AssessmentTimeVO> findById(Long id) {
        AssessmentTime entity = RepositoryObjectConverter
                .toDomain(assessmentTimeMapper.selectById(id), AssessmentTime.class);
        if (entity == null) {
            return Optional.empty();
        }
        return Optional.of(convertToVO(entity));
    }

    /**
     * 保存新的考核场次 记录。
     *
     * @param assessmentTime
     *            考核场次对象。
     * @return 新记录的主键。
     */
    @Override
    public Long save(AssessmentTimeVO assessmentTime) {
        AssessmentTime entity = convertToEntity(assessmentTime);
        RepositoryObjectConverter.insert(assessmentTimeMapper, entity, AssessmentTimeDO.class);
        return entity.getId();
    }

    /**
     * 更新已有考核场次 记录。
     *
     * @param assessmentTime
     *            考核场次对象。
     */
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
        RepositoryObjectConverter.updateById(assessmentTimeMapper, entity, AssessmentTimeDO.class);
        if (Boolean.FALSE.equals(assessmentTime.getTimeLimit())) {
            assessmentTimeMapper.clearTimeLimitMinutesById(assessmentTime.getId());
        }
    }

    /**
     * 删除指定考核场次 记录。
     *
     * @param id
     *            业务记录主键。
     */
    @Override
    public void deleteById(Long id) {
        assessmentTimeMapper.deleteById(id);
    }

    /**
     * 判断是否存在满足条件的考核场次 记录。
     *
     * @param id
     *            业务记录主键。
     * @return 满足条件时返回 true，否则返回 false。
     */
    @Override
    public boolean existsById(Long id) {
        return assessmentTimeMapper.selectById(id) != null;
    }

    /**
     * 判断是否存在满足条件的考核场次 记录。
     *
     * @param direction
     *            技术方向过滤条件。
     * @param epoch
     *            考核批次或轮次。
     * @param grade
     *            考核年级。
     * @return 满足条件时返回 true，否则返回 false。
     */
    @Override
    public boolean existsByDirectionAndEpochAndGrade(Direction direction, Integer epoch, Integer grade) {
        return assessmentTimeMapper.countByDirectionEpochGrade(direction, epoch, grade) > 0;
    }

    /**
     * 判断除当前记录外是否存在相同业务唯一键的考核场次 记录。
     *
     * @param direction
     *            技术方向过滤条件。
     * @param epoch
     *            考核批次或轮次。
     * @param grade
     *            考核年级。
     * @param excludeId
     *            需要排除的当前记录主键。
     * @return 满足条件时返回 true，否则返回 false。
     */
    @Override
    public boolean existsByDirectionAndEpochAndGradeAndIdNot(Direction direction, Integer epoch, Integer grade,
            Long excludeId) {
        return assessmentTimeMapper.countByDirectionEpochGradeAndIdNot(direction, epoch, grade, excludeId) > 0;
    }

    /**
     * 判断考核场次下是否仍有关联题目。
     *
     * @param assessmentTimeId
     *            考核场次主键。
     * @return 满足条件时返回 true，否则返回 false。
     */
    @Override
    public boolean hasAssociatedQuestions(Long assessmentTimeId) {
        return assessmentQuestionMapper.countByAssessmentTimeId(assessmentTimeId) > 0;
    }

    /**
     * 按组合筛选条件分页查询考核场次 视图。
     *
     * @param direction
     *            技术方向过滤条件。
     * @param grade
     *            考核年级。
     * @param pageable
     *            Spring 分页请求对象。
     * @return 分页后的考核场次 结果。
     */
    @Override
    public org.springframework.data.domain.Page<AssessmentTimeVO> findByFilters(
            Direction direction, Integer grade, org.springframework.data.domain.Pageable pageable) {
        Page<AssessmentTimeDO> page = new Page<>(pageable.getPageNumber() + 1, pageable.getPageSize());
        IPage<AssessmentTimeDO> result = assessmentTimeMapper.selectPageByFilters(page, direction, grade);

        List<AssessmentTimeVO> content = result.getRecords()
                .stream()
                .map(time -> convertToVO(RepositoryObjectConverter.toDomain(time, AssessmentTime.class)))
                .collect(Collectors.toList());
        return new PageImpl<>(content, pageable, result.getTotal());
    }

    /**
     * 查询用户已经参与过的考核场次。
     *
     * @param userId
     *            用户主键，用于限定用户范围。
     * @param direction
     *            技术方向过滤条件。
     * @param enrollmentYear
     *            入学年份过滤条件。
     * @param pageable
     *            Spring 分页请求对象。
     * @return 分页后的考核场次 结果。
     */
    @Override
    public org.springframework.data.domain.Page<AssessmentTimeVO> findByUserParticipation(
            Long userId, Direction direction, Integer enrollmentYear,
            org.springframework.data.domain.Pageable pageable) {
        Page<AssessmentTimeDO> page = new Page<>(pageable.getPageNumber() + 1, pageable.getPageSize());
        IPage<AssessmentTimeDO> result = assessmentTimeMapper.selectPageByUserParticipation(
                page,
                userId,
                direction,
                enrollmentYear);

        List<AssessmentTimeVO> content = result.getRecords()
                .stream()
                .map(time -> convertToVO(RepositoryObjectConverter.toDomain(time, AssessmentTime.class)))
                .collect(Collectors.toList());
        return new PageImpl<>(content, pageable, result.getTotal());
    }

    /**
     * 在考核场次 的持久层对象、领域对象和视图对象之间转换。
     *
     * @param entity
     *            领域实体。
     * @return 转换后的目标模型对象。
     */
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

    /**
     * 在考核场次 的持久层对象、领域对象和视图对象之间转换。
     *
     * @param vo
     *            领域视图对象。
     * @return 转换后的目标模型对象。
     */
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
