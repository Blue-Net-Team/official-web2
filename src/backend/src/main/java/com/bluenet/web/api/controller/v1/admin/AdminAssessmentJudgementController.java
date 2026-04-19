package com.bluenet.web.api.controller.v1.admin;

import com.bluenet.web.api.dto.ResponseMessage;
import com.bluenet.web.api.dto.assessment_judgement.AssessmentDecisionDTO;
import com.bluenet.web.api.dto.assessment_judgement.AssessmentDecisionRequestDTO;
import com.bluenet.web.api.dto.assessment_judgement.AssessmentJudgementDTO;
import com.bluenet.web.api.dto.assessment_judgement.ManualReviewRequestDTO;
import com.bluenet.web.application.service.AssessmentJudgementService;
import com.bluenet.web.infrastructure.security.annotation.AccessLevel;
import com.bluenet.web.infrastructure.security.annotation.RequiresPermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 考核评判管理控制器。
 */
@Tag(name = "考核评判管理", description = "评判查看、人工评分和最终通过决策接口")
@RestController
@RequestMapping("/api/v1/admin/assessment-judgements")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearer-jwt")
public class AdminAssessmentJudgementController {
    private final AssessmentJudgementService assessmentJudgementService;

    @Operation(summary = "查询答案最新评判", description = "按答案ID查询最新题目评判结果")
    @RequiresPermission(name = "查询题目评判列表", value = "assessment-judgement:list", access = AccessLevel.PROTECTED)
    @GetMapping("/answers/{answerId}/latest")
    public ResponseMessage<AssessmentJudgementDTO> getLatestByAnswerId(
            @Parameter(description = "答案ID", required = true) @PathVariable Long answerId) {
        return ResponseMessage.success(assessmentJudgementService.getLatestByAnswerId(answerId));
    }

    @Operation(summary = "查询题目评判列表", description = "按题目ID查询该题全部评判记录")
    @RequiresPermission(name = "查询考核评判", value = "assessment-judgement:query", access = AccessLevel.PROTECTED)
    @GetMapping
    public ResponseMessage<List<AssessmentJudgementDTO>> listByQuestionId(
            @Parameter(description = "题目ID", required = true) @RequestParam Long questionId) {
        return ResponseMessage.success(assessmentJudgementService.listByQuestionId(questionId));
    }

    @Operation(summary = "人工评分文件上传题", description = "团队成员及以上可对文件上传题进行人工评分和评论")
    @RequiresPermission(name = "人工评分考核答案", value = "assessment-judgement:manual-review", access = AccessLevel.PROTECTED)
    @PostMapping("/manual-review")
    public ResponseMessage<AssessmentJudgementDTO> reviewFileUploadAnswer(
            @Valid @RequestBody ManualReviewRequestDTO request) {
        return ResponseMessage.success(assessmentJudgementService.reviewFileUploadAnswer(request));
    }

    @Operation(summary = "设置考核最终通过决策", description = "方向管理员及以上可设置考生是否通过某次考核")
    @RequiresPermission(name = "设置考核通过决策", value = "assessment-decision:set", access = AccessLevel.PROTECTED)
    @PostMapping("/decisions")
    public ResponseMessage<AssessmentDecisionDTO> decideAssessment(
            @Valid @RequestBody AssessmentDecisionRequestDTO request) {
        return ResponseMessage.success(assessmentJudgementService.decideAssessment(request));
    }
}
