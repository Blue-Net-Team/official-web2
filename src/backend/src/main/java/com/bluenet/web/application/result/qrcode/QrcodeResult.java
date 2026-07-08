package com.bluenet.web.application.result.qrcode;

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
        Long fileId,
        /** 方向（仅考核群） */
        String direction,
        /** 考核轮次（仅考核群） */
        Integer epoch,
        /** 是否三方向共用（仅考核群） */
        Boolean isShared) {

    /**
     * 简化构造（仅咨询群）
     */
    public QrcodeResult(Long id, Long fileId) {
        this(id, fileId, null, null, null);
    }
}
