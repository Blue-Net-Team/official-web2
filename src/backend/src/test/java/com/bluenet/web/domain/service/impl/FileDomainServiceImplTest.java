package com.bluenet.web.domain.service.impl;

import com.bluenet.web.domain.exception.DataNotFound;
import com.bluenet.web.domain.exception.Forbidden;
import com.bluenet.web.domain.model.entity.AssessmentAnswer;
import com.bluenet.web.domain.model.entity.AssessmentQuestion;
import com.bluenet.web.domain.model.entity.AssessmentTime;
import com.bluenet.web.domain.model.entity.File;
import com.bluenet.web.domain.model.entity.User;
import com.bluenet.web.domain.model.enumerate.Direction;
import com.bluenet.web.domain.model.enumerate.FileStatus;
import com.bluenet.web.domain.model.enumerate.FileType;
import com.bluenet.web.domain.model.enumerate.ProgrammingLanguage;
import com.bluenet.web.domain.model.enumerate.QuestionType;
import com.bluenet.web.domain.model.enumerate.RoleType;
import com.bluenet.web.domain.model.result.ConfirmUploadResult;
import com.bluenet.web.domain.model.result.PresignedUploadResult;
import com.bluenet.web.domain.repository.AssessmentAnswerRepository;
import com.bluenet.web.domain.repository.AssessmentQuestionRepository;
import com.bluenet.web.domain.repository.AssessmentTimeRepository;
import com.bluenet.web.domain.repository.FileRepository;
import com.bluenet.web.infrastructure.config.properties.StorageProperties;
import com.bluenet.web.infrastructure.security.jwt.PresignedUploadTokenService;
import com.bluenet.web.infrastructure.security.principal.RoleTypeResolver;
import com.bluenet.web.infrastructure.storage.FileMagicChecker;
import com.bluenet.web.infrastructure.storage.ObjectStorage;
import com.bluenet.web.infrastructure.storage.StorageObjectMetadata;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.io.Resource;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@link FileDomainServiceImpl} 单元测试。
 */
