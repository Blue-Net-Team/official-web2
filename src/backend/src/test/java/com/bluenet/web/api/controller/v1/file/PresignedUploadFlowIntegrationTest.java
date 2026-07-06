package com.bluenet.web.api.controller.v1.file;

import com.bluenet.web.BaseIntegrationTest;
import com.bluenet.web.domain.model.enumerate.FileStatus;
import com.bluenet.web.infrastructure.repository.mapper.FileMapper;
import com.bluenet.web.infrastructure.security.principal.WithSecurityPrincipal;
import com.bluenet.web.infrastructure.security.util.UserCTX;
import com.bluenet.web.infrastructure.storage.ObjectStorage;
import com.bluenet.web.infrastructure.storage.StorageObjectMetadata;
import com.bluenet.web.testcontainers.TestcontainersConfiguration;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 预签名上传流程集成测试
 * <p>
 * 测试 prepare-upload → 直传 OSS（mock）→ confirm-upload 完整流程。
 * </p>
 */
@AutoConfigureMockMvc
@Testcontainers
@Import(TestcontainersConfiguration.class)
class PresignedUploadFlowIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private FileMapper fileMapper;

    @MockBean
    private ObjectStorage objectStorage;

    @BeforeEach
    void setUp() {
        fileMapper.delete(null);
    }

    private void assertStatus(Long fileId, FileStatus expectedStatus) {
        assertThat(fileMapper.selectById(fileId).getStatus()).isEqualTo(expectedStatus);
    }

    @AfterEach
    void tearDownUserCtx() {
        UserCTX.clear();
    }

    @Nested
    @DisplayName("预签名上传完整流程")
    class PresignedUploadFlowTests {

        @Test
        @DisplayName("prepare → confirm 成功，文件状态变为 ACTIVE")
        @WithSecurityPrincipal(userId = 1L, studentId = "2024001001", username = "测试用户", roleType = "MEMBER")
        void prepareThenConfirm_success_shouldReturnActive() throws Exception {
            // mock 预签名 URL 和 OSS HEAD 元数据
            when(objectStorage.getPresignedUploadUrl(any(), anyString(), anyString(), anyLong(), any()))
                    .thenReturn("http://minio.test/presigned-put-url");
            when(objectStorage.headObject(any(), anyString()))
                    .thenReturn(new StorageObjectMetadata("abc123", "image/jpeg", 1024));
            when(objectStorage.getObjectHeader(any(), anyString(), eq(8)))
                    .thenReturn(new byte[] { (byte) 0xFF, (byte) 0xD8, (byte) 0xFF, 0, 0, 0, 0, 0 });

            // Step 1: prepare-upload
            Map<String, Object> prepareRequest = Map.of(
                    "filename",
                    "avatar.jpg",
                    "type",
                    "AVATAR",
                    "size",
                    1024,
                    "contentType",
                    "image/jpeg");

            MvcResult prepareResult = mockMvc.perform(
                    post("/api/v1/file/prepare-upload")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(prepareRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.fileId").isNumber())
                    .andExpect(jsonPath("$.data.uploadUrl").value("http://minio.test/presigned-put-url"))
                    .andExpect(jsonPath("$.data.callbackToken").isString())
                    .andReturn();

            Long fileId = objectMapper.readTree(prepareResult.getResponse().getContentAsString())
                    .path("data")
                    .path("fileId")
                    .asLong();
            String callbackToken = objectMapper.readTree(prepareResult.getResponse().getContentAsString())
                    .path("data")
                    .path("callbackToken")
                    .asText();

            // 验证数据库状态为 PENDING
            assertStatus(fileId, FileStatus.PENDING);

            // Step 2: confirm-upload
            Map<String, Object> confirmRequest = Map.of(
                    "fileId",
                    fileId,
                    "callbackToken",
                    callbackToken,
                    "md5",
                    "abc123",
                    "size",
                    1024);

            mockMvc.perform(
                    post("/api/v1/file/confirm-upload")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(confirmRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.status").value("ACTIVE"));

            // 验证数据库状态变为 ACTIVE
            assertStatus(fileId, FileStatus.ACTIVE);
        }

        @Test
        @DisplayName("prepare → confirm MD5 不匹配，文件状态变为 REJECTED")
        @WithSecurityPrincipal(userId = 2L, studentId = "2024001002", username = "测试用户", roleType = "MEMBER")
        void prepareThenConfirm_md5Mismatch_shouldReturnRejected() throws Exception {
            when(objectStorage.getPresignedUploadUrl(any(), anyString(), anyString(), anyLong(), any()))
                    .thenReturn("http://minio.test/presigned-put-url");
            when(objectStorage.headObject(any(), anyString()))
                    .thenReturn(new StorageObjectMetadata("actual-md5", "image/jpeg", 1024));

            Map<String, Object> prepareRequest = Map.of(
                    "filename",
                    "avatar.jpg",
                    "type",
                    "AVATAR",
                    "size",
                    1024,
                    "contentType",
                    "image/jpeg");

            MvcResult prepareResult = mockMvc.perform(
                    post("/api/v1/file/prepare-upload")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(prepareRequest)))
                    .andExpect(status().isOk())
                    .andReturn();

            Long fileId = objectMapper.readTree(prepareResult.getResponse().getContentAsString())
                    .path("data")
                    .path("fileId")
                    .asLong();
            String callbackToken = objectMapper.readTree(prepareResult.getResponse().getContentAsString())
                    .path("data")
                    .path("callbackToken")
                    .asText();

            Map<String, Object> confirmRequest = Map.of(
                    "fileId",
                    fileId,
                    "callbackToken",
                    callbackToken,
                    "md5",
                    "wrong-md5",
                    "size",
                    1024);

            mockMvc.perform(
                    post("/api/v1/file/confirm-upload")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(confirmRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.status").value("REJECTED"));

            assertStatus(fileId, FileStatus.REJECTED);
        }

        @Test
        @DisplayName("confirm 时 OSS 对象不存在，文件状态变为 REJECTED")
        @WithSecurityPrincipal(userId = 3L, studentId = "2024001003", username = "测试用户", roleType = "MEMBER")
        void confirm_objectNotFound_shouldReturnRejected() throws Exception {
            when(objectStorage.getPresignedUploadUrl(any(), anyString(), anyString(), anyLong(), any()))
                    .thenReturn("http://minio.test/presigned-put-url");
            when(objectStorage.headObject(any(), anyString()))
                    .thenThrow(new com.bluenet.web.domain.exception.DataNotFound("File not found"));

            Map<String, Object> prepareRequest = Map.of(
                    "filename",
                    "avatar.jpg",
                    "type",
                    "AVATAR",
                    "size",
                    1024,
                    "contentType",
                    "image/jpeg");

            MvcResult prepareResult = mockMvc.perform(
                    post("/api/v1/file/prepare-upload")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(prepareRequest)))
                    .andExpect(status().isOk())
                    .andReturn();

            Long fileId = objectMapper.readTree(prepareResult.getResponse().getContentAsString())
                    .path("data")
                    .path("fileId")
                    .asLong();
            String callbackToken = objectMapper.readTree(prepareResult.getResponse().getContentAsString())
                    .path("data")
                    .path("callbackToken")
                    .asText();

            Map<String, Object> confirmRequest = Map.of(
                    "fileId",
                    fileId,
                    "callbackToken",
                    callbackToken,
                    "md5",
                    "abc123",
                    "size",
                    1024);

            mockMvc.perform(
                    post("/api/v1/file/confirm-upload")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(confirmRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.status").value("REJECTED"));

            assertStatus(fileId, FileStatus.REJECTED);
        }

        @Test
        @DisplayName("confirm 时 callbackToken 无效应返回403")
        @WithSecurityPrincipal(userId = 4L, studentId = "2024001004", username = "测试用户", roleType = "MEMBER")
        void confirm_invalidCallbackToken_shouldReturn403() throws Exception {
            when(objectStorage.getPresignedUploadUrl(any(), anyString(), anyString(), anyLong(), any()))
                    .thenReturn("http://minio.test/presigned-put-url");

            Map<String, Object> prepareRequest = Map.of(
                    "filename",
                    "avatar.jpg",
                    "type",
                    "AVATAR",
                    "size",
                    1024,
                    "contentType",
                    "image/jpeg");

            MvcResult prepareResult = mockMvc.perform(
                    post("/api/v1/file/prepare-upload")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(prepareRequest)))
                    .andExpect(status().isOk())
                    .andReturn();

            Long fileId = objectMapper.readTree(prepareResult.getResponse().getContentAsString())
                    .path("data")
                    .path("fileId")
                    .asLong();

            Map<String, Object> confirmRequest = Map.of(
                    "fileId",
                    fileId,
                    "callbackToken",
                    "invalid-token",
                    "md5",
                    "abc123",
                    "size",
                    1024);

            mockMvc.perform(
                    post("/api/v1/file/confirm-upload")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(confirmRequest)))
                    .andExpect(status().isForbidden());
        }
    }
}
