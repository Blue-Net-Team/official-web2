package com.bluenet.web.domain.model.entity;

import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 学院聚合根
 * <p>
 * 承载学院相关的业务规则和行为
 * </p>
 */
@Data
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class College {
    /**
     * 当前对象在系统中的唯一标识。
     */
    private Long id;
    /**
     * 业务对象名称。
     */
    private String name;

    private College(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    /**
     * 构造新聚合根 —— 带领域校验
     *
     * @param name
     *            学院名称
     * @return 新的学院实体
     * @throws IllegalArgumentException
     *             如果名称为空
     */
    public static College create(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("学院名称不能为空");
        }
        return new College(null, name.trim());
    }

    /**
     * 从数据库重建 —— 跳过创建校验
     *
     * @param id
     *            学院ID
     * @param name
     *            学院名称
     * @return 重建的学院实体
     */
    public static College reconstruct(Long id, String name) {
        return new College(id, name);
    }

    /**
     * 重命名学院 —— 带领域校验
     *
     * @param newName
     *            新名称
     * @throws IllegalArgumentException
     *             如果新名称为空
     */
    public void rename(String newName) {
        if (newName == null || newName.isBlank()) {
            throw new IllegalArgumentException("学院名称不能为空");
        }
        this.name = newName.trim();
    }
}
