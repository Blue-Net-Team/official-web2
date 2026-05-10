package com.bluenet.web.api.converter.assessment_answer;

import com.bluenet.web.api.converter.assessment_judgement.AssessmentJudgementResponseConverter;
import com.bluenet.web.api.dto.assessment_answer.AssessmentAnswerDTO;
import com.bluenet.web.application.AssessmentAnswerResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

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
                .build();
    }
}
