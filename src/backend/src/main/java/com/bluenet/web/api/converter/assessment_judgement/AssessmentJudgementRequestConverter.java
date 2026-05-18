package com.bluenet.web.api.converter.assessment_judgement;

import com.bluenet.web.api.dto.assessment_judgement.AssessmentDecisionRequestDTO;
import com.bluenet.web.application.command.assessment_judgement.AssessmentJudgementCommands;
import org.springframework.stereotype.Component;

/**
 * 考核评判请求转换器
 * <p>
 * 负责将 API 层的 RequestDTO 转换为应用层的 Command
 * </p>
 */
@Component
public class AssessmentJudgementRequestConverter {

    /**
     * 将决策请求 DTO 转换为命令
     */
    public AssessmentJudgementCommands.DecideAssessmentCommand toCommand(AssessmentDecisionRequestDTO dto) {
        return new AssessmentJudgementCommands.DecideAssessmentCommand(
                dto.getUserId(),
                dto.getAssessmentTimeId(),
                dto.getPassed(),
                dto.getDecisionComment());
    }
}
