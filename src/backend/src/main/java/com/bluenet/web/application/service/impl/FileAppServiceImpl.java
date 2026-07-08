package com.bluenet.web.application.service.impl;

import com.bluenet.web.application.result.file.FileDownloadResult;
import com.bluenet.web.application.result.file.FileResult;
import com.bluenet.web.application.command.file.FileCommands;
import com.bluenet.web.application.service.FileAppService;
import com.bluenet.web.domain.exception.DataNotFound;
import com.bluenet.web.domain.model.enumerate.FileType;
import com.bluenet.web.domain.model.entity.File;
import com.bluenet.web.domain.model.result.ConfirmUploadResult;
import com.bluenet.web.domain.model.result.PresignedUploadResult;
import com.bluenet.web.domain.repository.FileRepository;
import com.bluenet.web.domain.service.FileDomainService;
import com.bluenet.web.infrastructure.security.util.UserCTX;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * 文件应用服务实现。
 * <p>
 * 实现文件聚合在应用层的业务逻辑编排。
 * </p>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class FileAppServiceImpl implements FileAppService {

    private final FileDomainService fileDomainService;
    private final FileRepository fileRepository;

    /**
     * 上传文件。
     *
     * @param command
     *            上传文件命令
     * @return 文件上传结果
     */
    @Override
    public FileResult uploadFile(FileCommands.UploadFileCommand command) {
        MultipartFile file = command.file();
        FileType type = command.type();
        String filename = fileDomainService.generateFilename(type, file.getOriginalFilename());
        File savedFile = saveFile(type, filename, file);
        log.info("文件上传成功，文件id: {}, 类型: {}", savedFile.getId(), type);
        return new FileResult(savedFile.getId(), savedFile.getName(), savedFile.getType(), savedFile.getUrl(),
                savedFile.getStatus());
    }

    /**
     * 下载文件。
     *
     * @param command
     *            下载文件命令
     * @return 文件下载结果
     */
    @Override
    public FileDownloadResult downloadFile(FileCommands.DownloadFileCommand command) {
        File file = fileDomainService.getFileById(command.fileId());

        fileDomainService.checkDownloadPermission(file, UserCTX.getCurrentUser());

        Resource resource = fileRepository.loadFile(file.getName(), file.getType());
        if (resource == null || !resource.exists()) {
            log.warn("File resource not found for file: {}", file.getName());
            throw new DataNotFound("文件资源不存在");
        }

        log.info(
                "File downloaded successfully: id={}, type={}, name={}",
                command.fileId(),
                file.getType(),
                file.getName());
        return new FileDownloadResult(resource, file.getName());
    }

    @Override
    public StreamingResponseBody downloadBatchStream(FileCommands.BatchDownloadCommand command) {
        return outputStream -> {
            String zipName = command.zipName();
            if (zipName == null || zipName.isBlank()) {
                zipName = "download.zip";
            }
            if (!zipName.endsWith(".zip")) {
                zipName = zipName + ".zip";
            }

            try (ZipOutputStream zos = new ZipOutputStream(outputStream)) {
                for (FileCommands.BatchDownloadEntry entry : command.entries()) {
                    File file = fileDomainService.getFileById(entry.fileId());
                    fileDomainService.checkDownloadPermission(file, UserCTX.getCurrentUser());

                    Resource resource = fileRepository.loadFile(file.getName(), file.getType());
                    if (resource == null || !resource.exists()) {
                        log.warn("File resource not found for batch download: {}", file.getName());
                        throw new DataNotFound("文件资源不存在: " + file.getName());
                    }

                    String entryName = entry.filename();
                    if (entryName == null || entryName.isBlank()) {
                        entryName = file.getName();
                    } else if (!entryName.contains(".")) {
                        String originalName = file.getName();
                        int lastDot = originalName.lastIndexOf('.');
                        if (lastDot > 0) {
                            entryName = entryName + originalName.substring(lastDot);
                        }
                    }

                    ZipEntry zipEntry = new ZipEntry(entryName);
                    zos.putNextEntry(zipEntry);
                    try (InputStream is = resource.getInputStream()) {
                        is.transferTo(zos);
                    }
                    zos.closeEntry();

                    log.info(
                            "Batch download stream added entry: fileId={}, entryName={}",
                            entry.fileId(),
                            entryName);
                }
                zos.finish();
            } catch (IOException e) {
                log.error("Failed to create batch download zip stream", e);
                throw new RuntimeException("批量下载打包失败", e);
            }
        };
    }

    @Override
    public PresignedUploadResult prepareUpload(FileCommands.PrepareUploadCommand command) {
        PresignedUploadResult vo = fileDomainService
                .prepareUpload(command.type(), command.filename(), command.contentType(), command.size());
        return new PresignedUploadResult(vo.fileId(), vo.uploadUrl(), vo.callbackToken(), vo.filename(), vo.type());
    }

    @Override
    public ConfirmUploadResult confirmUpload(FileCommands.ConfirmUploadCommand command) {
        ConfirmUploadResult vo = fileDomainService
                .confirmUpload(command.fileId(), command.callbackToken(), command.md5(), command.size());
        return new ConfirmUploadResult(vo.fileId(), vo.filename(), vo.type(), vo.status());
    }

    @Override
    public String getPresignedDownloadUrl(FileCommands.DownloadFileCommand command) {
        File file = fileDomainService.getFileById(command.fileId());
        fileDomainService.checkDownloadPermission(file, UserCTX.getCurrentUser());
        return fileDomainService.getPresignedDownloadUrl(file.getType(), file.getName());
    }

    @NotNull
    private File saveFile(FileType type, String filename, MultipartFile file) {
        try {
            return fileDomainService.saveFile(type, filename, file.getInputStream());
        } catch (IOException e) {
            throw new RuntimeException("Failed to save file: " + filename, e);
        }
    }
}
