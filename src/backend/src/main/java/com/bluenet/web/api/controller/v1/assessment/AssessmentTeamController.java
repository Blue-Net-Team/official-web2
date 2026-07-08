package com.bluenet.web.api.controller.v1.assessment;

import com.bluenet.web.api.converter.assessment_team.AssessmentTeamResponseConverter;
import com.bluenet.web.api.dto.ResponseMessage;
import com.bluenet.web.api.dto.assessment_team.*;
import com.bluenet.web.application.result.team.TeamPreviewResult;
import com.bluenet.web.application.result.team.TeamResult;
import com.bluenet.web.application.service.AssessmentTeamAppService;
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
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 考核队伍控制器
 * <p>
 * 提供考核队伍的创建、加入、查询、管理等接口
 * </p>
 */
@Tag(name = "考核队伍", description = "考核队伍管理接口，已登录用户可访问")
@RestController
@RequestMapping("/api/v1/assessment-teams")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearer-jwt")
public class AssessmentTeamController {

    private final AssessmentTeamAppService assessmentTeamAppService;
    private final AssessmentTeamResponseConverter responseConverter;

    @Operation(summary = "创建队伍", description = "为指定考核时间创建一个新的队伍，当前用户自动成为队长")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "创建成功", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AssessmentTeamDTO.class))),
            @ApiResponse(responseCode = "400", description = "该考核不允许组队、已加入队伍或已提交个人答案", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseMessage.class)))
    })
    @RequiresPermission(name = "创建队伍", value = "assessment-team:create", access = AccessLevel.AUTHENTICATED)
    @PostMapping
    public ResponseMessage<AssessmentTeamDTO> createTeam(
            @Valid @RequestBody CreateTeamRequestDTO request) {
        Long userId = UserCTX.getCurrentUserId();
        TeamResult result = assessmentTeamAppService
                .createTeam(userId, request.getAssessmentTimeId(), request.getName());
        return ResponseMessage.success(responseConverter.toDTO(result));
    }

    @Operation(summary = "预览队伍", description = "通过邀请码预览队伍信息，无需登录也可查看")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "查询成功", content = @Content(mediaType = "application/json", schema = @Schema(implementation = TeamPreviewResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "邀请码无效或考核已结束", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseMessage.class)))
    })
    @RequiresPermission(name = "预览队伍", value = "assessment-team:preview", access = AccessLevel.AUTHENTICATED)
    @PostMapping("/preview")
    public ResponseMessage<TeamPreviewResponseDTO> previewTeam(
            @Valid @RequestBody PreviewTeamRequestDTO request) {
        TeamPreviewResult result = assessmentTeamAppService.previewTeam(request.getInviteCode());
        return ResponseMessage.success(toPreviewDTO(result));
    }

    @Operation(summary = "加入队伍", description = "通过邀请码加入指定队伍")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "加入成功", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AssessmentTeamDTO.class))),
            @ApiResponse(responseCode = "400", description = "邀请码无效、已加入队伍、已提交个人答案或考核已结束", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseMessage.class)))
    })
    @RequiresPermission(name = "加入队伍", value = "assessment-team:join", access = AccessLevel.AUTHENTICATED)
    @PostMapping("/join")
    public ResponseMessage<AssessmentTeamDTO> joinTeam(
            @Valid @RequestBody JoinTeamRequestDTO request) {
        Long userId = UserCTX.getCurrentUserId();
        TeamResult result = assessmentTeamAppService.joinTeam(userId, request.getInviteCode());
        return ResponseMessage.success(responseConverter.toDTO(result));
    }

    @Operation(summary = "查询我的队伍", description = "查询当前用户在指定考核时间下的队伍信息")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "查询成功", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AssessmentTeamDTO.class))),
            @ApiResponse(responseCode = "404", description = "未加入队伍", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseMessage.class)))
    })
    @RequiresPermission(name = "查询我的队伍", value = "assessment-team:query-my-team", access = AccessLevel.AUTHENTICATED)
    @GetMapping("/my-team")
    public ResponseMessage<AssessmentTeamDTO> getMyTeam(
            @Parameter(description = "考核时间ID", required = true) @RequestParam Long assessmentTimeId) {
        Long userId = UserCTX.getCurrentUserId();
        TeamResult result = assessmentTeamAppService.getMyTeam(userId, assessmentTimeId);
        if (result == null) {
            return ResponseMessage.error(404, "未加入队伍");
        }
        return ResponseMessage.success(responseConverter.toDTO(result));
    }

    @Operation(summary = "离开队伍", description = "离开当前所在的队伍（队长不能离开）")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "离开成功"),
            @ApiResponse(responseCode = "400", description = "不是队伍成员", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseMessage.class))),
            @ApiResponse(responseCode = "403", description = "队长不能离开队伍", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseMessage.class)))
    })
    @RequiresPermission(name = "离开队伍", value = "assessment-team:leave", access = AccessLevel.AUTHENTICATED)
    @PostMapping("/leave")
    public ResponseMessage<Void> leaveTeam(
            @Valid @RequestBody LeaveTeamRequestDTO request) {
        Long userId = UserCTX.getCurrentUserId();
        assessmentTeamAppService.leaveTeam(userId, request.getTeamId());
        return ResponseMessage.success();
    }

    @Operation(summary = "转让队长", description = "将队长权限转让给队伍中的另一名成员")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "转让成功", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AssessmentTeamDTO.class))),
            @ApiResponse(responseCode = "400", description = "新队长不是队伍成员", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseMessage.class))),
            @ApiResponse(responseCode = "403", description = "只有队长可以转让队长", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseMessage.class)))
    })
    @RequiresPermission(name = "转让队长", value = "assessment-team:transfer", access = AccessLevel.AUTHENTICATED)
    @PostMapping("/transfer")
    public ResponseMessage<AssessmentTeamDTO> transferLeader(
            @Valid @RequestBody TransferLeaderRequestDTO request) {
        Long userId = UserCTX.getCurrentUserId();
        TeamResult result = assessmentTeamAppService
                .transferLeader(userId, request.getTeamId(), request.getNewLeaderId());
        return ResponseMessage.success(responseConverter.toDTO(result));
    }

    @Operation(summary = "解散队伍", description = "解散当前队伍（仅队长可操作）")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "解散成功"),
            @ApiResponse(responseCode = "403", description = "只有队长可以解散队伍", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseMessage.class)))
    })
    @RequiresPermission(name = "解散队伍", value = "assessment-team:disband", access = AccessLevel.AUTHENTICATED)
    @DeleteMapping("/{id}")
    public ResponseMessage<Void> disbandTeam(
            @Parameter(description = "队伍ID", required = true) @PathVariable Long id) {
        Long userId = UserCTX.getCurrentUserId();
        assessmentTeamAppService.disbandTeam(userId, id);
        return ResponseMessage.success();
    }

    private TeamPreviewResponseDTO toPreviewDTO(TeamPreviewResult result) {
        return TeamPreviewResponseDTO.builder()
                .id(result.id())
                .assessmentTimeId(result.assessmentTimeId())
                .leaderUsername(result.leaderUsername())
                .name(result.name())
                .status(result.status())
                .createdAt(result.createdAt())
                .memberCount(result.memberCount())
                .memberUsernames(result.memberUsernames())
                .build();
    }
}
