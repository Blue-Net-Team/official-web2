package com.bluenet.web.api.controller.v1.admin;

import com.bluenet.web.api.dto.PageDTO;
import com.bluenet.web.api.dto.ResponseMessage;
import com.bluenet.web.api.dto.permission.*;
import com.bluenet.web.api.converter.permission.PermissionRequestConverter;
import com.bluenet.web.api.converter.rolepermission.RolePermissionManageRequestConverter;
import com.bluenet.web.application.PermissionResult;
import com.bluenet.web.application.RolePermissionManageResult;
import com.bluenet.web.api.converter.permission.PermissionResponseConverter;
import com.bluenet.web.application.service.PermissionAppService;
import com.bluenet.web.application.service.RolePermissionManageAppService;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "权限管理", description = "权限管理接口，仅超级管理员可访问")
@RestController
@RequestMapping("/api/v1/admin/permissions")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearer-jwt")
public class AdminPermissionController {
    private final PermissionAppService permissionAppService;
    private final RolePermissionManageAppService rolePermissionManageAppService;
    private final PermissionRequestConverter permissionRequestConverter;
    private final RolePermissionManageRequestConverter rolePermissionManageRequestConverter;
    private final PermissionResponseConverter permissionResponseConverter;

    @Operation(summary = "分页查询权限列表", description = "分页查询权限列表，支持关键词搜索和格式筛选")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "查询成功", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseMessage.class)))
    })
    @RequiresPermission(name = "查询权限列表", value = "permission:list", access = AccessLevel.PROTECTED)
    @GetMapping
    public ResponseMessage<PageDTO<PermissionDTO>> getPermissions(@Valid PermissionQueryDTO query) {
        Page<PermissionResult> resultPage = permissionAppService
                .getPermissions(permissionRequestConverter.toCommand(query));
        List<PermissionDTO> dtoList = resultPage.getContent()
                .stream()
                .map(permissionResponseConverter::toDTO)
                .toList();
        Page<PermissionDTO> dtoPage = new PageImpl<>(dtoList, resultPage.getPageable(), resultPage.getTotalElements());
        return ResponseMessage.success(PageDTO.from(dtoPage));
    }

    @Operation(summary = "获取权限详情", description = "根据权限ID获取权限详情，包括已分配的角色")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "查询成功", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseMessage.class))),
            @ApiResponse(responseCode = "404", description = "权限不存在", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseMessage.class)))
    })
    @RequiresPermission(name = "获取权限详情", value = "permission:detail", access = AccessLevel.PROTECTED)
    @GetMapping("/{id}")
    public ResponseMessage<PermissionDTO> getPermissionDetail(
            @Parameter(description = "权限ID", required = true) @PathVariable Long id) {
        PermissionResult result = permissionAppService.getPermissionDetail(id);
        return ResponseMessage.success(permissionResponseConverter.toDTO(result));
    }

    @Operation(summary = "获取权限树", description = "获取权限的树形结构，用于前端展示")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "查询成功", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseMessage.class)))
    })
    @RequiresPermission(name = "获取权限树", value = "permission:tree", access = AccessLevel.PROTECTED)
    @GetMapping("/tree")
    public ResponseMessage<List<PermissionTreeDTO>> getPermissionTree() {
        List<PermissionResult> results = permissionAppService.getPermissionTree();
        return ResponseMessage.success(permissionResponseConverter.buildPermissionTree(results));
    }

    @Operation(summary = "获取权限对应的角色列表", description = "获取拥有指定权限的所有角色名称列表")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "查询成功"),
            @ApiResponse(responseCode = "404", description = "权限不存在")
    })
    @RequiresPermission(name = "查询权限角色", value = "permission:role:list", access = AccessLevel.PROTECTED)
    @GetMapping("/{id}/roles")
    public ResponseMessage<List<String>> getPermissionRoles(
            @Parameter(description = "权限ID", required = true) @PathVariable Long id) {
        List<String> roles = rolePermissionManageAppService.getPermissionRoles(id);
        return ResponseMessage.success(roles);
    }

    @Operation(summary = "批量添加角色到权限", description = "为指定权限批量添加角色，自动跳过已存在的关联")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "添加成功"),
            @ApiResponse(responseCode = "404", description = "权限或角色不存在")
    })
    @RequiresPermission(name = "添加权限角色", value = "permission:role:assign", access = AccessLevel.PROTECTED)
    @PostMapping("/{id}/roles/batch")
    public ResponseMessage<PermissionRoleResponseDTO> assignRolesToPermission(
            @Parameter(description = "权限ID", required = true) @PathVariable Long id,
            @Valid @RequestBody PermissionRoleBatchRequestDTO request) {
        RolePermissionManageResult result = rolePermissionManageAppService.assignRolesToPermission(
                rolePermissionManageRequestConverter.toAssignRolesCommand(id, request));
        return ResponseMessage.success(
                PermissionRoleResponseDTO.builder()
                        .successCount(result.successCount())
                        .currentRoles(result.currentRoles())
                        .build());
    }

    @Operation(summary = "批量从权限移除角色", description = "从指定权限批量移除角色")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "移除成功"),
            @ApiResponse(responseCode = "404", description = "权限或角色不存在")
    })
    @RequiresPermission(name = "移除权限角色", value = "permission:role:remove", access = AccessLevel.PROTECTED)
    @DeleteMapping("/{id}/roles/batch")
    public ResponseMessage<PermissionRoleResponseDTO> removeRolesFromPermission(
            @Parameter(description = "权限ID", required = true) @PathVariable Long id,
            @Valid @RequestBody PermissionRoleBatchRequestDTO request) {
        RolePermissionManageResult result = rolePermissionManageAppService.removeRolesFromPermission(
                rolePermissionManageRequestConverter.toRemoveRolesCommand(id, request));
        return ResponseMessage.success(
                PermissionRoleResponseDTO.builder()
                        .successCount(result.successCount())
                        .currentRoles(result.currentRoles())
                        .build());
    }
}
