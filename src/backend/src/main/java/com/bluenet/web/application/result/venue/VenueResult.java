package com.bluenet.web.application.result.venue;

/**
 * 场地聚合的应用层结果对象。
 * <p>
 * 封装了场地相关操作返回给 API 层的数据。
 * </p>
 */
public record VenueResult(
        /** 唯一标识 */
        Long id,
        /** 名称 */
        String name,
        /** 副标题 */
        String subtitle,
        /** 描述 */
        String description,
        /** 图片URL地址 */
        String imageUrl,
        /** 图片文件ID */
        Long imageFileId,
        /** 排序序号 */
        Integer sortOrder) {
}