@DisplayName("FileDomainServiceImpl 测试")
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
    private RoleTypeResolver roleTypeResolver;

    private final FileMagicChecker fileMagicChecker = new FileMagicChecker();
    private final StorageProperties storageProperties = new StorageProperties();

    private FileDomainServiceImpl domainService;

    @BeforeEach
    void setUp() {
        domainService = new FileDomainServiceImpl(
                fileRepository,
                assessmentAnswerRepository,
                assessmentQuestionRepository,
                assessmentTimeRepository,
                applicationEventPublisher,
                objectStorage,
                presignedUploadTokenService,
                fileMagicChecker,
                storageProperties,
                roleTypeResolver);
    }

    @Test
    @DisplayName("getFileById: 文件存在时应返回文件")
    void getFileById_existingFile_shouldReturnFile() {
        Long fileId = 1L;
        File file = File
                .reconstruct(fileId, "avatar.png", FileType.AVATAR, "url", FileStatus.ACTIVE, LocalDateTime.now());
        when(fileRepository.findById(fileId)).thenReturn(Optional.of(file));

        File result = domainService.getFileById(fileId);

        assertEquals(file, result);
    }

    @Test
    @DisplayName("getFileById: 文件不存在时应抛出 DataNotFound")
    void getFileById_nonExistingFile_shouldThrowDataNotFound() {
        Long fileId = 1L;
        when(fileRepository.findById(fileId)).thenReturn(Optional.empty());

        assertThrows(DataNotFound.class, () -> domainService.getFileById(fileId));
    }

    @Test
    @DisplayName("getAnswerByFileId: 答题存在时应返回答题")
    void getAnswerByFileId_existingAnswer_shouldReturnAnswer() {
        Long fileId = 1L;
        AssessmentAnswer answer = AssessmentAnswer.reconstruct(
                10L,
                100L,
                20L,
                "content",
                ProgrammingLanguage.JAVA,
                fileId,
                LocalDateTime.now(),
                null);
        when(assessmentAnswerRepository.findByFileId(fileId)).thenReturn(Optional.of(answer));

        AssessmentAnswer result = domainService.getAnswerByFileId(fileId);

        assertEquals(answer, result);
    }

    @Test
    @DisplayName("getAnswerByFileId: 答题不存在时应抛出 DataNotFound")
    void getAnswerByFileId_nonExistingAnswer_shouldThrowDataNotFound() {
        Long fileId = 1L;
        when(assessmentAnswerRepository.findByFileId(fileId)).thenReturn(Optional.empty());

        assertThrows(DataNotFound.class, () -> domainService.getAnswerByFileId(fileId));
    }

    @Test
    @DisplayName("getQuestionByAttachmentId: 题目存在时应返回题目")
    void getQuestionByAttachmentId_existingQuestion_shouldReturnQuestion() {
        Long attachmentId = 1L;
        AssessmentQuestion question = AssessmentQuestion.reconstruct(
                10L,
                100L,
                1,
                QuestionType.SINGLE_CHOICE,
                "title",
                null,
                attachmentId,
                BigDecimal.TEN);
        when(assessmentQuestionRepository.findByAttachmentId(attachmentId)).thenReturn(Optional.of(question));

        AssessmentQuestion result = domainService.getQuestionByAttachmentId(attachmentId);

        assertEquals(question, result);
    }

    @Test
    @DisplayName("getQuestionByAttachmentId: 题目不存在时应抛出 DataNotFound")
    void getQuestionByAttachmentId_nonExistingQuestion_shouldThrowDataNotFound() {
        Long attachmentId = 1L;
        when(assessmentQuestionRepository.findByAttachmentId(attachmentId)).thenReturn(Optional.empty());

        assertThrows(DataNotFound.class, () -> domainService.getQuestionByAttachmentId(attachmentId));
    }

    @Test
    @DisplayName("getAssessmentTimeById: 考核场次存在时应返回考核场次")
    void getAssessmentTimeById_existingTime_shouldReturnTime() {
        Long timeId = 1L;
        AssessmentTime time = AssessmentTime.reconstruct(
                timeId,
                Direction.COMPUTER_VISION,
                1,
                2024,
                LocalDateTime.now(),
                LocalDateTime.now().plusHours(1),
                false,
                null,
                null,
                false);
        when(assessmentTimeRepository.findById(timeId)).thenReturn(Optional.of(time));

        AssessmentTime result = domainService.getAssessmentTimeById(timeId);

        assertEquals(time, result);
    }

    @Test
    @DisplayName("getAssessmentTimeById: 考核场次不存在时应抛出 DataNotFound")
    void getAssessmentTimeById_nonExistingTime_shouldThrowDataNotFound() {
        Long timeId = 1L;
        when(assessmentTimeRepository.findById(timeId)).thenReturn(Optional.empty());

        assertThrows(DataNotFound.class, () -> domainService.getAssessmentTimeById(timeId));
    }

    @Test
    @DisplayName("generateFilename: 生成的文件名应包含文件类型与小写扩展名")
    void generateFilename_shouldContainTypeAndExtension() {
        String result = domainService.generateFilename(FileType.WORK, "png");

        assertTrue(result.startsWith("work-"));
        assertTrue(result.endsWith(".png"));
    }

    @Test
    @DisplayName("saveFile: 应使用重构后的文件对象调用仓储保存")
    void saveFile_shouldCallRepositoryWithReconstructedFile() {
        InputStream inputStream = new ByteArrayInputStream(new byte[] { 0x01 });
        when(fileRepository.saveFile(any(InputStream.class), any(File.class))).thenAnswer(invocation -> {
            File file = invocation.getArgument(1);
            file.setId(2L);
            return file;
        });

        File result = domainService.saveFile(FileType.WORK, "test.png", inputStream);

        assertNotNull(result.getId());
        assertEquals(FileStatus.ACTIVE, result.getStatus());
        assertTrue(result.getName().startsWith("work-"));
        verify(fileRepository).saveFile(any(InputStream.class), any(File.class));
    }

    @Test
    @DisplayName("loadFile: 应返回仓储加载的资源")
    void loadFile_shouldReturnResource() {
        Resource resource = org.mockito.Mockito.mock(Resource.class);
        when(fileRepository.loadFile("test.png", FileType.WORK)).thenReturn(resource);

        Resource result = domainService.loadFile(FileType.WORK, "test.png");

        assertEquals(resource, result);
    }

    @Test
    @DisplayName("prepareUpload: 应返回包含预签名 URL 的上传结果")
    void prepareUpload_shouldReturnPresignedUploadResult() {
        String originalFilename = "test.png";
        String contentType = "image/png";
        long size = 1024L;
        String uploadUrl = "http://minio/upload";
        String callbackToken = "token";

        when(fileRepository.saveFileMetadata(any(File.class))).thenAnswer(invocation -> {
            File file = invocation.getArgument(0);
            file.setId(100L);
            return file;
        });
        when(
                objectStorage.getPresignedUploadUrl(
                        eq(FileType.NORMAL_IMG),
                        anyString(),
                        eq(contentType),
                        eq(size),
                        eq(Duration.ofMinutes(15)))).thenReturn(uploadUrl);
        when(presignedUploadTokenService.generateToken(eq(100L), eq(""), eq(Duration.ofMinutes(15))))
                .thenReturn(callbackToken);

        PresignedUploadResult result = domainService.prepareUpload(
                FileType.NORMAL_IMG,
                originalFilename,
                contentType,
                size);

        assertEquals(100L, result.fileId());
        assertEquals(uploadUrl, result.uploadUrl());
        assertEquals(callbackToken, result.callbackToken());
        assertEquals(FileType.NORMAL_IMG, result.type());
        assertTrue(result.filename().startsWith("normal_img-"));
        assertTrue(result.filename().endsWith(".png"));
    }

    @Test
    @DisplayName("confirmUpload: Token 无效时应抛出 Forbidden")
    void confirmUpload_invalidToken_shouldThrowForbidden() {
        Long fileId = 1L;
        String callbackToken = "invalid-token";
        when(presignedUploadTokenService.getFileId(callbackToken)).thenReturn(null);

        assertThrows(
                Forbidden.class,
                () -> domainService.confirmUpload(fileId, callbackToken, "md5", 100L));
    }

    @Test
    @DisplayName("confirmUpload: Token 中的 fileId 不匹配时应抛出 Forbidden")
    void confirmUpload_tokenFileIdMismatch_shouldThrowForbidden() {
        Long fileId = 1L;
        String callbackToken = "token";
        when(presignedUploadTokenService.getFileId(callbackToken)).thenReturn(2L);

        assertThrows(
                Forbidden.class,
                () -> domainService.confirmUpload(fileId, callbackToken, "md5", 100L));
    }

    @Test
    @DisplayName("confirmUpload: 文件不存在时应抛出 DataNotFound")
    void confirmUpload_fileNotFound_shouldThrowDataNotFound() {
        Long fileId = 1L;
        String callbackToken = "token";
        when(presignedUploadTokenService.getFileId(callbackToken)).thenReturn(fileId);
        when(fileRepository.findById(fileId)).thenReturn(Optional.empty());

        assertThrows(
                DataNotFound.class,
                () -> domainService.confirmUpload(fileId, callbackToken, "md5", 100L));
    }

    @Test
    @DisplayName("confirmUpload: 文件已是 ACTIVE 状态时应返回幂等结果")
    void confirmUpload_fileAlreadyActive_shouldReturnActiveResult() {
        Long fileId = 1L;
        String callbackToken = "token";
        File file = File.reconstruct(
                fileId,
                "work-uuid.png",
                FileType.WORK,
                "url",
                FileStatus.ACTIVE,
                LocalDateTime.now());
        when(presignedUploadTokenService.getFileId(callbackToken)).thenReturn(fileId);
        when(fileRepository.findById(fileId)).thenReturn(Optional.of(file));

        ConfirmUploadResult result = domainService.confirmUpload(fileId, callbackToken, "md5", 100L);

        assertEquals(FileStatus.ACTIVE, result.status());
        verify(objectStorage, never()).headObject(any(), anyString());
    }

    @Test
    @DisplayName("confirmUpload: 文件状态非 PENDING 时应抛出 Forbidden")
    void confirmUpload_fileStatusNotPending_shouldThrowForbidden() {
        Long fileId = 1L;
        String callbackToken = "token";
        File file = File.reconstruct(
                fileId,
                "work-uuid.png",
                FileType.WORK,
                "url",
                FileStatus.REJECTED,
                LocalDateTime.now());
        when(presignedUploadTokenService.getFileId(callbackToken)).thenReturn(fileId);
        when(fileRepository.findById(fileId)).thenReturn(Optional.of(file));

        assertThrows(
                Forbidden.class,
                () -> domainService.confirmUpload(fileId, callbackToken, "md5", 100L));
    }

    @Test
    @DisplayName("confirmUpload: OSS 对象不存在时应将文件状态更新为 REJECTED")
    void confirmUpload_objectNotFound_shouldRejectAndUpdateStatus() {
        Long fileId = 1L;
        String callbackToken = "token";
        File file = File.reconstruct(
                fileId,
                "work-uuid.png",
                FileType.WORK,
                "url",
                FileStatus.PENDING,
                LocalDateTime.now());
        when(presignedUploadTokenService.getFileId(callbackToken)).thenReturn(fileId);
        when(fileRepository.findById(fileId)).thenReturn(Optional.of(file));
        when(objectStorage.headObject(FileType.WORK, "work-uuid.png")).thenThrow(new DataNotFound("not found"));

        ConfirmUploadResult result = domainService.confirmUpload(fileId, callbackToken, "md5", 100L);

        assertEquals(FileStatus.REJECTED, result.status());
        verify(fileRepository).save(file);
        assertEquals(FileStatus.REJECTED, file.getStatus());
    }

    @Test
    @DisplayName("confirmUpload: 校验全部通过时应将文件状态更新为 ACTIVE")
    void confirmUpload_allChecksPass_shouldActivateFile() {
        Long fileId = 1L;
        String callbackToken = "token";
        File file = File.reconstruct(
                fileId,
                "normal_img-uuid.png",
                FileType.NORMAL_IMG,
                "url",
                FileStatus.PENDING,
                LocalDateTime.now());
        StorageObjectMetadata metadata = new StorageObjectMetadata("\"abc123\"", "image/png", 100L);
        when(presignedUploadTokenService.getFileId(callbackToken)).thenReturn(fileId);
        when(fileRepository.findById(fileId)).thenReturn(Optional.of(file));
        when(objectStorage.headObject(FileType.NORMAL_IMG, "normal_img-uuid.png")).thenReturn(metadata);
        when(objectStorage.getObjectHeader(FileType.NORMAL_IMG, "normal_img-uuid.png", 8))
                .thenReturn(new byte[] { (byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A });

        ConfirmUploadResult result = domainService.confirmUpload(fileId, callbackToken, "abc123", 100L);

        assertEquals(FileStatus.ACTIVE, result.status());
        verify(fileRepository).save(file);
        assertEquals(FileStatus.ACTIVE, file.getStatus());
    }

    @Test
    @DisplayName("confirmUpload: MD5 不匹配时应拒绝并删除 OSS 对象")
    void confirmUpload_md5Mismatch_shouldRejectAndDeleteObject() {
        Long fileId = 1L;
        String callbackToken = "token";
        File file = File.reconstruct(
                fileId,
                "normal_img-uuid.png",
                FileType.NORMAL_IMG,
                "url",
                FileStatus.PENDING,
                LocalDateTime.now());
        StorageObjectMetadata metadata = new StorageObjectMetadata("def456", "image/png", 100L);
        when(presignedUploadTokenService.getFileId(callbackToken)).thenReturn(fileId);
        when(fileRepository.findById(fileId)).thenReturn(Optional.of(file));
        when(objectStorage.headObject(FileType.NORMAL_IMG, "normal_img-uuid.png")).thenReturn(metadata);
        when(objectStorage.getObjectHeader(FileType.NORMAL_IMG, "normal_img-uuid.png", 8))
                .thenReturn(new byte[] { (byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A });

        ConfirmUploadResult result = domainService.confirmUpload(fileId, callbackToken, "abc123", 100L);

        assertEquals(FileStatus.REJECTED, result.status());
        verify(objectStorage).delete(FileType.NORMAL_IMG, "normal_img-uuid.png");
        verify(fileRepository).save(file);
    }

    @Test
    @DisplayName("confirmUpload: 大小不匹配时应拒绝并删除 OSS 对象")
    void confirmUpload_sizeMismatch_shouldRejectAndDeleteObject() {
        Long fileId = 1L;
        String callbackToken = "token";
        File file = File.reconstruct(
                fileId,
                "normal_img-uuid.png",
                FileType.NORMAL_IMG,
                "url",
                FileStatus.PENDING,
                LocalDateTime.now());
        StorageObjectMetadata metadata = new StorageObjectMetadata("abc123", "image/png", 100L);
        when(presignedUploadTokenService.getFileId(callbackToken)).thenReturn(fileId);
        when(fileRepository.findById(fileId)).thenReturn(Optional.of(file));
        when(objectStorage.headObject(FileType.NORMAL_IMG, "normal_img-uuid.png")).thenReturn(metadata);
        when(objectStorage.getObjectHeader(FileType.NORMAL_IMG, "normal_img-uuid.png", 8))
                .thenReturn(new byte[] { (byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A });

        ConfirmUploadResult result = domainService.confirmUpload(fileId, callbackToken, "abc123", 200L);

        assertEquals(FileStatus.REJECTED, result.status());
        verify(objectStorage).delete(FileType.NORMAL_IMG, "normal_img-uuid.png");
    }

    @Test
    @DisplayName("confirmUpload: 魔数不匹配时应拒绝并删除 OSS 对象")
    void confirmUpload_magicMismatch_shouldRejectAndDeleteObject() {
        Long fileId = 1L;
        String callbackToken = "token";
        File file = File.reconstruct(
                fileId,
                "normal_img-uuid.png",
                FileType.NORMAL_IMG,
                "url",
                FileStatus.PENDING,
                LocalDateTime.now());
        StorageObjectMetadata metadata = new StorageObjectMetadata("abc123", "image/png", 100L);
        when(presignedUploadTokenService.getFileId(callbackToken)).thenReturn(fileId);
        when(fileRepository.findById(fileId)).thenReturn(Optional.of(file));
        when(objectStorage.headObject(FileType.NORMAL_IMG, "normal_img-uuid.png")).thenReturn(metadata);
        when(objectStorage.getObjectHeader(FileType.NORMAL_IMG, "normal_img-uuid.png", 8))
                .thenReturn(new byte[] { 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00 });

        ConfirmUploadResult result = domainService.confirmUpload(fileId, callbackToken, "abc123", 100L);

        assertEquals(FileStatus.REJECTED, result.status());
        verify(objectStorage).delete(FileType.NORMAL_IMG, "normal_img-uuid.png");
    }

    @Test
    @DisplayName("confirmUpload: MD5 为空且大小不大于 0 时应跳过对应校验")
    void confirmUpload_nullMd5AndNonPositiveSize_shouldSkipThoseChecks() {
        Long fileId = 1L;
        String callbackToken = "token";
        File file = File.reconstruct(
                fileId,
                "normal_img-uuid.png",
                FileType.NORMAL_IMG,
                "url",
                FileStatus.PENDING,
                LocalDateTime.now());
        StorageObjectMetadata metadata = new StorageObjectMetadata(null, "image/png", 100L);
        when(presignedUploadTokenService.getFileId(callbackToken)).thenReturn(fileId);
        when(fileRepository.findById(fileId)).thenReturn(Optional.of(file));
        when(objectStorage.headObject(FileType.NORMAL_IMG, "normal_img-uuid.png")).thenReturn(metadata);
        when(objectStorage.getObjectHeader(FileType.NORMAL_IMG, "normal_img-uuid.png", 8))
                .thenReturn(new byte[] { (byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A });

        ConfirmUploadResult result = domainService.confirmUpload(fileId, callbackToken, null, 0L);

        assertEquals(FileStatus.ACTIVE, result.status());
        verify(objectStorage, never()).delete(any(), anyString());
    }

    @Test
    @DisplayName("getPresignedDownloadUrl: 应返回对象存储生成的预签名下载 URL")
    void getPresignedDownloadUrl_shouldReturnUrl() {
        String expectedUrl = "http://minio/download";
        when(objectStorage.getPresignedDownloadUrl(FileType.NORMAL_IMG, "test.png", Duration.ofMinutes(10)))
                .thenReturn(expectedUrl);

        String result = domainService.getPresignedDownloadUrl(FileType.NORMAL_IMG, "test.png");

        assertEquals(expectedUrl, result);
    }

    @Test
    @DisplayName("checkDownloadPermission: WORK 文件所有者可下载")
    void checkDownloadPermission_workFileOwner_shouldAllow() {
        Long fileId = 1L;
        Long userId = 100L;
        File file = File.reconstruct(
                fileId,
                "work-uuid.png",
                FileType.WORK,
                "url",
                FileStatus.ACTIVE,
                LocalDateTime.now());
        User currentUser = User.reconstruct(userId, "encoded");
        AssessmentAnswer answer = AssessmentAnswer.reconstruct(
                10L,
                userId,
                20L,
                "content",
                ProgrammingLanguage.JAVA,
                fileId,
                LocalDateTime.now(),
                null);
        when(assessmentAnswerRepository.findByFileId(fileId)).thenReturn(Optional.of(answer));
        when(assessmentAnswerRepository.existsByFileIdAndUserId(fileId, userId)).thenReturn(true);

        domainService.checkDownloadPermission(file, currentUser);
    }

    @Test
    @DisplayName("checkDownloadPermission: WORK 组队文件中任意一条答案属于当前用户即可下载")
    void checkDownloadPermission_workFileTeamMember_shouldAllow() {
        Long fileId = 1L;
        Long captainId = 100L;
        Long memberId = 200L;
        File file = File.reconstruct(
                fileId,
                "work-uuid.png",
                FileType.WORK,
                "url",
                FileStatus.ACTIVE,
                LocalDateTime.now());
        User currentUser = User.reconstruct(memberId, "encoded");
        // 组队答案同一 fileId 对应多条记录，findByFileId 可能命中队长那条
        AssessmentAnswer captainAnswer = AssessmentAnswer.reconstruct(
                10L,
                captainId,
                20L,
                "content",
                ProgrammingLanguage.JAVA,
                fileId,
                LocalDateTime.now(),
                null);
        when(assessmentAnswerRepository.findByFileId(fileId)).thenReturn(Optional.of(captainAnswer));
        when(assessmentAnswerRepository.existsByFileIdAndUserId(fileId, memberId)).thenReturn(true);

        domainService.checkDownloadPermission(file, currentUser);
    }

    @Test
    @DisplayName("checkDownloadPermission: WORK 文件非所有者但角色为 MEMBER 及以上可下载")
    void checkDownloadPermission_workFileMember_shouldAllow() {
        Long fileId = 1L;
        Long ownerId = 100L;
        Long memberId = 200L;
        Long memberRoleId = 2L;
        File file = File.reconstruct(
                fileId,
                "work-uuid.png",
                FileType.WORK,
                "url",
                FileStatus.ACTIVE,
                LocalDateTime.now());
        User currentUser = User.reconstruct(memberId, "encoded");
        currentUser.setRoleId(memberRoleId);
        AssessmentAnswer answer = AssessmentAnswer.reconstruct(
                10L,
                ownerId,
                20L,
                "content",
                ProgrammingLanguage.JAVA,
                fileId,
                LocalDateTime.now(),
                null);
        when(assessmentAnswerRepository.findByFileId(fileId)).thenReturn(Optional.of(answer));
        when(roleTypeResolver.resolve(memberRoleId)).thenReturn(RoleType.MEMBER);

        domainService.checkDownloadPermission(file, currentUser);
    }

    @Test
    @DisplayName("checkDownloadPermission: WORK 文件非所有者且角色低于 MEMBER 时禁止下载")
    void checkDownloadPermission_workFileCandidate_shouldForbid() {
        Long fileId = 1L;
        Long ownerId = 100L;
        Long candidateId = 200L;
        Long candidateRoleId = 1L;
        File file = File.reconstruct(
                fileId,
                "work-uuid.png",
                FileType.WORK,
                "url",
                FileStatus.ACTIVE,
                LocalDateTime.now());
        User currentUser = User.reconstruct(candidateId, "encoded");
        currentUser.setRoleId(candidateRoleId);
        AssessmentAnswer answer = AssessmentAnswer.reconstruct(
                10L,
                ownerId,
                20L,
                "content",
                ProgrammingLanguage.JAVA,
                fileId,
                LocalDateTime.now(),
                null);
        when(assessmentAnswerRepository.findByFileId(fileId)).thenReturn(Optional.of(answer));
        when(roleTypeResolver.resolve(candidateRoleId)).thenReturn(RoleType.CANDIDATE);

        assertThrows(Forbidden.class, () -> domainService.checkDownloadPermission(file, currentUser));
    }

    @Test
    @DisplayName("checkDownloadPermission: 未登录用户下载 WORK 文件时禁止")
    void checkDownloadPermission_workFileUnauthenticated_shouldForbid() {
        File file = File.reconstruct(
                1L,
                "work-uuid.png",
                FileType.WORK,
                "url",
                FileStatus.ACTIVE,
                LocalDateTime.now());

        assertThrows(Forbidden.class, () -> domainService.checkDownloadPermission(file, null));
    }

    @Test
    @DisplayName("checkDownloadPermission: ASSESSMENT_ATTACHMENT 方向匹配时可下载")
    void checkDownloadPermission_assessmentAttachmentSameDirection_shouldAllow() {
        Long fileId = 1L;
        Long questionId = 10L;
        Long timeId = 100L;
        File file = File.reconstruct(
                fileId,
                "attachment.pdf",
                FileType.ASSESSMENT_ATTACHMENT,
                "url",
                FileStatus.ACTIVE,
                LocalDateTime.now());
        User currentUser = User.reconstruct(1L, "encoded");
        currentUser.setDirection(Direction.COMPUTER_VISION);
        AssessmentQuestion question = AssessmentQuestion.reconstruct(
                questionId,
                timeId,
                1,
                QuestionType.SINGLE_CHOICE,
                "title",
                null,
                fileId,
                BigDecimal.TEN);
        AssessmentTime time = AssessmentTime.reconstruct(
                timeId,
                Direction.COMPUTER_VISION,
                1,
                2024,
                LocalDateTime.now(),
                LocalDateTime.now().plusHours(1),
                false,
                null,
                null,
                false);
        when(assessmentQuestionRepository.findByAttachmentId(fileId)).thenReturn(Optional.of(question));
        when(assessmentTimeRepository.findById(timeId)).thenReturn(Optional.of(time));

        domainService.checkDownloadPermission(file, currentUser);
    }

    @Test
    @DisplayName("checkDownloadPermission: ASSESSMENT_ATTACHMENT 方向不匹配时禁止下载")
    void checkDownloadPermission_assessmentAttachmentDifferentDirection_shouldForbid() {
        Long fileId = 1L;
        Long questionId = 10L;
        Long timeId = 100L;
        File file = File.reconstruct(
                fileId,
                "attachment.pdf",
                FileType.ASSESSMENT_ATTACHMENT,
                "url",
                FileStatus.ACTIVE,
                LocalDateTime.now());
        User currentUser = User.reconstruct(1L, "encoded");
        currentUser.setDirection(Direction.STRUCTURAL_DESIGN);
        AssessmentQuestion question = AssessmentQuestion.reconstruct(
                questionId,
                timeId,
                1,
                QuestionType.SINGLE_CHOICE,
                "title",
                null,
                fileId,
                BigDecimal.TEN);
        AssessmentTime time = AssessmentTime.reconstruct(
                timeId,
                Direction.COMPUTER_VISION,
                1,
                2024,
                LocalDateTime.now(),
                LocalDateTime.now().plusHours(1),
                false,
                null,
                null,
                false);
        when(assessmentQuestionRepository.findByAttachmentId(fileId)).thenReturn(Optional.of(question));
        when(assessmentTimeRepository.findById(timeId)).thenReturn(Optional.of(time));

        assertThrows(Forbidden.class, () -> domainService.checkDownloadPermission(file, currentUser));
    }

    @Test
    @DisplayName("checkDownloadPermission: 未登录用户下载 ASSESSMENT_ATTACHMENT 时禁止")
    void checkDownloadPermission_assessmentAttachmentUnauthenticated_shouldForbid() {
        File file = File.reconstruct(
                1L,
                "attachment.pdf",
                FileType.ASSESSMENT_ATTACHMENT,
                "url",
                FileStatus.ACTIVE,
                LocalDateTime.now());

        assertThrows(Forbidden.class, () -> domainService.checkDownloadPermission(file, null));
    }

    @Test
    @DisplayName("checkDownloadPermission: AVATAR 文件允许下载")
    void checkDownloadPermission_avatar_shouldAllow() {
        File file = File.reconstruct(
                1L,
                "avatar.png",
                FileType.AVATAR,
                "url",
                FileStatus.ACTIVE,
                LocalDateTime.now());
        domainService.checkDownloadPermission(file, User.reconstruct(1L, "encoded"));
    }

    @Test
    @DisplayName("checkDownloadPermission: NORMAL_IMG 文件允许下载")
    void checkDownloadPermission_normalImg_shouldAllow() {
        File file = File.reconstruct(
                1L,
                "img.png",
                FileType.NORMAL_IMG,
                "url",
                FileStatus.ACTIVE,
                LocalDateTime.now());
        domainService.checkDownloadPermission(file, null);
    }

    @Test
    @DisplayName("checkDownloadPermission: QRCODE 文件允许下载")
    void checkDownloadPermission_qrcode_shouldAllow() {
        File file = File.reconstruct(
                1L,
                "qr.png",
                FileType.QRCODE,
                "url",
                FileStatus.ACTIVE,
                LocalDateTime.now());
        domainService.checkDownloadPermission(file, null);
    }

    @Test
    @DisplayName("checkDownloadPermission: ENROLL_FORM 文件允许匿名下载")
    void checkDownloadPermission_enrollForm_shouldAllow() {
        File file = File.reconstruct(
                1L,
                "enroll_form-uuid.pdf",
                FileType.ENROLL_FORM,
                "url",
                FileStatus.ACTIVE,
                LocalDateTime.now());
        domainService.checkDownloadPermission(file, null);
    }

    @Test
    @DisplayName("checkDownloadPermission: 未知文件类型应抛出 Forbidden")
    void checkDownloadPermission_unknownFileType_shouldForbid() {
        File file = File.reconstruct(
                1L,
                "doc.pdf",
                FileType.KNOWLEDGE,
                "url",
                FileStatus.ACTIVE,
                LocalDateTime.now());

        assertThrows(
                Forbidden.class,
                () -> domainService.checkDownloadPermission(file, User.reconstruct(1L, "encoded")));
    }
}
