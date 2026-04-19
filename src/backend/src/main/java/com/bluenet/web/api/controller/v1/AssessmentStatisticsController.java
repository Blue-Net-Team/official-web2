package com.bluenet.web.api.controller.v1;

import com.bluenet.web.api.dto.ResponseMessage;
import com.bluenet.web.api.dto.assessment_statistics.QuestionStatisticsDTO;
import com.bluenet.web.application.service.AssessmentStatisticsService;
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

@Tag(name = "考生端题目统计", description = "考生端可选展示的客观题通过率统计接口")
@RestController
@RequestMapping("/api/v1/assessment-statistics")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearer-jwt")
public class AssessmentStatisticsController {
    private final AssessmentStatisticsService assessmentStatisticsService;

    @Operation(summary = "查询考生端题目统计", description = "配置开启后，考生可在有权查看的题目详情页看到聚合通过率。")
    @RequiresPermission(name = "查询考生端题目统计", value = "assessment-statistics:candidate-query", access = AccessLevel.AUTHENTICATED)
    @GetMapping("/questions/{questionId}")
    public ResponseMessage<QuestionStatisticsDTO> getCandidateQuestionStatistics(@PathVariable Long questionId) {
        return ResponseMessage.success(assessmentStatisticsService.getCandidateQuestionStatistics(questionId));
    }
}
