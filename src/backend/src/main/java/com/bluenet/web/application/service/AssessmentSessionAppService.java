package com.bluenet.web.application.service;

import com.bluenet.web.application.result.assessment.AssessmentSessionResult;
import com.bluenet.web.application.command.assessment_session.AssessmentSessionCommands;

/**
 * 考核会话应用服务接口。
 * <p>
 * 定义了考核会话聚合在应用层的所有业务操作。
 * </p>
 */
public interface AssessmentSessionAppService {

    /**
     * 获取或创建考核会话
     *
     * @param command
     *            获取或创建会话命令
     * @return 考核会话结果
     */
    AssessmentSessionResult getOrCreateSession(AssessmentSessionCommands.GetOrCreateSessionCommand command);
}
