package com.bluenet.web.application.converter;

import com.bluenet.web.api.dto.assessment_answer.AssessmentAnswerDTO;
import com.bluenet.web.application.AssessmentAnswerResult;
import org.springframework.stereotype.Component;

/**
 * 评测答案应用层转换器。
 * <p>
 * 负责应用层 Result 与 API 层 DTO 之间的转换。
 * </p>
 */
@Component
public class AssessmentAnswerAppConverter {

    /**
     * 将应用层结果转换为 API 响应 DTO。
     *
     * @param result
     *            评测答案应用层结果
     * @return 评测答案 API DTO
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
                .judgement(result.judgement())
                .build();
    }
}
