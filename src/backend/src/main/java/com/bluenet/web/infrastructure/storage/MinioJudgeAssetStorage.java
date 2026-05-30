package com.bluenet.web.infrastructure.storage;

import com.bluenet.web.infrastructure.config.properties.JudgeAssetStorageProperties;
import com.bluenet.web.infrastructure.config.properties.StorageProperties;
import io.minio.BucketExistsArgs;
import io.minio.GetObjectArgs;
import io.minio.GetObjectResponse;
import io.minio.ListObjectsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.Result;
import io.minio.messages.Item;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;

/**
 * 基于 MinIO 的判题资产存储实现。
 * <p>
 * 复用主应用 OSS 连接信息，只将对象写入判题专用 bucket。
 * </p>
 */
@Component
@RequiredArgsConstructor
@ConditionalOnExpression("'${storage.provider:minio}' == 'minio'")
public class MinioJudgeAssetStorage implements JudgeAssetStorage {
    private final JudgeAssetStorageProperties judgeProperties;
    private final StorageProperties storageProperties;

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
        try {
            MinioClient client = client();
            ensureBucket(client);
            client.putObject(
                    PutObjectArgs.builder()
                            .bucket(judgeProperties.getBucket())
                            .object(objectKey)
                            .stream(new ByteArrayInputStream(content), content.length, -1)
                            .contentType(contentType)
                            .build());
        } catch (Exception ex) {
            throw new RuntimeException("Failed to save judge asset: " + objectKey, ex);
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
        try {
            MinioClient client = client();
            client.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(judgeProperties.getBucket())
                            .object(objectKey)
                            .build());
        } catch (Exception ex) {
            throw new RuntimeException("Failed to delete judge asset: " + objectKey, ex);
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
        try {
            MinioClient client = client();
            Iterable<Result<Item>> results = client.listObjects(
                    ListObjectsArgs.builder()
                            .bucket(judgeProperties.getBucket())
                            .prefix(prefix)
                            .recursive(true)
                            .build());
            for (Result<Item> result : results) {
                Item item = result.get();
                client.removeObject(
                        RemoveObjectArgs.builder()
                                .bucket(judgeProperties.getBucket())
                                .object(item.objectName())
                                .build());
            }
        } catch (Exception ex) {
            throw new RuntimeException("Failed to delete judge assets by prefix: " + prefix, ex);
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
        try {
            MinioClient client = client();
            ensureBucket(client);
            try (GetObjectResponse stream = client.getObject(
                    GetObjectArgs.builder()
                            .bucket(judgeProperties.getBucket())
                            .object(objectKey)
                            .build())) {
                return stream.readAllBytes();
            }
        } catch (Exception ex) {
            throw new RuntimeException("Failed to read judge asset: " + objectKey, ex);
        }
    }

    /**
     * 创建使用主应用 OSS 连接参数的 MinIO 客户端。
     *
     * @return MinIO 客户端。
     */
    private MinioClient client() {
        StorageProperties.Minio minio = storageProperties.getMinio();
        return MinioClient.builder()
                .endpoint(
                        minio.getEndpoint(),
                        minio.getPort(),
                        Boolean.TRUE.equals(minio.getUseSSL()))
                .credentials(minio.getAccessKey(), minio.getSecretKey())
                .build();
    }

    /**
     * 确保判题 bucket 已存在。
     *
     * @param client
     *            MinIO 客户端。
     * @throws Exception
     *             bucket 查询或创建失败时抛出。
     */
    private void ensureBucket(MinioClient client) throws Exception {
        // The same OSS service is reused; only the bucket is separated from public
        // business files.
        boolean exists = client.bucketExists(BucketExistsArgs.builder().bucket(judgeProperties.getBucket()).build());
        if (!exists) {
            client.makeBucket(MakeBucketArgs.builder().bucket(judgeProperties.getBucket()).build());
        }
    }
}
