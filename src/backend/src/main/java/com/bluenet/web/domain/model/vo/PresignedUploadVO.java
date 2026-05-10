package com.bluenet.web.domain.model.vo;

import com.bluenet.web.domain.model.enumerate.FileType;

/**
 * 预签名上传准备结果（领域 VO）。
 */
public record PresignedUploadVO(
        /** 文件 ID */
        Long fileId,
        /** 预签名上传 URL */
        String uploadUrl,
        /** 回调令牌 */
        String callbackToken,
        /** 生成的文件名 */
        String filename,
        /** 文件类型 */
        FileType type) {
}
