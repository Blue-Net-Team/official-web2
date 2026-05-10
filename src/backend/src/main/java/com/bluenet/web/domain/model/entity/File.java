package com.bluenet.web.domain.model.entity;

import com.bluenet.web.domain.model.enumerate.FileStatus;
import com.bluenet.web.domain.model.enumerate.FileType;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class File {
    /**
     * 当前对象在系统中的唯一标识。
     */
    private Long id;
    /**
     * 业务对象名称。
     */
    private String name;
    /**
     * 业务分类或枚举类型。
     */
    private FileType type;
    /**
     * 资源访问地址。
     */
    @Deprecated
    private String url;

    /**
     * 文件状态。
     */
    private FileStatus status;

    /**
     * 构造新文件聚合根 —— 带领域校验
     *
     * @param name
     *            文件名
     * @param type
     *            文件类型
     * @return 新的文件实体
     * @throws IllegalArgumentException
     *             如果名称或类型为空
     */
    public static File create(String name, FileType type) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("文件名不能为空");
        }
        if (type == null) {
            throw new IllegalArgumentException("文件类型不能为空");
        }
        return new File(null, name.trim(), type, null, FileStatus.PENDING);
    }

    /**
     * 从数据库重建 —— 跳过创建校验
     *
     * @param id
     *            文件ID
     * @param name
     *            文件名
     * @param type
     *            文件类型
     * @param url
     *            资源访问地址
     * @return 重建的文件实体
     */
    public static File reconstruct(Long id, String name, FileType type, String url) {
        return new File(id, name, type, url, FileStatus.ACTIVE);
    }

    public static File reconstruct(Long id, String name, FileType type, String url, FileStatus status) {
        return new File(id, name, type, url, status);
    }
}
