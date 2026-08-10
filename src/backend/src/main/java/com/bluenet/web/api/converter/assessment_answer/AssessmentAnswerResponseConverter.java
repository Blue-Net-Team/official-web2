package com.bluenet.web.api.converter.assessment_answer;

import com.bluenet.web.api.converter.assessment_judgement.AssessmentJudgementResponseConverter;
import com.bluenet.web.api.converter.assessment_judgement.CommentResponseConverter;
import com.bluenet.web.api.dto.assessment_answer.AssessmentAnswerDTO;
import com.bluenet.web.application.result.assessment.AssessmentAnswerResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

/**
 * 评测答案响应转换器
 * <p>
 * 负责将应用层结果转换为 API 响应 DTO
 * </p>
 */
@Component
@RequiredArgsConstructor
public class AssessmentAnswerResponseConverter {

    private final AssessmentJudgementResponseConverter judgementResponseConverter;
    private final CommentResponseConverter commentResponseConverter;

    /**
     * 将应用层结果转换为 API 响应 DTO
     */
    public AssessmentAnswerDTO toDTO(AssessmentAnswerResult result) {
        if (result == null) {
            return null;
        }
        return AssessmentAnswerDTO.builder()
                .id(result.id())
                .questionId(result.questionId())
                .fileId(result.fileId())
                .content(result.content())
                .language(result.language())
                .submitTime(result.submitTime())
                .judgement(judgementResponseConverter.toDTO(result.judgement()))
                .comments(toCommentDTOs(result.comments()))
                .build();
    }

    private List<com.bluenet.web.api.dto.assessment_judgement.CommentDTO> toCommentDTOs(
            List<com.bluenet.web.application.result.comment.CommentResult> comments) {
        if (comments == null || comments.isEmpty()) {
            return Collections.emptyList();
        }
        return comments.stream()
                .map(commentResponseConverter::toDTO)
                .toList();
    }
}
