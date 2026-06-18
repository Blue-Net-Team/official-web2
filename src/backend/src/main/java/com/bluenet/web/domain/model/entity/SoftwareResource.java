package com.bluenet.web.domain.model.entity;

import com.bluenet.web.domain.model.enumerate.SoftwareResourceDirection;
import com.bluenet.web.domain.model.enumerate.SoftwareResourceStatus;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 软件资源聚合根。
 * <p>
 * 存储实验室各方向及通用软件的外部下载链接元数据。
 * </p>
 */
@Data
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class SoftwareResource {

    /**
     * 当前对象在系统中的唯一标识。
     */
    private Long id;

    /**
     * 软件名称。
     */
    private String name;

    /**
     * 所属方向。
     */
    private SoftwareResourceDirection direction;

    /**
     * 分类，如 IDE、工具链、库等。
     */
    private String category;

    /**
     * 描述或使用说明。
     */
    private String description;

    /**
     * 外部下载链接。
     */
    private String externalUrl;

    /**
     * 排序权重，越小越靠前。
     */
    private Integer sortOrder;

    /**
     * 启用状态。
     */
    private SoftwareResourceStatus status;

    private SoftwareResource(Long id, String name, SoftwareResourceDirection direction, String category,
            String description, String externalUrl, Integer sortOrder, SoftwareResourceStatus status) {
        this.id = id;
        this.name = name;
        this.direction = direction;
        this.category = category;
        this.description = description;
        this.externalUrl = externalUrl;
        this.sortOrder = sortOrder;
        this.status = status;
    }

    /**
     * 构造新软件资源聚合根 —— 带领域校验。
     */
    public static SoftwareResource create(String name, SoftwareResourceDirection direction, String category,
            String description, String externalUrl, Integer sortOrder) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("软件名称不能为空");
        }
        if (direction == null) {
            throw new IllegalArgumentException("方向不能为空");
        }
        if (externalUrl == null || externalUrl.isBlank()) {
            throw new IllegalArgumentException("外部链接不能为空");
        }
        return new SoftwareResource(null, name.trim(), direction, category, description, externalUrl.trim(),
                sortOrder != null ? sortOrder : 0, SoftwareResourceStatus.ACTIVE);
    }

    /**
     * 从数据库重建 —— 跳过创建校验。
     */
    public static SoftwareResource reconstruct(Long id, String name, SoftwareResourceDirection direction,
            String category, String description, String externalUrl, Integer sortOrder, SoftwareResourceStatus status) {
        return new SoftwareResource(id, name, direction, category, description, externalUrl, sortOrder, status);
    }

    /**
     * 更新软件资源信息。
     */
    public void update(String name, SoftwareResourceDirection direction, String category, String description,
            String externalUrl, Integer sortOrder, SoftwareResourceStatus status) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("软件名称不能为空");
        }
        if (direction == null) {
            throw new IllegalArgumentException("方向不能为空");
        }
        if (externalUrl == null || externalUrl.isBlank()) {
            throw new IllegalArgumentException("外部链接不能为空");
        }
        this.name = name.trim();
        this.direction = direction;
        this.category = category;
        this.description = description;
        this.externalUrl = externalUrl.trim();
        this.sortOrder = sortOrder != null ? sortOrder : 0;
        if (status != null) {
            this.status = status;
        }
    }

    /**
     * 切换启用状态。
     */
    public void changeStatus(SoftwareResourceStatus status) {
        if (status == null) {
            throw new IllegalArgumentException("状态不能为空");
        }
        this.status = status;
    }
}
