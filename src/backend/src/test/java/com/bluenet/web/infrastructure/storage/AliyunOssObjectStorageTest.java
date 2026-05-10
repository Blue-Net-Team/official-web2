package com.bluenet.web.infrastructure.storage;

import com.aliyun.oss.OSS;
import com.aliyun.oss.model.GeneratePresignedUrlRequest;
import com.bluenet.web.domain.model.enumerate.FileType;
import com.bluenet.web.infrastructure.config.properties.StorageProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.net.URL;
import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AliyunOssObjectStorageTest {

    @Mock
    private OSS ossClient;

    private AliyunOssObjectStorage objectStorage;

    @BeforeEach
    void setUp() {
        StorageProperties properties = new StorageProperties();
        properties.setBucket("bluenet");
        objectStorage = new AliyunOssObjectStorage(ossClient, new ObjectLocationResolver(properties), properties);
    }

    @Test
    @DisplayName("getPresignedUploadUrl returns URL from Aliyun OSS")
    void getPresignedUploadUrl_ShouldReturnUrl() throws Exception {
        when(ossClient.generatePresignedUrl(any(GeneratePresignedUrlRequest.class)))
                .thenReturn(new URL("https://oss.example.com/bluenet/normal-img/test.jpg?Expires=1234567890"));

        String url = objectStorage.getPresignedUploadUrl(
                FileType.NORMAL_IMG,
                "test.jpg",
                "image/jpeg",
                1024,
                Duration.ofMinutes(15));

        assertThat(url).isNotBlank().contains("bluenet").contains("normal-img/test.jpg");
        ArgumentCaptor<GeneratePresignedUrlRequest> captor = ArgumentCaptor.forClass(GeneratePresignedUrlRequest.class);
        verify(ossClient).generatePresignedUrl(captor.capture());
        assertThat(captor.getValue().getMethod().toString()).isEqualTo("PUT");
        assertThat(captor.getValue().getBucketName()).isEqualTo("bluenet");
        assertThat(captor.getValue().getKey()).isEqualTo("normal-img/test.jpg");
        assertThat(captor.getValue().getContentType()).isEqualTo("image/jpeg");
    }

    @Test
    @DisplayName("getPresignedDownloadUrl returns URL from Aliyun OSS")
    void getPresignedDownloadUrl_ShouldReturnUrl() throws Exception {
        when(ossClient.generatePresignedUrl(anyString(), anyString(), any(), any()))
                .thenReturn(new URL("https://oss.example.com/bluenet/work/test.zip?Expires=1234567890"));

        String url = objectStorage.getPresignedDownloadUrl(
                FileType.WORK,
                "test.zip",
                Duration.ofMinutes(10));

        assertThat(url).isNotBlank().contains("bluenet").contains("work/test.zip");
    }
}
