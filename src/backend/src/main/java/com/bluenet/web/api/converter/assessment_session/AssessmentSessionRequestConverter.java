package com.bluenet.web.api.converter.assessment_session;

import com.bluenet.web.application.command.assessment_session.AssessmentSessionCommands;
import org.springframework.stereotype.Component;

/**
 * 考核会话请求转换器
 * <p>
 * 负责将 API 层的参数转换为应用层的 Command
 * </p>
 */
@Component
public class AssessmentSessionRequestConverter {

    /**
     * 将请求参数转换为命令
     */
    public AssessmentSessionCommands.GetOrCreateSessionCommand toCommand(Long userId, Long assessmentTimeId) {
        return new AssessmentSessionCommands.GetOrCreateSessionCommand(userId, assessmentTimeId);
    }
}
