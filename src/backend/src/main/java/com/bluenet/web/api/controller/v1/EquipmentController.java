package com.bluenet.web.api.controller.v1;

import com.bluenet.web.api.dto.ResponseMessage;
import com.bluenet.web.api.dto.equipment.EquipmentDTO;
import com.bluenet.web.application.EquipmentResult;
import com.bluenet.web.api.converter.equipment.EquipmentResponseConverter;
import com.bluenet.web.application.service.EquipmentAppService;
import com.bluenet.web.infrastructure.security.annotation.AccessLevel;
import com.bluenet.web.infrastructure.security.annotation.RequiresPermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 设备公开接口
 * <p>
 * 提供设备列表查询功能，无需认证
 * </p>
 */
@Tag(name = "设备", description = "设备相关接口，公开访问")
@RestController
@RequestMapping("/api/v1/equipments")
@RequiredArgsConstructor
public class EquipmentController {
    private final EquipmentAppService equipmentAppService;
    private final EquipmentResponseConverter equipmentResponseConverter;

    @Operation(summary = "获取设备列表", description = "获取所有设备列表，按排序权重降序排列")
    @RequiresPermission(name = "获取设备列表", value = "equipment:list", access = AccessLevel.PUBLIC)
    @GetMapping
    public ResponseMessage<List<EquipmentDTO>> getEquipmentList() {
        List<EquipmentResult> results = equipmentAppService.getAllEquipments();
        return ResponseMessage.success(equipmentResponseConverter.toDTOList(results));
    }

    @Operation(summary = "获取设备详情", description = "根据ID获取设备详情")
    @RequiresPermission(name = "获取设备详情", value = "equipment:detail", access = AccessLevel.PUBLIC)
    @GetMapping("/{id}")
    public ResponseMessage<EquipmentDTO> getEquipmentById(@PathVariable Long id) {
        EquipmentResult result = equipmentAppService.getEquipmentDetail(id);
        return ResponseMessage.success(equipmentResponseConverter.toDTO(result));
    }
}
