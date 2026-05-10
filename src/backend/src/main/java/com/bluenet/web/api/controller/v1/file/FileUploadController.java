package com.bluenet.web.api.controller.v1.file;

import com.bluenet.web.api.dto.ResponseMessage;
import com.bluenet.web.api.dto.file.ConfirmUploadRequestDTO;
import com.bluenet.web.api.dto.file.ConfirmUploadResponse;
import com.bluenet.web.api.dto.file.FileInfo;
import com.bluenet.web.api.dto.file.PrepareUploadRequestDTO;
import com.bluenet.web.api.dto.file.PrepareUploadResponse;
import com.bluenet.web.api.converter.file.FileRequestConverter;
import com.bluenet.web.api.converter.file.FileResponseConverter;
import com.bluenet.web.application.FileResult;
import com.bluenet.web.application.service.FileAppService;
import com.bluenet.web.domain.exception.TooManyRequests;
import com.bluenet.web.domain.exception.Unauthorized;
import com.bluenet.web.domain.model.enumerate.FileType;
import com.bluenet.web.infrastructure.security.annotation.AccessLevel;
import com.bluenet.web.infrastructure.security.annotation.RequiresPermission;
import com.bluenet.web.infrastructure.security.rate.AnonymousUploadRateLimiter;
import com.bluenet.web.infrastructure.security.util.UserCTX;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.SchemaProperty;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * 统一文件上传控制器
 * <p>
 * 纯粹的文件存储操作，不涉及任何业务逻辑。 AVATAR 类型允许未登录用户上传（报名场景），其他类型需要认证。
 * </p>
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/file")
@RequiredArgsConstructor
@Validated
@Tag(name = "文件上传", description = "统一文件上传接口")
public class FileUploadController {

    private final FileAppService fileAppService;
    private final FileRequestConverter fileRequestConverter;
    private final FileResponseConverter fileResponseConverter;
    private final AnonymousUploadRateLimiter rateLimiter;

    @Deprecated
    @Operation(summary = "统一文件上传（已废弃）", description = "已废弃，请使用预签名直传流程：prepare-upload → 直传 OSS → confirm-upload")
    @PostMapping("/upload")
    @RequiresPermission(name = "统一文件上传", value = "file:upload", access = AccessLevel.PUBLIC)
    @RequestBody(content = @Content(mediaType = "multipart/form-data", schema = @Schema(type = "object"), schemaProperties = {
            @SchemaProperty(name = "file", schema = @Schema(type = "string", format = "binary")),
            @SchemaProperty(name = "type", schema = @Schema(type = "string", description = "文件类型", allowableValues = {
                    "AVATAR", "NORMAL_IMG", "ASSESSMENT_ATTACHMENT", "WORK", "QRCODE" })) }))
    @SecurityRequirement(name = "bearer-jwt")
    public ResponseMessage<FileInfo> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam("type") FileType type) {
        // AVATAR 和 NORMAL_IMG 类型允许未登录上传（报名、Bug 报告场景），其他类型需要登录
        if (type != FileType.AVATAR && type != FileType.NORMAL_IMG && UserCTX.getCurrentUserId() == null) {
            throw new Unauthorized("该文件类型需要登录");
        }

        FileResult result = fileAppService.uploadFile(fileRequestConverter.toCommand(file, type));
        log.info("文件上传成功，文件id: {}, 类型: {}", result.id(), type);
        return ResponseMessage.success(fileResponseConverter.toDTO(result));
    }

    @Operation(summary = "预签名上传准备", description = "生成预签名 PUT URL 和回调令牌。AVATAR/NORMAL_IMG 允许匿名，其他类型需要登录。")
    @PostMapping("/prepare-upload")
    @RequiresPermission(name = "预签名上传准备", value = "file:prepare-upload", access = AccessLevel.PUBLIC)
    @SecurityRequirement(name = "bearer-jwt")
    public ResponseMessage<PrepareUploadResponse> prepareUpload(
            @Valid @org.springframework.web.bind.annotation.RequestBody PrepareUploadRequestDTO dto,
            HttpServletRequest request) {
        if (dto.getType() != FileType.AVATAR && dto.getType() != FileType.NORMAL_IMG
                && UserCTX.getCurrentUserId() == null) {
            throw new Unauthorized("该文件类型需要登录");
        }

        // 匿名限流
        if (UserCTX.getCurrentUserId() == null) {
            String clientIp = getClientIp(request);
            if (!rateLimiter.tryAcquire(clientIp)) {
                throw new TooManyRequests("请求过于频繁，请稍后再试");
            }
        }

        var result = fileAppService.prepareUpload(fileRequestConverter.toCommand(dto));
        log.info("预签名上传准备成功，文件id: {}, 类型: {}", result.fileId(), dto.getType());
        return ResponseMessage.success(fileResponseConverter.toPrepareUploadDTO(result));
    }

    @Operation(summary = "预签名上传确认", description = "前端直传完成后回调，校验文件并激活记录。")
    @PostMapping("/confirm-upload")
    @RequiresPermission(name = "预签名上传确认", value = "file:confirm-upload", access = AccessLevel.PUBLIC)
    @SecurityRequirement(name = "bearer-jwt")
    public ResponseMessage<ConfirmUploadResponse> confirmUpload(
            @Valid @org.springframework.web.bind.annotation.RequestBody ConfirmUploadRequestDTO dto) {
        var result = fileAppService.confirmUpload(fileRequestConverter.toCommand(dto));
        log.info("预签名上传确认完成，文件id: {}, 状态: {}", result.fileId(), result.status());
        return ResponseMessage.success(fileResponseConverter.toConfirmUploadDTO(result));
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isBlank()) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isBlank()) {
            ip = request.getRemoteAddr();
        }
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}
