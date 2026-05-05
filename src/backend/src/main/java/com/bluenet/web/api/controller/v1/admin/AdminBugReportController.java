package com.bluenet.web.api.controller.v1.admin;

import com.bluenet.web.api.dto.PageDTO;
import com.bluenet.web.api.dto.ResponseMessage;
import com.bluenet.web.api.dto.bugreport.BugReportBriefDTO;
import com.bluenet.web.api.dto.bugreport.BugReportDetailDTO;
import com.bluenet.web.api.dto.bugreport.BugReportListQueryDTO;
import com.bluenet.web.api.dto.bugreport.UpdateBugReportStatusRequestDTO;
import com.bluenet.web.api.converter.bugreport.BugReportRequestConverter;
import com.bluenet.web.application.BugReportResult;
import com.bluenet.web.application.converter.BugReportAppConverter;
import com.bluenet.web.application.service.BugReportAdminAppService;
import com.bluenet.web.infrastructure.security.annotation.AccessLevel;
import com.bluenet.web.infrastructure.security.annotation.RequiresPermission;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

/**
 * Bug 报告管理控制器（管理端）
 * <p>
 * 提供管理员查询、查看详情、更新状态的接口。
 * </p>
 */
@Tag(name = "Bug 报告管理", description = "管理员 Bug 报告管理接口")
@RestController
@RequestMapping("/api/v1/admin/bug-reports")
@RequiredArgsConstructor
public class AdminBugReportController {

    private final BugReportAdminAppService bugReportAdminAppService;
    private final BugReportRequestConverter requestConverter;
    private final BugReportAppConverter appConverter;

    @Operation(summary = "分页查询 Bug 报告列表", description = "支持按状态筛选，按创建时间倒序")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "查询成功", content = @Content(mediaType = "application/json", schema = @Schema(implementation = PageDTO.class))),
            @ApiResponse(responseCode = "403", description = "无权限", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseMessage.class)))
    })
    @RequiresPermission(value = "bug-report:list", name = "查询 Bug 报告列表", access = AccessLevel.PROTECTED)
    @GetMapping
    public ResponseMessage<PageDTO<BugReportBriefDTO>> getBugReportList(
            @Valid BugReportListQueryDTO query) {
        Page<BugReportResult.Brief> page = bugReportAdminAppService
                .getBugReportList(requestConverter.toListCommand(query));
        return ResponseMessage.success(PageDTO.from(appConverter.toBriefDTOPage(page)));
    }

    @Operation(summary = "获取 Bug 报告详情", description = "获取指定 Bug 报告的完整信息，包括环境信息和图片")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "查询成功"),
            @ApiResponse(responseCode = "404", description = "报告不存在", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseMessage.class))),
            @ApiResponse(responseCode = "403", description = "无权限", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseMessage.class)))
    })
    @RequiresPermission(value = "bug-report:detail", name = "查看 Bug 报告详情", access = AccessLevel.PROTECTED)
    @GetMapping("/{id}")
    public ResponseMessage<BugReportDetailDTO> getBugReportDetail(
            @Parameter(description = "报告 ID", required = true) @PathVariable Long id) {
        BugReportResult.Detail detail = bugReportAdminAppService.getBugReportDetail(id);
        return ResponseMessage.success(appConverter.toDetailDTO(detail));
    }

    @Operation(summary = "更新 Bug 报告状态", description = "将指定 Bug 报告更新为新的处理状态")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "更新成功"),
            @ApiResponse(responseCode = "404", description = "报告不存在", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseMessage.class))),
            @ApiResponse(responseCode = "403", description = "无权限", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseMessage.class)))
    })
    @RequiresPermission(value = "bug-report:update", name = "更新 Bug 报告状态", access = AccessLevel.PROTECTED)
    @PutMapping("/{id}/status")
    public ResponseMessage<BugReportDetailDTO> updateBugReportStatus(
            @Parameter(description = "报告 ID", required = true) @PathVariable Long id,
            @Valid @RequestBody UpdateBugReportStatusRequestDTO requestDTO) {
        BugReportResult.Detail detail = bugReportAdminAppService.updateStatus(
                requestConverter.toCommand(id, requestDTO));
        return ResponseMessage.success(appConverter.toDetailDTO(detail));
    }
}
