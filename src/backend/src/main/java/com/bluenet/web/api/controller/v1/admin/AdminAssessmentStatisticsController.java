package com.bluenet.web.api.controller.v1.admin;

import com.bluenet.web.api.dto.ResponseMessage;
import com.bluenet.web.api.dto.assessment_statistics.QuestionStatisticsDTO;
import com.bluenet.web.application.AssessmentStatisticsResult;
import com.bluenet.web.application.converter.AssessmentStatisticsAppConverter;
import com.bluenet.web.application.service.AssessmentStatisticsAppService;
import com.bluenet.web.infrastructure.security.annotation.AccessLevel;
import com.bluenet.web.infrastructure.security.annotation.RequiresPermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "考题统计", description = "考题通过率和结果分布统计接口")
@RestController
@RequestMapping("/api/v1/admin/assessment-statistics")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearer-jwt")
public class AdminAssessmentStatisticsController {
    private final AssessmentStatisticsAppService assessmentStatisticsAppService;
    private final AssessmentStatisticsAppConverter assessmentStatisticsAppConverter;

    @Operation(summary = "查询题目统计", description = "按每名考生最新正式自动评判结果统计通过率和结果码分布。")
    @RequiresPermission(name = "查询题目统计", value = "assessment-statistics:query", access = AccessLevel.PROTECTED)
    @GetMapping("/questions/{questionId}")
    public ResponseMessage<QuestionStatisticsDTO> getQuestionStatistics(@PathVariable Long questionId) {
        AssessmentStatisticsResult result = assessmentStatisticsAppService.getQuestionStatistics(questionId);
        return ResponseMessage.success(assessmentStatisticsAppConverter.toDTO(result));
    }
}
