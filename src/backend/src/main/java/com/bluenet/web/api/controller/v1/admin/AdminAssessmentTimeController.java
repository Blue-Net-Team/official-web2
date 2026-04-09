package com.bluenet.web.api.controller.v1.admin;

import com.bluenet.web.api.dto.PageDTO;
import com.bluenet.web.api.dto.ResponseMessage;
import com.bluenet.web.api.dto.assessment_time.AssessmentTimeDTO;
import com.bluenet.web.api.dto.assessment_time.CreateAssessmentTimeRequestDTO;
import com.bluenet.web.api.dto.assessment_time.ResponseMessageAssessmentTime;
import com.bluenet.web.api.dto.assessment_time.ResponseMessageAssessmentTimeList;
import com.bluenet.web.api.dto.assessment_time.UpdateAssessmentTimeRequestDTO;
import com.bluenet.web.application.service.AssessmentTimeService;
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
 * 考核时间管理控制器
 * <p>
 * 提供考核时间管理接口，需要管理员权限
 * </p>
 */
@Tag(name = "考核时间管理", description = "考核时间管理接口，需要管理员权限")
@RestController
@RequestMapping("/api/v1/admin/assessment-times")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearer-jwt")
public class AdminAssessmentTimeController {
    private final AssessmentTimeService assessmentTimeService;

    @Operation(summary = "创建考核时间", description = "创建新的考核时间配置")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "创建成功", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseMessageAssessmentTime.class))),
            @ApiResponse(responseCode = "400", description = "参数错误或方向届次年级组合已存在", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseMessage.class), examples = @ExampleObject(value = "{\"code\":400,\"msg\":\"该方向届次年级的考核时间已存在\",\"data\":null}")))
    })
    @RequiresPermission(name = "创建考核时间", value = "assessment-time:create", access = AccessLevel.PROTECTED)
    @PostMapping
    public ResponseMessage<AssessmentTimeDTO> createAssessmentTime(
            @Valid @RequestBody CreateAssessmentTimeRequestDTO request) {
        try {
            AssessmentTimeDTO created = assessmentTimeService.createAssessmentTime(request);
            return ResponseMessage.success(created);
        } catch (IllegalArgumentException e) {
            return ResponseMessage.error(400, e.getMessage());
        }
    }

    @Operation(summary = "更新考核时间", description = "更新考核时间配置")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "更新成功", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseMessageAssessmentTime.class))),
            @ApiResponse(responseCode = "400", description = "参数错误", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseMessage.class), examples = @ExampleObject(value = "{\"code\":400,\"msg\":\"已开始的考核不允许修改开始时间\",\"data\":null}"))),
            @ApiResponse(responseCode = "404", description = "考核时间不存在", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseMessage.class), examples = @ExampleObject(value = "{\"code\":404,\"msg\":\"考核时间不存在\",\"data\":null}")))
    })
    @RequiresPermission(name = "更新考核时间", value = "assessment-time:update", access = AccessLevel.PROTECTED)
    @PutMapping("/{id}")
    public ResponseMessage<AssessmentTimeDTO> updateAssessmentTime(
            @Parameter(description = "考核时间ID", required = true) @PathVariable Long id,
            @Valid @RequestBody UpdateAssessmentTimeRequestDTO request) {
        try {
            AssessmentTimeDTO updated = assessmentTimeService.updateAssessmentTime(id, request);
            return ResponseMessage.success(updated);
        } catch (IllegalArgumentException e) {
            if (e.getMessage().contains("不存在")) {
                return ResponseMessage.error(404, e.getMessage());
            }
            return ResponseMessage.error(400, e.getMessage());
        }
    }

    @Operation(summary = "删除考核时间", description = "删除考核时间（如果存在关联题目则无法删除）")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "删除成功", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseMessage.class))),
            @ApiResponse(responseCode = "404", description = "考核时间不存在", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseMessage.class))),
            @ApiResponse(responseCode = "409", description = "存在关联题目无法删除", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseMessage.class), examples = @ExampleObject(value = "{\"code\":409,\"msg\":\"存在关联的考核题目，需先删除相关题目\",\"data\":null}")))
    })
    @RequiresPermission(name = "删除考核时间", value = "assessment-time:delete", access = AccessLevel.PROTECTED)
    @DeleteMapping("/{id}")
    public ResponseMessage<Void> deleteAssessmentTime(
            @Parameter(description = "考核时间ID", required = true) @PathVariable Long id) {
        try {
            assessmentTimeService.deleteAssessmentTime(id);
            return ResponseMessage.success(null);
        } catch (IllegalArgumentException e) {
            return ResponseMessage.error(404, e.getMessage());
        }
    }

    @Operation(summary = "查询考核时间列表", description = "分页查询考核时间，根据当前用户角色过滤")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "查询成功", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseMessageAssessmentTimeList.class)))
    })
    @RequiresPermission(name = "查询考核时间列表", value = "assessment-time:list", access = AccessLevel.PROTECTED)
    @GetMapping
    public ResponseMessage<PageDTO<AssessmentTimeDTO>> listAssessmentTimes(
            @Parameter(description = "页码（从0开始，默认0）") @RequestParam(required = false, defaultValue = "0") Integer page,
            @Parameter(description = "每页大小（默认5）") @RequestParam(required = false, defaultValue = "5") Integer size) {
        PageDTO<AssessmentTimeDTO> result = assessmentTimeService.listAssessmentTimes(page, size);
        return ResponseMessage.success(result);
    }
}
