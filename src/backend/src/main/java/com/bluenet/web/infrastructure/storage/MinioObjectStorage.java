package com.bluenet.web.infrastructure.storage;

import com.bluenet.web.domain.exception.DataNotFound;
import com.bluenet.web.domain.model.enumerate.FileType;
import io.minio.BucketExistsArgs;
import io.minio.GetObjectArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.errors.ErrorResponseException;
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
 * MinIO 对象存储适配器。
 * <p>
 * 负责将统一的 {@link ObjectStorage} 操作转换为 MinIO SDK 调用。
 * </p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnExpression("'${storage.enabled:true}' == 'true' && '${storage.provider:minio}' == 'minio'")
@ConditionalOnBean(MinioClient.class)
public class MinioObjectStorage implements ObjectStorage {

    private final MinioClient minioClient;
    private final ObjectLocationResolver objectLocationResolver;

    @Override
    public String providerName() {
        return "minio";
    }

    /**
     * 确保配置的单 bucket 已存在。
     */
    @Override
    public void ensureBucket() {
        String bucket = objectLocationResolver.resolve(FileType.AVATAR, "__bucket_check__").bucket();
        try {
            boolean exists = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucket).build());
            if (!exists) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucket).build());
                log.info("Created MinIO bucket: {}", bucket);
            } else {
                log.debug("MinIO bucket already exists: {}", bucket);
            }
        } catch (Exception e) {
            log.error("Failed to initialize MinIO bucket: {}", bucket, e);
            throw new RuntimeException("Failed to initialize MinIO bucket: " + bucket, e);
        }
    }

    /**
     * 保存文件到 MinIO。
     */
    @Override
    public void put(FileType fileType, String filename, InputStream inputStream) {
        if (inputStream == null) {
            throw new IllegalArgumentException("InputStream cannot be null");
        }
        ObjectLocation location = objectLocationResolver.resolve(fileType, filename);
        try {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(location.bucket())
                            .object(location.objectKey())
                            .stream(inputStream, -1, 10485760)
                            .build());
            log.debug("File saved successfully: {}/{}", location.bucket(), location.objectKey());
        } catch (Exception e) {
            log.error("Error saving file to MinIO: {}/{}", location.bucket(), location.objectKey(), e);
            throw new RuntimeException("Failed to save file: " + filename, e);
        }
    }

    /**
     * 从 MinIO 加载文件。
     */
    @Override
    public Resource get(FileType fileType, String filename) {
        ObjectLocation location = objectLocationResolver.resolve(fileType, filename);
        try (InputStream inputStream = minioClient.getObject(
                GetObjectArgs.builder()
                        .bucket(location.bucket())
                        .object(location.objectKey())
                        .build())) {
            byte[] data = inputStream.readAllBytes();
            log.debug("File loaded successfully: {}/{}", location.bucket(), location.objectKey());
            return resource(filename, data);
        } catch (ErrorResponseException e) {
            if (e.response().code() == 404) {
                log.warn("File not found in MinIO: {}/{}", location.bucket(), location.objectKey());
                throw new DataNotFound("File not found: " + filename);
            }
            log.error("MinIO error response while loading file: {}/{}", location.bucket(), location.objectKey(), e);
            throw new RuntimeException("Failed to load file from MinIO: " + filename, e);
        } catch (Exception e) {
            log.error("Error loading file from MinIO: {}/{}", location.bucket(), location.objectKey(), e);
            throw new RuntimeException("Failed to load file: " + filename, e);
        }
    }

    /**
     * 删除 MinIO 中的文件对象。
     */
    @Override
    public void delete(FileType fileType, String filename) {
        ObjectLocation location = objectLocationResolver.resolve(fileType, filename);
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(location.bucket())
                            .object(location.objectKey())
                            .build());
            log.debug("File deleted successfully from MinIO: {}/{}", location.bucket(), location.objectKey());
        } catch (Exception e) {
            log.error("Error deleting file from MinIO: {}/{}", location.bucket(), location.objectKey(), e);
            throw new RuntimeException("Failed to delete file from MinIO: " + filename, e);
        }
    }

    /**
     * 检查 MinIO bucket 是否可访问。
     */
    @Override
    public void checkHealth() {
        try {
            minioClient.bucketExists(
                    BucketExistsArgs.builder()
                            .bucket(objectLocationResolver.resolve(FileType.AVATAR, "__health_check__").bucket())
                            .build());
        } catch (Exception e) {
            throw new RuntimeException("MinIO health check failed", e);
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
