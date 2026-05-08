package com.bluenet.web.domain.model.vo;

import com.bluenet.web.domain.model.enumerate.FileStatus;
import com.bluenet.web.domain.model.enumerate.FileType;

/**
 * 预签名上传确认结果（领域 VO）。
 */
public record ConfirmUploadVO(
        /** 文件 ID */
        Long fileId,
        /** 文件名 */
        String filename,
        /** 文件类型 */
        FileType type,
        /** 文件状态 */
        FileStatus status) {
}
