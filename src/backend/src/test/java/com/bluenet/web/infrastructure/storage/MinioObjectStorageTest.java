package com.bluenet.web.infrastructure.storage;

import com.bluenet.web.domain.model.enumerate.FileType;
import com.bluenet.web.infrastructure.config.properties.StorageProperties;
import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MinioObjectStorageTest {

    @Mock
    private MinioClient minioClient;

    private MinioObjectStorage objectStorage;

    @BeforeEach
    void setUp() {
        StorageProperties properties = new StorageProperties();
        properties.setBucket("bluenet");
        objectStorage = new MinioObjectStorage(minioClient, new ObjectLocationResolver(properties));
    }

    @Test
    @DisplayName("put uses configured bucket and FileType prefix")
    void put_ShouldUseConfiguredBucketAndFileTypePrefix() throws Exception {
        objectStorage.put(FileType.NORMAL_IMG, "test.jpg", new ByteArrayInputStream("test".getBytes()));

        ArgumentCaptor<PutObjectArgs> captor = ArgumentCaptor.forClass(PutObjectArgs.class);
        verify(minioClient).putObject(captor.capture());
        assertThat(captor.getValue().bucket()).isEqualTo("bluenet");
        assertThat(captor.getValue().object()).isEqualTo("normal-img/test.jpg");
    }

    @Test
    @DisplayName("ensureBucket creates only configured bucket")
    void ensureBucket_ShouldCreateConfiguredBucket() throws Exception {
        when(minioClient.bucketExists(any(BucketExistsArgs.class))).thenReturn(false);

        objectStorage.ensureBucket();

        ArgumentCaptor<MakeBucketArgs> captor = ArgumentCaptor.forClass(MakeBucketArgs.class);
        verify(minioClient).makeBucket(captor.capture());
        assertThat(captor.getValue().bucket()).isEqualTo("bluenet");
    }
}
