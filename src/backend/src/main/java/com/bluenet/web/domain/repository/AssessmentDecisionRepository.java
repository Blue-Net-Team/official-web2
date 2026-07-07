package com.bluenet.web.domain.repository;

import com.bluenet.web.domain.model.entity.AssessmentDecision;

import java.util.List;
import java.util.Optional;

/**
 * 考生考核最终通过决策仓储接口。
 */
public interface AssessmentDecisionRepository {

    /**
     * 保存新的考核最终决策 记录。
     *
     * @param decision
     *            考核最终决策对象。
     */
    void save(AssessmentDecision decision);

    /**
     * 按主键查询考核最终决策 记录。
     *
     * @param id
     *            业务记录主键。
     * @return 查询到的考核最终决策 结果；不存在时为空。
     */
    Optional<AssessmentDecision> findById(Long id);

    /**
     * 更新已有考核最终决策 记录。
     *
     * @param decision
     *            考核最终决策对象。
     */
    void update(AssessmentDecision decision);

    /**
     * 按用户和考核场次查询对应记录。
     *
     * @param userId
     *            用户主键，用于限定用户范围。
     * @param assessmentTimeId
     *            考核场次主键。
     * @return 查询到的考核最终决策 结果；不存在时为空。
     */
    Optional<AssessmentDecision> findByUserIdAndAssessmentTimeId(Long userId, Long assessmentTimeId);

    /**
     * 查询指定考核场次下的记录列表。
     *
     * @param assessmentTimeId
     *            考核场次主键。
     * @return 满足条件的考核最终决策 结果集合。
     */
    List<AssessmentDecision> findByAssessmentTimeId(Long assessmentTimeId);

    /**
     * 查询指定用户的所有淘汰决策记录（passed = false）。
     *
     * @param userId
     *            用户主键。
     * @return 该用户的淘汰决策列表；无记录时返回空列表。
     */
    List<AssessmentDecision> findEliminatedDecisionsByUserId(Long userId);
}
