package com.bluenet.web.api.controller.v1.file;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.bluenet.web.BaseIntegrationTest;
import com.bluenet.web.domain.model.enumerate.FileType;
import com.bluenet.web.infrastructure.repository.mapper.FileMapper;
import com.bluenet.web.infrastructure.security.WithUserVO;
import com.bluenet.web.infrastructure.security.util.UserCTX;
import com.bluenet.web.testcontainers.TestcontainersConfiguration;

/**
 * FileUploadController 集成测试
 * <p>
 * 测试统一文件上传接口 POST /api/v1/file/upload
 * </p>
 */
@AutoConfigureMockMvc
@Testcontainers
@Import(TestcontainersConfiguration.class)
class FileUploadControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private FileMapper fileMapper;

    @BeforeEach
    void setUp() {
        fileMapper.delete(null);
    }

    @AfterEach
    void tearDownUserCtx() {
        UserCTX.clear();
    }

    // ==================== 统一文件上传测试 ====================

    @Nested
    @DisplayName("统一文件上传接口测试")
    class UploadFileTests {

        /**
         * TC-101: 上传 AVATAR 类型（允许未登录）
         */
        @Test
        @DisplayName("上传 AVATAR 类型 - 未登录应成功")
        void uploadFile_avatarUnauthenticated_shouldReturn200() throws Exception {
            MockMultipartFile file = new MockMultipartFile("file", "avatar.jpg", "image/jpeg",
                    "jpeg content".getBytes(StandardCharsets.UTF_8));

            mockMvc.perform(
                    multipart("/api/v1/file/upload")
                            .file(file)
                            .param("type", "AVATAR"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.id").isNumber())
                    .andExpect(jsonPath("$.data.type").value("AVATAR"));
        }

        /**
         * TC-102~105: 上传各类型文件（已登录用户）
         */
        @ParameterizedTest(name = "上传 {0} 类型 - 已登录应成功")
        @EnumSource(FileType.class)
        @DisplayName("上传各类型文件 - 已登录用户应成功")
        @WithUserVO(userId = 1L, studentId = "2024001001", username = "用户", roleName = "MEMBER")
        void uploadFile_authenticatedEachType_shouldReturn200(FileType fileType) throws Exception {
            MockMultipartFile file = new MockMultipartFile("file", "test.dat", "application/octet-stream",
                    "content".getBytes(StandardCharsets.UTF_8));

            mockMvc.perform(
                    multipart("/api/v1/file/upload")
                            .file(file)
                            .param("type", fileType.name()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.id").isNumber())
                    .andExpect(jsonPath("$.data.type").value(fileType.name()));
        }

        /**
         * TC-107: 非 AVATAR 类型未登录应被拒绝
         */
        @Test
        @DisplayName("上传 WORK 类型 - 未登录应返回401")
        void uploadFile_workUnauthenticated_shouldReturn401() throws Exception {
            MockMultipartFile file = new MockMultipartFile("file", "work.zip", "application/zip",
                    "zip content".getBytes(StandardCharsets.UTF_8));

            mockMvc.perform(
                    multipart("/api/v1/file/upload")
                            .file(file)
                            .param("type", "WORK"))
                    .andExpect(status().isUnauthorized());
        }

        /**
         * TC-108: type 参数缺失应返回400
         */
        @Test
        @DisplayName("上传文件 - type 参数缺失应返回400")
        void uploadFile_missingType_shouldReturn400() throws Exception {
            MockMultipartFile file = new MockMultipartFile("file", "test.jpg", "image/jpeg",
                    "jpeg content".getBytes(StandardCharsets.UTF_8));

            mockMvc.perform(
                    multipart("/api/v1/file/upload")
                            .file(file))
                    .andExpect(status().isBadRequest());
        }

        /**
         * 上传无效的 type 值应返回400
         */
        @Test
        @DisplayName("上传文件 - 无效 type 值应返回400")
        void uploadFile_invalidType_shouldReturn400() throws Exception {
            MockMultipartFile file = new MockMultipartFile("file", "test.jpg", "image/jpeg",
                    "jpeg content".getBytes(StandardCharsets.UTF_8));

            mockMvc.perform(
                    multipart("/api/v1/file/upload")
                            .file(file)
                            .param("type", "INVALID_TYPE"))
                    .andExpect(status().isBadRequest());
        }

        /**
         * 上传 AVATAR 类型 - 已登录用户也能上传
         */
        @Test
        @DisplayName("上传 AVATAR 类型 - 已登录用户应成功")
        @WithUserVO(userId = 2L, studentId = "2024001002", username = "已登录用户", roleName = "CANDIDATE")
        void uploadFile_avatarAuthenticated_shouldReturn200() throws Exception {
            MockMultipartFile file = new MockMultipartFile("file", "avatar.jpg", "image/jpeg",
                    "jpeg content".getBytes(StandardCharsets.UTF_8));

            mockMvc.perform(
                    multipart("/api/v1/file/upload")
                            .file(file)
                            .param("type", "AVATAR"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.type").value("AVATAR"));
        }
    }
}
