package com.bluenet.web.infrastructure.repository.impl;

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

    @Override
    public void save(AssessmentDecision decision) {
        log.info("save assessment decision {}", decision);
        assessmentDecisionMapper.insert(decision);
    }

    @Override
    public Optional<AssessmentDecisionVO> findById(Long id) {
        AssessmentDecision decision = assessmentDecisionMapper.selectById(id);
        if (decision == null) {
            log.warn("assessment decision not found id {}", id);
            return Optional.empty();
        }
        return Optional.of(convertToVO(decision));
    }

    @Override
    public void update(AssessmentDecisionVO decision) {
        AssessmentDecision entity = convertToEntity(decision);
        int influence = assessmentDecisionMapper.updateById(entity);
        if (influence == 0) {
            log.warn("更新考核通过决策失败，decisionId {}", decision.getId());
            throw new GlobalException("更新考核通过决策失败");
        }
    }

    @Override
    public Optional<AssessmentDecisionVO> findByUserIdAndAssessmentTimeId(Long userId, Long assessmentTimeId) {
        AssessmentDecision decision = assessmentDecisionMapper.selectByUserIdAndAssessmentTimeId(
                userId,
                assessmentTimeId);
        if (decision == null) {
            return Optional.empty();
        }
        return Optional.of(convertToVO(decision));
    }

    private AssessmentDecision convertToEntity(AssessmentDecisionVO decision) {
        AssessmentDecision entity = new AssessmentDecision();
        entity.setId(decision.getId());
        entity.setUserId(decision.getUserId());
        entity.setAssessmentTimeId(decision.getAssessmentTimeId());
        entity.setPassed(decision.getPassed());
        entity.setDecidedBy(decision.getDecidedBy());
        entity.setDecisionComment(decision.getDecisionComment());
        entity.setDecidedAt(decision.getDecidedAt());
        entity.setUpdatedAt(decision.getUpdatedAt());
        return entity;
    }

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
     * 查询指定考核时间下的全部录用决策，将实体转换为 VO。
     *
     * @param assessmentTimeId
     *            考核时间ID
     * @return 决策 VO 列表
     */
    @Override
    public List<AssessmentDecisionVO> findByAssessmentTimeId(Long assessmentTimeId) {
        return assessmentDecisionMapper.selectByAssessmentTimeId(assessmentTimeId)
                .stream()
                .map(this::convertToVO)
                .toList();
    }
}
