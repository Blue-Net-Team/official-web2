package com.bluenet.web.infrastructure.storage;

import com.bluenet.web.domain.model.enumerate.FileType;
import com.bluenet.web.infrastructure.config.properties.StorageProperties;
import io.minio.BucketExistsArgs;
import io.minio.GetPresignedObjectUrlArgs;
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
import java.time.Duration;

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
        objectStorage = new MinioObjectStorage(minioClient, new ObjectLocationResolver(properties), properties);
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

    @Test
    @DisplayName("getPresignedUploadUrl returns URL from MinIO")
    void getPresignedUploadUrl_ShouldReturnUrl() throws Exception {
        when(minioClient.getPresignedObjectUrl(any(GetPresignedObjectUrlArgs.class)))
                .thenReturn("http://localhost:9000/bluenet/normal-img/test.jpg?X-Amz-Algorithm=AWS4-HMAC-SHA256");

        String url = objectStorage.getPresignedUploadUrl(
                FileType.NORMAL_IMG,
                "test.jpg",
                "image/jpeg",
                1024,
                Duration.ofMinutes(15));

        assertThat(url).isNotBlank().contains("bluenet").contains("normal-img/test.jpg");
        ArgumentCaptor<GetPresignedObjectUrlArgs> captor = ArgumentCaptor.forClass(GetPresignedObjectUrlArgs.class);
        verify(minioClient).getPresignedObjectUrl(captor.capture());
        assertThat(captor.getValue().method().name()).isEqualTo("PUT");
        assertThat(captor.getValue().bucket()).isEqualTo("bluenet");
        assertThat(captor.getValue().object()).isEqualTo("normal-img/test.jpg");
    }

    @Test
    @DisplayName("getPresignedDownloadUrl returns URL from MinIO")
    void getPresignedDownloadUrl_ShouldReturnUrl() throws Exception {
        when(minioClient.getPresignedObjectUrl(any(GetPresignedObjectUrlArgs.class)))
                .thenReturn("http://localhost:9000/bluenet/work/test.zip?X-Amz-Algorithm=AWS4-HMAC-SHA256");

        String url = objectStorage.getPresignedDownloadUrl(
                FileType.WORK,
                "test.zip",
                Duration.ofMinutes(10));

        assertThat(url).isNotBlank().contains("bluenet").contains("work/test.zip");
        ArgumentCaptor<GetPresignedObjectUrlArgs> captor = ArgumentCaptor.forClass(GetPresignedObjectUrlArgs.class);
        verify(minioClient).getPresignedObjectUrl(captor.capture());
        assertThat(captor.getValue().method().name()).isEqualTo("GET");
    }
}
