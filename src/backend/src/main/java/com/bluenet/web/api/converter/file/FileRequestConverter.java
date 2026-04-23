package com.bluenet.web.api.converter.file;

import com.bluenet.web.application.command.file.FileCommands;
import com.bluenet.web.domain.model.enumerate.FileType;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

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
}
