package com.bluenet.web.api.converter.assessment_question;

import com.bluenet.web.api.dto.assessment_question.CreateQuestionRequestDTO;
import com.bluenet.web.api.dto.assessment_question.UpdateQuestionRequestDTO;
import com.bluenet.web.application.command.assessment_question.AssessmentQuestionCommands;
import com.bluenet.web.domain.model.vo.evaluation.QuestionContent;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AssessmentQuestionRequestConverter {

    private final ObjectMapper objectMapper;

    public AssessmentQuestionCommands.CreateAssessmentQuestionCommand toCommand(CreateQuestionRequestDTO dto) {
        QuestionContent content = dto.getContent() != null
                ? objectMapper.convertValue(dto.getContent(), QuestionContent.class)
                : null;
        return new AssessmentQuestionCommands.CreateAssessmentQuestionCommand(
                dto.getAssessmentTimeId(),
                dto.getQuestionNo(),
                dto.getQuestionType(),
                dto.getTitle(),
                content,
                dto.getAttachmentId(),
                dto.getScore());
    }

    public AssessmentQuestionCommands.UpdateAssessmentQuestionCommand toCommand(Long id, UpdateQuestionRequestDTO dto) {
        QuestionContent content = dto.getContent() != null
                ? objectMapper.convertValue(dto.getContent(), QuestionContent.class)
                : null;
        return new AssessmentQuestionCommands.UpdateAssessmentQuestionCommand(
                id,
                dto.getQuestionNo(),
                dto.getQuestionType(),
                dto.getTitle(),
                content,
                dto.getAttachmentId(),
                dto.getScore());
    }
}
