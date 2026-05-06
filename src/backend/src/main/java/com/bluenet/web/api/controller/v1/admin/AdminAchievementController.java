package com.bluenet.web.api.controller.v1.admin;

import com.bluenet.web.api.dto.ResponseMessage;
import com.bluenet.web.api.dto.achievement.AchievementDTO;
import com.bluenet.web.api.dto.achievement.CreateAchievementRequestDTO;
import com.bluenet.web.api.dto.achievement.UpdateAchievementRequestDTO;
import com.bluenet.web.api.converter.achievement.AchievementRequestConverter;
import com.bluenet.web.api.converter.achievement.AchievementResponseConverter;
import com.bluenet.web.application.AchievementResult;
import com.bluenet.web.application.service.AchievementAppService;
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

@Tag(name = "成就管理", description = "成就管理接口，需要超级管理员权限")
@RestController
@RequestMapping("/api/v1/admin/achievements")
@SecurityRequirement(name = "bearer-jwt")
@RequiredArgsConstructor
public class AdminAchievementController {
    private final AchievementAppService achievementAppService;
    private final AchievementRequestConverter achievementRequestConverter;
    private final AchievementResponseConverter achievementResponseConverter;

    @Operation(summary = "创建成就", description = "创建新的成就")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "创建成功", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseMessage.class))),
            @ApiResponse(responseCode = "400", description = "参数错误", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseMessage.class))) })
    @RequiresPermission(name = "创建成就", value = "achievement:create", access = AccessLevel.PROTECTED)
    @PostMapping
    public ResponseMessage<AchievementDTO> createAchievement(
            @Valid @RequestBody CreateAchievementRequestDTO request) {
        AchievementResult result = achievementAppService.createAchievement(
                achievementRequestConverter.toCommand(request));
        return ResponseMessage.success(achievementResponseConverter.toDTO(result));
    }

    @Operation(summary = "更新成就", description = "更新成就信息")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "更新成功", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseMessage.class))),
            @ApiResponse(responseCode = "404", description = "成就不存在", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseMessage.class))) })
    @RequiresPermission(name = "更新成就", value = "achievement:update", access = AccessLevel.PROTECTED)
    @PutMapping("/{id}")
    public ResponseMessage<AchievementDTO> updateAchievement(
            @Parameter(description = "成就ID", required = true) @PathVariable Long id,
            @Valid @RequestBody UpdateAchievementRequestDTO request) {
        AchievementResult result = achievementAppService.updateAchievement(
                achievementRequestConverter.toCommand(id, request));
        return ResponseMessage.success(achievementResponseConverter.toDTO(result));
    }

    @Operation(summary = "删除成就", description = "删除成就")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "删除成功", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseMessage.class))),
            @ApiResponse(responseCode = "404", description = "成就不存在", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseMessage.class))) })
    @RequiresPermission(name = "删除成就", value = "achievement:delete", access = AccessLevel.PROTECTED)
    @DeleteMapping("/{id}")
    public ResponseMessage<Void> deleteAchievement(
            @Parameter(description = "成就ID", required = true) @PathVariable Long id) {
        achievementAppService.deleteAchievement(id);
        return ResponseMessage.success(null);
    }
}
