package com.bluenet.web.api.controller.v1.file;

import com.bluenet.web.BaseIntegrationTest;
import com.bluenet.web.application.command.file.FileCommands;
import com.bluenet.web.application.service.FileAppService;
import com.bluenet.web.domain.model.enumerate.FileType;
import com.bluenet.web.infrastructure.security.principal.WithSecurityPrincipal;
import com.bluenet.web.infrastructure.security.rate.AnonymousUploadRateLimiter;
import com.bluenet.web.infrastructure.security.util.UserCTX;
import com.bluenet.web.testconfig.TestSecurityConfig;
import com.bluenet.web.domain.model.result.PresignedUploadResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestSecurityConfig.class)
@DisplayName("FileUploadController 集成测试")
class FileUploadControllerIntegrationTest extends BaseIntegrationTest {

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
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private FileAppService fileAppService;

    @MockitoBean
    private AnonymousUploadRateLimiter rateLimiter;

    @AfterEach
    void tearDown() {
        UserCTX.clear();
    }

    @Test
    @DisplayName("uploadFile: 匿名上传 AVATAR 类型文件应成功")
    void uploadFile_anonymousAvatar_shouldReturnOk() throws Exception {
        MockMultipartFile multipartFile = new MockMultipartFile(
                "file",
                "avatar.png",
                MediaType.IMAGE_PNG_VALUE,
                TEXT_BYTES);

        mockMvc.perform(
                multipart("/api/v1/file/upload")
                        .file(multipartFile)
                        .param("type", "AVATAR"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").exists())
                .andExpect(jsonPath("$.data.type").value("AVATAR"));
    }

    @Test
    @DisplayName("uploadFile: 匿名上传 WORK 类型文件应返回 401")
    void uploadFile_anonymousWork_shouldReturnUnauthorized() throws Exception {
        MockMultipartFile multipartFile = new MockMultipartFile(
                "file",
                "work.txt",
                MediaType.TEXT_PLAIN_VALUE,
                TEXT_BYTES);

        mockMvc.perform(
                multipart("/api/v1/file/upload")
                        .file(multipartFile)
                        .param("type", "WORK"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401));
    }

    @Test
    @DisplayName("uploadFile: 已登录用户上传 WORK 类型文件应成功")
    @WithSecurityPrincipal(userId = 1L, roleType = "MEMBER", roleId = 3L)
    void uploadFile_authenticatedWork_shouldReturnOk() throws Exception {
        MockMultipartFile multipartFile = new MockMultipartFile(
                "file",
                "work.txt",
                MediaType.TEXT_PLAIN_VALUE,
                TEXT_BYTES);

        mockMvc.perform(
                multipart("/api/v1/file/upload")
                        .file(multipartFile)
                        .param("type", "WORK"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.type").value("WORK"));
    }

    @Test
    @DisplayName("prepareUpload: 匿名上传 AVATAR 预签名准备应成功")
    void prepareUpload_anonymousAvatar_shouldReturnPresignedUrl() throws Exception {
        when(rateLimiter.tryAcquire(anyString())).thenReturn(true);

        String payload = "{\"filename\":\"avatar.png\",\"type\":\"AVATAR\",\"size\":17,\"contentType\":\"image/png\"}";

        mockMvc.perform(
                post("/api/v1/file/prepare-upload")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.fileId").exists())
                .andExpect(jsonPath("$.data.uploadUrl").exists())
                .andExpect(jsonPath("$.data.callbackToken").exists());
    }

    @Test
    @DisplayName("prepareUpload: 匿名限流失败时应返回 429")
    void prepareUpload_rateLimited_shouldReturnTooManyRequests() throws Exception {
        when(rateLimiter.tryAcquire(anyString())).thenReturn(false);

        String payload = "{\"filename\":\"avatar.png\",\"type\":\"AVATAR\",\"size\":17,\"contentType\":\"image/png\"}";

        mockMvc.perform(
                post("/api/v1/file/prepare-upload")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.code").value(429));
    }

    @Test
    @DisplayName("confirmUpload: 预签名上传确认应成功")
    void confirmUpload_afterPresignedUpload_shouldReturnActive() throws Exception {
        when(rateLimiter.tryAcquire(anyString())).thenReturn(true);

        FileCommands.PrepareUploadCommand prepareCommand = new FileCommands.PrepareUploadCommand(
                "sample.png",
                FileType.NORMAL_IMG,
                PNG_BYTES.length,
                MediaType.IMAGE_PNG_VALUE);
        PresignedUploadResult prepared = fileAppService.prepareUpload(prepareCommand);

        uploadViaPresignedUrl(prepared.uploadUrl(), PNG_BYTES, MediaType.IMAGE_PNG_VALUE);

        String md5 = org.springframework.util.DigestUtils.md5DigestAsHex(PNG_BYTES);
        String payload = String.format(
                "{\"fileId\":%d,\"callbackToken\":\"%s\",\"md5\":\"%s\",\"size\":%d}",
                prepared.fileId(),
                prepared.callbackToken(),
                md5,
                PNG_BYTES.length);

        mockMvc.perform(
                post("/api/v1/file/confirm-upload")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.status").value("ACTIVE"));
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
        org.assertj.core.api.Assertions.assertThat(response.statusCode()).isBetween(200, 299);
    }
}
