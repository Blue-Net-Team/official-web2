package com.bluenet.web.domain.service;

import com.bluenet.web.domain.model.vo.AssessmentDecisionVO;

/**
 * 考生最终通过决策领域服务接口。
 */
public interface AssessmentDecisionDomainService {

    /**
     * 创建或更新考生最终通过决策。
     *
     * @param decision
     *            决策VO
     * @return 保存后的决策记录
     */
    AssessmentDecisionVO saveDecision(AssessmentDecisionVO decision);

    /**
     * 查询最终通过决策详情。
     *
     * @param id
     *            决策ID
     * @return 决策记录
     */
    AssessmentDecisionVO getDecisionById(Long id);

    /**
     * 查询某考生在某次考核中的最终通过决策。
     *
     * @param userId
     *            考生用户ID
     * @param assessmentTimeId
     *            考核时间ID
     * @return 决策记录
     */
    AssessmentDecisionVO getDecision(Long userId, Long assessmentTimeId);

    /**
     * 检查用户是否因在某个更早轮次被淘汰而无法参与目标考核。
     *
     * @param userId
     *            用户ID
     * @param assessmentTime
     *            目标考核时间
     * @return 如果用户在相同 direction+grade 组合的某个更早 epoch 中被淘汰，返回 true
     */
    boolean isEliminatedFromPriorEpoch(Long userId, com.bluenet.web.domain.model.entity.AssessmentTime assessmentTime);
}
