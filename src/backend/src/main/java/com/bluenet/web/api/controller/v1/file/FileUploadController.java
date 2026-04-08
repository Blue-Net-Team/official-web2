package com.bluenet.web.api.controller.v1.file;

import com.bluenet.web.api.dto.file.FileInfo;
import com.bluenet.web.api.dto.ResponseMessage;
import com.bluenet.web.application.service.FileService;
import com.bluenet.web.domain.exception.DataConflict;
import com.bluenet.web.domain.exception.DataNotFound;
import com.bluenet.web.domain.model.enumerate.ImageType;
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
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文件上传控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/file/upload")
@RequiredArgsConstructor
@Validated
@Tag(name = "文件上传", description = "文件上传相关接口")
public class FileUploadController {

    private final FileService fileService;

    @Operation(summary = "上传头像", description = "上传用户头像或报名用户头像")
    @PostMapping("/avatar")
    @RequiresPermission(name = "上传头像", value = "file:upload:avatar", access = AccessLevel.PUBLIC)
    @RequestBody(content = @Content(mediaType = "multipart/form-data", schema = @Schema(type = "object"), schemaProperties = {
            @SchemaProperty(name = "file", schema = @Schema(type = "string", format = "binary")) }))
    @SecurityRequirement(name = "bearer-jwt")
    public ResponseMessage<FileInfo> uploadAvatar(@RequestParam("file") MultipartFile file) {
        Long userId = UserCTX.getCurrentUserId();

        FileInfo fileInfo;
        if (userId == null) {
            fileInfo = fileService.updateEnrollAvatar(file);
        } else {
            fileInfo = fileService.updateUserAvatar(userId, file);
        }

        return ResponseMessage.success(fileInfo);
    }

    // 上传考题附件，由于权限考虑，必须将附件上传和作品上传分开写
    @Operation(summary = "上传考题附件", description = "上传考题相关的附件文件")
    @PostMapping("/assessment/attachment")
    @RequiresPermission(name = "上传考题附件", value = "file:upload:assessment:attachment", access = AccessLevel.AUTHENTICATED)
    @RequestBody(content = @Content(mediaType = "multipart/form-data", schema = @Schema(type = "object"), schemaProperties = {
            @SchemaProperty(name = "file", schema = @Schema(type = "string", format = "binary")),
            @SchemaProperty(name = "questionId", schema = @Schema(type = "integer", format = "int64")) }))
    @SecurityRequirement(name = "bearer-jwt")
    public ResponseMessage<FileInfo> uploadAssessmentAttachment(@RequestParam("file") MultipartFile file,
            @RequestParam("questionId") Long questionId) {
        FileInfo fileInfo = fileService.uploadAssessmentAttachment(questionId, file);
        return ResponseMessage.success(fileInfo);
    }

    // 上传考题作品
    @Operation(summary = "上传考题作品", description = "上传考题的作品文件")
    @PostMapping("/assessment/work")
    @RequiresPermission(name = "上传考题作品", value = "file:upload:assessment:work", access = AccessLevel.AUTHENTICATED)
    @RequestBody(content = @Content(mediaType = "multipart/form-data", schema = @Schema(type = "object"), schemaProperties = {
            @SchemaProperty(name = "file", schema = @Schema(type = "string", format = "binary")),
            @SchemaProperty(name = "questionId", schema = @Schema(type = "integer", format = "int64")) }))
    @SecurityRequirement(name = "bearer-jwt")
    public ResponseMessage<FileInfo> uploadAssessmentWork(@RequestParam("file") MultipartFile file,
            @RequestParam("questionId") Long questionId) {
        FileInfo fileInfo = fileService.uploadAssessmentWork(questionId, file);
        return ResponseMessage.success(fileInfo);
    }

    // 上传个人二维码
    @Operation(summary = "上传个人二维码", description = "上传用户个人二维码")
    @PostMapping("/qrcode/self")
    @RequiresPermission(name = "上传个人二维码", value = "file:upload:qrcode:self", access = AccessLevel.AUTHENTICATED)
    @RequestBody(content = @Content(mediaType = "multipart/form-data", schema = @Schema(type = "object"), schemaProperties = {
            @SchemaProperty(name = "file", schema = @Schema(type = "string", format = "binary")) }))
    @SecurityRequirement(name = "bearer-jwt")
    public ResponseMessage<FileInfo> uploadSelfQrcode(@RequestParam("file") MultipartFile file) {
        FileInfo fileInfo = fileService.uploadQrcode("USER", file);
        return ResponseMessage.success(fileInfo);
    }

