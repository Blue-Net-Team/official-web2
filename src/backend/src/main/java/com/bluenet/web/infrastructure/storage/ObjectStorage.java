package com.bluenet.web.infrastructure.storage;

import com.bluenet.web.domain.model.enumerate.FileType;
import org.springframework.core.io.Resource;

import java.io.InputStream;

/**
 * 对象存储统一接口。
 * <p>
 * 文件仓储只依赖该接口保存、读取和删除对象，具体由 MinIO 或阿里云 OSS 适配器实现。
 * </p>
 */
public interface ObjectStorage {

    /**
     * 当前对象存储提供方名称，用于日志和健康检查输出。
     */
    String providerName();

    /**
     * 初始化并确保配置的 bucket 可用。
     */
    void ensureBucket();

    /**
     * 保存对象。
     *
     * @param fileType
     *            文件业务类型，用于生成对象目录前缀
     * @param filename
     *            文件名
     * @param inputStream
     *            文件输入流
     */
    void put(FileType fileType, String filename, InputStream inputStream);

    /**
     * 读取对象。
     *
     * @param fileType
     *            文件业务类型
     * @param filename
     *            文件名
     * @return 文件资源
     */
    Resource get(FileType fileType, String filename);

    /**
     * 删除对象。
     *
     * @param fileType
     *            文件业务类型
     * @param filename
     *            文件名
     */
    void delete(FileType fileType, String filename);

    /**
     * 检查对象存储连接和 bucket 可访问性。
     */
    void checkHealth();
}
