package com.bluenet.web.api.controller.v1.admin;

import com.bluenet.web.api.dto.ResponseMessage;
import com.bluenet.web.api.dto.qrcode.ConsultationQrcodeDTO;
import com.bluenet.web.api.dto.qrcode.UpdateConsultationQrcodeRequestDTO;
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

/**
 * 咨询群二维码管理接口
 * <p>
 * 管理员管理咨询群二维码
 * </p>
 */
@Tag(name = "咨询群二维码管理", description = "管理员管理咨询群二维码")
@RestController
@RequestMapping("/api/v1/admin/qrcodes/consultation")
@RequiredArgsConstructor
@Slf4j
public class AdminQrcodeController {

    private final QrcodeAppService qrcodeAppService;
    private final QrcodeRequestConverter qrcodeRequestConverter;
    private final QrcodeResponseConverter qrcodeResponseConverter;

    @Operation(summary = "获取咨询群二维码列表", description = "管理员获取所有咨询群二维码列表")
    @RequiresPermission(name = "查看咨询群二维码", value = "admin:qrcode:consultation:read", access = AccessLevel.PROTECTED)
    @SecurityRequirement(name = "cookie-auth")
    @GetMapping
    public ResponseMessage<List<ConsultationQrcodeDTO>> getConsultationQrcodes() {
        try {
            List<QrcodeResult> results = qrcodeAppService.getConsultationQrcodes();
            return ResponseMessage.success(qrcodeResponseConverter.toConsultationDTOList(results));
        } catch (GlobalException e) {
            return ResponseMessage.error(e);
        }
    }

    @Operation(summary = "创建咨询群二维码", description = "通过已上传的 fileId 创建咨询群二维码，文件类型必须为 QRCODE")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "创建成功"),
            @ApiResponse(responseCode = "400", description = "文件类型不匹配", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseMessage.class))),
            @ApiResponse(responseCode = "404", description = "文件不存在", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseMessage.class)))
    })
    @RequiresPermission(name = "创建咨询群二维码", value = "admin:qrcode:consultation:create", access = AccessLevel.PROTECTED)
    @SecurityRequirement(name = "cookie-auth")
    @PostMapping
    public ResponseMessage<Void> createConsultationQrcode(
            @Parameter(description = "文件ID", required = true) @RequestParam("fileId") Long fileId) {
        try {
            QrcodeCommands.CreateConsultationQrcodeCommand command = qrcodeRequestConverter.toCreateCommand(fileId);
            qrcodeAppService.createConsultationQrcode(command);
            return ResponseMessage.success();
        } catch (GlobalException e) {
            return ResponseMessage.error(e);
        }
    }

    @Operation(summary = "更新咨询群二维码", description = "管理员更新咨询群二维码")
    @RequiresPermission(name = "更新咨询群二维码", value = "admin:qrcode:consultation:update", access = AccessLevel.PROTECTED)
    @SecurityRequirement(name = "cookie-auth")
    @PutMapping("/{id}")
    public ResponseMessage<Void> updateConsultationQrcode(@PathVariable Long id,
            @Valid @RequestBody UpdateConsultationQrcodeRequestDTO request) {
        try {
            QrcodeCommands.UpdateConsultationQrcodeCommand command = qrcodeRequestConverter.toUpdateCommand(
                    id,
                    request);
            qrcodeAppService.updateConsultationQrcode(command);
            return ResponseMessage.success();
        } catch (GlobalException e) {
            return ResponseMessage.error(e);
        }
    }

    @Operation(summary = "删除咨询群二维码", description = "管理员删除咨询群二维码")
    @RequiresPermission(name = "删除咨询群二维码", value = "admin:qrcode:consultation:delete", access = AccessLevel.PROTECTED)
    @SecurityRequirement(name = "cookie-auth")
    @DeleteMapping("/{id}")
    public ResponseMessage<Void> deleteConsultationQrcode(@PathVariable Long id) {
        try {
            QrcodeCommands.DeleteConsultationQrcodeCommand command = qrcodeRequestConverter.toDeleteCommand(id);
            qrcodeAppService.deleteConsultationQrcode(command);
            return ResponseMessage.success();
        } catch (GlobalException e) {
            return ResponseMessage.error(e);
        }
    }
}
