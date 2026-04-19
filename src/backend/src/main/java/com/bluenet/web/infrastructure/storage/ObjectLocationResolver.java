package com.bluenet.web.infrastructure.storage;

import com.bluenet.web.domain.model.enumerate.FileType;
import com.bluenet.web.infrastructure.config.properties.StorageProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * 对象存储路径解析器。
 * <p>
 * 当前采用单 bucket 模型：bucket 来自 {@code storage.bucket}，对象 key 使用
 * {@code FileType.getValue() + "/" + filename} 在 bucket 内按文件类型分目录。
 * </p>
 */
@Component
@RequiredArgsConstructor
public class ObjectLocationResolver {

    private final StorageProperties storageProperties;

    /**
     * 根据文件类型和文件名解析实际存储位置。
     *
     * @param fileType
     *            文件业务类型
     * @param filename
     *            文件名
     * @return bucket 和 object key
     */
    public ObjectLocation resolve(FileType fileType, String filename) {
        if (fileType == null) {
            throw new IllegalArgumentException("FileType cannot be null");
        }
        if (!StringUtils.hasText(filename)) {
            throw new IllegalArgumentException("Filename cannot be null or empty");
        }
        String bucket = storageProperties.getBucket();
        if (!StringUtils.hasText(bucket)) {
            throw new IllegalStateException("storage.bucket must not be empty");
        }
        return new ObjectLocation(bucket, fileType.getValue() + "/" + filename);
    }
}
