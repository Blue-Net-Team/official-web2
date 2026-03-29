package com.bluenet.web.domain.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 场地值对象
 * <p>
 * 封装场地完整信息，用于领域层传递
 * </p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VenueVO {
    /**
     * 场地ID
     */
    private Long id;

    /**
     * 场地名称
     */
    private String name;

    /**
     * 场地副标题
     */
    private String subtitle;

    /**
     * 场地描述
     */
    private String description;

    /**
     * 图片URL
     */
    private String imageUrl;

    /**
     * 图片文件ID
     */
    private Long imageFileId;

    /**
     * 排序权重
     */
    private Integer sortOrder;
}
