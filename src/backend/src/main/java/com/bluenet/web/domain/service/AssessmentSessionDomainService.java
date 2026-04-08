package com.bluenet.web.domain.service;

import com.bluenet.web.domain.model.vo.AssessmentSessionVO;

/**
 * 考核会话领域服务接口
 */
public interface AssessmentSessionDomainService {
    /**
     * 获取或创建考核会话
     * <p>
     * 如果用户已有该考核时间的会话则直接返回，否则创建新会话并计算截止时间。 截止时间 = min(startTime + timeLimitMinutes,
     * endTime)
     * </p>
     *
     * @param userId
     *            用户ID
     * @param assessmentTimeId
     *            考核时间ID
     * @return 考核会话VO
     */
    AssessmentSessionVO getOrCreateSession(Long userId, Long assessmentTimeId);
}
