package com.bluenet.web.api.controller.v1.admin;

import com.bluenet.web.api.dto.ResponseMessage;
import com.bluenet.web.api.dto.qrcode.AssessmentQrcodeDTO;
import com.bluenet.web.api.dto.qrcode.CreateAssessmentQrcodeRequestDTO;
import com.bluenet.web.api.dto.qrcode.UpdateAssessmentQrcodeRequestDTO;
import com.bluenet.web.api.converter.qrcode.QrcodeRequestConverter;
import com.bluenet.web.api.converter.qrcode.QrcodeResponseConverter;
import com.bluenet.web.application.QrcodeResult;
import com.bluenet.web.application.command.qrcode.QrcodeCommands;
import com.bluenet.web.application.service.QrcodeAppService;
import com.bluenet.web.domain.exception.GlobalException;
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
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 考核群二维码管理接口
 */
@Tag(name = "考核群二维码管理", description = "管理员管理考核群二维码")
@RestController
@RequestMapping("/api/v1/admin/qrcodes/assessment")
@RequiredArgsConstructor
@Slf4j
public class AdminAssessmentQrcodeController {

    private final QrcodeAppService qrcodeAppService;
    private final QrcodeRequestConverter qrcodeRequestConverter;
    private final QrcodeResponseConverter qrcodeResponseConverter;

    @Operation(summary = "获取考核群二维码列表", description = "管理员获取所有考核群二维码列表，支持按方向和轮次筛选")
    @RequiresPermission(name = "查看考核群二维码", value = "admin:qrcode:assessment:read", access = AccessLevel.PROTECTED)
    @SecurityRequirement(name = "cookie-auth")
    @GetMapping
    public ResponseMessage<List<AssessmentQrcodeDTO>> getAssessmentQrcodes(
            @Parameter(description = "方向") @RequestParam(required = false) String direction,
            @Parameter(description = "考核轮次") @RequestParam(required = false) Integer epoch) {
        try {
            List<QrcodeResult> results = qrcodeAppService.getAssessmentQrcodes(direction, epoch);
            return ResponseMessage.success(qrcodeResponseConverter.toAssessmentDTOList(results));
        } catch (GlobalException e) {
            return ResponseMessage.error(e);
        }
    }

    @Operation(summary = "创建考核群二维码", description = "通过已上传的fileId创建考核群二维码，文件类型必须为QRCODE")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "创建成功"),
            @ApiResponse(responseCode = "400", description = "文件类型不匹配", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseMessage.class))),
            @ApiResponse(responseCode = "404", description = "文件不存在", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseMessage.class)))
    })
    @RequiresPermission(name = "创建考核群二维码", value = "admin:qrcode:assessment:create", access = AccessLevel.PROTECTED)
    @SecurityRequirement(name = "cookie-auth")
    @PostMapping
    public ResponseMessage<Void> createAssessmentQrcode(
            @Valid @RequestBody CreateAssessmentQrcodeRequestDTO request) {
        try {
            QrcodeCommands.CreateAssessmentQrcodeCommand command = qrcodeRequestConverter.toCreateAssessmentCommand(request);
            qrcodeAppService.createAssessmentQrcode(command);
            return ResponseMessage.success();
        } catch (GlobalException e) {
            return ResponseMessage.error(e);
        }
    }

    @Operation(summary = "更新考核群二维码", description = "管理员更新考核群二维码")
    @RequiresPermission(name = "更新考核群二维码", value = "admin:qrcode:assessment:update", access = AccessLevel.PROTECTED)
    @SecurityRequirement(name = "cookie-auth")
    @PutMapping("/{id}")
    public ResponseMessage<Void> updateAssessmentQrcode(
            @PathVariable Long id,
            @Valid @RequestBody UpdateAssessmentQrcodeRequestDTO request) {
        try {
            QrcodeCommands.UpdateAssessmentQrcodeCommand command = qrcodeRequestConverter.toUpdateAssessmentCommand(id, request);
            qrcodeAppService.updateAssessmentQrcode(command);
            return ResponseMessage.success();
        } catch (GlobalException e) {
            return ResponseMessage.error(e);
        }
    }

    @Operation(summary = "删除考核群二维码", description = "管理员删除考核群二维码")
    @RequiresPermission(name = "删除考核群二维码", value = "admin:qrcode:assessment:delete", access = AccessLevel.PROTECTED)
    @SecurityRequirement(name = "cookie-auth")
    @DeleteMapping("/{id}")
    public ResponseMessage<Void> deleteAssessmentQrcode(@PathVariable Long id) {
        try {
            QrcodeCommands.DeleteAssessmentQrcodeCommand command = qrcodeRequestConverter.toDeleteAssessmentCommand(id);
            qrcodeAppService.deleteAssessmentQrcode(command);
            return ResponseMessage.success();
        } catch (GlobalException e) {
            return ResponseMessage.error(e);
        }
    }
}
