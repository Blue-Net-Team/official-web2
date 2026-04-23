package com.bluenet.web.infrastructure.repository.impl;

import com.bluenet.web.infrastructure.repository.converter.AssessmentDecisionRepositoryConverter;

import com.bluenet.web.infrastructure.repository.dataobject.*;

import com.bluenet.web.domain.exception.GlobalException;
import com.bluenet.web.domain.model.entity.AssessmentDecision;
import com.bluenet.web.domain.model.vo.AssessmentDecisionVO;
import com.bluenet.web.domain.repository.AssessmentDecisionRepository;
import com.bluenet.web.infrastructure.repository.mapper.AssessmentDecisionMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@Slf4j
@RequiredArgsConstructor
public class AssessmentDecisionRepositoryImpl implements AssessmentDecisionRepository {
    private final AssessmentDecisionMapper assessmentDecisionMapper;
    private final AssessmentDecisionRepositoryConverter converter;

    /**
     * 保存新的考核最终决策 记录。
     *
     * @param decision
     *            考核最终决策对象。
     */
    @Override
    public void save(AssessmentDecision decision) {
        log.info("save assessment decision {}", decision);
        AssessmentDecisionDO dataObject = converter.toDataObject(decision);
        assessmentDecisionMapper.insert(dataObject);
        decision.setId(dataObject.getId());
    }

    /**
     * 按主键查询考核最终决策 记录。
     *
     * @param id
     *            业务记录主键。
     * @return 查询到的考核最终决策 结果；不存在时为空。
     */
    @Override
    public Optional<AssessmentDecisionVO> findById(Long id) {
        AssessmentDecision decision = converter.toEntity(assessmentDecisionMapper.selectById(id));
        if (decision == null) {
            log.warn("assessment decision not found id {}", id);
            return Optional.empty();
        }
        return Optional.of(convertToVO(decision));
    }

    /**
     * 更新已有考核最终决策 记录。
     *
     * @param decision
     *            考核最终决策对象。
     */
    @Override
    public void update(AssessmentDecisionVO decision) {
        AssessmentDecision entity = convertToEntity(decision);
        int influence = assessmentDecisionMapper.updateById(converter.toDataObject(entity));
        if (influence == 0) {
            log.warn("更新考核通过决策失败，decisionId {}", decision.getId());
            throw new GlobalException("更新考核通过决策失败");
        }
    }

    /**
     * 按用户和考核场次查询对应记录。
     *
     * @param userId
     *            用户主键，用于限定用户范围。
     * @param assessmentTimeId
     *            考核场次主键。
     * @return 查询到的考核最终决策 结果；不存在时为空。
     */
    @Override
    public Optional<AssessmentDecisionVO> findByUserIdAndAssessmentTimeId(Long userId, Long assessmentTimeId) {
        AssessmentDecision decision = converter.toEntity(
                assessmentDecisionMapper.selectByUserIdAndAssessmentTimeId(userId, assessmentTimeId));
        if (decision == null) {
            return Optional.empty();
        }
        return Optional.of(convertToVO(decision));
    }

    /**
     * 在考核最终决策 的持久层对象、领域对象和视图对象之间转换。
     *
     * @param decision
     *            考核最终决策对象。
     * @return 转换后的目标模型对象。
     */
    private AssessmentDecision convertToEntity(AssessmentDecisionVO decision) {
        return AssessmentDecision.reconstruct(
                decision.getId(),
                decision.getUserId(),
                decision.getAssessmentTimeId(),
                decision.getPassed(),
                decision.getDecidedBy(),
                decision.getDecisionComment(),
                decision.getDecidedAt(),
                decision.getUpdatedAt());
    }

    /**
     * 在考核最终决策 的持久层对象、领域对象和视图对象之间转换。
     *
     * @param decision
     *            考核最终决策对象。
     * @return 转换后的目标模型对象。
     */
    private AssessmentDecisionVO convertToVO(AssessmentDecision decision) {
        return AssessmentDecisionVO.builder()
                .id(decision.getId())
                .userId(decision.getUserId())
                .assessmentTimeId(decision.getAssessmentTimeId())
                .passed(decision.getPassed())
                .decidedBy(decision.getDecidedBy())
                .decisionComment(decision.getDecisionComment())
                .decidedAt(decision.getDecidedAt())
                .updatedAt(decision.getUpdatedAt())
                .build();
    }

    /**
     * 查询指定考核场次下的记录列表。
     *
     * @param assessmentTimeId
     *            考核场次主键。
     * @return 满足条件的考核最终决策 结果集合。
     */
    @Override
    public List<AssessmentDecisionVO> findByAssessmentTimeId(Long assessmentTimeId) {
        // Mapper 只返回决策 DO，RepositoryImpl 负责转换成领域对象后再组装 VO。
        return converter.toEntityList(
                assessmentDecisionMapper.selectByAssessmentTimeId(assessmentTimeId))
                .stream()
                .map(this::convertToVO)
                .toList();
    }
}
