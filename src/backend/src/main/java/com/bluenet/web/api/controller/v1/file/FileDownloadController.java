package com.bluenet.web.api.controller.v1.file;

import com.bluenet.web.application.FileDownloadResult;
import com.bluenet.web.application.command.file.FileCommands;
import com.bluenet.web.application.converter.FileAppConverter;
import com.bluenet.web.application.service.FileAppService;
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
import org.springframework.web.bind.annotation.*;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * 文件下载控制器
 * <p>
 * 提供文件下载接口，支持按文件 ID 形式下载文件
 * </p>
 */
@Slf4j
@Tag(name = "文件下载", description = "文件下载相关接口")
@RestController
@RequestMapping("/api/v1/file/download")
@RequiredArgsConstructor
public class FileDownloadController {

    private final FileAppService fileAppService;
    private final FileAppConverter fileAppConverter;

    @Operation(summary = "下载文件", description = "根据文件 ID 下载文件并支持格式内联")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "文件下载成功", content = @Content(schema = @Schema(type = "string", format = "binary"))),
            @ApiResponse(responseCode = "403", description = "权限不足", content = @Content(mediaType = "application/json", schema = @Schema(implementation = com.bluenet.web.api.dto.ResponseMessage.class), examples = {
                    @ExampleObject(value = "{\"code\":403,\"msg\":\"权限不足\",\"data\":null}") })),
            @ApiResponse(responseCode = "404", description = "文件不存在", content = @Content(mediaType = "application/json", schema = @Schema(implementation = com.bluenet.web.api.dto.ResponseMessage.class), examples = {
                    @ExampleObject(value = "{\"code\":404,\"msg\":\"文件不存在\",\"data\":null}") })) })
    @RequiresPermission(value = "file:download", name = "下载文件", access = AccessLevel.PUBLIC)
    @GetMapping("/{fileId}")
    @SecurityRequirement(name = "bearer-jwt")
    public ResponseEntity<?> downloadFile(
            @Parameter(description = "文件 ID", required = true) @PathVariable Long fileId) {
        FileCommands.DownloadFileCommand command = new FileCommands.DownloadFileCommand(fileId);
        FileDownloadResult result = fileAppService.downloadFile(command);

        Resource resource = result.resource();
        String filename = result.filename();
        if (filename == null) {
            filename = "download";
        }

        String encodedFilename = URLEncoder.encode(filename, StandardCharsets.UTF_8).replaceAll("\\+", "%20");
        MediaType mediaType = fileAppConverter.determineMediaType(filename);

        return ResponseEntity.ok()
                .contentType(mediaType)
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + encodedFilename + "\"; filename*=UTF-8''" + encodedFilename)
                .header(HttpHeaders.CACHE_CONTROL, "max-age=3600")
                .body(resource);
    }

    @Operation(summary = "批量下载文件", description = "按文件 ID 列表批量下载并打包为 ZIP")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "批量下载成功", content = @Content(schema = @Schema(type = "string", format = "binary"))),
            @ApiResponse(responseCode = "403", description = "权限不足", content = @Content(mediaType = "application/json", schema = @Schema(implementation = com.bluenet.web.api.dto.ResponseMessage.class), examples = {
                    @ExampleObject(value = "{\"code\":403,\"msg\":\"权限不足\",\"data\":null}") })),
            @ApiResponse(responseCode = "404", description = "文件不存在", content = @Content(mediaType = "application/json", schema = @Schema(implementation = com.bluenet.web.api.dto.ResponseMessage.class), examples = {
                    @ExampleObject(value = "{\"code\":404,\"msg\":\"文件不存在\",\"data\":null}") })) })
    @RequiresPermission(value = "file:download:batch", name = "批量下载文件", access = AccessLevel.PUBLIC)
    @PostMapping("/batch")
    @SecurityRequirement(name = "bearer-jwt")
    public ResponseEntity<?> downloadBatch(
            @Parameter(description = "批量下载命令", required = true) @RequestBody FileCommands.BatchDownloadCommand command) {
        FileDownloadResult result = fileAppService.downloadBatch(command);

        Resource resource = result.resource();
        String filename = result.filename();
        if (filename == null) {
            filename = "download.zip";
        }

        String encodedFilename = URLEncoder.encode(filename, StandardCharsets.UTF_8).replaceAll("\\+", "%20");

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/zip"))
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + encodedFilename + "\"; filename*=UTF-8''" + encodedFilename)
                .header(HttpHeaders.CACHE_CONTROL, "max-age=3600")
                .body(resource);
    }
}
