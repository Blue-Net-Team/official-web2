package com.bluenet.web.api.controller.v1.admin;

import com.bluenet.web.api.dto.ResponseMessage;
import com.bluenet.web.api.dto.permission.RolePermissionBatchRequestDTO;
import com.bluenet.web.api.dto.permission.RolePermissionResponseDTO;
import com.bluenet.web.api.converter.rolepermission.RolePermissionManageRequestConverter;
import com.bluenet.web.application.RolePermissionManageResult;
import com.bluenet.web.application.service.RolePermissionManageAppService;
import com.bluenet.web.infrastructure.security.annotation.AccessLevel;
import com.bluenet.web.infrastructure.security.annotation.RequiresPermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "角色权限管理", description = "角色权限分配管理接口，仅超级管理员可访问")
@RestController
@RequestMapping("/api/v1/admin/roles")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearer-jwt")
public class AdminRolePermissionController {

    private final RolePermissionManageAppService rolePermissionManageAppService;
    private final RolePermissionManageRequestConverter rolePermissionManageRequestConverter;

    @Operation(summary = "获取角色权限列表", description = "获取指定角色当前拥有的所有权限标识符列表")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "查询成功"),
            @ApiResponse(responseCode = "404", description = "角色不存在")
    })
    @RequiresPermission(name = "查询角色权限", value = "role:permission:list", access = AccessLevel.PROTECTED)
    @GetMapping("/{roleName}/permissions")
    public ResponseMessage<List<String>> getRolePermissions(
            @Parameter(description = "角色名称", required = true, example = "MEMBER") @PathVariable String roleName) {
        List<String> permissions = rolePermissionManageAppService.getRolePermissions(roleName);
        return ResponseMessage.success(permissions);
    }

    @Operation(summary = "批量分配权限给角色", description = "为指定角色批量添加权限，自动跳过已存在的分配")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "分配成功"),
            @ApiResponse(responseCode = "400", description = "参数错误或SUPER_ADMIN角色"),
            @ApiResponse(responseCode = "404", description = "角色或权限不存在")
    })
    @RequiresPermission(name = "分配角色权限", value = "role:permission:assign", access = AccessLevel.PROTECTED)
    @PostMapping("/{roleName}/permissions/batch")
    public ResponseMessage<RolePermissionResponseDTO> assignPermissionsToRole(
            @Parameter(description = "角色名称", required = true, example = "MEMBER") @PathVariable String roleName,
            @Valid @RequestBody RolePermissionBatchRequestDTO request) {
        RolePermissionManageResult result = rolePermissionManageAppService.assignPermissionsToRole(
                rolePermissionManageRequestConverter.toAssignPermissionsCommand(roleName, request));
        return ResponseMessage.success(
                RolePermissionResponseDTO.builder()
                        .successCount(result.successCount())
                        .currentPermissions(result.currentPermissions())
                        .build());
    }

    @Operation(summary = "批量移除角色权限", description = "从指定角色批量移除权限")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "移除成功"),
            @ApiResponse(responseCode = "400", description = "参数错误或SUPER_ADMIN角色"),
            @ApiResponse(responseCode = "404", description = "角色或权限不存在")
    })
    @RequiresPermission(name = "移除角色权限", value = "role:permission:remove", access = AccessLevel.PROTECTED)
    @DeleteMapping("/{roleName}/permissions/batch")
    public ResponseMessage<RolePermissionResponseDTO> removePermissionsFromRole(
            @Parameter(description = "角色名称", required = true, example = "MEMBER") @PathVariable String roleName,
            @Valid @RequestBody RolePermissionBatchRequestDTO request) {
        RolePermissionManageResult result = rolePermissionManageAppService.removePermissionsFromRole(
                rolePermissionManageRequestConverter.toRemovePermissionsCommand(roleName, request));
        return ResponseMessage.success(
                RolePermissionResponseDTO.builder()
                        .successCount(result.successCount())
                        .currentPermissions(result.currentPermissions())
                        .build());
    }
}
