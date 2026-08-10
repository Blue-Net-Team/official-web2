package com.bluenet.web.api.controller.v1.admin;

import com.bluenet.web.api.dto.ResponseMessage;
import com.bluenet.web.api.dto.college.CollegeDTO;
import com.bluenet.web.api.dto.college.CreateCollegeRequestDTO;
import com.bluenet.web.api.dto.college.ResponseMessageCollege;
import com.bluenet.web.api.dto.college.UpdateCollegeRequestDTO;
import com.bluenet.web.api.converter.college.CollegeRequestConverter;
import com.bluenet.web.application.result.college.CollegeResult;
import com.bluenet.web.application.command.college.CollegeCommands;
import com.bluenet.web.api.converter.college.CollegeResponseConverter;
import com.bluenet.web.application.service.CollegeAppService;
import com.bluenet.web.infrastructure.security.annotation.AccessLevel;
import com.bluenet.web.infrastructure.security.annotation.RequiresPermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 学院管理控制器
 * <p>
 * 提供学院管理接口，需要管理员权限
 * </p>
 */
@Tag(name = "学院管理", description = "学院管理接口，需要管理员权限")
@RestController
@RequestMapping("/api/v1/admin/colleges")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearer-jwt")
public class AdminCollegeController {
    private final CollegeAppService collegeAppService;
    private final CollegeRequestConverter collegeRequestConverter;
    private final CollegeResponseConverter collegeResponseConverter;

    @Operation(summary = "创建学院", description = "创建新的学院")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "创建成功", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseMessageCollege.class))),
            @ApiResponse(responseCode = "400", description = "参数错误或学院名称已存在", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseMessage.class), examples = @ExampleObject(value = "{\"code\":400,\"msg\":\"学院名称已存在\",\"data\":null}"))) })
    @RequiresPermission(name = "创建学院", value = "college:create", access = AccessLevel.PROTECTED)
    @PostMapping
    public ResponseMessage<CollegeDTO> createCollege(
            @Valid @RequestBody CreateCollegeRequestDTO request) {
        CollegeCommands.CreateCollegeCommand command = collegeRequestConverter.toCommand(request);
        CollegeResult result = collegeAppService.createCollege(command);
        return ResponseMessage.success(collegeResponseConverter.toDTO(result));
    }

    @Operation(summary = "更新学院", description = "更新学院信息")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "更新成功", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseMessageCollege.class))),
            @ApiResponse(responseCode = "400", description = "参数错误或学院名称已存在", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseMessage.class), examples = @ExampleObject(value = "{\"code\":400,\"msg\":\"学院名称已存在\",\"data\":null}"))),
            @ApiResponse(responseCode = "404", description = "学院不存在", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseMessage.class), examples = @ExampleObject(value = "{\"code\":404,\"msg\":\"学院不存在\",\"data\":null}"))) })
    @RequiresPermission(name = "更新学院", value = "college:update", access = AccessLevel.PROTECTED)
    @PutMapping("/{id}")
    public ResponseMessage<CollegeDTO> updateCollege(
            @Parameter(description = "学院ID", required = true) @PathVariable Long id,
            @Valid @RequestBody UpdateCollegeRequestDTO request) {
        CollegeCommands.UpdateCollegeCommand command = collegeRequestConverter.toCommand(id, request);
        CollegeResult result = collegeAppService.updateCollege(command);
        return ResponseMessage.success(collegeResponseConverter.toDTO(result));
    }

    @Operation(summary = "删除学院", description = "删除学院（如果存在关联用户或报名记录则无法删除）")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "删除成功", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseMessage.class))),
            @ApiResponse(responseCode = "400", description = "存在关联数据无法删除", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseMessage.class), examples = @ExampleObject(value = "{\"code\":400,\"msg\":\"该学院下存在关联用户，无法删除\",\"data\":null}"))),
            @ApiResponse(responseCode = "404", description = "学院不存在", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseMessage.class))) })
    @RequiresPermission(name = "删除学院", value = "college:delete", access = AccessLevel.PROTECTED)
    @DeleteMapping("/{id}")
    public ResponseMessage<Void> deleteCollege(
            @Parameter(description = "学院ID", required = true) @PathVariable Long id) {
        collegeAppService.deleteCollege(id);
        return ResponseMessage.success(null);
    }
}
