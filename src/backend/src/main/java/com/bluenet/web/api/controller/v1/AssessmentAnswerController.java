package com.bluenet.web.api.controller.v1;

import com.bluenet.web.api.dto.ResponseMessage;
import com.bluenet.web.api.dto.assessment_answer.AssessmentAnswerDTO;
import com.bluenet.web.api.dto.assessment_answer.CreateAnswerRequestDTO;
import com.bluenet.web.application.service.AssessmentAnswerService;
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
 * 答案控制器
 * <p>
 * 提供已登录用户提交答案和查询答案接口
 * </p>
 */
@Tag(name = "答题", description = "答案提交与查询接口，已登录用户可访问")
@RestController
@RequestMapping("/api/v1/assessment-answers")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearer-jwt")
public class AssessmentAnswerController {

    private final AssessmentAnswerService assessmentAnswerService;

    @Operation(summary = "提交答案", description = "提交指定题目的答案。支持文件上传题（传fileId）和内容题（传content）。")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "提交成功", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AssessmentAnswerDTO.class))),
            @ApiResponse(responseCode = "400", description = "参数错误", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseMessage.class))),
            @ApiResponse(responseCode = "403", description = "考核时间已到", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseMessage.class))),
            @ApiResponse(responseCode = "409", description = "重复提交", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseMessage.class)))
    })
    @RequiresPermission(name = "提交答案", value = "assessment-answer:create", access = AccessLevel.AUTHENTICATED)
    @PostMapping
    public ResponseMessage<AssessmentAnswerDTO> createAnswer(
            @Valid @RequestBody CreateAnswerRequestDTO request) {
        try {
            AssessmentAnswerDTO result = assessmentAnswerService.createAnswer(request);
            return ResponseMessage.success(result);
        } catch (IllegalStateException e) {
            return ResponseMessage.error(403, e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseMessage.error(404, e.getMessage());
        }
    }

    @Operation(summary = "更新答案", description = "重新提交指定题目的答案。支持文件上传题（传fileId）和内容题（传content）。")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "更新成功", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AssessmentAnswerDTO.class))),
            @ApiResponse(responseCode = "400", description = "参数错误", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseMessage.class))),
            @ApiResponse(responseCode = "403", description = "考核时间已到或尚未提交", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseMessage.class)))
    })
    @RequiresPermission(name = "更新答案", value = "assessment-answer:update", access = AccessLevel.AUTHENTICATED)
    @PutMapping
    public ResponseMessage<AssessmentAnswerDTO> updateAnswer(
            @Valid @RequestBody CreateAnswerRequestDTO request) {
        try {
            AssessmentAnswerDTO result = assessmentAnswerService.updateAnswer(request);
            return ResponseMessage.success(result);
        } catch (IllegalStateException e) {
            return ResponseMessage.error(403, e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseMessage.error(404, e.getMessage());
        }
    }

    @Operation(summary = "查询我的答案", description = "查询当前用户对指定题目的答案")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "查询成功", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AssessmentAnswerDTO.class))),
            @ApiResponse(responseCode = "404", description = "未作答", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseMessage.class)))
    })
    @RequiresPermission(name = "查询答案", value = "assessment-answer:query", access = AccessLevel.AUTHENTICATED)
    @GetMapping
    public ResponseMessage<AssessmentAnswerDTO> getMyAnswer(
            @Parameter(description = "题目ID", required = true) @RequestParam Long questionId) {
        AssessmentAnswerDTO result = assessmentAnswerService.getMyAnswer(questionId);
        if (result == null) {
            return ResponseMessage.error(404, "未找到答案");
        }
        return ResponseMessage.success(result);
    }
}
