package com.bluenet.web.application;

/**
 * 二维码聚合的应用层结果对象。
 * <p>
 * 封装了二维码相关操作返回给 API 层的数据。
 * </p>
 */
public record QrcodeResult(
        /** 唯一标识 */
        Long id,
        /** 文件ID */
        Long fileId) {
}
