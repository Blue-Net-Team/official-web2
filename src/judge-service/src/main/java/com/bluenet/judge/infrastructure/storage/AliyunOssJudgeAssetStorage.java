package com.bluenet.judge.infrastructure.storage;

import com.aliyun.oss.ClientException;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSException;
import com.aliyun.oss.model.ObjectMetadata;
import com.aliyun.oss.model.OSSObject;
import com.bluenet.judge.infrastructure.config.JudgeStorageProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

/**
 * 阿里云 OSS 判题资产存储实现。
 * <p>
 * 负责将 {@link JudgeAssetStorage} 操作转换为阿里云 OSS SDK 调用。
 * </p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnExpression("'${storage.provider:minio}' == 'aliyun-oss'")
@ConditionalOnBean(OSS.class)
public class AliyunOssJudgeAssetStorage implements JudgeAssetStorage {

    private final OSS ossClient;
    private final JudgeStorageProperties judgeStorageProperties;

    /**
     * 读取判题资产对象。
     *
     * @param objectKey
     *            判题 bucket 内对象键。
     * @return 文件字节内容。
     */
    @Override
    public byte[] get(String objectKey) {
        String bucket = judgeStorageProperties.bucket();
        try (OSSObject ossObject = ossClient.getObject(bucket, objectKey);
                InputStream inputStream = ossObject.getObjectContent()) {
            byte[] data = inputStream.readAllBytes();
            log.debug("Judge asset loaded from Aliyun OSS: {}/{}", bucket, objectKey);
            return data;
        } catch (OSSException e) {
            log.error("Aliyun OSS error while loading judge asset: {}/{}", bucket, objectKey, e);
            throw new RuntimeException("读取判题资产失败：" + objectKey, e);
        } catch (Exception e) {
            log.error("Error loading judge asset from Aliyun OSS: {}/{}", bucket, objectKey, e);
            throw new RuntimeException("读取判题资产失败：" + objectKey, e);
        }
    }

    /**
     * 写入判题资产对象。
     *
     * @param objectKey
     *            判题 bucket 内对象键。
     * @param content
     *            文件字节内容。
     * @param contentType
     *            文件内容类型。
     */
    @Override
    public void put(String objectKey, byte[] content, String contentType) {
        String bucket = judgeStorageProperties.bucket();
        ObjectMetadata metadata = new ObjectMetadata();
        if (contentType != null && !contentType.isBlank()) {
            metadata.setContentType(contentType);
        }
        metadata.setContentLength(content.length);

        ByteArrayInputStream inputStream = new ByteArrayInputStream(content);
        try {
            ossClient.putObject(bucket, objectKey, inputStream, metadata);
            log.debug("Judge asset saved to Aliyun OSS: {}/{}", bucket, objectKey);
        } catch (OSSException | ClientException e) {
            log.error("Error saving judge asset to Aliyun OSS: {}/{}", bucket, objectKey, e);
            throw new RuntimeException("写入判题资产失败：" + objectKey, e);
        }
    }

    /**
     * 删除判题资产对象。
     *
     * @param objectKey
     *            判题 bucket 内对象键。
     */
    @Override
    public void delete(String objectKey) {
        String bucket = judgeStorageProperties.bucket();
        try {
            ossClient.deleteObject(bucket, objectKey);
            log.debug("Judge asset deleted from Aliyun OSS: {}/{}", bucket, objectKey);
        } catch (OSSException | ClientException e) {
            log.error("Error deleting judge asset from Aliyun OSS: {}/{}", bucket, objectKey, e);
            throw new RuntimeException("删除判题资产失败：" + objectKey, e);
        }
    }
}
