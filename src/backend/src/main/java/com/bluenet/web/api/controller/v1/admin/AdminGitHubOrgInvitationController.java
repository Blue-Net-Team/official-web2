package com.bluenet.web.api.controller.v1.admin;

import com.bluenet.web.api.dto.ResponseMessage;
import com.bluenet.web.api.dto.githuborg.GitHubOrgBatchInviteRequestDTO;
import com.bluenet.web.api.dto.githuborg.GitHubOrgBatchInviteResponseDTO;
import com.bluenet.web.api.dto.githuborg.GitHubOrgInviteDetailResponseDTO;
import com.bluenet.web.application.result.githuborg.GitHubOrgInvitationAdminResult;
import com.bluenet.web.application.service.GitHubOrgInvitationAdminAppService;
import com.bluenet.web.infrastructure.security.annotation.AccessLevel;
import com.bluenet.web.infrastructure.security.annotation.RequiresPermission;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * GitHub 组织邀请管理控制器（管理端）。
 * <p>
 * 提供管理员手动邀请用户加入 GitHub 组织的接口，支持单个邀请和批量邀请。
 * </p>
 */
@Tag(name = "GitHub 组织邀请管理", description = "管理员 GitHub 组织成员邀请接口")
@RestController
@RequestMapping("/api/v1/admin/github-org-invitations")
@RequiredArgsConstructor
public class AdminGitHubOrgInvitationController {

    private final GitHubOrgInvitationAdminAppService invitationAdminAppService;

    @Operation(summary = "邀请单个用户加入 GitHub 组织", description = "优先通过绑定的 GitHub ID 邀请，未绑定时回退到邮箱邀请")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "邀请完成（结果见 success/reason 字段）"),
            @ApiResponse(responseCode = "404", description = "用户不存在", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseMessage.class))),
            @ApiResponse(responseCode = "403", description = "无权限", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseMessage.class)))
    })
    @RequiresPermission(value = "github-org-invitation:invite", name = "邀请用户加入 GitHub 组织", access = AccessLevel.PROTECTED)
    @PostMapping("/users/{userId}")
    public ResponseMessage<GitHubOrgInviteDetailResponseDTO> inviteUser(
            @Parameter(description = "用户 ID", required = true) @PathVariable Long userId) {
        GitHubOrgInvitationAdminResult.Detail result = invitationAdminAppService.inviteUser(userId);
        return ResponseMessage.success(toDetailDTO(result));
    }

    @Operation(summary = "批量邀请用户加入 GitHub 组织", description = "一次最多邀请 50 个用户，逐个处理，单个失败不影响其他用户")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "批量邀请完成（结果见 details 字段）"),
            @ApiResponse(responseCode = "400", description = "请求参数非法", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseMessage.class))),
            @ApiResponse(responseCode = "403", description = "无权限", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseMessage.class)))
    })
    @RequiresPermission(value = "github-org-invitation:invite-batch", name = "批量邀请用户加入 GitHub 组织", access = AccessLevel.PROTECTED)
    @PostMapping("/batch")
    public ResponseMessage<GitHubOrgBatchInviteResponseDTO> inviteBatch(
            @Valid @RequestBody GitHubOrgBatchInviteRequestDTO request) {
        GitHubOrgInvitationAdminResult.Batch result = invitationAdminAppService
                .inviteBatch(request.getUserIds());
        List<GitHubOrgInviteDetailResponseDTO> details = result.details()
                .stream()
                .map(this::toDetailDTO)
                .toList();
        return ResponseMessage.success(
                GitHubOrgBatchInviteResponseDTO.builder()
                        .total(result.total())
                        .succeeded(result.succeeded())
                        .failed(result.failed())
                        .details(details)
                        .build());
    }

    private GitHubOrgInviteDetailResponseDTO toDetailDTO(GitHubOrgInvitationAdminResult.Detail detail) {
        return GitHubOrgInviteDetailResponseDTO.builder()
                .userId(detail.userId())
                .success(detail.success())
                .reason(detail.reason())
                .build();
    }
}
