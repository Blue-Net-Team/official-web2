package com.bluenet.web.infrastructure.storage;

import com.aliyun.oss.ClientException;
import com.aliyun.oss.HttpMethod;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSException;
import com.aliyun.oss.model.GeneratePresignedUrlRequest;
import com.aliyun.oss.model.OSSObject;
import com.bluenet.web.domain.exception.DataNotFound;
import com.bluenet.web.domain.model.enumerate.FileType;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.net.URL;
import java.time.Duration;
import java.util.Date;

/**
 * 阿里云 OSS 对象存储适配器。
 * <p>
 * 负责将统一的 {@link ObjectStorage} 操作转换为阿里云 OSS SDK 调用。
 * </p>
 */
@Slf4j
@Component
@ConditionalOnExpression("'${storage.enabled:true}' == 'true' && '${storage.provider:minio}' == 'aliyun-oss'")
@ConditionalOnBean(OSS.class)
public class AliyunOssObjectStorage implements ObjectStorage {

    private final OSS ossClient;
    private final OSS publicOssClient;
    private final ObjectLocationResolver objectLocationResolver;

    public AliyunOssObjectStorage(
            OSS ossClient,
            @Qualifier("publicOssClient") OSS publicOssClient,
            ObjectLocationResolver objectLocationResolver) {
        this.ossClient = ossClient;
        this.publicOssClient = publicOssClient;
        this.objectLocationResolver = objectLocationResolver;
    }

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

    @Override
    public String getPresignedUploadUrl(FileType fileType, String filename, String contentType, long size,
            Duration expiry) {
        ObjectLocation location = objectLocationResolver.resolve(fileType, filename);
        try {
            Date expiration = new Date(System.currentTimeMillis() + expiry.toMillis());
            GeneratePresignedUrlRequest request = new GeneratePresignedUrlRequest(location.bucket(),
                    location.objectKey(), HttpMethod.PUT);
            request.setExpiration(expiration);
            if (contentType != null && !contentType.isBlank()) {
                request.setContentType(contentType);
            }
            URL url = publicOssClient.generatePresignedUrl(request);
            return url.toString();
        } catch (OSSException | ClientException e) {
            log.error(
                    "Error generating presigned upload URL from Aliyun OSS: {}/{}",
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
            Date expiration = new Date(System.currentTimeMillis() + expiry.toMillis());
            URL url = publicOssClient
                    .generatePresignedUrl(location.bucket(), location.objectKey(), expiration, HttpMethod.GET);
            return url.toString();
        } catch (OSSException | ClientException e) {
            log.error(
                    "Error generating presigned download URL from Aliyun OSS: {}/{}",
                    location.bucket(),
                    location.objectKey(),
                    e);
            throw new RuntimeException("Failed to generate presigned download URL: " + filename, e);
        }
    }

    @Override
    public byte[] getObjectHeader(FileType fileType, String filename, int bytes) {
        ObjectLocation location = objectLocationResolver.resolve(fileType, filename);
        try {
            com.aliyun.oss.model.GetObjectRequest request = new com.aliyun.oss.model.GetObjectRequest(location.bucket(),
                    location.objectKey());
            request.setRange(0, bytes - 1);
            OSSObject object = ossClient.getObject(request);
            try (InputStream is = object.getObjectContent()) {
                return is.readNBytes(bytes);
            }
        } catch (OSSException e) {
            if ("NoSuchKey".equals(e.getErrorCode()) || "NoSuchBucket".equals(e.getErrorCode())) {
                throw new DataNotFound("File not found: " + filename);
            }
            throw new RuntimeException("Failed to get object header from Aliyun OSS: " + filename, e);
        } catch (ClientException e) {
            throw new RuntimeException("Failed to get object header: " + filename, e);
        } catch (java.io.IOException e) {
            throw new RuntimeException("Failed to read object header: " + filename, e);
        }
    }

    @Override
    public StorageObjectMetadata headObject(FileType fileType, String filename) {
        ObjectLocation location = objectLocationResolver.resolve(fileType, filename);
        try {
            com.aliyun.oss.model.ObjectMetadata metadata = ossClient
                    .headObject(location.bucket(), location.objectKey());
            return new StorageObjectMetadata(metadata.getETag(), metadata.getContentType(),
                    metadata.getContentLength());
        } catch (OSSException e) {
            if ("NoSuchKey".equals(e.getErrorCode()) || "NoSuchBucket".equals(e.getErrorCode())) {
                log.warn("File not found in Aliyun OSS: {}/{}", location.bucket(), location.objectKey());
                throw new DataNotFound("File not found: " + filename);
            }
            log.error("Aliyun OSS error while getting metadata: {}/{}", location.bucket(), location.objectKey(), e);
            throw new RuntimeException("Failed to get object metadata from Aliyun OSS: " + filename, e);
        } catch (ClientException e) {
            log.error(
                    "Error getting object metadata from Aliyun OSS: {}/{}",
                    location.bucket(),
                    location.objectKey(),
                    e);
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
