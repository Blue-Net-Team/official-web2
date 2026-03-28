package com.bluenet.web.infrastructure.repository.impl;

import com.bluenet.web.domain.model.entity.File;
import com.bluenet.web.domain.model.enumerate.FileType;
import com.bluenet.web.infrastructure.repository.mapper.AssessmentAnswerMapper;
import com.bluenet.web.infrastructure.repository.mapper.AssessmentQuestionMapper;
import com.bluenet.web.infrastructure.repository.mapper.AssessmentTimeMapper;
import com.bluenet.web.infrastructure.repository.mapper.FileMapper;
import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * MinIO文件存储实现单元测试
 */
@ExtendWith(MockitoExtension.class)
class MinioFileRepositoryTest {

    @Mock
    private MinioClient minioClient;

    @Mock
    private FileMapper fileMapper;

    @Mock
    private AssessmentAnswerMapper assessmentAnswerMapper;

    @Mock
    private AssessmentQuestionMapper assessmentQuestionMapper;

    @Mock
    private AssessmentTimeMapper assessmentTimeMapper;

    private MinioFileRepository repository;

    @BeforeEach
    void setUp() {
        repository = new MinioFileRepository(minioClient, fileMapper, assessmentAnswerMapper, assessmentQuestionMapper,
                assessmentTimeMapper);
    }

    private File createFile(String name, FileType type) {
        return File.builder().name(name).type(type).build();
    }

    @Test
    @DisplayName("保存文件 - 成功")
    void saveFile_Success() throws Exception {
        // Given
        String filename = "test.jpg";
        InputStream inputStream = new ByteArrayInputStream("test content".getBytes());
        FileType fileType = FileType.NORMAL_IMG;
        File file = createFile(filename, fileType);

        // When
        repository.saveFile(inputStream, file);

        // Then
        ArgumentCaptor<PutObjectArgs> captor = ArgumentCaptor.forClass(PutObjectArgs.class);
        verify(minioClient).putObject(captor.capture());
        verify(fileMapper).insert(any(File.class));

        PutObjectArgs capturedArgs = captor.getValue();
        assertThat(capturedArgs.bucket()).isEqualTo("normal-img");
        assertThat(capturedArgs.object()).isEqualTo(filename);
    }

    @Test
    @DisplayName("保存文件 - 文件名为空应抛出异常")
    void saveFile_EmptyFilename_ShouldThrowException() {
        InputStream inputStream = new ByteArrayInputStream("test".getBytes());
        File file = createFile("", FileType.AVATAR);

        assertThatThrownBy(() -> repository.saveFile(inputStream, file)).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Filename cannot be null or empty");
    }

    @Test
    @DisplayName("保存文件 - 文件名为null应抛出异常")
    void saveFile_NullFilename_ShouldThrowException() {
        InputStream inputStream = new ByteArrayInputStream("test".getBytes());
        File file = createFile(null, FileType.AVATAR);

        assertThatThrownBy(() -> repository.saveFile(inputStream, file)).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Filename cannot be null or empty");
    }

    @Test
    @DisplayName("保存文件 - InputStream为null应抛出异常")
    void saveFile_NullInputStream_ShouldThrowException() {
        File file = createFile("test.jpg", FileType.AVATAR);

        assertThatThrownBy(() -> repository.saveFile((InputStream) null, file))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("InputStream cannot be null");
    }

    @Test
    @DisplayName("保存文件 - FileType为null应抛出异常")
    void saveFile_NullFileType_ShouldThrowException() {
        InputStream inputStream = new ByteArrayInputStream("test".getBytes());
        File file = createFile("test.jpg", null);

        assertThatThrownBy(() -> repository.saveFile(inputStream, file)).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("FileType cannot be null");
    }

    @Test
    @DisplayName("加载文件 - 验证调用参数")
    void loadFile_VerifyCallArguments() throws Exception {
        // Given
        String filename = "test.jpg";
        FileType fileType = FileType.AVATAR;

        // When - just verify the method is called with correct arguments
        // GetObjectResponse is hard to mock, so we just verify the exception handling
        // works
        when(minioClient.getObject(any(GetObjectArgs.class))).thenThrow(new RuntimeException("Connection failed"));

        // Then
        assertThatThrownBy(() -> repository.loadFile(filename, fileType)).isInstanceOf(RuntimeException.class);

        ArgumentCaptor<GetObjectArgs> captor = ArgumentCaptor.forClass(GetObjectArgs.class);
        verify(minioClient).getObject(captor.capture());
        assertThat(captor.getValue().bucket()).isEqualTo("avatar");
        assertThat(captor.getValue().object()).isEqualTo(filename);
    }

