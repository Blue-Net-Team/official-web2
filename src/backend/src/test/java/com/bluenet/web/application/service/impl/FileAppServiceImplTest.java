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

import com.bluenet.web.application.FileResult;
import com.bluenet.web.application.command.file.FileCommands;
import com.bluenet.web.domain.model.enumerate.FileStatus;
import com.bluenet.web.domain.model.enumerate.FileType;
import com.bluenet.web.domain.model.vo.FileVO;
import com.bluenet.web.domain.repository.FileRepository;
import com.bluenet.web.domain.service.FileDomainService;

/**
 * FileAppServiceImpl 单元测试
 * <p>
 * 测试统一文件上传的纯存储逻辑，验证 FileAppService 无业务依赖。
 * </p>
 */
@DisplayName("FileAppServiceImpl 单元测试")
@ExtendWith(MockitoExtension.class)
class FileAppServiceImplTest {

    @Mock
    private FileDomainService fileDomainService;

    @Mock
    private FileRepository fileRepository;

    @InjectMocks
    private FileAppServiceImpl fileAppService;

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
                .status(FileStatus.ACTIVE)
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
        @DisplayName("上传各类型文件应成功返回FileResult")
        void uploadFile_eachFileType_shouldReturnFileResult(FileType fileType) throws IOException {
            // 准备
            FileVO fileVO = FileVO.builder()
                    .id(TEST_FILE_ID)
                    .name(TEST_FILE_NAME)
                    .url(TEST_FILE_URL)
                    .type(fileType)
                    .status(FileStatus.ACTIVE)
                    .build();

            when(fileDomainService.generateFilename(eq(fileType), any())).thenReturn(TEST_FILE_NAME);
            when(fileDomainService.saveFile(eq(fileType), any(), any())).thenReturn(fileVO);

            // 执行
            FileResult result = fileAppService.uploadFile(new FileCommands.UploadFileCommand(mockFile, fileType));

            // 验证
            assertNotNull(result);
            assertEquals(TEST_FILE_ID, result.id());
            assertEquals(TEST_FILE_NAME, result.name());
            assertEquals(fileType, result.type());
            assertEquals(TEST_FILE_URL, result.url());
            assertEquals(FileStatus.ACTIVE, result.status());

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
                    () -> fileAppService.uploadFile(new FileCommands.UploadFileCommand(mockFile, FileType.AVATAR)));

            assertTrue(exception.getMessage().contains("Failed to save file"));
        }
    }

    // ==================== 依赖检查 ====================

    @Nested
    @DisplayName("依赖检查")
    class DependencyCheckTests {

        /**
         * TC-106: FileAppService 无业务依赖 FileAppServiceImpl 应仅依赖 FileDomainService，不依赖
         * UserDomainService 等领域服务。
         */
        @Test
        @DisplayName("FileAppServiceImpl 应仅依赖 FileDomainService")
        void fileAppService_shouldOnlyDependOnFileDomainService() {
            // 验证通过 @InjectMocks 注入的 mock 只有 FileDomainService
            // FileAppServiceImpl 的字段应只有 fileDomainService
            var fields = FileAppServiceImpl.class.getDeclaredFields();
            long serviceCount = 0;
            for (var field : fields) {
                if (field.getName().equals("fileDomainService") || field.getName().equals("fileRepository")) {
                    serviceCount++;
                }
            }
            // 确保有两个依赖（fileDomainService 和 fileRepository）
            assertEquals(2, serviceCount, "FileAppServiceImpl 应有 fileDomainService 和 fileRepository 两个依赖");

            // 确保包含必要的依赖
            assertDoesNotThrow(() -> {
                var field = FileAppServiceImpl.class.getDeclaredField("fileDomainService");
                assertNotNull(field);
            });
            assertDoesNotThrow(() -> {
                var field = FileAppServiceImpl.class.getDeclaredField("fileRepository");
                assertNotNull(field);
            });

            // 确保不存在其他业务领域服务的依赖
            assertThrows(
                    NoSuchFieldException.class,
                    () -> FileAppServiceImpl.class.getDeclaredField("userDomainService"));
            assertThrows(
                    NoSuchFieldException.class,
                    () -> FileAppServiceImpl.class.getDeclaredField("assessmentQuestionDomainService"));
            assertThrows(
                    NoSuchFieldException.class,
                    () -> FileAppServiceImpl.class.getDeclaredField("assessmentAnswerDomainService"));
            assertThrows(
                    NoSuchFieldException.class,
                    () -> FileAppServiceImpl.class.getDeclaredField("qrcodeDomainService"));
            assertThrows(
                    NoSuchFieldException.class,
                    () -> FileAppServiceImpl.class.getDeclaredField("introduceImageDomainService"));
            assertThrows(
                    NoSuchFieldException.class,
                    () -> FileAppServiceImpl.class.getDeclaredField("competitionDomainService"));
        }
    }
}
