package com.bluenet.web.application.result.enrollform;

import java.time.LocalDateTime;

/**
 * 报名表的应用层结果对象。
 * <p>
 * 封装当前报名表返回给 API 层的数据。
 * </p>
 */
public record EnrollFormResult(
        /** 文件ID */
        Long fileId,
        /** 上传时间（文件记录创建时间） */
        LocalDateTime createdAt) {
}
