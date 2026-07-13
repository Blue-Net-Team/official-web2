package com.bluenet.web.application.service.impl;

import com.bluenet.web.BaseIntegrationTest;
import com.bluenet.web.application.command.file.FileCommands;
import com.bluenet.web.application.result.file.FileDownloadResult;
import com.bluenet.web.application.result.file.FileResult;
import com.bluenet.web.application.service.FileAppService;
import com.bluenet.web.domain.exception.DataNotFound;
import com.bluenet.web.domain.exception.Forbidden;
import com.bluenet.web.domain.model.entity.AssessmentAnswer;
import com.bluenet.web.domain.model.enumerate.FileStatus;
import com.bluenet.web.domain.model.enumerate.FileType;
import com.bluenet.web.domain.model.result.ConfirmUploadResult;
import com.bluenet.web.domain.model.result.PresignedUploadResult;
import com.bluenet.web.domain.repository.AssessmentAnswerRepository;
import com.bluenet.web.infrastructure.security.principal.WithSecurityPrincipal;
import com.bluenet.web.infrastructure.security.util.UserCTX;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.util.DigestUtils;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * FileAppServiceImpl 集成测试。
 * <p>
 * 覆盖文件上传、下载、预签名上传确认等核心链路，使用真实 MinIO Testcontainer。
 * </p>
 */
@DisplayName("FileAppServiceImpl 集成测试")
class FileAppServiceImplIntegrationTest extends BaseIntegrationTest {

    private static final byte[] TEXT_BYTES = "Hello, BlueNet!".getBytes(StandardCharsets.UTF_8);