    @Test
    @DisplayName("加载文件 - 文件名为空应抛出异常")
    void loadFile_EmptyFilename_ShouldThrowException() {
        assertThatThrownBy(() -> repository.loadFile("", FileType.QRCODE)).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Filename cannot be null or empty");
    }

    @Test
    @DisplayName("加载文件 - FileType为null应抛出异常")
    void loadFile_NullFileType_ShouldThrowException() {
        assertThatThrownBy(() -> repository.loadFile("test.jpg", null)).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("FileType cannot be null");
    }

    @Test
    @DisplayName("保存文件 - MinIO异常应抛出RuntimeException")
    void saveFile_MinioException_ShouldThrowRuntimeException() throws Exception {
        // Given
        String filename = "test.jpg";
        InputStream inputStream = new ByteArrayInputStream("test content".getBytes());
        FileType fileType = FileType.ASSESSMENT_ATTACHMENT;
        File file = createFile(filename, fileType);

        when(minioClient.putObject(any(PutObjectArgs.class))).thenThrow(new RuntimeException("Connection refused"));

        // When & Then
        assertThatThrownBy(() -> repository.saveFile(inputStream, file)).isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Failed to save file");
    }

    @Test
    @DisplayName("加载文件 - MinIO异常应抛出RuntimeException")
    void loadFile_MinioException_ShouldThrowRuntimeException() throws Exception {
        // Given
        String filename = "test.jpg";
        FileType fileType = FileType.NORMAL_IMG;

        when(minioClient.getObject(any(GetObjectArgs.class))).thenThrow(new RuntimeException("Connection refused"));

        // When & Then
        assertThatThrownBy(() -> repository.loadFile(filename, fileType)).isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Failed to load file");
    }

    @ParameterizedTest
    @EnumSource(FileType.class)
    @DisplayName("所有FileType类型的bucket映射正确")
    void allFileTypes_ShouldMapToCorrectBucket(FileType fileType) throws Exception {
        // Given
        String filename = "test.jpg";
        InputStream inputStream = new ByteArrayInputStream("test".getBytes());
        File file = createFile(filename, fileType);

        // When
        repository.saveFile(inputStream, file);

        // Then
        ArgumentCaptor<PutObjectArgs> captor = ArgumentCaptor.forClass(PutObjectArgs.class);
        verify(minioClient).putObject(captor.capture());
        assertThat(captor.getValue().bucket()).isEqualTo(fileType.getValue());
    }

    @Test
    @DisplayName("验证所有5种FileType的bucket名称")
    void verifyAllFiveFileTypeMappings() throws Exception {
        // Test AVATAR -> avatar
        repository.saveFile(new ByteArrayInputStream("test".getBytes()), createFile("avatar.jpg", FileType.AVATAR));
        ArgumentCaptor<PutObjectArgs> captor1 = ArgumentCaptor.forClass(PutObjectArgs.class);
        verify(minioClient).putObject(captor1.capture());
        assertThat(captor1.getValue().bucket()).isEqualTo("avatar");

        // Test NORMAL_IMG -> normal_img
        repository.saveFile(new ByteArrayInputStream("test".getBytes()), createFile("image.jpg", FileType.NORMAL_IMG));
        ArgumentCaptor<PutObjectArgs> captor2 = ArgumentCaptor.forClass(PutObjectArgs.class);
        verify(minioClient, times(2)).putObject(captor2.capture());
        assertThat(captor2.getValue().bucket()).isEqualTo("normal-img");

        // Test ASSESSMENT_ATTACHMENT -> assessment-attachment
        repository.saveFile(
                new ByteArrayInputStream("test".getBytes()),
                createFile("doc.pdf", FileType.ASSESSMENT_ATTACHMENT));
        ArgumentCaptor<PutObjectArgs> captor3 = ArgumentCaptor.forClass(PutObjectArgs.class);
        verify(minioClient, times(3)).putObject(captor3.capture());
        assertThat(captor3.getValue().bucket()).isEqualTo("assessment-attachment");

        // Test WORK -> work
        repository.saveFile(new ByteArrayInputStream("test".getBytes()), createFile("work.jpg", FileType.WORK));
        ArgumentCaptor<PutObjectArgs> captor4 = ArgumentCaptor.forClass(PutObjectArgs.class);
        verify(minioClient, times(4)).putObject(captor4.capture());
        assertThat(captor4.getValue().bucket()).isEqualTo("work");

        // Test QRCODE -> qrcode
        repository.saveFile(new ByteArrayInputStream("test".getBytes()), createFile("qrcode.png", FileType.QRCODE));
        ArgumentCaptor<PutObjectArgs> captor5 = ArgumentCaptor.forClass(PutObjectArgs.class);
        verify(minioClient, times(5)).putObject(captor5.capture());
        assertThat(captor5.getValue().bucket()).isEqualTo("qrcode");
    }
}
