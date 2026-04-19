package com.bluenet.web.infrastructure.storage;

import com.aliyun.oss.ClientException;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSException;
import com.aliyun.oss.model.OSSObject;
import com.bluenet.web.domain.exception.DataNotFound;
import com.bluenet.web.domain.model.enumerate.FileType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.InputStream;

/**
 * 阿里云 OSS 对象存储适配器。
 * <p>
 * 负责将统一的 {@link ObjectStorage} 操作转换为阿里云 OSS SDK 调用。
 * </p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnExpression("'${storage.enabled:true}' == 'true' && '${storage.provider:minio}' == 'aliyun-oss'")
@ConditionalOnBean(OSS.class)
public class AliyunOssObjectStorage implements ObjectStorage {

    private final OSS ossClient;
    private final ObjectLocationResolver objectLocationResolver;

    @Override
    public String providerName() {
        return "aliyun-oss";
    }

    /**
     * 确保配置的单 bucket 已存在。
     */
    @Override
    public void ensureBucket() {
        String bucket = objectLocationResolver.resolve(FileType.AVATAR, "__bucket_check__").bucket();
        try {
            if (!ossClient.doesBucketExist(bucket)) {
                ossClient.createBucket(bucket);
                log.info("Created Aliyun OSS bucket: {}", bucket);
            } else {
                log.debug("Aliyun OSS bucket already exists: {}", bucket);
            }
        } catch (OSSException | ClientException e) {
            log.error("Failed to initialize Aliyun OSS bucket: {}", bucket, e);
            throw new RuntimeException("Failed to initialize Aliyun OSS bucket: " + bucket, e);
        }
    }

    /**
     * 保存文件到阿里云 OSS。
     */
    @Override
    public void put(FileType fileType, String filename, InputStream inputStream) {
        if (inputStream == null) {
            throw new IllegalArgumentException("InputStream cannot be null");
        }
        ObjectLocation location = objectLocationResolver.resolve(fileType, filename);
        try {
            ossClient.putObject(location.bucket(), location.objectKey(), inputStream);
            log.debug("File saved successfully: {}/{}", location.bucket(), location.objectKey());
        } catch (OSSException | ClientException e) {
            log.error("Error saving file to Aliyun OSS: {}/{}", location.bucket(), location.objectKey(), e);
            throw new RuntimeException("Failed to save file: " + filename, e);
        }
    }

    /**
     * 从阿里云 OSS 加载文件。
     */
    @Override
    public Resource get(FileType fileType, String filename) {
        ObjectLocation location = objectLocationResolver.resolve(fileType, filename);
        try (OSSObject ossObject = ossClient.getObject(location.bucket(), location.objectKey());
                InputStream inputStream = ossObject.getObjectContent()) {
            byte[] data = inputStream.readAllBytes();
            log.debug("File loaded successfully: {}/{}", location.bucket(), location.objectKey());
            return resource(filename, data);
        } catch (OSSException e) {
            if ("NoSuchKey".equals(e.getErrorCode()) || "NoSuchBucket".equals(e.getErrorCode())) {
                log.warn("File not found in Aliyun OSS: {}/{}", location.bucket(), location.objectKey());
                throw new DataNotFound("File not found: " + filename);
            }
            log.error("Aliyun OSS error while loading file: {}/{}", location.bucket(), location.objectKey(), e);
            throw new RuntimeException("Failed to load file from Aliyun OSS: " + filename, e);
        } catch (Exception e) {
            log.error("Error loading file from Aliyun OSS: {}/{}", location.bucket(), location.objectKey(), e);
            throw new RuntimeException("Failed to load file: " + filename, e);
        }
    }

    /**
     * 删除阿里云 OSS 中的文件对象。
     */
    @Override
    public void delete(FileType fileType, String filename) {
        ObjectLocation location = objectLocationResolver.resolve(fileType, filename);
        try {
            ossClient.deleteObject(location.bucket(), location.objectKey());
            log.debug("File deleted successfully from Aliyun OSS: {}/{}", location.bucket(), location.objectKey());
        } catch (OSSException | ClientException e) {
            log.error("Error deleting file from Aliyun OSS: {}/{}", location.bucket(), location.objectKey(), e);
            throw new RuntimeException("Failed to delete file from Aliyun OSS: " + filename, e);
        }
    }

    /**
     * 检查阿里云 OSS bucket 是否可访问。
     */
    @Override
    public void checkHealth() {
        try {
            ossClient.doesBucketExist(objectLocationResolver.resolve(FileType.AVATAR, "__health_check__").bucket());
        } catch (OSSException | ClientException e) {
            throw new RuntimeException("Aliyun OSS health check failed", e);
        }
    }

    private Resource resource(String filename, byte[] data) {
        return new ByteArrayResource(data) {
            @Override
            public @NonNull String getFilename() {
                return filename;
            }
        };
    }
}
