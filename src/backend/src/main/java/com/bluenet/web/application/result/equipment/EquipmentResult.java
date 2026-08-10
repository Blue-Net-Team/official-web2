package com.bluenet.web.application.result.equipment;

/**
 * 设备聚合的应用层结果对象。
 * <p>
 * 封装了设备相关操作返回给 API 层的数据。
 * </p>
 */
public record EquipmentResult(
        /** 唯一标识 */
        Long id,
        /** 名称 */
        String name,
        /** 品牌 */
        String brand,
        /** 描述 */
        String description,
        /** 图片URL地址 */
        String imageUrl,
        /** 图片文件ID */
        Long imageFileId,
        /** 排序序号 */
        Integer sortOrder) {
}
