package com.bluenet.web.api.converter.file;

import com.bluenet.web.api.dto.file.BatchDownloadRequestDTO;
import com.bluenet.web.api.dto.file.ConfirmUploadRequestDTO;
import com.bluenet.web.api.dto.file.PrepareUploadRequestDTO;
import com.bluenet.web.application.command.file.FileCommands;
import com.bluenet.web.domain.model.enumerate.FileType;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 文件请求转换器
 * <p>
 * 负责将 API 层的请求参数转换为应用层的 Command
 * </p>
 */
@Component
public class FileRequestConverter {

    /**
     * 将上传请求参数转换为命令
     */
    public FileCommands.UploadFileCommand toCommand(MultipartFile file, FileType type) {
        return new FileCommands.UploadFileCommand(file, type);
    }

    /**
     * 将批量下载请求 DTO 转换为命令
     */
    public FileCommands.BatchDownloadCommand toCommand(BatchDownloadRequestDTO dto) {
        List<FileCommands.BatchDownloadEntry> entries = dto.getEntries()
                .stream()
                .map(e -> new FileCommands.BatchDownloadEntry(e.getFileId(), e.getFilename()))
                .toList();
        return new FileCommands.BatchDownloadCommand(entries, dto.getZipName());
    }

    /**
     * 将预签名上传准备请求 DTO 转换为命令
     */
    public FileCommands.PrepareUploadCommand toCommand(PrepareUploadRequestDTO dto) {
        return new FileCommands.PrepareUploadCommand(dto.getFilename(), dto.getType(), dto.getSize(),
                dto.getContentType());
    }

    /**
     * 将预签名上传确认请求 DTO 转换为命令
     */
    public FileCommands.ConfirmUploadCommand toCommand(ConfirmUploadRequestDTO dto) {
        return new FileCommands.ConfirmUploadCommand(dto.getFileId(), dto.getCallbackToken(), dto.getMd5(),
                dto.getSize());
    }
}
