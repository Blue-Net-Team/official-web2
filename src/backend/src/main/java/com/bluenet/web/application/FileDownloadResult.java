package com.bluenet.web.application;

import org.springframework.core.io.Resource;

/**
 * 文件下载聚合的应用层结果对象。
 * <p>
 * 封装了文件下载相关操作返回给 API 层的数据。
 * </p>
 */
public record FileDownloadResult(
        /** 资源 */
        Resource resource,
        /** 文件名 */
        String filename) {
}
