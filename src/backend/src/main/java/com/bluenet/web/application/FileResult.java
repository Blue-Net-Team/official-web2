package com.bluenet.web.application;

import com.bluenet.web.domain.model.enumerate.FileStatus;
import com.bluenet.web.domain.model.enumerate.FileType;

/**
 * 文件聚合的应用层结果对象。
 * <p>
 * 封装了文件相关操作返回给 API 层的数据。
 * </p>
 */
public record FileResult(
        /** 唯一标识 */
        Long id,
        /** 名称 */
        String name,
        /** 类型 */
        FileType type,
        /** URL地址 */
        String url,
        /** 文件状态 */
        FileStatus status) {
}
