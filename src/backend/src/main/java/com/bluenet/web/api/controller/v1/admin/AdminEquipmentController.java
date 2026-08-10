package com.bluenet.web.api.controller.v1.admin;

import com.bluenet.web.api.dto.ResponseMessage;
import com.bluenet.web.api.dto.equipment.CreateEquipmentRequestDTO;
import com.bluenet.web.api.dto.equipment.EquipmentDTO;
import com.bluenet.web.api.dto.equipment.UpdateEquipmentRequestDTO;
import com.bluenet.web.api.converter.equipment.EquipmentRequestConverter;
import com.bluenet.web.application.result.equipment.EquipmentResult;
import com.bluenet.web.application.command.equipment.EquipmentCommands;
import com.bluenet.web.api.converter.equipment.EquipmentResponseConverter;
import com.bluenet.web.application.service.EquipmentAppService;
import com.bluenet.web.infrastructure.security.annotation.AccessLevel;
import com.bluenet.web.infrastructure.security.annotation.RequiresPermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 设备管理接口
 * <p>
 * 提供设备的增删改查管理功能，需要管理员权限
 * </p>
 */
@Tag(name = "设备管理", description = "设备管理接口，需要管理员权限")
@RestController
@RequestMapping("/api/v1/admin/equipments")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearer-jwt")
public class AdminEquipmentController {
    private final EquipmentAppService equipmentAppService;
    private final EquipmentRequestConverter equipmentRequestConverter;
    private final EquipmentResponseConverter equipmentResponseConverter;

    @Operation(summary = "创建设备", description = "创建新的设备")
    @RequiresPermission(name = "创建设备", value = "equipment:create", access = AccessLevel.PROTECTED)
    @PostMapping
    public ResponseMessage<EquipmentDTO> createEquipment(@Valid @RequestBody CreateEquipmentRequestDTO request) {
        EquipmentCommands.CreateEquipmentCommand command = equipmentRequestConverter.toCommand(request);
        EquipmentResult result = equipmentAppService.createEquipment(command);
        return ResponseMessage.success(equipmentResponseConverter.toDTO(result));
    }

    @Operation(summary = "更新设备", description = "更新设备信息")
    @RequiresPermission(name = "更新设备", value = "equipment:update", access = AccessLevel.PROTECTED)
    @PutMapping("/{id}")
    public ResponseMessage<EquipmentDTO> updateEquipment(
            @Parameter(description = "设备ID", required = true) @PathVariable Long id,
            @Valid @RequestBody UpdateEquipmentRequestDTO request) {
        EquipmentCommands.UpdateEquipmentCommand command = equipmentRequestConverter.toCommand(id, request);
        EquipmentResult result = equipmentAppService.updateEquipment(command);
        return ResponseMessage.success(equipmentResponseConverter.toDTO(result));
    }

    @Operation(summary = "删除设备", description = "删除设备")
    @RequiresPermission(name = "删除设备", value = "equipment:delete", access = AccessLevel.PROTECTED)
    @DeleteMapping("/{id}")
    public ResponseMessage<Void> deleteEquipment(
            @Parameter(description = "设备ID", required = true) @PathVariable Long id) {
        equipmentAppService.deleteEquipment(id);
        return ResponseMessage.success(null);
    }

    @Operation(summary = "更新设备图片", description = "更新设备图片")
    @RequiresPermission(name = "更新设备图片", value = "equipment:update-image", access = AccessLevel.PROTECTED)
    @PutMapping("/{id}/image")
    public ResponseMessage<Void> updateEquipmentImage(
            @Parameter(description = "设备ID", required = true) @PathVariable Long id,
            @Parameter(description = "图片文件ID", required = true) @RequestParam Long imageFileId) {
        equipmentAppService.updateEquipmentImage(id, imageFileId);
        return ResponseMessage.success(null);
    }
}
