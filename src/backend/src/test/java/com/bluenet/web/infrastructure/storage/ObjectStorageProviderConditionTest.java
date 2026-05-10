package com.bluenet.web.infrastructure.storage;

import com.aliyun.oss.OSS;
import com.bluenet.web.infrastructure.config.MinioObjectStorageConfig;
import com.bluenet.web.infrastructure.config.properties.StorageProperties;
import io.minio.MinioClient;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class ObjectStorageProviderConditionTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withBean(MinioClient.class, () -> mock(MinioClient.class))
            .withBean(OSS.class, () -> mock(OSS.class))
            .withBean(ObjectLocationResolver.class, () -> mock(ObjectLocationResolver.class))
            .withBean(StorageProperties.class, () -> {
                StorageProperties props = new StorageProperties();
                props.setBucket("test-bucket");
                return props;
            })
            .withUserConfiguration(MinioObjectStorageConfig.class, AliyunOssObjectStorage.class);

    @Test
    @DisplayName("minio provider selects MinioObjectStorage only")
    void minioProvider_ShouldSelectMinioObjectStorage() {
        contextRunner
                .withPropertyValues("storage.enabled=true", "storage.provider=minio")
                .run(context -> {
                    assertThat(context).hasSingleBean(MinioObjectStorage.class);
                    assertThat(context).doesNotHaveBean(AliyunOssObjectStorage.class);
                });
    }

    @Test
    @DisplayName("aliyun-oss provider selects AliyunOssObjectStorage only")
    void aliyunOssProvider_ShouldSelectAliyunOssObjectStorage() {
        contextRunner
                .withPropertyValues("storage.enabled=true", "storage.provider=aliyun-oss")
                .run(context -> {
                    assertThat(context).hasSingleBean(AliyunOssObjectStorage.class);
                    assertThat(context).doesNotHaveBean(MinioObjectStorage.class);
                });
    }
}