    private static final byte[] PNG_BYTES = new byte[] {
            (byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A,
            0x00, 0x00, 0x00, 0x0D, 0x49, 0x48, 0x44, 0x52,
            0x00, 0x00, 0x00, 0x01, 0x00, 0x00, 0x00, 0x01,
            0x08, 0x02, 0x00, 0x00, 0x00, (byte) 0x90, 0x77, 0x53,
            (byte) 0xDE, 0x00, 0x00, 0x00, 0x0C, 0x49, 0x44, 0x41, 0x54,
            0x08, (byte) 0xD7, 0x63, (byte) 0xF8, 0x0F, 0x00, 0x00, 0x01,
            0x01, 0x00, 0x05, 0x18, (byte) 0xD8, (byte) 0xB4, 0x00, 0x00,
            0x00, 0x00, 0x49, 0x45, 0x4E, 0x44, (byte) 0xAE, 0x42, 0x60, (byte) 0x82
    };

    @Autowired
    private FileAppService fileAppService;

    @Autowired
    private AssessmentAnswerRepository assessmentAnswerRepository;

    @AfterEach
    void clearContext() {
        UserCTX.clear();
    }

    @Test
    @DisplayName("uploadFile: 使用 MockMultipartFile 上传文本文件应返回 ACTIVE 状态文件结果")
    void uploadFile_withMockMultipartFile_shouldReturnActiveFileResult() {
        MockMultipartFile multipartFile = new MockMultipartFile(
                "file",
                "hello.txt",
                MediaType.TEXT_PLAIN_VALUE,
                TEXT_BYTES);
        FileCommands.UploadFileCommand command = new FileCommands.UploadFileCommand(multipartFile, FileType.NORMAL_IMG);

        FileResult result = fileAppService.uploadFile(command);

        assertThat(result).isNotNull();
        assertThat(result.id()).isPositive();
        assertThat(result.name()).isNotBlank();
        assertThat(result.type()).isEqualTo(FileType.NORMAL_IMG);
        assertThat(result.status()).isEqualTo(FileStatus.ACTIVE);
    }

    @Test
    @DisplayName("downloadFile: 应能下载已上传的文件并返回正确内容")
    void downloadFile_withExistingFile_shouldReturnResource() throws IOException {
        Long fileId = uploadAndReturnId(TEXT_BYTES, "download.txt", MediaType.TEXT_PLAIN_VALUE, FileType.NORMAL_IMG);
        FileCommands.DownloadFileCommand command = new FileCommands.DownloadFileCommand(fileId);

        FileDownloadResult result = fileAppService.downloadFile(command);

        assertThat(result).isNotNull();
        assertThat(result.filename()).isNotBlank();
        assertThat(result.resource()).isNotNull();
        assertThat(result.resource().exists()).isTrue();
        byte[] actual = result.resource().getInputStream().readAllBytes();
        assertThat(actual).isEqualTo(TEXT_BYTES);
    }

    @Test
    @DisplayName("getPresignedDownloadUrl: 应为已上传文件返回非空预签名下载 URL")
    void getPresignedDownloadUrl_withExistingFile_shouldReturnNonBlankUrl() {
        Long fileId = uploadAndReturnId(TEXT_BYTES, "presigned.txt", MediaType.TEXT_PLAIN_VALUE, FileType.NORMAL_IMG);
        FileCommands.DownloadFileCommand command = new FileCommands.DownloadFileCommand(fileId);

        String url = fileAppService.getPresignedDownloadUrl(command);

        assertThat(url).isNotBlank();
        assertThat(url).contains("X-Amz");
    }

    @Test
    @DisplayName("prepareUpload: 应返回预签名上传 URL、文件 ID 与回调令牌")
    void prepareUpload_shouldReturnPresignedUrlAndFileId() {
        FileCommands.PrepareUploadCommand command = new FileCommands.PrepareUploadCommand(
                "sample.png",
                FileType.NORMAL_IMG,
                PNG_BYTES.length,
                MediaType.IMAGE_PNG_VALUE);

        PresignedUploadResult result = fileAppService.prepareUpload(command);

        assertThat(result).isNotNull();
        assertThat(result.fileId()).isPositive();
        assertThat(result.uploadUrl()).isNotBlank();
        assertThat(result.callbackToken()).isNotBlank();
        assertThat(result.filename()).isNotBlank();
        assertThat(result.type()).isEqualTo(FileType.NORMAL_IMG);
    }

    @Test
    @DisplayName("confirmUpload: 通过预签名 URL 实际上传后确认应返回 ACTIVE 状态")
    void confirmUpload_afterPresignedUpload_shouldReturnActive() throws Exception {
        FileCommands.PrepareUploadCommand prepareCommand = new FileCommands.PrepareUploadCommand(
                "sample.png",
                FileType.NORMAL_IMG,
                PNG_BYTES.length,
                MediaType.IMAGE_PNG_VALUE);
        PresignedUploadResult prepared = fileAppService.prepareUpload(prepareCommand);

        uploadViaPresignedUrl(prepared.uploadUrl(), PNG_BYTES, MediaType.IMAGE_PNG_VALUE);

        String md5 = DigestUtils.md5DigestAsHex(PNG_BYTES);
        FileCommands.ConfirmUploadCommand confirmCommand = new FileCommands.ConfirmUploadCommand(
                prepared.fileId(),
                prepared.callbackToken(),
                md5,
                PNG_BYTES.length);

        ConfirmUploadResult result = fileAppService.confirmUpload(confirmCommand);

        assertThat(result).isNotNull();
        assertThat(result.fileId()).isEqualTo(prepared.fileId());
        assertThat(result.filename()).isEqualTo(prepared.filename());
        assertThat(result.type()).isEqualTo(FileType.NORMAL_IMG);
        assertThat(result.status()).isEqualTo(FileStatus.ACTIVE);
    }

    @Test
    @DisplayName("downloadFile: 文件不存在时应抛出 DataNotFound")
    void downloadFile_withNonExistingFile_shouldThrowDataNotFound() {
        FileCommands.DownloadFileCommand command = new FileCommands.DownloadFileCommand(999_999L);

        assertThatThrownBy(() -> fileAppService.downloadFile(command))
                .isInstanceOf(DataNotFound.class)
                .hasMessageContaining("文件不存在");
    }

    @Test
    @DisplayName("downloadFile: 考生下载其他用户作品文件时应抛出 Forbidden")
    @WithSecurityPrincipal(userId = 1L, roleType = "CANDIDATE", roleId = 4L)
    void downloadFile_withoutPermission_shouldThrowForbidden() {
        Long fileId = uploadAndReturnId(TEXT_BYTES, "work.txt", MediaType.TEXT_PLAIN_VALUE, FileType.WORK);
        AssessmentAnswer answer = AssessmentAnswer.create(999L, 1L, null, null, fileId);
        assessmentAnswerRepository.save(answer);

        FileCommands.DownloadFileCommand command = new FileCommands.DownloadFileCommand(fileId);

        assertThatThrownBy(() -> fileAppService.downloadFile(command))
                .isInstanceOf(Forbidden.class)
                .hasMessageContaining("权限不够");
    }

    @Test
    @DisplayName("downloadBatchStream: 应返回非空的 StreamingResponseBody")
    void downloadBatchStream_shouldReturnStreamingResponseBody() {
        Long fileId = uploadAndReturnId(TEXT_BYTES, "batch.txt", MediaType.TEXT_PLAIN_VALUE, FileType.NORMAL_IMG);
        FileCommands.BatchDownloadCommand command = new FileCommands.BatchDownloadCommand(
                List.of(new FileCommands.BatchDownloadEntry(fileId, "batch-entry.txt")),
                "batch-download");

        StreamingResponseBody result = fileAppService.downloadBatchStream(command);

        assertThat(result).isNotNull();
    }

    private Long uploadAndReturnId(byte[] content, String filename, String contentType, FileType type) {
        MockMultipartFile multipartFile = new MockMultipartFile("file", filename, contentType, content);
        FileResult result = fileAppService.uploadFile(new FileCommands.UploadFileCommand(multipartFile, type));
        return result.id();
    }

    private void uploadViaPresignedUrl(String uploadUrl, byte[] content, String contentType) throws Exception {
        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        HttpRequest request = HttpRequest.newBuilder(URI.create(uploadUrl))
                .header("Content-Type", contentType)
                .PUT(HttpRequest.BodyPublishers.ofByteArray(content))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertThat(response.statusCode()).isBetween(200, 299);
    }
}
