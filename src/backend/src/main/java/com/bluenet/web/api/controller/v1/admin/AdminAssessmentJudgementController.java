package com.bluenet.web.api.controller.v1.admin;

import com.bluenet.web.api.dto.ResponseMessage;
import com.bluenet.web.api.dto.assessment_judgement.AssessmentDecisionDTO;
import com.bluenet.web.api.dto.assessment_judgement.AssessmentDecisionRequestDTO;
import com.bluenet.web.api.dto.assessment_judgement.AssessmentDecisionWorkspaceDTO;
import com.bluenet.web.api.dto.assessment_judgement.AssessmentCandidateScoreboardDTO;
import com.bluenet.web.api.dto.assessment_judgement.AssessmentJudgementDTO;
import com.bluenet.web.api.dto.assessment_judgement.AssessmentQuestionScoreboardDTO;
import com.bluenet.web.api.dto.assessment_judgement.AssessmentQuestionSubmissionDTO;
import com.bluenet.web.api.dto.assessment_judgement.ManualReviewRequestDTO;
import com.bluenet.web.application.service.AssessmentJudgementService;
import com.bluenet.web.domain.model.enumerate.QuestionType;
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

    /**
     * 查询指定答案的最新评判结果。
     *
     * @param answerId
     *            答案ID
     * @return 最新评判 DTO
     */
    @Operation(summary = "查询答案最新评判", description = "按答案ID查询最新题目评判结果")
    @RequiresPermission(name = "查询题目评判列表", value = "assessment-judgement:list", access = AccessLevel.PROTECTED)
    @GetMapping("/answers/{answerId}/latest")
    public ResponseMessage<AssessmentJudgementDTO> getLatestByAnswerId(
            @Parameter(description = "答案ID", required = true) @PathVariable Long answerId) {
        return ResponseMessage.success(assessmentJudgementService.getLatestByAnswerId(answerId));
    }

    /**
     * 查询指定题目下的评判列表。
     *
     * @param questionId
     *            题目ID
     * @return 评判 DTO 列表
     */
    @Operation(summary = "查询题目评判列表", description = "按题目ID查询该题全部评判记录")
    @RequiresPermission(name = "查询考核评判", value = "assessment-judgement:query", access = AccessLevel.PROTECTED)
    @GetMapping
    public ResponseMessage<List<AssessmentJudgementDTO>> listByQuestionId(
            @Parameter(description = "题目ID", required = true) @RequestParam Long questionId) {
        return ResponseMessage.success(assessmentJudgementService.listByQuestionId(questionId));
    }

    /**
     * 提交文件上传题人工评分。
     *
     * @param request
     *            人工评分请求
     * @return 新创建的评判 DTO
     */
    @Operation(summary = "人工评分文件上传题", description = "团队成员及以上可对文件上传题进行人工评分和评论")
    @RequiresPermission(name = "人工评分考核答案", value = "assessment-judgement:manual-review", access = AccessLevel.PROTECTED)
    @PostMapping("/manual-review")
    public ResponseMessage<AssessmentJudgementDTO> reviewFileUploadAnswer(
            @Valid @RequestBody ManualReviewRequestDTO request) {
        return ResponseMessage.success(assessmentJudgementService.reviewFileUploadAnswer(request));
    }

    /**
     * 保存考生本轮考核的录用决策。
     *
     * @param request
     *            决策请求
     * @return 保存后的决策 DTO
     */
    @Operation(summary = "设置考核最终通过决策", description = "方向管理员及以上可设置考生是否通过某次考核")
    @RequiresPermission(name = "设置考核通过决策", value = "assessment-decision:set", access = AccessLevel.PROTECTED)
    @PostMapping("/decisions")
    public ResponseMessage<AssessmentDecisionDTO> decideAssessment(
            @Valid @RequestBody AssessmentDecisionRequestDTO request) {
        return ResponseMessage.success(assessmentJudgementService.decideAssessment(request));
    }

    /**
     * 查询题目视图左侧题目评分汇总。
     *
     * @param assessmentTimeId
     *            考核时间ID
     * @param questionType
     *            题型筛选
     * @param keyword
     *            题目关键词
     * @return 题目评分汇总 DTO 列表
     */
    @Operation(summary = "查询题目评分汇总", description = "按考核时间查询题目维度提交数、已评分数、待评分数和平均分")
    @RequiresPermission(name = "查询题目评分汇总", value = "assessment-judgement:query", access = AccessLevel.PROTECTED)
    @GetMapping("/scoreboard/questions")
    public ResponseMessage<List<AssessmentQuestionScoreboardDTO>> listQuestionScoreboard(
            @Parameter(description = "考核时间ID", required = true) @RequestParam Long assessmentTimeId,
            @Parameter(description = "题型") @RequestParam(required = false) QuestionType questionType,
            @Parameter(description = "题目关键词") @RequestParam(required = false) String keyword) {
        return ResponseMessage.success(
                assessmentJudgementService
                        .listQuestionScoreboard(assessmentTimeId, questionType, keyword));
    }

    /**
     * 查询题目视图右侧的考生提交和评判列表。
     *
     * @param questionId
     *            题目ID
     * @param keyword
     *            考生关键词
     * @param status
     *            评分状态（JUDGED/PENDING）
     * @return 提交评分 DTO 列表
     */
    @Operation(summary = "查询题目提交评分列表", description = "按题目查询所有考生提交及最新评判")
    @RequiresPermission(name = "查询题目提交评分", value = "assessment-judgement:query", access = AccessLevel.PROTECTED)
    @GetMapping("/scoreboard/questions/{questionId}/submissions")
    public ResponseMessage<List<AssessmentQuestionSubmissionDTO>> listQuestionSubmissions(
            @Parameter(description = "题目ID", required = true) @PathVariable Long questionId,
            @Parameter(description = "考生关键词") @RequestParam(required = false) String keyword,
            @Parameter(description = "评分状态：JUDGED/PENDING") @RequestParam(required = false) String status) {
        return ResponseMessage.success(
                assessmentJudgementService
                        .listQuestionSubmissions(questionId, keyword, status));
    }

    /**
     * 查询人员视图的考生评分矩阵。
     *
     * @param assessmentTimeId
     *            考核时间ID
     * @param keyword
     *            考生关键词
     * @return 考生评分汇总 DTO 列表
     */
    @Operation(summary = "查询考生评分矩阵", description = "按考核时间查询考生维度总分、各题提交和评分状态")
    @RequiresPermission(name = "查询考生评分矩阵", value = "assessment-judgement:query", access = AccessLevel.PROTECTED)
    @GetMapping("/scoreboard/candidates")
    public ResponseMessage<List<AssessmentCandidateScoreboardDTO>> listCandidateScoreboard(
            @Parameter(description = "考核时间ID", required = true) @RequestParam Long assessmentTimeId,
            @Parameter(description = "考生关键词") @RequestParam(required = false) String keyword) {
        return ResponseMessage.success(
                assessmentJudgementService
                        .listCandidateScoreboard(assessmentTimeId, keyword));
    }

    /**
     * 查询录用决策页面所需的统计和候选人列表。
     *
     * @param assessmentTimeId
     *            考核时间ID
     * @param keyword
     *            考生关键词
     * @param decisionStatus
     *            决策状态（PENDING/PASSED/ELIMINATED）
     * @return 决策工作台 DTO
     */
    @Operation(summary = "查询录用决策工作台", description = "按考核时间查询候选人表现、已有决策和决策统计")
    @RequiresPermission(name = "查询录用决策工作台", value = "assessment-decision:set", access = AccessLevel.PROTECTED)
    @GetMapping("/decisions")
    public ResponseMessage<AssessmentDecisionWorkspaceDTO> getDecisionWorkspace(
            @Parameter(description = "考核时间ID", required = true) @RequestParam Long assessmentTimeId,
            @Parameter(description = "考生关键词") @RequestParam(required = false) String keyword,
            @Parameter(description = "决策状态：PENDING/PASSED/ELIMINATED") @RequestParam(required = false) String decisionStatus) {
        return ResponseMessage.success(
                assessmentJudgementService
                        .getDecisionWorkspace(assessmentTimeId, keyword, decisionStatus));
    }
}
