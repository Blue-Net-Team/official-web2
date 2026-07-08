package com.bluenet.web.api.controller.v1.admin;

import com.bluenet.web.api.dto.ResponseMessage;
import com.bluenet.web.api.dto.learningpath.CreateLearningStepRequestDTO;
import com.bluenet.web.api.dto.learningpath.LearningStepDTO;
import com.bluenet.web.api.dto.learningpath.UpdateLearningStepRequestDTO;
import com.bluenet.web.api.converter.learningpath.LearningPathRequestConverter;
import com.bluenet.web.application.result.learningpath.LearningPathResult;
import com.bluenet.web.application.command.learningpath.LearningPathCommands;
import com.bluenet.web.api.converter.learningpath.LearningPathResponseConverter;
import com.bluenet.web.application.service.LearningPathAppService;
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
 * 学习路径管理接口Controller
 * <p>
 * 提供学习路径的管理接口，需要管理员权限
 * </p>
 */
@Tag(name = "学习路径管理", description = "学习路径管理接口，需要管理员权限")
@RestController
@RequestMapping("/api/v1/admin/directions")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearer-jwt")
public class AdminLearningPathController {
    private final LearningPathAppService learningPathAppService;
    private final LearningPathRequestConverter learningPathRequestConverter;
    private final LearningPathResponseConverter learningPathResponseConverter;

    @Operation(summary = "创建学习步骤", description = "为指定方向创建新的学习步骤")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "创建成功", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseMessage.class))),
            @ApiResponse(responseCode = "400", description = "参数错误或步骤序号已存在", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseMessage.class), examples = @ExampleObject(value = "{\"code\":400,\"msg\":\"该方向的步骤序号已存在\",\"data\":null}"))),
            @ApiResponse(responseCode = "404", description = "方向标识无效", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseMessage.class), examples = @ExampleObject(value = "{\"code\":404,\"msg\":\"无效的方向标识: invalid\",\"data\":null}")))
    })
    @RequiresPermission(name = "创建学习步骤", value = "direction-learning-path:create", access = AccessLevel.PROTECTED)
    @PostMapping("/{slug}/learning-steps")
    public ResponseMessage<LearningStepDTO> createStep(
            @Parameter(description = "方向标识（cv/embed/struct）", required = true, example = "cv") @PathVariable String slug,
            @Valid @RequestBody CreateLearningStepRequestDTO request) {
        LearningPathCommands.CreateLearningStepCommand command = learningPathRequestConverter
                .toCommand(slug, request);
        LearningPathResult result = learningPathAppService.createStep(command);
        return ResponseMessage.success(learningPathResponseConverter.toDTO(result));
    }

    @Operation(summary = "更新学习步骤", description = "更新指定学习步骤的信息")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "更新成功", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseMessage.class))),
            @ApiResponse(responseCode = "400", description = "参数错误或步骤序号冲突", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseMessage.class), examples = @ExampleObject(value = "{\"code\":400,\"msg\":\"该方向的步骤序号已存在\",\"data\":null}"))),
            @ApiResponse(responseCode = "404", description = "学习步骤不存在", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseMessage.class), examples = @ExampleObject(value = "{\"code\":404,\"msg\":\"学习步骤不存在\",\"data\":null}")))
    })
    @RequiresPermission(name = "更新学习步骤", value = "direction-learning-path:update", access = AccessLevel.PROTECTED)
    @PutMapping("/learning-steps/{id}")
    public ResponseMessage<LearningStepDTO> updateStep(
            @Parameter(description = "步骤ID", required = true, example = "1") @PathVariable Long id,
            @Valid @RequestBody UpdateLearningStepRequestDTO request) {
        LearningPathCommands.UpdateLearningStepCommand command = learningPathRequestConverter
                .toCommand(id, request);
        LearningPathResult result = learningPathAppService.updateStep(command);
        return ResponseMessage.success(learningPathResponseConverter.toDTO(result));
    }

    @Operation(summary = "删除学习步骤", description = "删除指定的学习步骤")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "删除成功", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseMessage.class))),
            @ApiResponse(responseCode = "404", description = "学习步骤不存在", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseMessage.class), examples = @ExampleObject(value = "{\"code\":404,\"msg\":\"学习步骤不存在\",\"data\":null}")))
    })
    @RequiresPermission(name = "删除学习步骤", value = "direction-learning-path:delete", access = AccessLevel.PROTECTED)
    @DeleteMapping("/learning-steps/{id}")
    public ResponseMessage<Void> deleteStep(
            @Parameter(description = "步骤ID", required = true, example = "1") @PathVariable Long id) {
        learningPathAppService.deleteStep(id);
        return ResponseMessage.success(null);
    }
}
