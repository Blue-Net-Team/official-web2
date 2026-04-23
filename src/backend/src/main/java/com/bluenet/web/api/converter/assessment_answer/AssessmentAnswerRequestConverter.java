package com.bluenet.web.api.converter.assessment_answer;

import com.bluenet.web.api.dto.assessment_answer.CreateAnswerRequestDTO;
import com.bluenet.web.application.command.assessment_answer.AssessmentAnswerCommands;
import org.springframework.stereotype.Component;

@Component
public class AssessmentAnswerRequestConverter {
    public AssessmentAnswerCommands.CreateAssessmentAnswerCommand toCreateCommand(Long userId,
            CreateAnswerRequestDTO dto) {
        return new AssessmentAnswerCommands.CreateAssessmentAnswerCommand(
                userId, dto.getQuestionId(), dto.getContent(), dto.getLanguage(), dto.getFileId());
    }

    public AssessmentAnswerCommands.UpdateAssessmentAnswerCommand toUpdateCommand(Long userId,
            CreateAnswerRequestDTO dto) {
        return new AssessmentAnswerCommands.UpdateAssessmentAnswerCommand(
                userId, dto.getQuestionId(), dto.getContent(), dto.getLanguage(), dto.getFileId());
    }
}
