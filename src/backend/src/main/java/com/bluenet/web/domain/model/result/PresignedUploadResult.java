package com.bluenet.web.domain.model.result;

import com.bluenet.web.domain.model.enumerate.FileType;

/**
 * 预签名上传准备结果（领域结果对象）。
 */
public record PresignedUploadResult(
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
