package com.bluenet.web.infrastructure.storage;

import com.bluenet.web.domain.exception.DataNotFound;
import com.bluenet.web.domain.model.enumerate.FileType;
import com.bluenet.web.infrastructure.config.properties.StorageProperties;
import io.minio.BucketExistsArgs;
import io.minio.GetObjectArgs;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.StatObjectArgs;
import io.minio.StatObjectResponse;
import io.minio.errors.ErrorResponseException;
import io.minio.http.Method;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.util.StringUtils;

import java.io.InputStream;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * MinIO 对象存储适配器。
 * <p>
 * 负责将统一的 {@link ObjectStorage} 操作转换为 MinIO SDK 调用。
 * </p>
 */
@Slf4j
public class MinioObjectStorage implements ObjectStorage {

    private final MinioClient minioClient;
    private final MinioClient publicMinioClient;
    private final ObjectLocationResolver objectLocationResolver;
    private final StorageProperties storageProperties;

    public MinioObjectStorage(
            MinioClient minioClient,
            ObjectLocationResolver objectLocationResolver,
            StorageProperties storageProperties) {
        log.info("MinioObjectStorage constructor called");
        this.minioClient = minioClient;
        this.objectLocationResolver = objectLocationResolver;
        this.storageProperties = storageProperties;
        this.publicMinioClient = createPublicClient(storageProperties);
    }

    private static MinioClient createPublicClient(StorageProperties storageProperties) {
        String publicUrl = storageProperties.getMinio().getPublicUrl();
        if (StringUtils.hasText(publicUrl)) {
            try {
                MinioClient client = MinioClient.builder()
                        .endpoint(publicUrl)
                        .credentials(
                                storageProperties.getMinio().getAccessKey(),
                                storageProperties.getMinio().getSecretKey())
                        .build();
                log.info("MinIO public client initialized: {}", publicUrl);
                return client;
            } catch (Exception e) {
                log.error("Failed to initialize MinIO public client with URL: {}", publicUrl, e);
            }
        }
        return null;
    }

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

    private MinioClient presignedClient() {
        return publicMinioClient != null ? publicMinioClient : minioClient;
    }

    @Override
    public String getPresignedUploadUrl(FileType fileType, String filename, String contentType, long size,
            Duration expiry) {
        ObjectLocation location = objectLocationResolver.resolve(fileType, filename);
        try {
            Map<String, String> extraHeaders = new HashMap<>();
            if (contentType != null && !contentType.isBlank()) {
                extraHeaders.put("Content-Type", contentType);
            }
            return presignedClient().getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.PUT)
                            .bucket(location.bucket())
                            .object(location.objectKey())
                            .expiry((int) expiry.getSeconds())
                            .extraHeaders(extraHeaders)
                            .build());
        } catch (Exception e) {
            log.error(
                    "Error generating presigned upload URL from MinIO: {}/{}",
                    location.bucket(),
                    location.objectKey(),
                    e);
            throw new RuntimeException("Failed to generate presigned upload URL: " + filename, e);
        }
    }

    @Override
    public String getPresignedDownloadUrl(FileType fileType, String filename, Duration expiry) {
        ObjectLocation location = objectLocationResolver.resolve(fileType, filename);
        try {
            return presignedClient().getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)
                            .bucket(location.bucket())
                            .object(location.objectKey())
                            .expiry((int) expiry.getSeconds())
                            .build());
        } catch (Exception e) {
            log.error(
                    "Error generating presigned download URL from MinIO: {}/{}",
                    location.bucket(),
                    location.objectKey(),
                    e);
            throw new RuntimeException("Failed to generate presigned download URL: " + filename, e);
        }
    }

    @Override
    public byte[] getObjectHeader(FileType fileType, String filename, int bytes) {
        ObjectLocation location = objectLocationResolver.resolve(fileType, filename);
        try (InputStream is = minioClient.getObject(
                GetObjectArgs.builder()
                        .bucket(location.bucket())
                        .object(location.objectKey())
                        .offset(0L)
                        .length((long) bytes)
                        .build())) {
            return is.readNBytes(bytes);
        } catch (ErrorResponseException e) {
            if (e.response().code() == 404) {
                throw new com.bluenet.web.domain.exception.DataNotFound("File not found: " + filename);
            }
            throw new RuntimeException("Failed to get object header from MinIO: " + filename, e);
        } catch (Exception e) {
            throw new RuntimeException("Failed to get object header: " + filename, e);
        }
    }

    @Override
    public StorageObjectMetadata headObject(FileType fileType, String filename) {
        ObjectLocation location = objectLocationResolver.resolve(fileType, filename);
        try {
            StatObjectResponse stat = minioClient.statObject(
                    StatObjectArgs.builder()
                            .bucket(location.bucket())
                            .object(location.objectKey())
                            .build());
            return new StorageObjectMetadata(stat.etag(), stat.contentType(), stat.size());
        } catch (ErrorResponseException e) {
            if (e.response().code() == 404) {
                log.warn("File not found in MinIO: {}/{}", location.bucket(), location.objectKey());
                throw new com.bluenet.web.domain.exception.DataNotFound("File not found: " + filename);
            }
            log.error("MinIO error response while getting metadata: {}/{}", location.bucket(), location.objectKey(), e);
            throw new RuntimeException("Failed to get object metadata from MinIO: " + filename, e);
        } catch (Exception e) {
            log.error("Error getting object metadata from MinIO: {}/{}", location.bucket(), location.objectKey(), e);
            throw new RuntimeException("Failed to get object metadata: " + filename, e);
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
