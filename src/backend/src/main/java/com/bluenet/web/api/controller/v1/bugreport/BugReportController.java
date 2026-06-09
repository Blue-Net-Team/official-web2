package com.bluenet.web.api.controller.v1.bugreport;

import com.bluenet.web.api.dto.ResponseMessage;
import com.bluenet.web.api.dto.bugreport.BugReportCreatedDTO;
import com.bluenet.web.api.dto.bugreport.CreateBugReportRequestDTO;
import com.bluenet.web.api.converter.bugreport.BugReportRequestConverter;
import com.bluenet.web.application.BugReportResult;
import com.bluenet.web.api.converter.bugreport.BugReportResponseConverter;
import com.bluenet.web.application.service.BugReportAppService;
import com.bluenet.web.infrastructure.security.annotation.AccessLevel;
import com.bluenet.web.infrastructure.security.annotation.RequiresPermission;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * Bug 报告控制器（公开端）
 * <p>
 * 提供公开访问的 Bug 报告提交接口，无需登录。
 * </p>
 */
@Tag(name = "Bug 报告", description = "公开 Bug 报告提交接口")
@RestController
@RequestMapping("/api/v1/bug-reports")
@RequiredArgsConstructor
public class BugReportController {

    private final BugReportAppService bugReportAppService;
    private final BugReportRequestConverter requestConverter;
    private final BugReportResponseConverter responseConverter;

    @Operation(summary = "提交 Bug 报告", description = "任何人可提交 Bug 报告，无需登录。支持上传最多 3 张截图。")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "提交成功", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BugReportCreatedDTO.class))),
            @ApiResponse(responseCode = "400", description = "参数校验失败", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseMessage.class)))
    })
    @RequiresPermission(value = "bug-report:create", name = "提交 Bug 报告", access = AccessLevel.PUBLIC)
    @PostMapping
    public ResponseMessage<BugReportCreatedDTO> submitBugReport(
            @Valid @RequestBody CreateBugReportRequestDTO requestDTO) {
        BugReportResult.Created result = bugReportAppService.submitBugReport(requestConverter.toCommand(requestDTO));
        return ResponseMessage.success(responseConverter.toCreatedDTO(result));
    }
}
