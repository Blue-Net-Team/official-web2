package com.bluenet.web.api.converter.assessment_time;

import com.bluenet.web.api.dto.assessment_time.AssessmentProgressDTO;
import com.bluenet.web.api.dto.assessment_time.AssessmentTimeDTO;
import com.bluenet.web.application.AssessmentProgressResult;
import com.bluenet.web.application.AssessmentTimeResult;
import com.bluenet.web.domain.model.entity.AssessmentTime;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 考核时间响应转换器
 * <p>
 * 负责将考核时间实体/结果转换为 API 响应 DTO
 * </p>
 */
@Component
public class AssessmentTimeResponseConverter {

    /**
     * 将考核时间实体转换为 DTO
     */
    public AssessmentTimeDTO toDTO(AssessmentTime entity) {
        return AssessmentTimeDTO.builder()
                .id(entity.getId())
                .direction(entity.getDirection())
                .epoch(entity.getEpoch())
                .grade(entity.getGrade())
                .startTime(entity.getStartTime())
                .endTime(entity.getEndTime())
                .timeLimit(entity.getTimeLimit())
                .timeLimitMinutes(entity.getTimeLimitMinutes())
                .build();
    }

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

    /**
     * 将考核时间实体列表转换为 DTO 列表
     */
    public List<AssessmentTimeDTO> toDTOList(List<AssessmentTime> entityList) {
        return entityList.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
}
