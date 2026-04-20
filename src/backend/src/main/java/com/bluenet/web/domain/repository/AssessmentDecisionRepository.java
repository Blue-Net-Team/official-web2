package com.bluenet.web.domain.repository;

import com.bluenet.web.domain.model.entity.AssessmentDecision;
import com.bluenet.web.domain.model.vo.AssessmentDecisionVO;

import java.util.List;
import java.util.Optional;

/**
 * 考生考核最终通过决策仓储接口。
 */
public interface AssessmentDecisionRepository {

    /**
     * 保存新的最终通过决策。
     *
     * @param decision
     *            决策实体
     */
    void save(AssessmentDecision decision);

    /**
     * 按主键查询最终通过决策。
     *
     * @param id
     *            决策ID
     * @return 决策记录
     */
    Optional<AssessmentDecisionVO> findById(Long id);

    /**
     * 更新已有最终通过决策。
     *
     * @param decision
     *            决策VO
     */
    void update(AssessmentDecisionVO decision);

    /**
     * 查询某考生在某次考核中的最终通过决策。
     *
     * @param userId
     *            考生用户ID
     * @param assessmentTimeId
     *            考核时间ID
     * @return 决策记录
     */
    Optional<AssessmentDecisionVO> findByUserIdAndAssessmentTimeId(Long userId, Long assessmentTimeId);

    /**
     * 查询指定考核时间下的全部录用决策。
     *
     * @param assessmentTimeId
     *            考核时间ID
     * @return 决策列表
     */
    List<AssessmentDecisionVO> findByAssessmentTimeId(Long assessmentTimeId);
}
