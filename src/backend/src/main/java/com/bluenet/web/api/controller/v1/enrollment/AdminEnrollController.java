package com.bluenet.web.api.controller.v1.enrollment;

import com.bluenet.web.api.dto.ResponseMessage;
import com.bluenet.web.api.dto.enrollment.*;
import com.bluenet.web.application.service.EnrollService;
import com.bluenet.web.domain.exception.DataNotFound;
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
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@Tag(name = "报名管理", description = "管理员报名相关接口，需要管理员权限")
@RestController
@RequestMapping("/api/v1/admin/enrollments")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearer-jwt")
public class AdminEnrollController {
    private final EnrollService enrollService;

    @Operation(summary = "分页查询报名列表", description = "管理员分页查询报名列表，支持按状态、方向、关键词筛选")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "成功，返回报名列表", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Page.class)))
    })
    @RequiresPermission(name = "查询报名列表", value = "enrollment:list", access = AccessLevel.PROTECTED)
    @GetMapping
    public ResponseMessage<Page<EnrollmentBriefDTO>> getEnrollmentList(
            @Parameter(description = "页码，从0开始") @RequestParam(defaultValue = "0") Integer page,
            @Parameter(description = "每页数量，默认20，最大100") @RequestParam(defaultValue = "20") Integer size,
            @Parameter(description = "状态筛选：pending/approved/rejected") @RequestParam(required = false) String status,
            @Parameter(description = "方向筛选") @RequestParam(required = false) String direction,
            @Parameter(description = "关键词搜索（姓名/学号）") @RequestParam(required = false) String keyword) {

        EnrollmentListQueryDTO query = EnrollmentListQueryDTO.builder()
                .page(page)
                .size(size)
                .keyword(keyword)
                .build();

        if (status != null) {
            try {
                query.setStatus(com.bluenet.web.domain.model.enumerate.EnrollStatus.valueOf(status.toUpperCase()));
            } catch (IllegalArgumentException ignored) {
            }
        }

        if (direction != null) {
            try {
                query.setDirection(com.bluenet.web.domain.model.enumerate.Direction.valueOf(direction.toUpperCase()));
            } catch (IllegalArgumentException ignored) {
            }
        }

        Page<EnrollmentBriefDTO> result = enrollService.getEnrollmentList(query);
        return ResponseMessage.success(result);
    }

    @Operation(summary = "获取报名详情", description = "管理员获取单个报名的详细信息")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "成功，返回报名详情", content = @Content(mediaType = "application/json", schema = @Schema(implementation = EnrollmentDetailDTO.class))),
            @ApiResponse(responseCode = "404", description = "报名不存在", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseMessage.class), examples = @ExampleObject(value = "{\"code\":404,\"msg\":\"报名记录不存在\",\"data\":null}")))
    })
    @RequiresPermission(name = "查看报名详情", value = "enrollment:detail", access = AccessLevel.PROTECTED)
    @GetMapping("/{id}")
    public ResponseMessage<EnrollmentDetailDTO> getEnrollmentDetail(
            @Parameter(description = "报名ID", required = true) @PathVariable Long id) {
        try {
            EnrollmentDetailDTO detail = enrollService.getEnrollmentDetail(id);
            return ResponseMessage.success(detail);
        } catch (DataNotFound e) {
            return ResponseMessage.error(HttpStatus.NOT_FOUND.value(), e.getMessage());
        }
    }

    @Operation(summary = "通过报名", description = "管理员审核通过报名，系统将自动创建用户账号")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "审核通过", content = @Content(mediaType = "application/json", schema = @Schema(implementation = EnrollmentApprovalResultDTO.class))),
            @ApiResponse(responseCode = "400", description = "报名状态不允许审核", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseMessage.class))),
            @ApiResponse(responseCode = "404", description = "报名不存在", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseMessage.class)))
    })
    @RequiresPermission(name = "通过报名", value = "enrollment:approve", access = AccessLevel.PROTECTED)
    @PutMapping("/{id}/approve")
    public ResponseMessage<EnrollmentApprovalResultDTO> approveEnrollment(
            @Parameter(description = "报名ID", required = true) @PathVariable Long id,
            @Valid @RequestBody(required = false) ApproveEnrollmentRequestDTO request) {
        try {
            EnrollmentApprovalResultDTO result = enrollService.approveEnrollment(id, request);
            return ResponseMessage.success("审核通过，账号已发放", result);
        } catch (DataNotFound e) {
            return ResponseMessage.error(HttpStatus.NOT_FOUND.value(), e.getMessage());
        }
    }

    @Operation(summary = "拒绝报名", description = "管理员审核拒绝报名")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "已拒绝", content = @Content(mediaType = "application/json", schema = @Schema(implementation = EnrollmentApprovalResultDTO.class))),
            @ApiResponse(responseCode = "400", description = "报名状态不允许审核", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseMessage.class))),
            @ApiResponse(responseCode = "404", description = "报名不存在", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseMessage.class)))
    })
    @RequiresPermission(name = "拒绝报名", value = "enrollment:reject", access = AccessLevel.PROTECTED)
    @PutMapping("/{id}/reject")
    public ResponseMessage<EnrollmentApprovalResultDTO> rejectEnrollment(
            @Parameter(description = "报名ID", required = true) @PathVariable Long id,
            @RequestBody(required = false) RejectEnrollmentRequestDTO request) {
        try {
            EnrollmentApprovalResultDTO result = enrollService.rejectEnrollment(id, request);
            return ResponseMessage.success("已拒绝", result);
        } catch (DataNotFound e) {
            return ResponseMessage.error(HttpStatus.NOT_FOUND.value(), e.getMessage());
        }
    }

    @Operation(summary = "报名统计", description = "获取报名统计数据，包括按状态和方向分组的人数")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "成功，返回统计数据", content = @Content(mediaType = "application/json", schema = @Schema(implementation = EnrollmentStatisticsDTO.class)))
    })
    @RequiresPermission(name = "报名统计", value = "enrollment:statistics", access = AccessLevel.PROTECTED)
    @GetMapping("/statistics")
    public ResponseMessage<EnrollmentStatisticsDTO> getStatistics() {
        EnrollmentStatisticsDTO statistics = enrollService.getStatistics();
        return ResponseMessage.success(statistics);
    }
}
