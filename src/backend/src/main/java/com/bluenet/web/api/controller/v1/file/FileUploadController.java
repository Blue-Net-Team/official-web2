package com.bluenet.web.api.controller.v1.file;

import com.bluenet.web.api.dto.ResponseMessage;
import com.bluenet.web.api.dto.file.FileInfo;
import com.bluenet.web.application.service.FileService;
import com.bluenet.web.domain.exception.Unauthorized;
import com.bluenet.web.domain.model.enumerate.FileType;
import com.bluenet.web.infrastructure.security.annotation.AccessLevel;
import com.bluenet.web.infrastructure.security.annotation.RequiresPermission;
import com.bluenet.web.infrastructure.security.util.UserCTX;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.SchemaProperty;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@RequestMapping("/api/v1/file/upload")
@RequiredArgsConstructor
@Validated
@Tag(name = "文件上传", description = "统一文件上传接口")
public class FileUploadController {

    private final FileService fileService;

    @Operation(summary = "统一文件上传", description = "上传文件到指定类型的存储桶。AVATAR 类型无需登录（报名场景），其他类型需要登录。")
    @PostMapping
    @RequiresPermission(name = "统一文件上传", value = "file:upload", access = AccessLevel.PUBLIC)
    @RequestBody(content = @Content(mediaType = "multipart/form-data", schema = @Schema(type = "object"), schemaProperties = {
            @SchemaProperty(name = "file", schema = @Schema(type = "string", format = "binary")),
            @SchemaProperty(name = "type", schema = @Schema(type = "string", description = "文件类型", allowableValues = {
                    "AVATAR", "NORMAL_IMG", "ASSESSMENT_ATTACHMENT", "WORK", "QRCODE" })) }))
    @SecurityRequirement(name = "bearer-jwt")
    public ResponseMessage<FileInfo> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam("type") FileType type) {
        // 非 AVATAR 类型需要登录
        if (type != FileType.AVATAR && UserCTX.getCurrentUserId() == null) {
            throw new Unauthorized("该文件类型需要登录");
        }

        FileInfo fileInfo = fileService.uploadFile(file, type);
        log.info("文件上传成功，文件id: {}, 类型: {}", fileInfo.getId(), type);
        return ResponseMessage.success(fileInfo);
    }
}
