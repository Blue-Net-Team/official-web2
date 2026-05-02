package com.bluenet.web.api.controller.v1.admin;

import com.bluenet.web.api.dto.PageDTO;
import com.bluenet.web.api.dto.ResponseMessage;
import com.bluenet.web.api.dto.adminuser.*;
import com.bluenet.web.api.converter.adminuser.AdminUserRequestConverter;
import com.bluenet.web.application.AdminUserResult;
import com.bluenet.web.application.converter.AdminUserAppConverter;
import com.bluenet.web.application.service.AdminUserAppService;
import com.bluenet.web.domain.exception.BadRequest;
import com.bluenet.web.domain.exception.DataNotFound;
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
 * 管理员用户管理控制器
 */
@Tag(name = "用户管理", description = "管理员用户管理接口，仅超级管理员可访问")
@RestController
@RequestMapping("/api/v1/admin/users")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearer-jwt")
public class AdminUserController {

    private final AdminUserAppService adminUserAppService;
    private final AdminUserRequestConverter requestConverter;
    private final AdminUserAppConverter appConverter;

    @Operation(summary = "分页查询用户列表", description = "支持按角色、方向、学院筛选，按学号/姓名搜索")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "查询成功", content = @Content(mediaType = "application/json", schema = @Schema(implementation = PageDTO.class))),
            @ApiResponse(responseCode = "403", description = "无权限", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseMessage.class)))
    })
    @RequiresPermission(name = "查询用户列表", value = "user:manage:list", access = AccessLevel.PROTECTED)
    @GetMapping
    public ResponseMessage<PageDTO<AdminUserListItemResponseDTO>> getUserList(
            @Valid AdminUserListQueryDTO query) {
        var page = adminUserAppService.getUserList(requestConverter.toCommand(query));
        return ResponseMessage.success(appConverter.toPageDTO(page));
    }

    @Operation(summary = "获取用户详情", description = "获取用户详细信息及关联数据统计")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "查询成功"),
            @ApiResponse(responseCode = "404", description = "用户不存在", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseMessage.class)))
    })
    @RequiresPermission(name = "查看用户详情", value = "user:manage:detail", access = AccessLevel.PROTECTED)
    @GetMapping("/{id}")
    public ResponseMessage<AdminUserDetailResponseDTO> getUserDetail(
            @Parameter(description = "用户ID", required = true) @PathVariable Long id) {
        try {
            AdminUserResult.Detail detail = adminUserAppService.getUserDetail(id);
            return ResponseMessage.success(appConverter.toDetailDTO(detail));
        } catch (DataNotFound e) {
            return ResponseMessage.error(404, e.getMessage());
        }
    }

    @Operation(summary = "更新用户信息", description = "更新用户角色、方向、禁用状态等字段")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "更新成功"),
            @ApiResponse(responseCode = "404", description = "用户不存在", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseMessage.class)))
    })
    @RequiresPermission(name = "更新用户信息", value = "user:manage:update", access = AccessLevel.PROTECTED)
    @PutMapping("/{id}")
    public ResponseMessage<Void> updateUser(
            @Parameter(description = "用户ID", required = true) @PathVariable Long id,
            @Valid @RequestBody AdminUserUpdateRequestDTO request) {
        try {
            adminUserAppService.updateUser(requestConverter.toCommand(id, request));
            return ResponseMessage.success();
        } catch (DataNotFound e) {
            return ResponseMessage.error(404, e.getMessage());
        } catch (BadRequest e) {
            return ResponseMessage.error(400, e.getMessage());
        }
    }

    @Operation(summary = "重置用户密码", description = "管理员直接重置用户密码，需二次确认")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "重置成功"),
            @ApiResponse(responseCode = "400", description = "密码不一致或参数错误", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseMessage.class))),
            @ApiResponse(responseCode = "404", description = "用户不存在", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseMessage.class)))
    })
    @RequiresPermission(name = "重置用户密码", value = "user:manage:reset-password", access = AccessLevel.PROTECTED)
    @PutMapping("/{id}/password")
    public ResponseMessage<Void> resetPassword(
            @Parameter(description = "用户ID", required = true) @PathVariable Long id,
            @Valid @RequestBody AdminUserResetPasswordRequestDTO request) {
        try {
            adminUserAppService.resetPassword(requestConverter.toCommand(id, request));
            return ResponseMessage.success();
        } catch (DataNotFound e) {
            return ResponseMessage.error(404, e.getMessage());
        } catch (IllegalArgumentException | BadRequest e) {
            return ResponseMessage.error(400, e.getMessage());
        }
    }

    @Operation(summary = "删除用户", description = "物理删除用户及关联数据")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "删除成功"),
            @ApiResponse(responseCode = "400", description = "不能删除超级管理员", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseMessage.class))),
            @ApiResponse(responseCode = "404", description = "用户不存在", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseMessage.class)))
    })
    @RequiresPermission(name = "删除用户", value = "user:manage:delete", access = AccessLevel.PROTECTED)
    @DeleteMapping("/{id}")
    public ResponseMessage<Void> deleteUser(
            @Parameter(description = "用户ID", required = true) @PathVariable Long id) {
        try {
            adminUserAppService.deleteUser(id);
            return ResponseMessage.success();
        } catch (DataNotFound e) {
            return ResponseMessage.error(404, e.getMessage());
        } catch (BadRequest e) {
            return ResponseMessage.error(400, e.getMessage());
        }
    }

    @Operation(summary = "批量删除用户", description = "批量物理删除用户及关联数据，最多50个")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "删除成功"),
            @ApiResponse(responseCode = "400", description = "参数错误或包含超级管理员", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseMessage.class)))
    })
    @RequiresPermission(name = "批量删除用户", value = "user:manage:batch-delete", access = AccessLevel.PROTECTED)
    @PostMapping("/batch-delete")
    public ResponseMessage<Void> batchDelete(
            @Valid @RequestBody AdminUserBatchOperateRequestDTO request) {
        try {
            adminUserAppService.batchDelete(requestConverter.toCommand(request));
            return ResponseMessage.success();
        } catch (BadRequest e) {
            return ResponseMessage.error(400, e.getMessage());
        }
    }

    @Operation(summary = "批量禁用用户", description = "批量禁用用户，最多50个")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "操作成功"),
            @ApiResponse(responseCode = "400", description = "参数错误或包含超级管理员", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseMessage.class)))
    })
    @RequiresPermission(name = "批量禁用用户", value = "user:manage:batch-disable", access = AccessLevel.PROTECTED)
    @PostMapping("/batch-disable")
    public ResponseMessage<Void> batchDisable(
            @Valid @RequestBody AdminUserBatchOperateRequestDTO request) {
        try {
            adminUserAppService.batchDisable(requestConverter.toCommand(request), true);
            return ResponseMessage.success();
        } catch (BadRequest e) {
            return ResponseMessage.error(400, e.getMessage());
        }
    }

    @Operation(summary = "批量启用用户", description = "批量启用用户，最多50个")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "操作成功"),
            @ApiResponse(responseCode = "400", description = "参数错误", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseMessage.class)))
    })
    @RequiresPermission(name = "批量启用用户", value = "user:manage:batch-enable", access = AccessLevel.PROTECTED)
    @PostMapping("/batch-enable")
    public ResponseMessage<Void> batchEnable(
            @Valid @RequestBody AdminUserBatchOperateRequestDTO request) {
        try {
            adminUserAppService.batchDisable(requestConverter.toCommand(request), false);
            return ResponseMessage.success();
        } catch (BadRequest e) {
            return ResponseMessage.error(400, e.getMessage());
        }
    }

    @Operation(summary = "批量更新用户角色", description = "批量修改用户角色，最多50个")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "操作成功"),
            @ApiResponse(responseCode = "400", description = "参数错误", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseMessage.class)))
    })
    @RequiresPermission(name = "批量更新用户角色", value = "user:manage:batch-update-role", access = AccessLevel.PROTECTED)
    @PostMapping("/batch-role")
    public ResponseMessage<Void> batchUpdateRole(
            @Valid @RequestBody AdminUserBatchUpdateRoleRequestDTO request) {
        try {
            adminUserAppService.batchUpdateRole(requestConverter.toCommand(request));
            return ResponseMessage.success();
        } catch (BadRequest e) {
            return ResponseMessage.error(400, e.getMessage());
        }
    }
}
