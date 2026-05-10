package com.bluenet.web.infrastructure.storage;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSException;
import com.aliyun.oss.model.ObjectListing;
import com.aliyun.oss.model.ObjectMetadata;
import com.aliyun.oss.model.OSSObject;
import com.aliyun.oss.model.OSSObjectSummary;
import com.bluenet.web.infrastructure.config.properties.JudgeAssetStorageProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

/**
 * 基于阿里云 OSS 的判题资产存储实现。
 * <p>
 * 复用主应用 OSS 客户端连接，只将对象写入判题专用 bucket。
 * </p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnExpression("'${storage.provider:minio}' == 'aliyun-oss'")
@ConditionalOnBean(OSS.class)
public class AliyunOssJudgeAssetStorage implements JudgeAssetStorage {

    private final OSS ossClient;
    private final JudgeAssetStorageProperties judgeProperties;

    /**
     * 保存判题资产对象。
     *
     * @param objectKey
     *            判题 bucket 内的对象键。
     * @param content
     *            文件字节内容。
     * @param contentType
     *            文件内容类型。
     */
    @Override
    public void put(String objectKey, byte[] content, String contentType) {
        String bucket = judgeProperties.getBucket();
        ObjectMetadata metadata = new ObjectMetadata();
        if (contentType != null && !contentType.isBlank()) {
            metadata.setContentType(contentType);
        }
        metadata.setContentLength(content.length);

        try {
            ossClient.putObject(bucket, objectKey, new ByteArrayInputStream(content), metadata);
            log.debug("Judge asset saved to Aliyun OSS: {}/{}", bucket, objectKey);
        } catch (OSSException e) {
            log.error("Error saving judge asset to Aliyun OSS: {}/{}", bucket, objectKey, e);
            throw new RuntimeException("Failed to save judge asset: " + objectKey, e);
        } catch (Exception e) {
            log.error("IO error saving judge asset to Aliyun OSS: {}/{}", bucket, objectKey, e);
            throw new RuntimeException("Failed to save judge asset: " + objectKey, e);
        }
    }

    /**
     * 删除判题资产对象。
     *
     * @param objectKey
     *            判题 bucket 内的对象键。
     */
    @Override
    public void delete(String objectKey) {
        String bucket = judgeProperties.getBucket();
        try {
            ossClient.deleteObject(bucket, objectKey);
            log.debug("Judge asset deleted from Aliyun OSS: {}/{}", bucket, objectKey);
        } catch (OSSException e) {
            log.error("Error deleting judge asset from Aliyun OSS: {}/{}", bucket, objectKey, e);
            throw new RuntimeException("Failed to delete judge asset: " + objectKey, e);
        }
    }

    /**
     * 按前缀批量删除判题资产对象。
     *
     * @param prefix
     *            对象键前缀。
     */
    @Override
    public void deleteByPrefix(String prefix) {
        String bucket = judgeProperties.getBucket();
        try {
            ObjectListing listing = ossClient.listObjects(bucket, prefix);
            for (OSSObjectSummary summary : listing.getObjectSummaries()) {
                ossClient.deleteObject(bucket, summary.getKey());
                log.debug("Judge asset deleted from Aliyun OSS: {}/{}", bucket, summary.getKey());
            }
        } catch (OSSException e) {
            log.error("Error deleting judge assets by prefix from Aliyun OSS: {}/{}", bucket, prefix, e);
            throw new RuntimeException("Failed to delete judge assets by prefix: " + prefix, e);
        }
    }

    /**
     * 读取判题资产对象内容。
     *
     * @param objectKey
     *            判题 bucket 内的对象键。
     * @return 对象字节内容；不存在时返回空字节数组。
     */
    @Override
    public byte[] get(String objectKey) {
        String bucket = judgeProperties.getBucket();
        try (OSSObject ossObject = ossClient.getObject(bucket, objectKey);
                InputStream inputStream = ossObject.getObjectContent()) {
            byte[] data = inputStream.readAllBytes();
            log.debug("Judge asset loaded from Aliyun OSS: {}/{}", bucket, objectKey);
            return data;
        } catch (OSSException e) {
            log.error("Error loading judge asset from Aliyun OSS: {}/{}", bucket, objectKey, e);
            throw new RuntimeException("Failed to read judge asset: " + objectKey, e);
        } catch (Exception e) {
            log.error("Error reading judge asset from Aliyun OSS: {}/{}", bucket, objectKey, e);
            throw new RuntimeException("Failed to read judge asset: " + objectKey, e);
        }
    }
}
