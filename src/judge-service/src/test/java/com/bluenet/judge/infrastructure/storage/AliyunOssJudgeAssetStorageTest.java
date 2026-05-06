package com.bluenet.judge.infrastructure.storage;

import com.aliyun.oss.OSS;
import com.aliyun.oss.model.OSSObject;
import com.bluenet.judge.infrastructure.config.JudgeStorageProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AliyunOssJudgeAssetStorageTest {

    @Mock
    private OSS ossClient;

    private AliyunOssJudgeAssetStorage storage;

    @BeforeEach
    void setUp() {
        JudgeStorageProperties properties = new JudgeStorageProperties("bluenet-judge");
        storage = new AliyunOssJudgeAssetStorage(ossClient, properties);
    }

    @Test
    @DisplayName("get reads object content from OSS")
    void get_ShouldReadObjectContent() throws Exception {
        String objectKey = "test-cases/1/in.txt";
        byte[] expectedContent = "hello world".getBytes(StandardCharsets.UTF_8);
        OSSObject ossObject = new OSSObject();
        ossObject.setObjectContent(new ByteArrayInputStream(expectedContent));
        when(ossClient.getObject("bluenet-judge", objectKey)).thenReturn(ossObject);

        byte[] result = storage.get(objectKey);

        assertThat(result).isEqualTo(expectedContent);
        verify(ossClient).getObject("bluenet-judge", objectKey);
    }

    @Test
    @DisplayName("put uploads object with Content-Type and Content-Length metadata")
    void put_ShouldUploadWithMetadata() {
        String objectKey = "test-cases/1/out.txt";
        byte[] content = "expected output".getBytes(StandardCharsets.UTF_8);
        String contentType = "text/plain";

        storage.put(objectKey, content, contentType);

        ArgumentCaptor<com.aliyun.oss.model.ObjectMetadata> metadataCaptor =
                ArgumentCaptor.forClass(com.aliyun.oss.model.ObjectMetadata.class);
        verify(ossClient).putObject(eq("bluenet-judge"), eq(objectKey), any(ByteArrayInputStream.class), metadataCaptor.capture());

        com.aliyun.oss.model.ObjectMetadata capturedMetadata = metadataCaptor.getValue();
        assertThat(capturedMetadata.getContentType()).isEqualTo(contentType);
        assertThat(capturedMetadata.getContentLength()).isEqualTo(content.length);
    }

    @Test
    @DisplayName("put uploads object without Content-Type when null")
    void put_ShouldUploadWithoutContentTypeWhenNull() {
        String objectKey = "test-cases/1/out.txt";
        byte[] content = "expected output".getBytes(StandardCharsets.UTF_8);

        storage.put(objectKey, content, null);

        ArgumentCaptor<com.aliyun.oss.model.ObjectMetadata> metadataCaptor =
                ArgumentCaptor.forClass(com.aliyun.oss.model.ObjectMetadata.class);
        verify(ossClient).putObject(eq("bluenet-judge"), eq(objectKey), any(ByteArrayInputStream.class), metadataCaptor.capture());

        com.aliyun.oss.model.ObjectMetadata capturedMetadata = metadataCaptor.getValue();
        assertThat(capturedMetadata.getContentType()).isNull();
        assertThat(capturedMetadata.getContentLength()).isEqualTo(content.length);
    }

    @Test
    @DisplayName("delete removes object from OSS")
    void delete_ShouldRemoveObject() {
        String objectKey = "test-cases/1/in.txt";

        storage.delete(objectKey);

        verify(ossClient).deleteObject("bluenet-judge", objectKey);
    }
}
