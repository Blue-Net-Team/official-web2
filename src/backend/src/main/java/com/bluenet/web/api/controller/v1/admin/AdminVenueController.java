package com.bluenet.web.api.controller.v1.admin;

import com.bluenet.web.api.dto.ResponseMessage;
import com.bluenet.web.api.dto.venue.CreateVenueRequestDTO;
import com.bluenet.web.api.dto.venue.UpdateVenueRequestDTO;
import com.bluenet.web.api.dto.venue.VenueDTO;
import com.bluenet.web.api.converter.venue.VenueRequestConverter;
import com.bluenet.web.application.result.venue.VenueResult;
import com.bluenet.web.application.command.venue.VenueCommands;
import com.bluenet.web.api.converter.venue.VenueResponseConverter;
import com.bluenet.web.application.service.VenueAppService;
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
 * 场地管理接口
 * <p>
 * 提供场地的增删改查管理功能，需要管理员权限
 * </p>
 */
@Tag(name = "场地管理", description = "场地管理接口，需要管理员权限")
@RestController
@RequestMapping("/api/v1/admin/venues")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearer-jwt")
public class AdminVenueController {
    private final VenueAppService venueAppService;
    private final VenueRequestConverter venueRequestConverter;
    private final VenueResponseConverter venueResponseConverter;

    @Operation(summary = "创建场地", description = "创建新的场地")
    @RequiresPermission(name = "创建场地", value = "venue:create", access = AccessLevel.PROTECTED)
    @PostMapping
    public ResponseMessage<VenueDTO> createVenue(@Valid @RequestBody CreateVenueRequestDTO request) {
        VenueCommands.CreateVenueCommand command = venueRequestConverter.toCommand(request);
        VenueResult result = venueAppService.createVenue(command);
        return ResponseMessage.success(venueResponseConverter.toDTO(result));
    }

    @Operation(summary = "更新场地", description = "更新场地信息")
    @RequiresPermission(name = "更新场地", value = "venue:update", access = AccessLevel.PROTECTED)
    @PutMapping("/{id}")
    public ResponseMessage<VenueDTO> updateVenue(
            @Parameter(description = "场地ID", required = true) @PathVariable Long id,
            @Valid @RequestBody UpdateVenueRequestDTO request) {
        VenueCommands.UpdateVenueCommand command = venueRequestConverter.toCommand(id, request);
        VenueResult result = venueAppService.updateVenue(command);
        return ResponseMessage.success(venueResponseConverter.toDTO(result));
    }

    @Operation(summary = "删除场地", description = "删除场地")
    @RequiresPermission(name = "删除场地", value = "venue:delete", access = AccessLevel.PROTECTED)
    @DeleteMapping("/{id}")
    public ResponseMessage<Void> deleteVenue(
            @Parameter(description = "场地ID", required = true) @PathVariable Long id) {
        venueAppService.deleteVenue(id);
        return ResponseMessage.success(null);
    }

    @Operation(summary = "更新场地图片", description = "更新场地图片")
    @RequiresPermission(name = "更新场地图片", value = "venue:update-image", access = AccessLevel.PROTECTED)
    @PutMapping("/{id}/image")
    public ResponseMessage<Void> updateVenueImage(
            @Parameter(description = "场地ID", required = true) @PathVariable Long id,
            @Parameter(description = "图片文件ID", required = true) @RequestParam Long imageFileId) {
        venueAppService.updateVenueImage(id, imageFileId);
        return ResponseMessage.success(null);
    }
}