    // 上传群聊二维码
    @Operation(summary = "上传群聊二维码", description = "上传群聊二维码")
    @PostMapping("/qrcode/group")
    @RequiresPermission(name = "上传群聊二维码", value = "file:upload:qrcode:group", access = AccessLevel.AUTHENTICATED)
    @RequestBody(content = @Content(mediaType = "multipart/form-data", schema = @Schema(type = "object"), schemaProperties = {
            @SchemaProperty(name = "file", schema = @Schema(type = "string", format = "binary")) }))
    @SecurityRequirement(name = "bearer-jwt")
    public ResponseMessage<FileInfo> uploadGroupQrcode(@RequestParam("file") MultipartFile file) {
        FileInfo fileInfo = fileService.uploadQrcode("GROUP", file);
        return ResponseMessage.success(fileInfo);
    }

    // 上传介绍图片
    @Operation(summary = "上传介绍图片", description = "上传实验室介绍、设备介绍、竞赛介绍图片。支持类型：laboratory、equipment、competition")
    @PostMapping("/introduce-image")
    @RequiresPermission(name = "上传介绍图片", value = "file:upload:introduce-image", access = AccessLevel.PROTECTED)
    @RequestBody(content = @Content(mediaType = "multipart/form-data", schema = @Schema(type = "object"), schemaProperties = {
            @SchemaProperty(name = "file", schema = @Schema(type = "string", format = "binary")),
            @SchemaProperty(name = "type", schema = @Schema(type = "string", description = "图片类型", allowableValues = {
                    "laboratory", "equipment", "competition" })),
            @SchemaProperty(name = "description", schema = @Schema(type = "string", description = "图片描述")) }))
    @SecurityRequirement(name = "bearer-jwt")
    public ResponseMessage<FileInfo> uploadIntroduceImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam("type") ImageType type,
            @RequestParam(value = "description", required = false) @Size(max = 500, message = "描述长度不能超过500字符") String description) {
        try {
            FileInfo fileInfo = fileService.uploadIntroduceImage(type, null, description, file);
            log.info("介绍图片上传成功，文件id: {}, 类型: {}", fileInfo.getId(), type);
            return ResponseMessage.success(fileInfo);
        } catch (IllegalArgumentException e) {
            log.warn("介绍图片上传失败: {}", e.getMessage());
            return ResponseMessage.error(400, e.getMessage());
        }
    }

    // 上传竞赛合照
    @Operation(summary = "上传竞赛合照", description = "上传竞赛相关的合照，每个竞赛最多上传20张图片")
    @PostMapping("/competition/image")
    @RequiresPermission(name = "上传竞赛合照", value = "file:upload:competition:image", access = AccessLevel.PROTECTED)
    @RequestBody(content = @Content(mediaType = "multipart/form-data", schema = @Schema(type = "object"), schemaProperties = {
            @SchemaProperty(name = "file", schema = @Schema(type = "string", format = "binary")),
            @SchemaProperty(name = "competitionId", schema = @Schema(type = "integer", format = "int64", description = "竞赛ID")),
            @SchemaProperty(name = "description", schema = @Schema(type = "string", description = "图片描述")) }))
    @SecurityRequirement(name = "bearer-jwt")
    public ResponseMessage<FileInfo> uploadCompetitionImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam("competitionId") Long competitionId,
            @RequestParam(value = "description", required = false) @Size(max = 500, message = "描述长度不能超过500字符") String description) {
        try {
            FileInfo fileInfo = fileService.uploadCompetitionImage(competitionId, description, file);
            log.info("竞赛合照上传成功，文件id: {}, 竞赛id: {}", fileInfo.getId(), competitionId);
            return ResponseMessage.success(fileInfo);
        } catch (DataNotFound | DataConflict e) {
            log.warn("竞赛合照上传失败: {}", e.getMessage());
            return ResponseMessage.error(e);
        }
    }

    // 上传竞赛Logo
    @Operation(summary = "上传竞赛Logo", description = "上传竞赛的Logo图片")
    @PostMapping("/competition/logo")
    @RequiresPermission(name = "上传竞赛Logo", value = "file:upload:competition:logo", access = AccessLevel.PROTECTED)
    @RequestBody(content = @Content(mediaType = "multipart/form-data", schema = @Schema(type = "object"), schemaProperties = {
            @SchemaProperty(name = "file", schema = @Schema(type = "string", format = "binary")),
            @SchemaProperty(name = "competitionId", schema = @Schema(type = "integer", format = "int64", description = "竞赛ID")) }))
    @SecurityRequirement(name = "bearer-jwt")
    public ResponseMessage<FileInfo> uploadCompetitionLogo(
            @RequestParam("file") MultipartFile file,
            @RequestParam("competitionId") Long competitionId) {
        try {
            FileInfo fileInfo = fileService.uploadCompetitionLogo(competitionId, file);
            log.info("竞赛Logo上传成功，文件id: {}, 竞赛id: {}", fileInfo.getId(), competitionId);
            return ResponseMessage.success(fileInfo);
        } catch (IllegalArgumentException e) {
            log.warn("竞赛Logo上传失败: {}", e.getMessage());
            return ResponseMessage.error(400, e.getMessage());
        }
    }
}
