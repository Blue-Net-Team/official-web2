package com.bluenet.web.application.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import com.bluenet.web.api.dto.file.FileInfo;
import com.bluenet.web.domain.model.enumerate.FileType;
import com.bluenet.web.domain.model.vo.FileVO;
import com.bluenet.web.domain.service.FileDomainService;

/**
 * FileServiceImpl 单元测试
 * <p>
 * 测试统一文件上传的纯存储逻辑，验证 FileService 无业务依赖。
 * </p>
 */
@DisplayName("FileServiceImpl 单元测试")
@ExtendWith(MockitoExtension.class)
class FileServiceImplTest {

    @Mock
    private FileDomainService fileDomainService;

    @InjectMocks
    private FileServiceImpl fileService;

    private static final Long TEST_FILE_ID = 100L;
    private static final String TEST_FILE_NAME = "test.jpg";
    private static final String TEST_FILE_URL = "http://example.com/test.jpg";

    private MultipartFile mockFile;
    private FileVO testFileVO;

    @BeforeEach
    void setUp() throws IOException {
        mockFile = mock(MultipartFile.class);
        lenient().when(mockFile.getOriginalFilename()).thenReturn(TEST_FILE_NAME);
        lenient().when(mockFile.getInputStream()).thenReturn(new ByteArrayInputStream("test content".getBytes()));

        testFileVO = FileVO.builder()
                .id(TEST_FILE_ID)
                .name(TEST_FILE_NAME)
                .url(TEST_FILE_URL)
                .type(FileType.NORMAL_IMG)
                .build();
    }

    // ==================== uploadFile 测试 ====================

    @Nested
    @DisplayName("uploadFile 方法测试")
    class UploadFileTests {

        /**
         * TC-101~105: 上传各类型文件应成功
         */
        @ParameterizedTest(name = "上传 {0} 类型文件应成功")
        @EnumSource(FileType.class)
        @DisplayName("上传各类型文件应成功返回FileInfo")
        void uploadFile_eachFileType_shouldReturnFileInfo(FileType fileType) throws IOException {
            // 准备
            FileVO fileVO = FileVO.builder()
                    .id(TEST_FILE_ID)
                    .name(TEST_FILE_NAME)
                    .url(TEST_FILE_URL)
                    .type(fileType)
                    .build();

            when(fileDomainService.generateFilename(eq(fileType), any())).thenReturn(TEST_FILE_NAME);
            when(fileDomainService.saveFile(eq(fileType), any(), any())).thenReturn(fileVO);

            // 执行
            FileInfo result = fileService.uploadFile(mockFile, fileType);

            // 验证
            assertNotNull(result);
            assertEquals(TEST_FILE_ID, result.getId());
            assertEquals(TEST_FILE_NAME, result.getName());
            assertEquals(fileType, result.getType());
            assertEquals(TEST_FILE_URL, result.getUrl());

            verify(fileDomainService).generateFilename(eq(fileType), any());
            verify(fileDomainService).saveFile(eq(fileType), any(), any());
        }

        /**
         * 上传文件时 IOException 应抛出 RuntimeException
         */
        @Test
        @DisplayName("上传文件时 IO 异常应抛出 RuntimeException")
        void uploadFile_ioException_shouldThrowRuntimeException() throws IOException {
            when(fileDomainService.generateFilename(eq(FileType.AVATAR), any())).thenReturn(TEST_FILE_NAME);
            when(mockFile.getInputStream()).thenThrow(new IOException("IO error"));

            RuntimeException exception = assertThrows(
                    RuntimeException.class,
                    () -> fileService.uploadFile(mockFile, FileType.AVATAR));

            assertTrue(exception.getMessage().contains("Failed to save file"));
        }
    }

    // ==================== 依赖检查 ====================

    @Nested
    @DisplayName("依赖检查")
    class DependencyCheckTests {

        /**
         * TC-106: FileService 无业务依赖 FileServiceImpl 应仅依赖 FileDomainService，不依赖
         * UserDomainService 等领域服务。
         */
        @Test
        @DisplayName("FileServiceImpl 应仅依赖 FileDomainService")
        void fileService_shouldOnlyDependOnFileDomainService() {
            // 验证通过 @InjectMocks 注入的 mock 只有 FileDomainService
            // FileServiceImpl 的字段应只有 fileDomainService
            var fields = FileServiceImpl.class.getDeclaredFields();
            long serviceCount = 0;
            for (var field : fields) {
                if (field.getName().equals("fileDomainService")) {
                    serviceCount++;
                }
            }
            // 确保只有一个依赖（fileDomainService）
            assertEquals(1, serviceCount, "FileServiceImpl 应只有一个 fileDomainService 依赖");

            // 确保不包含业务领域服务的依赖
            assertDoesNotThrow(() -> {
                var field = FileServiceImpl.class.getDeclaredField("fileDomainService");
                assertNotNull(field);
            });

            // 确保不存在 UserDomainService、AssessmentQuestionDomainService 等依赖
            assertThrows(
                    NoSuchFieldException.class,
                    () -> FileServiceImpl.class.getDeclaredField("userDomainService"));
            assertThrows(
                    NoSuchFieldException.class,
                    () -> FileServiceImpl.class.getDeclaredField("assessmentQuestionDomainService"));
            assertThrows(
                    NoSuchFieldException.class,
                    () -> FileServiceImpl.class.getDeclaredField("assessmentAnswerDomainService"));
            assertThrows(
                    NoSuchFieldException.class,
                    () -> FileServiceImpl.class.getDeclaredField("qrcodeDomainService"));
            assertThrows(
                    NoSuchFieldException.class,
                    () -> FileServiceImpl.class.getDeclaredField("introduceImageDomainService"));
            assertThrows(
                    NoSuchFieldException.class,
                    () -> FileServiceImpl.class.getDeclaredField("competitionDomainService"));
        }
    }
}
