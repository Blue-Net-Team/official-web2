package com.bluenet.web.api.controller.v1;

import com.bluenet.web.api.dto.PageDTO;
import com.bluenet.web.api.dto.ResponseMessage;
import com.bluenet.web.api.dto.assessment_question.AssessmentQuestionDTO;
import com.bluenet.web.api.dto.assessment_question.ResponseMessageAssessmentQuestionList;
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
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 考题查询控制器
 * <p>
 * 提供已登录用户查询考题目录接口
 * </p>
 */
@Tag(name = "考题查询", description = "考题目录查询接口，已登录用户可访问")
@RestController
@RequestMapping("/api/v1/assessment-questions")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearer-jwt")
public class AssessmentQuestionController {
    private final AssessmentQuestionService assessmentQuestionService;

    @Operation(summary = "查询考题目录", description = "分页查询指定考核时间下的考题目录。考生只能看到自己方向和年级的考题，不包含题目内容。")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "查询成功", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseMessageAssessmentQuestionList.class))),
            @ApiResponse(responseCode = "403", description = "无权查看", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseMessage.class)))
    })
    @RequiresPermission(name = "查询考题目录", value = "assessment-question:query", access = AccessLevel.AUTHENTICATED)
    @GetMapping
    public ResponseMessage<PageDTO<AssessmentQuestionDTO>> listQuestions(
            @Parameter(description = "考核时间ID", required = true) @RequestParam Long assessmentTimeId,
            @Parameter(description = "页码（从0开始，默认0）") @RequestParam(required = false, defaultValue = "0") Integer page,
            @Parameter(description = "每页大小（默认10）") @RequestParam(required = false, defaultValue = "10") Integer size) {
        try {
            PageDTO<AssessmentQuestionDTO> result = assessmentQuestionService
                    .listQuestionsForUser(assessmentTimeId, page, size);
            return ResponseMessage.success(result);
        } catch (SecurityException e) {
            return ResponseMessage.error(403, e.getMessage());
        }
    }
}
