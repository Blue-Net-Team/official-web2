package com.bluenet.web.api.controller.v1.file;

import com.bluenet.web.infrastructure.repository.dataobject.*;

import com.bluenet.web.testsupport.RepositoryTestObjects;

import com.bluenet.web.BaseIntegrationTest;
import com.bluenet.web.domain.model.entity.AssessmentAnswer;
import com.bluenet.web.domain.model.entity.AssessmentQuestion;
import com.bluenet.web.domain.model.entity.AssessmentTime;
import com.bluenet.web.domain.model.entity.File;
import com.bluenet.web.domain.model.entity.User;
import com.bluenet.web.domain.model.enumerate.Direction;
import com.bluenet.web.domain.model.enumerate.FileStatus;
import com.bluenet.web.domain.model.enumerate.FileType;
import com.bluenet.web.domain.model.vo.FileVO;
import com.bluenet.web.domain.repository.FileRepository;
import com.bluenet.web.infrastructure.repository.mapper.AssessmentAnswerMapper;
import com.bluenet.web.infrastructure.repository.mapper.AssessmentQuestionMapper;
import com.bluenet.web.infrastructure.repository.mapper.AssessmentTimeMapper;
import com.bluenet.web.infrastructure.repository.mapper.FileMapper;
import com.bluenet.web.infrastructure.repository.mapper.UserMapper;
import com.bluenet.web.infrastructure.security.principal.WithSecurityPrincipal;
import com.bluenet.web.infrastructure.security.util.UserCTX;
import com.bluenet.web.testcontainers.TestcontainersConfiguration;
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

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 文件上传下载集成测试
 * <p>
 * 上传部分使用统一接口 POST /api/v1/file/upload， 下载部分使用 GET
 * /api/v1/file/download/{fileId}。
 * </p>
 */
