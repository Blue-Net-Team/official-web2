package com.bluenet.web.api.converter.assessment_time;

import com.bluenet.web.api.dto.assessment_time.AssessmentProgressDTO;
import com.bluenet.web.api.dto.assessment_time.AssessmentTimeDTO;
import com.bluenet.web.application.result.assessment.AssessmentProgressResult;
import com.bluenet.web.application.result.assessment.AssessmentTimeResult;
import org.springframework.stereotype.Component;

/**
 * 考核时间响应转换器
 * <p>
 * 负责将考核时间应用层结果转换为 API 响应 DTO
 * </p>
 */
@Component
public class AssessmentTimeResponseConverter {

    /**
     * 将考核时间结果转换为 DTO
     */
    public AssessmentTimeDTO toDTO(AssessmentTimeResult result) {
        return AssessmentTimeDTO.builder()
                .id(result.id())
                .direction(result.direction())
                .epoch(result.epoch())
                .grade(result.grade())
                .startTime(result.startTime())
                .endTime(result.endTime())
                .timeLimit(result.timeLimit())
                .timeLimitMinutes(result.timeLimitMinutes())
                .totalQuestions(result.totalQuestions())
                .completedQuestions(result.completedQuestions())
                .allowTeam(result.allowTeam())
                .eliminated(result.eliminated())
                .build();
    }

    /**
     * 将考核进度结果转换为 DTO
     */
    public AssessmentProgressDTO toDTO(AssessmentProgressResult result) {
        return AssessmentProgressDTO.builder()
                .assessmentTimeId(result.assessmentTimeId())
                .totalQuestions(result.totalQuestions())
                .completedQuestions(result.completedQuestions())
                .build();
    }

}
