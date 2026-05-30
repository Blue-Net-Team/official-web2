package com.bluenet.web.domain.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.bluenet.web.domain.exception.Forbidden;
import com.bluenet.web.domain.model.entity.File;
import com.bluenet.web.domain.model.enumerate.FileStatus;
import com.bluenet.web.domain.model.enumerate.FileType;
import com.bluenet.web.domain.model.vo.ConfirmUploadVO;
import com.bluenet.web.domain.repository.AssessmentAnswerRepository;
import com.bluenet.web.domain.repository.AssessmentQuestionRepository;
import com.bluenet.web.domain.repository.AssessmentTimeRepository;
import com.bluenet.web.domain.repository.FileRepository;
import com.bluenet.web.infrastructure.config.properties.StorageProperties;
import com.bluenet.web.infrastructure.security.jwt.PresignedUploadTokenService;
import com.bluenet.web.infrastructure.storage.FileMagicChecker;
import com.bluenet.web.infrastructure.storage.ObjectStorage;
import org.springframework.context.ApplicationEventPublisher;

/**
 * FileDomainServiceImpl 单元测试
 */
@DisplayName("FileDomainServiceImpl 单元测试")
@ExtendWith(MockitoExtension.class)
class FileDomainServiceImplTest {

    @Mock
    private FileRepository fileRepository;
    @Mock
    private AssessmentAnswerRepository assessmentAnswerRepository;
    @Mock
    private AssessmentQuestionRepository assessmentQuestionRepository;
    @Mock
    private AssessmentTimeRepository assessmentTimeRepository;
    @Mock
    private ApplicationEventPublisher applicationEventPublisher;
    @Mock
    private ObjectStorage objectStorage;
    @Mock
    private PresignedUploadTokenService presignedUploadTokenService;
    @Mock
    private FileMagicChecker fileMagicChecker;
    @Mock
    private StorageProperties storageProperties;

    @InjectMocks
    private FileDomainServiceImpl fileDomainService;

    private static final Long TEST_FILE_ID = 1L;
    private static final String TEST_CALLBACK_TOKEN = "valid_token";
    private static final String TEST_FILENAME = "avatar-abc123.jpg";
    private static final String TEST_MD5 = "abc123";
    private static final long TEST_SIZE = 1024L;

    @Nested
    @DisplayName("confirmUpload 幂等性测试")
    class ConfirmUploadIdempotencyTests {

        @Test
        @DisplayName("重复 confirm 已 ACTIVE 文件应返回成功")
        void confirmUpload_alreadyActive_shouldReturnSuccessIdempotently() {
            // 准备
            File activeFile = File
                    .reconstruct(TEST_FILE_ID, TEST_FILENAME, FileType.AVATAR, "url", FileStatus.ACTIVE, null);

            when(presignedUploadTokenService.getFileId(TEST_CALLBACK_TOKEN)).thenReturn(TEST_FILE_ID);
            when(fileRepository.findById(TEST_FILE_ID)).thenReturn(Optional.of(activeFile));

            // 执行
            ConfirmUploadVO result = fileDomainService
                    .confirmUpload(TEST_FILE_ID, TEST_CALLBACK_TOKEN, TEST_MD5, TEST_SIZE);

            // 验证
            assertNotNull(result);
            assertEquals(TEST_FILE_ID, result.fileId());
            assertEquals(TEST_FILENAME, result.filename());
            assertEquals(FileType.AVATAR, result.type());
            assertEquals(FileStatus.ACTIVE, result.status());

            // 不应调用 OSS 操作
            verify(objectStorage, never()).headObject(any(), any());
            verify(fileRepository, never()).updateFileMetadata(any());
        }

        @Test
        @DisplayName("confirm 已 REJECTED 文件应返回 403")
        void confirmUpload_rejected_shouldThrowForbidden() {
            // 准备
            File rejectedFile = File
                    .reconstruct(TEST_FILE_ID, TEST_FILENAME, FileType.AVATAR, "url", FileStatus.REJECTED, null);

            when(presignedUploadTokenService.getFileId(TEST_CALLBACK_TOKEN)).thenReturn(TEST_FILE_ID);
            when(fileRepository.findById(TEST_FILE_ID)).thenReturn(Optional.of(rejectedFile));

            // 执行 & 验证
            Forbidden exception = assertThrows(
                    Forbidden.class,
                    () -> fileDomainService.confirmUpload(TEST_FILE_ID, TEST_CALLBACK_TOKEN, TEST_MD5, TEST_SIZE));

            assertEquals("文件状态无效", exception.getMessage());
        }
    }
}
