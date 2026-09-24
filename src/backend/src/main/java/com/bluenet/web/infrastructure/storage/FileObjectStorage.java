package com.bluenet.web.infrastructure.storage;

import java.io.InputStream;
import java.time.Duration;

import io.github.ivencn.infra.storage.StorageProperties;
import io.github.ivencn.infra.storage.api.ObjectStorage;
import io.github.ivencn.infra.storage.api.StorageObjectMetadata;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.bluenet.web.domain.model.enumerate.FileType;

/**
 * 面向业务的文件对象存储门面。
 *
 * <p>
 * 在 iven 框架的 key-based {@link ObjectStorage} 之上保留 {@link FileType} 业务语义： 对象 key
 * 规则为 {@code <FileType.getValue()>/<filename>}，bucket 由
 * {@code iven.storage.bucket} 配置。
 * </p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class FileObjectStorage {

    private final ObjectStorage objectStorage;
    private final StorageProperties storageProperties;

    /**
     * 根据文件类型和文件名解析对象 key。
     */
    public String resolveKey(FileType fileType, String filename) {
        if (fileType == null) {
            throw new IllegalArgumentException("FileType cannot be null");
        }
        if (!StringUtils.hasText(filename)) {
            throw new IllegalArgumentException("Filename cannot be null or empty");
        }
        return fileType.getValue() + "/" + filename;
    }

    public String providerName() {
        return objectStorage.providerName();
    }

    public void put(FileType fileType, String filename, InputStream inputStream) {
        objectStorage.put(resolveKey(fileType, filename), inputStream);
    }

    public Resource get(FileType fileType, String filename) {
        return objectStorage.get(resolveKey(fileType, filename));
    }

    public void delete(FileType fileType, String filename) {
        objectStorage.delete(resolveKey(fileType, filename));
    }

    public void checkHealth() {
        objectStorage.checkHealth();
    }

    public String getPresignedUploadUrl(FileType fileType, String filename, String contentType, long size,
            Duration expiry) {
        return objectStorage.getPresignedUploadUrl(resolveKey(fileType, filename), contentType, expiry);
    }

    public String getPresignedDownloadUrl(FileType fileType, String filename, Duration expiry) {
        return objectStorage.getPresignedDownloadUrl(resolveKey(fileType, filename), expiry);
    }

    public StorageObjectMetadata headObject(FileType fileType, String filename) {
        return objectStorage.headObject(resolveKey(fileType, filename));
    }

    public byte[] getObjectHeader(FileType fileType, String filename, int bytes) {
        return objectStorage.getObjectHeader(resolveKey(fileType, filename), bytes);
    }

    /**
     * 获取统一 bucket 名称。
     */
    public String bucket() {
        return storageProperties.getBucket();
    }
}
