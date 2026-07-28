package com.bluenet.web.api.controller.v1.user;

import com.bluenet.web.api.dto.ResponseMessage;
import com.bluenet.web.api.dto.experience.CreateExperienceRequestDTO;
import com.bluenet.web.api.dto.experience.ExperienceDTO;
import com.bluenet.web.api.dto.experience.UpdateExperienceRequestDTO;
import com.bluenet.web.api.converter.userexperience.UserExperienceRequestConverter;
import com.bluenet.web.api.converter.userexperience.UserExperienceResponseConverter;
import com.bluenet.web.application.service.UserExperienceAppService;
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
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户经历接口
 */
@Tag(name = "用户经历", description = "用户经历管理接口（项目/实习）")
@RestController
@RequestMapping("/api/v1/user/experiences")
@RequiredArgsConstructor
class UserExperienceController {
    private final UserExperienceAppService userExperienceAppService;
    private final UserExperienceRequestConverter requestConverter;
    private final UserExperienceResponseConverter responseConverter;

    @Operation(summary = "获取经历列表", description = "返回当前登录用户的经历列表，可通过type参数过滤")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "成功获取经历列表"),
            @ApiResponse(responseCode = "401", description = "未登录或token无效", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseMessage.class)))
    })
    @SecurityRequirement(name = "bearer-jwt")
    @RequiresPermission(name = "获取用户经历", value = "user:experience:read", access = AccessLevel.AUTHENTICATED)
    @GetMapping
    public ResponseMessage<List<ExperienceDTO>> getExperiences(
            @Parameter(description = "经历类型：PROJECT/INTERNSHIP") @RequestParam(required = false) String type) {
        return ResponseMessage.success(responseConverter.toDTOList(userExperienceAppService.getExperiences(type)));
    }

    @Operation(summary = "创建经历", description = "创建新的经历记录（项目/实习）。需要MEMBER及以上角色权限。")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "成功创建经历"),
            @ApiResponse(responseCode = "401", description = "未登录或token无效", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseMessage.class))),
            @ApiResponse(responseCode = "403", description = "无权限（需要MEMBER及以上角色）", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseMessage.class))),
            @ApiResponse(responseCode = "400", description = "请求参数错误", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseMessage.class)))
    })
    @SecurityRequirement(name = "bearer-jwt")
    @RequiresPermission(name = "创建用户经历", value = "user:experience:create", access = AccessLevel.PROTECTED)
    @PostMapping
    public ResponseMessage<ExperienceDTO> createExperience(@RequestBody CreateExperienceRequestDTO request) {
        return ResponseMessage.success(
                responseConverter.toDTO(
                        userExperienceAppService.createExperience(requestConverter.toCommand(request))));
    }

    @Operation(summary = "更新经历", description = "更新指定的经历记录。需要MEMBER及以上角色权限。")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "成功更新经历"),
            @ApiResponse(responseCode = "401", description = "未登录或token无效", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseMessage.class))),
            @ApiResponse(responseCode = "403", description = "无权限（需要MEMBER及以上角色）", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseMessage.class))),
            @ApiResponse(responseCode = "404", description = "经历不存在或不属于当前用户", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseMessage.class)))
    })
    @SecurityRequirement(name = "bearer-jwt")
    @RequiresPermission(name = "更新用户经历", value = "user:experience:update", access = AccessLevel.PROTECTED)
    @PutMapping("/{id}")
    public ResponseMessage<ExperienceDTO> updateExperience(
            @Parameter(description = "经历ID") @PathVariable Long id,
            @RequestBody UpdateExperienceRequestDTO request) {
        return ResponseMessage.success(
                responseConverter.toDTO(
                        userExperienceAppService.updateExperience(requestConverter.toCommand(id, request))));
    }

    @Operation(summary = "删除经历", description = "删除指定的经历记录。需要MEMBER及以上角色权限。")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "成功删除经历"),
            @ApiResponse(responseCode = "401", description = "未登录或token无效", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseMessage.class))),
            @ApiResponse(responseCode = "403", description = "无权限（需要MEMBER及以上角色）", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseMessage.class))),
            @ApiResponse(responseCode = "404", description = "经历不存在或不属于当前用户", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseMessage.class)))
    })
    @SecurityRequirement(name = "bearer-jwt")
    @RequiresPermission(name = "删除用户经历", value = "user:experience:delete", access = AccessLevel.PROTECTED)
    @DeleteMapping("/{id}")
    public ResponseMessage<Void> deleteExperience(
            @Parameter(description = "经历ID") @PathVariable Long id) {
        userExperienceAppService.deleteExperience(id);
        return ResponseMessage.success();
    }
}
