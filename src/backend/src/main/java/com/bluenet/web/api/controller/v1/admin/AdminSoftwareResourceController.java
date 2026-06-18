package com.bluenet.web.api.controller.v1.admin;

import com.bluenet.web.api.dto.PageDTO;
import com.bluenet.web.api.dto.ResponseMessage;
import com.bluenet.web.api.dto.softwareresource.CreateSoftwareResourceRequestDTO;
import com.bluenet.web.api.dto.softwareresource.SoftwareResourceDTO;
import com.bluenet.web.api.dto.softwareresource.UpdateSoftwareResourceRequestDTO;
import com.bluenet.web.api.converter.softwareresource.SoftwareResourceRequestConverter;
import com.bluenet.web.api.converter.softwareresource.SoftwareResourceResponseConverter;
import com.bluenet.web.application.SoftwareResourceResult;
import com.bluenet.web.application.command.softwareresource.SoftwareResourceCommands;
import com.bluenet.web.application.service.SoftwareResourceAppService;
import com.bluenet.web.infrastructure.security.annotation.AccessLevel;
import com.bluenet.web.infrastructure.security.annotation.RequiresPermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 软件资源管理接口。
 */
@Tag(name = "软件资源管理", description = "软件资源管理接口，需要成员及以上权限")
@RestController
@RequestMapping("/api/v1/admin/software-resources")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearer-jwt")
public class AdminSoftwareResourceController {

    private final SoftwareResourceAppService softwareResourceAppService;
    private final SoftwareResourceRequestConverter requestConverter;
    private final SoftwareResourceResponseConverter responseConverter;

    @Operation(summary = "查询软件资源列表", description = "分页查询所有软件资源")
    @RequiresPermission(value = "software-resource:admin-list", name = "查询软件资源列表", access = AccessLevel.PROTECTED)
    @GetMapping
    public ResponseMessage<PageDTO<SoftwareResourceDTO>> listSoftwareResources(Pageable pageable) {
        Page<SoftwareResourceResult> resultPage = softwareResourceAppService.listAllForAdmin(pageable);
        return ResponseMessage.success(responseConverter.toPageDTO(resultPage));
    }

    @Operation(summary = "创建软件资源", description = "创建新的软件资源")
    @RequiresPermission(value = "software-resource:create", name = "创建软件资源", access = AccessLevel.PROTECTED)
    @PostMapping
    public ResponseMessage<SoftwareResourceDTO> createSoftwareResource(
            @Valid @RequestBody CreateSoftwareResourceRequestDTO request) {
        SoftwareResourceCommands.CreateSoftwareResourceCommand command = requestConverter.toCreateCommand(request);
        SoftwareResourceResult result = softwareResourceAppService.createSoftwareResource(command);
        return ResponseMessage.success(responseConverter.toDTO(result));
    }

    @Operation(summary = "更新软件资源", description = "更新已有软件资源")
    @RequiresPermission(value = "software-resource:update", name = "更新软件资源", access = AccessLevel.PROTECTED)
    @PutMapping("/{id}")
    public ResponseMessage<SoftwareResourceDTO> updateSoftwareResource(
            @Parameter(description = "资源ID", required = true) @PathVariable Long id,
            @Valid @RequestBody UpdateSoftwareResourceRequestDTO request) {
        SoftwareResourceCommands.UpdateSoftwareResourceCommand command = requestConverter.toUpdateCommand(id, request);
        SoftwareResourceResult result = softwareResourceAppService.updateSoftwareResource(command);
        return ResponseMessage.success(responseConverter.toDTO(result));
    }

    @Operation(summary = "删除软件资源", description = "删除软件资源")
    @RequiresPermission(value = "software-resource:delete", name = "删除软件资源", access = AccessLevel.PROTECTED)
    @DeleteMapping("/{id}")
    public ResponseMessage<Void> deleteSoftwareResource(
            @Parameter(description = "资源ID", required = true) @PathVariable Long id) {
        softwareResourceAppService.deleteSoftwareResource(id);
        return ResponseMessage.success(null);
    }
}
