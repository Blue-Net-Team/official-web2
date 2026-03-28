package com.bluenet.web.domain.model.vo;

import com.bluenet.web.domain.model.enumerate.Direction;
import com.bluenet.web.domain.model.enumerate.ImageType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

/**
 * 介绍图片领域值对象
 * <p>
 * 封装介绍图片相关的领域数据，用于在领域层传递介绍图片信息。 包含文件URL信息，用于前端直接展示。
 * </p>
 */
@Data
@AllArgsConstructor
@Builder
public class IntroduceImageVO {
    /**
     * 图片ID
     */
    private Long id;

    /**
     * 图片类型
     */
    private ImageType type;

    /**
     * 图片描述
     */
    private String description;

    /**
     * 关联的文件ID
     */
    private Long fileId;

    /**
     * 方向（仅在 type=DIRECTION 时有效）
     */
    private Direction direction;

    /**
     * 文件URL（用于前端直接访问）
     */
    private String fileUrl;

    /**
     * 竞赛ID（仅在 type=COMPETITION 时有效）
     */
    private Long competitionId;

    /**
     * 排序权重
     */
    private Integer sortOrder;
}
