package com.bluenet.web.api.converter.assessment_time;

import com.bluenet.web.api.dto.assessment_time.CreateAssessmentTimeRequestDTO;
import com.bluenet.web.api.dto.assessment_time.UpdateAssessmentTimeRequestDTO;
import com.bluenet.web.application.command.assessment_time.AssessmentTimeCommands;
import org.springframework.stereotype.Component;

/**
 * 考核时间请求转换器
 * <p>
 * 负责将 API 层的 RequestDTO 转换为应用层的 Command
 * </p>
 */
@Component
public class AssessmentTimeRequestConverter {

    /**
     * 将创建请求 DTO 转换为命令
     */
    public AssessmentTimeCommands.CreateAssessmentTimeCommand toCommand(CreateAssessmentTimeRequestDTO dto) {
        return new AssessmentTimeCommands.CreateAssessmentTimeCommand(
                dto.getDirection(),
                dto.getEpoch(),
                dto.getGrade(),
                dto.getStartTime(),
                dto.getEndTime(),
                dto.getTimeLimit(),
                Boolean.TRUE.equals(dto.getTimeLimit()) ? dto.getTimeLimitMinutes() : null,
                dto.getAllowTeam());
    }

    /**
     * 将更新请求 DTO 转换为命令
     */
    public AssessmentTimeCommands.UpdateAssessmentTimeCommand toCommand(Long id, UpdateAssessmentTimeRequestDTO dto) {
        return new AssessmentTimeCommands.UpdateAssessmentTimeCommand(
                id,
                dto.getDirection(),
                dto.getEpoch(),
                dto.getGrade(),
                dto.getStartTime(),
                dto.getEndTime(),
                dto.getTimeLimit(),
                Boolean.FALSE.equals(dto.getTimeLimit()) ? null : dto.getTimeLimitMinutes(),
                dto.getAllowTeam());
    }
}
