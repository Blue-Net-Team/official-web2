package com.bluenet.web.application.converter;

import com.bluenet.web.api.dto.assessment_statistics.QuestionStatisticsDTO;
import com.bluenet.web.application.AssessmentStatisticsResult;
import org.springframework.stereotype.Component;

/**
 * 考核统计应用层转换器
 * <p>
 * 负责应用层 Result 与 API 层 DTO 之间的转换
 * </p>
 */
@Component
public class AssessmentStatisticsAppConverter {

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
