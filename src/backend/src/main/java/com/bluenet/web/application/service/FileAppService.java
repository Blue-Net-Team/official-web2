package com.bluenet.web.application.service;

import com.bluenet.web.application.FileDownloadResult;
import com.bluenet.web.application.FileResult;
import com.bluenet.web.application.command.file.FileCommands;

/**
 * 文件应用服务接口。
 * <p>
 * 定义了文件聚合在应用层的所有业务操作。
 * </p>
 */
public interface FileAppService {
    /**
     * 统一文件上传
     *
     * @param command
     *            上传文件命令
     * @return 文件结果
     */
    FileResult uploadFile(FileCommands.UploadFileCommand command);

    /**
     * 下载文件
     *
     * @param command
     *            下载文件命令
     * @return 文件下载结果
     */
    FileDownloadResult downloadFile(FileCommands.DownloadFileCommand command);
}
