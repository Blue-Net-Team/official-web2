package com.bluenet.web.application.command.file;

import com.bluenet.web.domain.model.enumerate.FileType;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

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

    /**
     * 批量下载文件条目。
     * <p>
     * 用于指定单个文件在批量下载中的自定义名称。
     * </p>
     */
    public record BatchDownloadEntry(
            /** 文件ID */
            Long fileId,
            /** 自定义文件名（可不含扩展名，后端自动补全） */
            String filename) {
    }

    /**
     * 批量下载文件命令。
     * <p>
     * 用于将多个文件打包为 ZIP 下载。
     * </p>
     */
    public record BatchDownloadCommand(
            /** 文件条目列表 */
            List<BatchDownloadEntry> entries,
            /** ZIP 包名称 */
            String zipName) {
    }

    /**
     * 预签名上传准备命令。
     */
    public record PrepareUploadCommand(
            /** 原始文件名 */
            String filename,
            /** 文件类型 */
            FileType type,
            /** 文件大小（字节） */
            long size,
            /** 文件 Content-Type */
            String contentType) {
    }

    /**
     * 预签名上传确认命令。
     */
    public record ConfirmUploadCommand(
            /** 文件 ID */
            Long fileId,
            /** 回调令牌 */
            String callbackToken,
            /** 文件 MD5 */
            String md5,
            /** 文件大小（字节） */
            long size) {
    }
}
