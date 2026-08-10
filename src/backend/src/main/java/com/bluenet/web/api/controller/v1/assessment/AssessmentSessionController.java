package com.bluenet.web.api.controller.v1.assessment;

import com.bluenet.web.api.dto.ResponseMessage;
import com.bluenet.web.api.dto.assessment_session.AssessmentSessionDTO;
import com.bluenet.web.api.converter.assessment_session.AssessmentSessionRequestConverter;
import com.bluenet.web.api.converter.assessment_session.AssessmentSessionResponseConverter;
import com.bluenet.web.application.result.assessment.AssessmentSessionResult;
import com.bluenet.web.application.command.assessment_session.AssessmentSessionCommands;
import com.bluenet.web.application.service.AssessmentSessionAppService;
import com.bluenet.web.infrastructure.security.annotation.AccessLevel;
import com.bluenet.web.infrastructure.security.annotation.RequiresPermission;
import com.bluenet.web.infrastructure.security.util.UserCTX;
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
 * 考核会话控制器
 * <p>
 * 提供已登录用户查询/创建考核会话接口，用于获取限时考核的截止时间
 * </p>
 */
@Tag(name = "考核会话", description = "考核会话接口，用于获取限时考核截止时间")
@RestController
@RequestMapping("/api/v1/assessment-sessions")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearer-jwt")
public class AssessmentSessionController {

    private final AssessmentSessionAppService assessmentSessionAppService;
    private final AssessmentSessionRequestConverter assessmentSessionRequestConverter;
    private final AssessmentSessionResponseConverter assessmentSessionResponseConverter;

    @Operation(summary = "获取考核会话", description = "获取或创建当前用户对指定考核时间的会话，返回截止时间等信息。限时考核首次调用会自动创建会话。")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "查询成功", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AssessmentSessionDTO.class))),
            @ApiResponse(responseCode = "404", description = "考核时间不存在", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseMessage.class)))
    })
    @RequiresPermission(name = "查询考核会话", value = "assessment-session:query", access = AccessLevel.AUTHENTICATED)
    @GetMapping("/{assessmentTimeId}")
    public ResponseMessage<AssessmentSessionDTO> getSession(
            @Parameter(description = "考核时间ID", required = true) @PathVariable Long assessmentTimeId) {
        Long userId = UserCTX.getCurrentUserId();
        AssessmentSessionCommands.GetOrCreateSessionCommand command = assessmentSessionRequestConverter
                .toCommand(userId, assessmentTimeId);
        AssessmentSessionResult result = assessmentSessionAppService.getOrCreateSession(command);
        return ResponseMessage.success(assessmentSessionResponseConverter.toDTO(result));
    }
}
