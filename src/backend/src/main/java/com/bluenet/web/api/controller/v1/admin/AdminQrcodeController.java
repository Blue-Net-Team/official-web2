package com.bluenet.web.api.controller.v1.admin;

import com.bluenet.web.api.dto.ResponseMessage;
import com.bluenet.web.api.dto.file.FileInfo;
import com.bluenet.web.application.service.QrcodeService;
import com.bluenet.web.infrastructure.security.annotation.AccessLevel;
import com.bluenet.web.infrastructure.security.annotation.RequiresPermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.SchemaProperty;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 咨询群二维码管理接口
 * <p>
 * 管理员上传和删除咨询群二维码
 * </p>
 */
@Tag(name = "咨询群二维码管理", description = "管理员上传和删除咨询群二维码")
@RestController
@RequestMapping("/api/v1/admin/qrcodes/consultation")
@RequiredArgsConstructor
@Slf4j
public class AdminQrcodeController {

    private final QrcodeService qrcodeService;

    @Operation(summary = "上传咨询群二维码", description = "管理员上传咨询群二维码图片")
    @RequiresPermission(name = "上传咨询群二维码", value = "admin:qrcode:consultation:upload", access = AccessLevel.PROTECTED)
    @RequestBody(content = @Content(mediaType = "multipart/form-data", schema = @Schema(type = "object"), schemaProperties = {
            @SchemaProperty(name = "file", schema = @Schema(type = "string", format = "binary")) }))
    @SecurityRequirement(name = "bearer-jwt")
    @PostMapping
    public ResponseMessage<FileInfo> uploadConsultationQrcode(@RequestParam("file") MultipartFile file) {
        FileInfo fileInfo = qrcodeService.uploadConsultationQrcode(file);
        return ResponseMessage.success(fileInfo);
    }

    @Operation(summary = "删除咨询群二维码", description = "管理员删除咨询群二维码")
    @RequiresPermission(name = "删除咨询群二维码", value = "admin:qrcode:consultation:delete", access = AccessLevel.PROTECTED)
    @SecurityRequirement(name = "bearer-jwt")
    @DeleteMapping("/{id}")
    public ResponseMessage<Void> deleteConsultationQrcode(@PathVariable Long id) {
        qrcodeService.deleteConsultationQrcode(id);
        return ResponseMessage.success();
    }
}
