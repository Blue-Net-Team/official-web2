package com.bluenet.judge.infrastructure.storage;

import com.bluenet.judge.infrastructure.config.ObjectStorageProperties;
import com.bluenet.judge.infrastructure.config.JudgeStorageProperties;
import io.minio.BucketExistsArgs;
import io.minio.GetObjectArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;

/**
 * MinIO 判题资产存储实现。
 * <p>
 * 复用主应用 OSS 连接信息，只读写判题专用 bucket。
 * </p>
 */
@Component
@RequiredArgsConstructor
@ConditionalOnExpression("'${storage.provider:minio}' == 'minio'")
public class MinioJudgeAssetStorage implements JudgeAssetStorage {
    private final JudgeStorageProperties judgeStorageProperties;
    private final ObjectStorageProperties objectStorageProperties;

    /**
     * 读取判题资产对象。
     *
     * @param objectKey
     *            判题 bucket 内对象键。
     * @return 文件字节内容。
     */
    @Override
    public byte[] get(String objectKey) {
        try {
            MinioClient client = client();
            return client.getObject(
                    GetObjectArgs.builder()
                            .bucket(judgeStorageProperties.bucket())
                            .object(objectKey)
                            .build())
                    .readAllBytes();
        } catch (Exception ex) {
            throw new RuntimeException("读取判题资产失败：" + objectKey, ex);
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
        try {
            MinioClient client = client();
            ensureBucket(client);
            client.putObject(
                    PutObjectArgs.builder()
                            .bucket(judgeStorageProperties.bucket())
                            .object(objectKey)
                            .stream(new ByteArrayInputStream(content), content.length, -1)
                            .contentType(contentType)
                            .build());
        } catch (Exception ex) {
            throw new RuntimeException("写入判题资产失败：" + objectKey, ex);
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
        try {
            MinioClient client = client();
            client.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(judgeStorageProperties.bucket())
                            .object(objectKey)
                            .build());
        } catch (Exception ex) {
            throw new RuntimeException("删除判题资产失败：" + objectKey, ex);
        }
    }

    /**
     * 创建 MinIO 客户端。
     *
     * @return MinIO 客户端。
     */
    private MinioClient client() {
        ObjectStorageProperties.Minio minio = objectStorageProperties.minio();
        return MinioClient.builder()
                .endpoint(minio.endpoint(), minio.port(), minio.useSsl())
                .credentials(minio.accessKey(), minio.secretKey())
                .build();
    }

    /**
     * 确保判题 bucket 存在。
     *
     * @param client
     *            MinIO 客户端。
     * @throws Exception
     *             查询或创建 bucket 失败时抛出。
     */
    private void ensureBucket(MinioClient client) throws Exception {
        // 复用同一个 OSS 服务，只把判题资产放入独立 bucket。
        boolean exists = client
                .bucketExists(BucketExistsArgs.builder().bucket(judgeStorageProperties.bucket()).build());
        if (!exists) {
            client.makeBucket(MakeBucketArgs.builder().bucket(judgeStorageProperties.bucket()).build());
        }
    }
}
