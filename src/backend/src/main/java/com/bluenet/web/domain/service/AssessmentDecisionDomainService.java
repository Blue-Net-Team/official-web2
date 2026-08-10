package com.bluenet.web.domain.service;

import com.bluenet.web.domain.model.entity.AssessmentDecision;
import com.bluenet.web.domain.model.entity.AssessmentTime;

import java.util.List;
import java.util.Map;

/**
 * 考生最终通过决策领域服务接口。
 */
public interface AssessmentDecisionDomainService {

    /**
     * 检查用户是否因在某个更早轮次被淘汰而无法参与目标考核。
     *
     * @param userId
     *            用户ID
     * @param assessmentTime
     *            目标考核时间
     * @return 如果用户在相同 direction+grade 组合的某个更早 epoch 中被淘汰，返回 true
     */
    boolean isEliminatedFromPriorEpoch(Long userId, AssessmentTime assessmentTime);

    /**
     * 基于已预加载的淘汰决策列表和考核场次信息，判断用户是否因某个更早轮次被淘汰而无法参与目标考核。
     *
     * @param targetTime
     *            目标考核时间
     * @param eliminatedDecisions
     *            该用户的所有淘汰决策列表
     * @param decisionTimeMap
     *            决策关联的考核场次映射（key: assessmentTimeId）
     * @return 如果用户在相同 direction+grade 组合的某个更早 epoch 中被淘汰，返回 true
     */
    boolean isEliminatedFromPriorEpoch(AssessmentTime targetTime, List<AssessmentDecision> eliminatedDecisions,
            Map<Long, AssessmentTime> decisionTimeMap);
}
