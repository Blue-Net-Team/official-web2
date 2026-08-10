package com.bluenet.web.domain.model.result;

import com.bluenet.web.domain.model.enumerate.FileStatus;
import com.bluenet.web.domain.model.enumerate.FileType;

/**
 * 预签名上传确认结果（领域结果对象）。
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
