package com.bluenet.web.api.converter.assessment_statistics;

import com.bluenet.web.api.dto.assessment_statistics.QuestionStatisticsDTO;
import com.bluenet.web.application.result.assessment.AssessmentStatisticsResult;
import org.springframework.stereotype.Component;

/**
 * 考核统计响应转换器
 * <p>
 * 负责将应用层结果转换为 API 响应 DTO
 * </p>
 */
@Component
public class AssessmentStatisticsResponseConverter {

    /**
     * 将应用层结果转换为 API 响应 DTO
     */
    public QuestionStatisticsDTO toDTO(AssessmentStatisticsResult result) {
        return QuestionStatisticsDTO.builder()
                .questionId(result.questionId())
                .questionType(result.questionType())
                .submittedCount(result.submittedCount())
                .acceptedCount(result.acceptedCount())
                .passRate(result.passRate())
                .resultDistribution(result.resultDistribution())
                .build();
    }
}
