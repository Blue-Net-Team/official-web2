package com.bluenet.web.application.service;

import com.bluenet.web.application.result.file.FileDownloadResult;
import com.bluenet.web.application.result.file.FileResult;
import com.bluenet.web.application.command.file.FileCommands;
import com.bluenet.web.domain.model.result.ConfirmUploadResult;
import com.bluenet.web.domain.model.result.PresignedUploadResult;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

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

    /**
     * 预签名上传准备。
     *
     * @param command
     *            准备命令
     * @return 准备结果
     */
    PresignedUploadResult prepareUpload(FileCommands.PrepareUploadCommand command);

    /**
     * 预签名上传确认。
     *
     * @param command
     *            确认命令
     * @return 确认结果
     */
    ConfirmUploadResult confirmUpload(FileCommands.ConfirmUploadCommand command);

    /**
     * 获取预签名下载 URL。
     *
     * @param command
     *            下载命令
     * @return 预签名 GET URL
     */
    String getPresignedDownloadUrl(FileCommands.DownloadFileCommand command);

    /**
     * 批量下载文件并流式输出。
     *
     * @param command
     *            批量下载命令
     * @return 流式响应体
     */
    StreamingResponseBody downloadBatchStream(FileCommands.BatchDownloadCommand command);
}
