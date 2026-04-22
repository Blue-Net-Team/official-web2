package com.bluenet.web.api.controller.v1.file;

import com.bluenet.web.api.dto.ResponseMessage;
import com.bluenet.web.application.service.FileDownloadService;
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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * 文件下载控制器
 * <p>
 * 提供文件下载接口，支持大文件流式下载
 * </p>
 */
@Slf4j
@Tag(name = "文件下载", description = "文件下载相关接口")
@RestController
@RequestMapping("/api/v1/file/download")
@RequiredArgsConstructor
public class FileDownloadController {

    private final FileDownloadService fileDownloadService;

    @Operation(summary = "下载文件", description = "根据文件ID下载文件，支持流式下载大文件")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "文件下载成功", content = @Content(schema = @Schema(type = "string", format = "binary"))),
            @ApiResponse(responseCode = "403", description = "权限不足", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseMessage.class), examples = {
                    @ExampleObject(value = "{\"code\":403,\"msg\":\"权限不足\",\"data\":null}") })),
            @ApiResponse(responseCode = "404", description = "文件不存在", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseMessage.class), examples = {
                    @ExampleObject(value = "{\"code\":404,\"msg\":\"文件不存在\",\"data\":null}") })) })
    @RequiresPermission(value = "file:download", name = "下载文件", access = AccessLevel.PUBLIC)
    @GetMapping("/{fileId}")
    @SecurityRequirement(name = "bearer-jwt")
    public ResponseEntity<?> downloadFile(@Parameter(description = "文件ID", required = true) @PathVariable Long fileId) {
        // 1. 调用服务层下载文件（包含权限校验）
        Resource resource = fileDownloadService.downloadFile(fileId);

        // 2. 获取文件名并设置响应头
        String filename = resource.getFilename();
        if (filename == null) {
            filename = "download";
        }

        // 3. URL编码文件名以支持中文文件名
        String encodedFilename = URLEncoder.encode(filename, StandardCharsets.UTF_8).replaceAll("\\+", "%20");

        // 4. 确定Content-Type
        MediaType mediaType = determineMediaType(filename);

        // 5. 构建响应
        return ResponseEntity.ok()
                .contentType(mediaType)
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + encodedFilename + "\"; filename*=UTF-8''" + encodedFilename)
                .header(HttpHeaders.CACHE_CONTROL, "max-age=3600")
                .body(resource);
    }

    /**
     * 根据文件名确定Content-Type
     *
     * @param filename
     *            文件名
     * @return MediaType
     */
    private MediaType determineMediaType(String filename) {
        String lowerFilename = filename.toLowerCase();

        if (lowerFilename.endsWith(".jpg") || lowerFilename.endsWith(".jpeg")) {
            return MediaType.IMAGE_JPEG;
        } else if (lowerFilename.endsWith(".png")) {
            return MediaType.IMAGE_PNG;
        } else if (lowerFilename.endsWith(".gif")) {
            return MediaType.IMAGE_GIF;
        } else if (lowerFilename.endsWith(".webp")) {
            return MediaType.parseMediaType("image/webp");
        } else if (lowerFilename.endsWith(".svg")) {
            return MediaType.parseMediaType("image/svg+xml");
        } else if (lowerFilename.endsWith(".pdf")) {
            return MediaType.APPLICATION_PDF;
        } else if (lowerFilename.endsWith(".doc") || lowerFilename.endsWith(".docx")) {
            return MediaType.parseMediaType("application/msword");
        } else if (lowerFilename.endsWith(".xls") || lowerFilename.endsWith(".xlsx")) {
            return MediaType.parseMediaType("application/vnd.ms-excel");
        } else if (lowerFilename.endsWith(".ppt") || lowerFilename.endsWith(".pptx")) {
            return MediaType.parseMediaType("application/vnd.ms-powerpoint");
        } else if (lowerFilename.endsWith(".txt")) {
            return MediaType.TEXT_PLAIN;
        } else if (lowerFilename.endsWith(".zip")) {
            return MediaType.parseMediaType("application/zip");
        } else if (lowerFilename.endsWith(".rar")) {
            return MediaType.parseMediaType("application/vnd.rar");
        } else if (lowerFilename.endsWith(".7z")) {
            return MediaType.parseMediaType("application/x-7z-compressed");
        } else if (lowerFilename.endsWith(".mp4")) {
            return MediaType.parseMediaType("video/mp4");
        } else if (lowerFilename.endsWith(".mp3")) {
            return MediaType.parseMediaType("audio/mpeg");
        } else if (lowerFilename.endsWith(".wav")) {
            return MediaType.parseMediaType("audio/wav");
        } else {
            // 默认使用二进制流
            return MediaType.APPLICATION_OCTET_STREAM;
        }
    }
}