@AutoConfigureMockMvc
@Testcontainers
@Import(TestcontainersConfiguration.class)
class FileUploadDownloadIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private AssessmentTimeMapper assessmentTimeMapper;

    @Autowired
    private AssessmentQuestionMapper assessmentQuestionMapper;

    @Autowired
    private AssessmentAnswerMapper assessmentAnswerMapper;

    @Autowired
    private FileMapper fileMapper;

    @Autowired
    private FileRepository fileRepository;

    @BeforeEach
    void setUp() {
        fileMapper.delete(null);
        userMapper.delete(null);
        assessmentTimeMapper.delete(null);
        assessmentQuestionMapper.delete(null);
        assessmentAnswerMapper.delete(null);
    }

    @AfterEach
    void tearDownUserCtx() {
        UserCTX.clear();
    }

    private FileVO createFileInMinio(String filename, FileType fileType) {
        File file = File.reconstruct(null, filename, fileType, "test-url", FileStatus.ACTIVE, null);
        ByteArrayInputStream inputStream = new ByteArrayInputStream("test content".getBytes(StandardCharsets.UTF_8));
        var savedFile = fileRepository.saveFile(inputStream, file);
        return FileVO.builder()
                .id(savedFile.getId())
                .name(savedFile.getName())
                .type(savedFile.getType())
                .url(savedFile.getUrl())
                .build();
    }

    // ==================== 上传测试 ====================

    @Nested
    @DisplayName("统一文件上传测试")
    class UploadTests {

        @Test
        @DisplayName("上传头像文件 - 应创建文件记录并返回FileInfo")
        void uploadAvatar_shouldCreateFileRecord() throws Exception {
            MockMultipartFile file = new MockMultipartFile("file", "avatar.png", "image/png",
                    "png".getBytes(StandardCharsets.UTF_8));

            mockMvc.perform(
                    multipart("/api/v1/file/upload")
                            .file(file)
                            .param("type", "AVATAR"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.id").isNumber())
                    .andExpect(jsonPath("$.data.type").value("AVATAR"));

            // 验证文件记录已创建
            assertEquals(1, fileMapper.selectCount(null));
        }

        @Test
        @DisplayName("上传作品文件 - 已登录用户应成功")
        @WithSecurityPrincipal(userId = 2L, studentId = "2024001002", username = "李四", roleType = "CANDIDATE")
        void uploadWork_authenticated_shouldCreateFileRecord() throws Exception {
            MockMultipartFile file = new MockMultipartFile("file", "work.zip", "application/zip",
                    "zip".getBytes(StandardCharsets.UTF_8));

            mockMvc.perform(
                    multipart("/api/v1/file/upload")
                            .file(file)
                            .param("type", "WORK"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.id").exists())
                    .andExpect(jsonPath("$.data.type").value("WORK"));

            assertEquals(1, fileMapper.selectCount(null));
        }

        @Test
        @DisplayName("上传作品文件 - 未登录应返回401")
        void uploadWork_unauthenticated_shouldReturn401() throws Exception {
            MockMultipartFile file = new MockMultipartFile("file", "work.zip", "application/zip",
                    "zip".getBytes(StandardCharsets.UTF_8));

            mockMvc.perform(
                    multipart("/api/v1/file/upload")
                            .file(file)
                            .param("type", "WORK"))
                    .andExpect(status().isUnauthorized());

            // 不应创建文件记录
            assertEquals(0, fileMapper.selectCount(null));
        }
    }

    // ==================== 下载测试 ====================

    @Test
    @DisplayName("下载文件 - 文件不存在应返回404")
    void downloadFile_notFound_shouldReturn404() throws Exception {
        mockMvc.perform(get("/api/v1/file/download/99999")).andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("下载作品文件 - 提交者下载自己的作品应返回200")
    @WithSecurityPrincipal(userId = 100L, studentId = "2024001100", username = "提交者", roleType = "CANDIDATE", direction = Direction.COMPUTER_VISION)
    void downloadWorkFile_asSubmitter_shouldReturn200() throws Exception {
        User user = User.reconstruct(
                100L,
                "2024001100",
                null,
                null,
                null,
                "提交者",
                null,
                null,
                null,
                null,
                Direction.COMPUTER_VISION,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null);
        RepositoryTestObjects.insert(userMapper, user, UserDO.class);

        AssessmentTime time = AssessmentTime
                .reconstruct(100L, Direction.COMPUTER_VISION, null, null, null, null, false, null, null, null);
        RepositoryTestObjects.insert(assessmentTimeMapper, time, AssessmentTimeDO.class);

        AssessmentQuestion question = AssessmentQuestion.reconstruct(100L, 100L, 1, null, null, null, null, null);
        RepositoryTestObjects.insert(assessmentQuestionMapper, question, AssessmentQuestionDO.class);

        AssessmentAnswer answer = AssessmentAnswer.reconstruct(100L, 100L, 100L, null, null, null, null, null);
        RepositoryTestObjects.insert(assessmentAnswerMapper, answer, AssessmentAnswerDO.class);

        FileVO workFileVO = createFileInMinio("work.zip", FileType.WORK);

        answer.setFileId(workFileVO.getId());
        RepositoryTestObjects.updateById(assessmentAnswerMapper, answer, AssessmentAnswerDO.class);

        mockMvc.perform(get("/api/v1/file/download/" + workFileVO.getId()))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", org.hamcrest.Matchers.notNullValue()));
    }

    @Test
    @DisplayName("下载作品文件 - 其他考生尝试下载应返回403")
    @WithSecurityPrincipal(userId = 101L, studentId = "2024001101", username = "其他考生", roleType = "CANDIDATE", direction = Direction.COMPUTER_VISION)
    void downloadWorkFile_asOtherCandidate_shouldReturn403() throws Exception {
        User submitter = User.reconstruct(
                102L,
                "2024001102",
                null,
                null,
                null,
                "提交者",
                null,
                null,
                null,
                null,
                Direction.COMPUTER_VISION,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null);
        RepositoryTestObjects.insert(userMapper, submitter, UserDO.class);

        AssessmentTime time = AssessmentTime
                .reconstruct(101L, Direction.COMPUTER_VISION, null, null, null, null, false, null, null, null);
        RepositoryTestObjects.insert(assessmentTimeMapper, time, AssessmentTimeDO.class);

        AssessmentQuestion question = AssessmentQuestion.reconstruct(101L, 101L, 1, null, null, null, null, null);
        RepositoryTestObjects.insert(assessmentQuestionMapper, question, AssessmentQuestionDO.class);

        AssessmentAnswer answer = AssessmentAnswer.reconstruct(101L, 102L, 101L, null, null, null, null, null);
        RepositoryTestObjects.insert(assessmentAnswerMapper, answer, AssessmentAnswerDO.class);

        FileVO workFileVO = createFileInMinio("work.zip", FileType.WORK);

        answer.setFileId(workFileVO.getId());
        RepositoryTestObjects.updateById(assessmentAnswerMapper, answer, AssessmentAnswerDO.class);

        mockMvc.perform(get("/api/v1/file/download/" + workFileVO.getId())).andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("下载作品文件 - MEMBER角色下载任意作品应返回200")
    @WithSecurityPrincipal(userId = 103L, studentId = "2024001103", username = "管理员", roleType = "MEMBER", direction = Direction.COMPUTER_VISION)
    void downloadWorkFile_asMember_shouldReturn200() throws Exception {
        User submitter = User.reconstruct(
                104L,
                "2024001104",
                null,
                null,
                null,
                "提交者",
                null,
                null,
                null,
                null,
                Direction.COMPUTER_VISION,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null);
        RepositoryTestObjects.insert(userMapper, submitter, UserDO.class);

        AssessmentTime time = AssessmentTime
                .reconstruct(102L, Direction.COMPUTER_VISION, null, null, null, null, false, null, null, null);
        RepositoryTestObjects.insert(assessmentTimeMapper, time, AssessmentTimeDO.class);

        AssessmentQuestion question = AssessmentQuestion.reconstruct(102L, 102L, 1, null, null, null, null, null);
        RepositoryTestObjects.insert(assessmentQuestionMapper, question, AssessmentQuestionDO.class);

        AssessmentAnswer answer = AssessmentAnswer.reconstruct(102L, 104L, 102L, null, null, null, null, null);
        RepositoryTestObjects.insert(assessmentAnswerMapper, answer, AssessmentAnswerDO.class);

        FileVO workFileVO = createFileInMinio("work.zip", FileType.WORK);

        answer.setFileId(workFileVO.getId());
        RepositoryTestObjects.updateById(assessmentAnswerMapper, answer, AssessmentAnswerDO.class);

        mockMvc.perform(get("/api/v1/file/download/" + workFileVO.getId()))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", org.hamcrest.Matchers.notNullValue()));
    }

    @Test
    @DisplayName("下载考题附件 - 方向匹配应返回200")
    @WithSecurityPrincipal(userId = 105L, studentId = "2024001105", username = "考生A", roleType = "CANDIDATE", direction = Direction.COMPUTER_VISION)
    void downloadAssessmentAttachment_asMatchingDirection_shouldReturn200() throws Exception {
        User user = User.reconstruct(
                105L,
                "2024001105",
                null,
                null,
                null,
                "考生A",
                null,
                null,
                null,
                null,
                Direction.COMPUTER_VISION,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null);
        RepositoryTestObjects.insert(userMapper, user, UserDO.class);

        AssessmentTime time = AssessmentTime
                .reconstruct(103L, Direction.COMPUTER_VISION, null, null, null, null, false, null, null, null);
        RepositoryTestObjects.insert(assessmentTimeMapper, time, AssessmentTimeDO.class);

        AssessmentQuestion question = AssessmentQuestion.reconstruct(103L, 103L, 1, null, null, null, null, null);
        RepositoryTestObjects.insert(assessmentQuestionMapper, question, AssessmentQuestionDO.class);

        FileVO attachmentFileVO = createFileInMinio("attachment.pdf", FileType.ASSESSMENT_ATTACHMENT);

        question.setAttachmentId(attachmentFileVO.getId());
        RepositoryTestObjects.updateById(assessmentQuestionMapper, question, AssessmentQuestionDO.class);

        mockMvc.perform(get("/api/v1/file/download/" + attachmentFileVO.getId()))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", org.hamcrest.Matchers.notNullValue()));
    }

    @Test
    @DisplayName("下载考题附件 - 方向不匹配应返回403")
    @WithSecurityPrincipal(userId = 106L, studentId = "2024001106", username = "考生B", roleType = "CANDIDATE", direction = Direction.EMBEDDED)
    void downloadAssessmentAttachment_asDifferentDirection_shouldReturn403() throws Exception {
        User user = User.reconstruct(
                106L,
                "2024001106",
                null,
                null,
                null,
                "考生B",
                null,
                null,
                null,
                null,
                Direction.EMBEDDED,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null);
        RepositoryTestObjects.insert(userMapper, user, UserDO.class);

        AssessmentTime time = AssessmentTime
                .reconstruct(104L, Direction.COMPUTER_VISION, null, null, null, null, false, null, null, null);
        RepositoryTestObjects.insert(assessmentTimeMapper, time, AssessmentTimeDO.class);

        AssessmentQuestion question = AssessmentQuestion.reconstruct(104L, 104L, 1, null, null, null, null, null);
        RepositoryTestObjects.insert(assessmentQuestionMapper, question, AssessmentQuestionDO.class);

        FileVO attachmentFileVO = createFileInMinio("attachment.pdf", FileType.ASSESSMENT_ATTACHMENT);

        question.setAttachmentId(attachmentFileVO.getId());
        RepositoryTestObjects.updateById(assessmentQuestionMapper, question, AssessmentQuestionDO.class);

        mockMvc.perform(get("/api/v1/file/download/" + attachmentFileVO.getId())).andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("下载头像文件 - 公开访问应返回200")
    void downloadAvatar_asPublic_shouldReturn200() throws Exception {
        FileVO avatarFileVO = createFileInMinio("avatar.png", FileType.AVATAR);

        mockMvc.perform(get("/api/v1/file/download/" + avatarFileVO.getId()))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", org.hamcrest.Matchers.notNullValue()));
    }

    @Test
    @DisplayName("下载普通图片文件 - 公开访问应返回200")
    void downloadNormalImg_asPublic_shouldReturn200() throws Exception {
        FileVO normalImgFileVO = createFileInMinio("image.jpg", FileType.NORMAL_IMG);

        mockMvc.perform(get("/api/v1/file/download/" + normalImgFileVO.getId()))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", org.hamcrest.Matchers.notNullValue()));
    }

    @Test
    @DisplayName("下载二维码文件 - 公开访问应返回200")
    void downloadQrcode_asPublic_shouldReturn200() throws Exception {
        FileVO qrcodeFileVO = createFileInMinio("qrcode.png", FileType.QRCODE);

        mockMvc.perform(get("/api/v1/file/download/" + qrcodeFileVO.getId()))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", org.hamcrest.Matchers.notNullValue()));
    }

    @Test
    @DisplayName("下载考题附件 - 用户无方向应返回403")
    @WithSecurityPrincipal(userId = 107L, studentId = "2024001107", username = "未报名考生", roleType = "CANDIDATE", noDirection = true)
    void downloadAssessmentAttachment_asNoDirection_shouldReturn403() throws Exception {
        User user = User.reconstruct(
                107L,
                "2024001107",
                null,
                null,
                null,
                "未报名考生",
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null);
        RepositoryTestObjects.insert(userMapper, user, UserDO.class);

        AssessmentTime time = AssessmentTime
                .reconstruct(105L, Direction.COMPUTER_VISION, null, null, null, null, false, null, null, null);
        RepositoryTestObjects.insert(assessmentTimeMapper, time, AssessmentTimeDO.class);

        AssessmentQuestion question = AssessmentQuestion.reconstruct(105L, 105L, 1, null, null, null, null, null);
        RepositoryTestObjects.insert(assessmentQuestionMapper, question, AssessmentQuestionDO.class);

        FileVO attachmentFileVO = createFileInMinio("attachment.pdf", FileType.ASSESSMENT_ATTACHMENT);

        question.setAttachmentId(attachmentFileVO.getId());
        RepositoryTestObjects.updateById(assessmentQuestionMapper, question, AssessmentQuestionDO.class);

        mockMvc.perform(get("/api/v1/file/download/" + attachmentFileVO.getId())).andExpect(status().isForbidden());
    }
}
