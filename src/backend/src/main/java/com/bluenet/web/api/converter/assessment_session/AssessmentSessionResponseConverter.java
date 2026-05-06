package com.bluenet.web.api.converter.assessment_session;

import com.bluenet.web.api.dto.assessment_session.AssessmentSessionDTO;
import com.bluenet.web.application.AssessmentSessionResult;
import org.springframework.stereotype.Component;

/**
 * 考核会话响应转换器
 * <p>
 * 负责将考核会话 Result 转换为接口 DTO
 * </p>
 */
@Component
public class AssessmentSessionResponseConverter {

    public AssessmentSessionDTO toDTO(AssessmentSessionResult result) {
        if (result == null) {
            return null;
        }
        return AssessmentSessionDTO.builder()
                .id(result.id())
                .userId(result.userId())
                .assessmentTimeId(result.assessmentTimeId())
                .startTime(result.startTime())
                .deadline(result.deadline())
                .build();
    }
}
