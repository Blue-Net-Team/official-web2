package com.bluenet.web.application.command.file;

import com.bluenet.web.domain.model.enumerate.FileType;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文件聚合的命令对象集合。
 * <p>
 * 定义了该聚合下所有应用层操作所需的命令参数。
 * </p>
 */
public class FileCommands {

    /** 禁止实例化。 */
    private FileCommands() {
    }

    /**
     * 上传文件命令。
     * <p>
     * 用于上传文件到系统。
     * </p>
     */
    public record UploadFileCommand(
            /** 文件 */
            MultipartFile file,
            /** 类型 */
            FileType type) {
    }

    /**
     * 下载文件命令。
     * <p>
     * 用于下载指定文件。
     * </p>
     */
    public record DownloadFileCommand(
            /** 文件ID */
            Long fileId) {
    }
}
