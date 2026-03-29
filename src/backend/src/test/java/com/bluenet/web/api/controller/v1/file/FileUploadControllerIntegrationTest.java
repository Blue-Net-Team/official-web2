package com.bluenet.web.api.controller.v1.file;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.bluenet.web.BaseIntegrationTest;
import com.bluenet.web.domain.model.entity.Competition;
import com.bluenet.web.domain.model.entity.File;
import com.bluenet.web.domain.model.entity.IntroduceImage;
import com.bluenet.web.domain.model.enumerate.FileType;
import com.bluenet.web.domain.model.enumerate.ImageType;
import com.bluenet.web.domain.repository.FileRepository;
import com.bluenet.web.infrastructure.repository.mapper.CompetitionMapper;
import com.bluenet.web.infrastructure.repository.mapper.FileMapper;
import com.bluenet.web.infrastructure.repository.mapper.IntroduceImageMapper;
import com.bluenet.web.infrastructure.security.WithUserVO;
import com.bluenet.web.infrastructure.security.util.UserCTX;
import com.bluenet.web.testcontainers.TestcontainersConfiguration;

/**
 * FileUploadController 集成测试
 * <p>
 * 测试介绍图片上传接口的完整流程
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

    @Autowired
    private IntroduceImageMapper introduceImageMapper;

    @Autowired
    private CompetitionMapper competitionMapper;

    @Autowired
    private FileRepository fileRepository;

    private static final Long TEST_COMPETITION_ID = 1L;
    private static final String TEST_COMPETITION_NAME = "蓝桥杯";

    @BeforeEach
    void setUp() {
        fileMapper.delete(null);
        introduceImageMapper.delete(null);
        competitionMapper.delete(null);
    }

    @AfterEach
    void tearDownUserCtx() {
        UserCTX.clear();
    }

    // ==================== uploadIntroduceImage 测试 ====================

    @Nested
    @DisplayName("上传介绍图片接口测试")
    class UploadIntroduceImageTests {

        /**
         * 上传竞赛介绍图片：应成功上传
         */
        @Test
        @DisplayName("上传竞赛介绍图片：应成功上传")
        @WithUserVO(userId = 1L, studentId = "2024001001", username = "管理员", roleName = "MEMBER", permissions = "file:upload:introduce-image")
        void uploadIntroduceImage_competitionType_shouldUploadSuccessfully() throws Exception {
            MockMultipartFile file = new MockMultipartFile("file", "competition.jpg", "image/jpeg",
                    "jpeg content".getBytes(StandardCharsets.UTF_8));

            mockMvc.perform(
                    multipart("/api/v1/file/upload/introduce-image")
                            .file(file)
                            .param("type", "COMPETITION"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.id").isNumber())
                    .andExpect(jsonPath("$.data.type").value("NORMAL_IMG"));
        }

        /**
         * 上传介绍图片带描述：应成功上传
         */
        @Test
        @DisplayName("上传介绍图片带描述：应成功上传")
        @WithUserVO(userId = 1L, studentId = "2024001001", username = "管理员", roleName = "MEMBER", permissions = "file:upload:introduce-image")
        void uploadIntroduceImage_withDescription_shouldUploadSuccessfully() throws Exception {
            MockMultipartFile file = new MockMultipartFile("file", "competition.jpg", "image/jpeg",
                    "jpeg content".getBytes(StandardCharsets.UTF_8));

            mockMvc.perform(
                    multipart("/api/v1/file/upload/introduce-image")
                            .file(file)
                            .param("type", "COMPETITION")
                            .param("description", "竞赛介绍"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.id").isNumber());
        }

        /**
         * 上传介绍图片：描述超长应返回400错误
         */
        @Test
        @DisplayName("上传介绍图片：描述超长应返回400错误")
        @WithUserVO(userId = 1L, studentId = "2024001001", username = "管理员", roleName = "MEMBER", permissions = "file:upload:introduce-image")
        void uploadIntroduceImage_descriptionTooLong_shouldReturn400() throws Exception {
            MockMultipartFile file = new MockMultipartFile("file", "competition.jpg", "image/jpeg",
                    "jpeg content".getBytes(StandardCharsets.UTF_8));

            // 构造超长描述（超过500字符）
            String longDescription = "a".repeat(501);

            mockMvc.perform(
                    multipart("/api/v1/file/upload/introduce-image")
                            .file(file)
                            .param("type", "COMPETITION")
                            .param("description", longDescription))
                    .andExpect(status().isBadRequest());
        }

        /**
         * 上传介绍图片：未登录应返回401错误
         */
        @Test
        @DisplayName("上传介绍图片：未登录应返回401错误")
        void uploadIntroduceImage_unauthenticated_shouldReturn401() throws Exception {
            MockMultipartFile file = new MockMultipartFile("file", "competition.jpg", "image/jpeg",
                    "jpeg content".getBytes(StandardCharsets.UTF_8));

            mockMvc.perform(
                    multipart("/api/v1/file/upload/introduce-image")
                            .file(file)
                            .param("type", "COMPETITION"))
                    .andExpect(status().isUnauthorized());
        }

        /**
         * 上传介绍图片：非MEMBER角色应返回403错误
         */
        @Test
        @DisplayName("上传介绍图片：非MEMBER角色应返回403错误")
        @WithUserVO(userId = 2L, studentId = "2024001002", username = "考生", roleName = "CANDIDATE")
        void uploadIntroduceImage_nonMember_shouldReturn403() throws Exception {
            MockMultipartFile file = new MockMultipartFile("file", "competition.jpg", "image/jpeg",
                    "jpeg content".getBytes(StandardCharsets.UTF_8));

            mockMvc.perform(
                    multipart("/api/v1/file/upload/introduce-image")
                            .file(file)
                            .param("type", "COMPETITION"))
                    .andExpect(status().isForbidden());
        }

        /**
         * 上传介绍图片：无效类型应返回400错误
         */
        @Test
        @DisplayName("上传介绍图片：无效类型应返回400错误")
        @WithUserVO(userId = 1L, studentId = "2024001001", username = "管理员", roleName = "MEMBER", permissions = "file:upload:introduce-image")
        void uploadIntroduceImage_invalidType_shouldReturn400() throws Exception {
            MockMultipartFile file = new MockMultipartFile("file", "test.jpg", "image/jpeg",
                    "jpeg content".getBytes(StandardCharsets.UTF_8));

            mockMvc.perform(
                    multipart("/api/v1/file/upload/introduce-image")
                            .file(file)
                            .param("type", "INVALID_TYPE"))
                    .andExpect(status().isBadRequest());
        }
    }

    // ==================== uploadCompetitionImage 测试 ====================

    @Nested
    @DisplayName("上传竞赛图片接口测试")
    class UploadCompetitionImageTests {

        /**
         * 上传竞赛图片：应成功上传
         */
        @Test
        @DisplayName("上传竞赛图片：应成功上传")
        @WithUserVO(userId = 1L, studentId = "2024001001", username = "管理员", roleName = "MEMBER", permissions = "file:upload:competition:image")
        void uploadCompetitionImage_validCompetition_shouldUploadSuccessfully() throws Exception {
            // 创建测试竞赛
            createTestCompetition();

            MockMultipartFile file = new MockMultipartFile("file", "competition.jpg", "image/jpeg",
                    "jpeg content".getBytes(StandardCharsets.UTF_8));

            mockMvc.perform(
                    multipart("/api/v1/file/upload/competition/image")
                            .file(file)
                            .param("competitionId", TEST_COMPETITION_ID.toString()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.id").isNumber())
                    .andExpect(jsonPath("$.data.type").value("NORMAL_IMG"));
        }

        /**
         * 上传竞赛图片带描述：应成功上传
         */
        @Test
        @DisplayName("上传竞赛图片带描述：应成功上传")
        @WithUserVO(userId = 1L, studentId = "2024001001", username = "管理员", roleName = "MEMBER", permissions = "file:upload:competition:image")
        void uploadCompetitionImage_withDescription_shouldUploadSuccessfully() throws Exception {
            // 创建测试竞赛
            createTestCompetition();

            MockMultipartFile file = new MockMultipartFile("file", "competition.jpg", "image/jpeg",
                    "jpeg content".getBytes(StandardCharsets.UTF_8));

            mockMvc.perform(
                    multipart("/api/v1/file/upload/competition/image")
                            .file(file)
                            .param("competitionId", TEST_COMPETITION_ID.toString())
                            .param("description", "竞赛合影"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));
        }

        /**
         * 上传竞赛图片：竞赛不存在应返回404错误
         */
        @Test
        @DisplayName("上传竞赛图片：竞赛不存在应返回404错误")
        @WithUserVO(userId = 1L, studentId = "2024001001", username = "管理员", roleName = "MEMBER", permissions = "file:upload:competition:image")
        void uploadCompetitionImage_nonExistentCompetition_shouldReturn404() throws Exception {
            MockMultipartFile file = new MockMultipartFile("file", "competition.jpg", "image/jpeg",
                    "jpeg content".getBytes(StandardCharsets.UTF_8));

            mockMvc.perform(
                    multipart("/api/v1/file/upload/competition/image")
                            .file(file)
                            .param("competitionId", "99999"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.code").value(404))
                    .andExpect(jsonPath("$.msg").value("竞赛不存在"));
        }

        /**
         * 上传竞赛图片：图片数量已达上限应返回409错误
         */
        @Test
        @DisplayName("上传竞赛图片：图片数量已达上限应返回409错误")
        @WithUserVO(userId = 1L, studentId = "2024001001", username = "管理员", roleName = "MEMBER", permissions = "file:upload:competition:image")
        void uploadCompetitionImage_exceedLimit_shouldReturn409() throws Exception {
            // 创建测试竞赛
            createTestCompetition();

            // 创建20张已存在的竞赛图片
            for (int i = 0; i < 20; i++) {
                File file = File.builder()
                        .name("existing_" + i + ".jpg")
                        .type(FileType.NORMAL_IMG)
                        .url("http://example.com/existing_" + i + ".jpg")
                        .build();
                fileMapper.insert(file);

                IntroduceImage image = new IntroduceImage();
                image.setType(ImageType.COMPETITION);
                image.setCompetitionId(TEST_COMPETITION_ID);
                image.setFileId(file.getId());
                introduceImageMapper.insert(image);
            }

            MockMultipartFile file = new MockMultipartFile("file", "new.jpg", "image/jpeg",
                    "jpeg content".getBytes(StandardCharsets.UTF_8));

            mockMvc.perform(
                    multipart("/api/v1/file/upload/competition/image")
                            .file(file)
                            .param("competitionId", TEST_COMPETITION_ID.toString()))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.code").value(409))
                    .andExpect(jsonPath("$.msg").value("竞赛图片数量已达上限（最多20张）"));
        }

        /**
         * 上传竞赛图片：已有19张图片时应允许上传
         */
        @Test
        @DisplayName("上传竞赛图片：已有19张图片时应允许上传")
        @WithUserVO(userId = 1L, studentId = "2024001001", username = "管理员", roleName = "MEMBER", permissions = "file:upload:competition:image")
        void uploadCompetitionImage_19Images_shouldAllowUpload() throws Exception {
            // 创建测试竞赛
            createTestCompetition();

            // 创建19张已存在的竞赛图片
            for (int i = 0; i < 19; i++) {
                File file = File.builder()
                        .name("existing_" + i + ".jpg")
                        .type(FileType.NORMAL_IMG)
                        .url("http://example.com/existing_" + i + ".jpg")
                        .build();
                fileMapper.insert(file);

                IntroduceImage image = new IntroduceImage();
                image.setType(ImageType.COMPETITION);
                image.setCompetitionId(TEST_COMPETITION_ID);
                image.setFileId(file.getId());
                introduceImageMapper.insert(image);
            }

            MockMultipartFile file = new MockMultipartFile("file", "new.jpg", "image/jpeg",
                    "jpeg content".getBytes(StandardCharsets.UTF_8));

            mockMvc.perform(
                    multipart("/api/v1/file/upload/competition/image")
                            .file(file)
                            .param("competitionId", TEST_COMPETITION_ID.toString()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));
        }

        /**
         * 上传竞赛图片：未登录应返回401错误
         */
        @Test
        @DisplayName("上传竞赛图片：未登录应返回401错误")
        void uploadCompetitionImage_unauthenticated_shouldReturn401() throws Exception {
            MockMultipartFile file = new MockMultipartFile("file", "competition.jpg", "image/jpeg",
                    "jpeg content".getBytes(StandardCharsets.UTF_8));

            mockMvc.perform(
                    multipart("/api/v1/file/upload/competition/image")
                            .file(file)
                            .param("competitionId", "1"))
                    .andExpect(status().isUnauthorized());
        }

        /**
         * 上传竞赛图片：非MEMBER角色应返回403错误
         */
        @Test
        @DisplayName("上传竞赛图片：非MEMBER角色应返回403错误")
        @WithUserVO(userId = 2L, studentId = "2024001002", username = "考生", roleName = "CANDIDATE")
        void uploadCompetitionImage_nonMember_shouldReturn403() throws Exception {
            MockMultipartFile file = new MockMultipartFile("file", "competition.jpg", "image/jpeg",
                    "jpeg content".getBytes(StandardCharsets.UTF_8));

            mockMvc.perform(
                    multipart("/api/v1/file/upload/competition/image")
                            .file(file)
                            .param("competitionId", "1"))
                    .andExpect(status().isForbidden());
        }

        /**
         * 上传竞赛图片：描述超长应返回400错误
         */
        @Test
        @DisplayName("上传竞赛图片：描述超长应返回400错误")
        @WithUserVO(userId = 1L, studentId = "2024001001", username = "管理员", roleName = "MEMBER", permissions = "file:upload:competition:image")
        void uploadCompetitionImage_descriptionTooLong_shouldReturn400() throws Exception {
            // 创建测试竞赛
            createTestCompetition();

            MockMultipartFile file = new MockMultipartFile("file", "competition.jpg", "image/jpeg",
                    "jpeg content".getBytes(StandardCharsets.UTF_8));

            // 构造超长描述（超过500字符）
            String longDescription = "a".repeat(501);

            mockMvc.perform(
                    multipart("/api/v1/file/upload/competition/image")
                            .file(file)
                            .param("competitionId", TEST_COMPETITION_ID.toString())
                            .param("description", longDescription))
                    .andExpect(status().isBadRequest());
        }
    }

    // ==================== uploadCompetitionLogo 测试 ====================

    @Nested
    @DisplayName("上传竞赛Logo接口测试")
    class UploadCompetitionLogoTests {

        /**
         * 上传竞赛Logo：应成功上传并更新竞赛Logo
         */
        @Test
        @DisplayName("上传竞赛Logo：应成功上传并更新竞赛Logo")
        @WithUserVO(userId = 1L, studentId = "2024001001", username = "管理员", roleName = "MEMBER", permissions = "file:upload:competition:logo")
        void uploadCompetitionLogo_validCompetition_shouldUploadSuccessfully() throws Exception {
            // 创建测试竞赛
            createTestCompetition();

            MockMultipartFile file = new MockMultipartFile("file", "logo.png", "image/png",
                    "png content".getBytes(StandardCharsets.UTF_8));

            mockMvc.perform(
                    multipart("/api/v1/file/upload/competition/logo")
                            .file(file)
                            .param("competitionId", TEST_COMPETITION_ID.toString()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.id").isNumber())
                    .andExpect(jsonPath("$.data.type").value("NORMAL_IMG"));

            // 验证竞赛Logo已更新
            Competition updatedCompetition = competitionMapper.selectById(TEST_COMPETITION_ID);
            assertNotNull(updatedCompetition.getLogoFileId());
        }

        /**
         * 上传竞赛Logo：竞赛不存在应返回404错误
         */
        @Test
        @DisplayName("上传竞赛Logo：竞赛不存在应返回404错误")
        @WithUserVO(userId = 1L, studentId = "2024001001", username = "管理员", roleName = "MEMBER", permissions = "file:upload:competition:logo")
        void uploadCompetitionLogo_nonExistentCompetition_shouldReturn404() throws Exception {
            MockMultipartFile file = new MockMultipartFile("file", "logo.png", "image/png",
                    "png content".getBytes(StandardCharsets.UTF_8));

            mockMvc.perform(
                    multipart("/api/v1/file/upload/competition/logo")
                            .file(file)
                            .param("competitionId", "99999"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.code").value(404))
                    .andExpect(jsonPath("$.msg").value("竞赛不存在"));
        }

        /**
         * 上传竞赛Logo：未登录应返回401错误
         */
        @Test
        @DisplayName("上传竞赛Logo：未登录应返回401错误")
        void uploadCompetitionLogo_unauthenticated_shouldReturn401() throws Exception {
            MockMultipartFile file = new MockMultipartFile("file", "logo.png", "image/png",
                    "png content".getBytes(StandardCharsets.UTF_8));

            mockMvc.perform(
                    multipart("/api/v1/file/upload/competition/logo")
                            .file(file)
                            .param("competitionId", "1"))
                    .andExpect(status().isUnauthorized());
        }

        /**
         * 上传竞赛Logo：非MEMBER角色应返回403错误
         */
        @Test
        @DisplayName("上传竞赛Logo：非MEMBER角色应返回403错误")
        @WithUserVO(userId = 2L, studentId = "2024001002", username = "考生", roleName = "CANDIDATE")
        void uploadCompetitionLogo_nonMember_shouldReturn403() throws Exception {
            MockMultipartFile file = new MockMultipartFile("file", "logo.png", "image/png",
                    "png content".getBytes(StandardCharsets.UTF_8));

            mockMvc.perform(
                    multipart("/api/v1/file/upload/competition/logo")
                            .file(file)
                            .param("competitionId", "1"))
                    .andExpect(status().isForbidden());
        }

        /**
         * 上传竞赛Logo：覆盖已有Logo应成功
         */
        @Test
        @DisplayName("上传竞赛Logo：覆盖已有Logo应成功")
        @WithUserVO(userId = 1L, studentId = "2024001001", username = "管理员", roleName = "MEMBER", permissions = "file:upload:competition:logo")
        void uploadCompetitionLogo_overwriteExistingLogo_shouldUploadSuccessfully() throws Exception {
            // 创建测试竞赛
            createTestCompetition();

            // 创建已有Logo文件
            File existingLogo = File.builder()
                    .name("old_logo.png")
                    .type(FileType.NORMAL_IMG)
                    .url("http://example.com/old_logo.png")
                    .build();
            fileMapper.insert(existingLogo);

            // 更新竞赛Logo
            Competition competition = competitionMapper.selectById(TEST_COMPETITION_ID);
            competition.setLogoFileId(existingLogo.getId());
            competitionMapper.updateById(competition);

            // 上传新Logo
            MockMultipartFile file = new MockMultipartFile("file", "new_logo.png", "image/png",
                    "png content".getBytes(StandardCharsets.UTF_8));

            mockMvc.perform(
                    multipart("/api/v1/file/upload/competition/logo")
                            .file(file)
                            .param("competitionId", TEST_COMPETITION_ID.toString()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));

            // 验证竞赛Logo已更新为新Logo
            Competition updatedCompetition = competitionMapper.selectById(TEST_COMPETITION_ID);
            assertNotEquals(existingLogo.getId(), updatedCompetition.getLogoFileId());
        }
    }

    // ==================== 辅助方法 ====================

    /**
     * 创建测试竞赛
     */
    private void createTestCompetition() {
        Competition competition = new Competition();
        competition.setId(TEST_COMPETITION_ID);
        competition.setName(TEST_COMPETITION_NAME);
        competition.setShortName("蓝桥杯");
        competition.setSummary("全国软件和信息技术专业人才大赛");
        competition.setDetail("蓝桥杯全国软件和信息技术专业人才大赛是由工业和信息化部人才交流中心举办的全国性IT学科赛事。");
        competition.setSortOrder(0);
        competition.setEnabled(true);
        competitionMapper.insert(competition);
    }
}
