package com.bluenet.web.application.service;

import com.bluenet.web.api.dto.assessment_session.AssessmentSessionDTO;

/**
 * 考核会话应用服务接口
 */
public interface AssessmentSessionService {

    /**
     * 获取或创建考核会话
     *
     * @param userId
     *            用户ID
     * @param assessmentTimeId
     *            考核时间ID
     * @return 考核会话DTO
     */
    AssessmentSessionDTO getOrCreateSession(Long userId, Long assessmentTimeId);
}
