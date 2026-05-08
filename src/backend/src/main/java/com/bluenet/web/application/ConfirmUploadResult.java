package com.bluenet.web.application;

import com.bluenet.web.domain.model.enumerate.FileStatus;
import com.bluenet.web.domain.model.enumerate.FileType;

/**
 * 预签名上传确认结果。
 */
public record ConfirmUploadResult(
        /** 文件 ID */
        Long fileId,
        /** 文件名 */
        String filename,
        /** 文件类型 */
        FileType type,
        /** 文件状态 */
        FileStatus status) {
}
