package com.bluenet.web.api.controller.v1.admin;

import com.bluenet.web.api.dto.PageDTO;
import com.bluenet.web.api.dto.ResponseMessage;
import com.bluenet.web.api.dto.assessment_question.AssessmentQuestionDTO;
import com.bluenet.web.api.dto.assessment_question.CreateQuestionRequestDTO;
import com.bluenet.web.api.dto.assessment_question.ResponseMessageAssessmentQuestion;
import com.bluenet.web.api.dto.assessment_question.ResponseMessageAssessmentQuestionList;
import com.bluenet.web.api.dto.assessment_question.UpdateQuestionRequestDTO;
import com.bluenet.web.application.service.AssessmentQuestionService;
import com.bluenet.web.infrastructure.security.annotation.AccessLevel;
import com.bluenet.web.infrastructure.security.annotation.RequiresPermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 考题管理控制器
 * <p>
 * 提供考题管理接口，需要管理员权限
 * </p>
 */
@Tag(name = "考题管理", description = "考题管理接口，需要管理员权限")
@RestController
@RequestMapping("/api/v1/admin/assessment-questions")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearer-jwt")
public class AdminAssessmentQuestionController {
    private final AssessmentQuestionService assessmentQuestionService;

    @Operation(summary = "创建考题", description = "创建新的考题")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "创建成功", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseMessageAssessmentQuestion.class))),
            @ApiResponse(responseCode = "400", description = "参数错误", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseMessage.class))),
            @ApiResponse(responseCode = "409", description = "题号重复", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseMessage.class)))
    })
    @RequiresPermission(name = "创建考题", value = "assessment-question:create", access = AccessLevel.PROTECTED)
    @PostMapping
    public ResponseMessage<AssessmentQuestionDTO> createQuestion(
            @Valid @RequestBody CreateQuestionRequestDTO request) {
        try {
            AssessmentQuestionDTO created = assessmentQuestionService.createQuestion(request);
            return ResponseMessage.success(created);
        } catch (IllegalArgumentException e) {
            return ResponseMessage.error(400, e.getMessage());
        } catch (IllegalStateException e) {
            return ResponseMessage.error(409, e.getMessage());
        }
    }

    @Operation(summary = "更新考题", description = "更新考题信息")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "更新成功", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseMessageAssessmentQuestion.class))),
            @ApiResponse(responseCode = "400", description = "参数错误", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseMessage.class))),
            @ApiResponse(responseCode = "404", description = "考题不存在", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseMessage.class)))
    })
    @RequiresPermission(name = "更新考题", value = "assessment-question:update", access = AccessLevel.PROTECTED)
    @PutMapping("/{id}")
    public ResponseMessage<AssessmentQuestionDTO> updateQuestion(
            @Parameter(description = "考题ID", required = true) @PathVariable Long id,
            @Valid @RequestBody UpdateQuestionRequestDTO request) {
        try {
            AssessmentQuestionDTO updated = assessmentQuestionService.updateQuestion(id, request);
            return ResponseMessage.success(updated);
        } catch (IllegalArgumentException e) {
            if (e.getMessage().contains("不存在")) {
                return ResponseMessage.error(404, e.getMessage());
            }
            return ResponseMessage.error(400, e.getMessage());
        } catch (IllegalStateException e) {
            return ResponseMessage.error(409, e.getMessage());
        }
    }

    @Operation(summary = "删除考题", description = "删除考题")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "删除成功", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseMessage.class))),
            @ApiResponse(responseCode = "404", description = "考题不存在", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseMessage.class)))
    })
    @RequiresPermission(name = "删除考题", value = "assessment-question:delete", access = AccessLevel.PROTECTED)
    @DeleteMapping("/{id}")
    public ResponseMessage<Void> deleteQuestion(
            @Parameter(description = "考题ID", required = true) @PathVariable Long id) {
        try {
            assessmentQuestionService.deleteQuestion(id);
            return ResponseMessage.success(null);
        } catch (IllegalArgumentException e) {
            return ResponseMessage.error(404, e.getMessage());
        }
    }

    @Operation(summary = "分页查询考题列表", description = "管理端分页查询指定考核时间下的考题列表")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "查询成功", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseMessageAssessmentQuestionList.class)))
    })
    @RequiresPermission(name = "查询考题列表", value = "assessment-question:list", access = AccessLevel.PROTECTED)
    @GetMapping
    public ResponseMessage<PageDTO<AssessmentQuestionDTO>> listQuestions(
            @Parameter(description = "考核时间ID", required = true) @RequestParam Long assessmentTimeId,
            @Parameter(description = "页码（从0开始，默认0）") @RequestParam(required = false, defaultValue = "0") Integer page,
            @Parameter(description = "每页大小（默认10）") @RequestParam(required = false, defaultValue = "10") Integer size) {
        PageDTO<AssessmentQuestionDTO> result = assessmentQuestionService
                .listQuestionsForAdmin(assessmentTimeId, page, size);
        return ResponseMessage.success(result);
    }
}
