package com.bluenet.web.api.controller.v1.file;

import com.bluenet.web.BaseIntegrationTest;
import com.bluenet.web.domain.model.entity.AssessmentAnswer;
import com.bluenet.web.domain.model.entity.AssessmentQuestion;
import com.bluenet.web.domain.model.entity.AssessmentTime;
import com.bluenet.web.domain.model.entity.File;
import com.bluenet.web.domain.model.entity.User;
import com.bluenet.web.domain.model.enumerate.Direction;
import com.bluenet.web.domain.model.enumerate.FileType;
import com.bluenet.web.domain.model.vo.FileVO;
import com.bluenet.web.domain.repository.FileRepository;
import com.bluenet.web.infrastructure.repository.mapper.AssessmentAnswerMapper;
import com.bluenet.web.infrastructure.repository.mapper.AssessmentQuestionMapper;
import com.bluenet.web.infrastructure.repository.mapper.AssessmentTimeMapper;
import com.bluenet.web.infrastructure.repository.mapper.FileMapper;
import com.bluenet.web.infrastructure.repository.mapper.UserMapper;
import com.bluenet.web.infrastructure.security.WithUserVO;
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
        File file = File.builder().name(filename).type(fileType).url("test-url").build();
        ByteArrayInputStream inputStream = new ByteArrayInputStream("test content".getBytes(StandardCharsets.UTF_8));
        return fileRepository.saveFile(inputStream, file);
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
        @WithUserVO(userId = 2L, studentId = "2024001002", username = "李四", roleName = "CANDIDATE")
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
    @WithUserVO(userId = 100L, studentId = "2024001100", username = "提交者", roleName = "CANDIDATE", direction = Direction.COMPUTER_VISION)
    void downloadWorkFile_asSubmitter_shouldReturn200() throws Exception {
        User user = new User();
        user.setId(100L);
        user.setStudentId("2024001100");
        user.setUsername("提交者");
        user.setDirection(Direction.COMPUTER_VISION);
        userMapper.insert(user);

        AssessmentTime time = new AssessmentTime();
        time.setId(100L);
        time.setDirection(Direction.COMPUTER_VISION);
        assessmentTimeMapper.insert(time);

        AssessmentQuestion question = new AssessmentQuestion();
        question.setId(100L);
        question.setAssessmentTimeId(100L);
        question.setQuestionNo(1);
        assessmentQuestionMapper.insert(question);

        AssessmentAnswer answer = new AssessmentAnswer();
        answer.setId(100L);
        answer.setUserId(100L);
        answer.setQuestionId(100L);
        assessmentAnswerMapper.insert(answer);

        FileVO workFileVO = createFileInMinio("work.zip", FileType.WORK);

        answer.setFileId(workFileVO.getId());
        assessmentAnswerMapper.updateById(answer);

        mockMvc.perform(get("/api/v1/file/download/" + workFileVO.getId())).andExpect(status().isOk());
    }

    @Test
    @DisplayName("下载作品文件 - 其他考生尝试下载应返回403")
    @WithUserVO(userId = 101L, studentId = "2024001101", username = "其他考生", roleName = "CANDIDATE", direction = Direction.COMPUTER_VISION)
    void downloadWorkFile_asOtherCandidate_shouldReturn403() throws Exception {
        User submitter = new User();
        submitter.setId(102L);
        submitter.setStudentId("2024001102");
        submitter.setUsername("提交者");
        submitter.setDirection(Direction.COMPUTER_VISION);
        userMapper.insert(submitter);

        AssessmentTime time = new AssessmentTime();
        time.setId(101L);
        time.setDirection(Direction.COMPUTER_VISION);
        assessmentTimeMapper.insert(time);

        AssessmentQuestion question = new AssessmentQuestion();
        question.setId(101L);
        question.setAssessmentTimeId(101L);
        question.setQuestionNo(1);
        assessmentQuestionMapper.insert(question);

        AssessmentAnswer answer = new AssessmentAnswer();
        answer.setId(101L);
        answer.setUserId(102L);
        answer.setQuestionId(101L);
        assessmentAnswerMapper.insert(answer);

        FileVO workFileVO = createFileInMinio("work.zip", FileType.WORK);

        answer.setFileId(workFileVO.getId());
        assessmentAnswerMapper.updateById(answer);

        mockMvc.perform(get("/api/v1/file/download/" + workFileVO.getId())).andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("下载作品文件 - MEMBER角色下载任意作品应返回200")
    @WithUserVO(userId = 103L, studentId = "2024001103", username = "管理员", roleName = "MEMBER", direction = Direction.COMPUTER_VISION)
    void downloadWorkFile_asMember_shouldReturn200() throws Exception {
        User submitter = new User();
        submitter.setId(104L);
        submitter.setStudentId("2024001104");
        submitter.setUsername("提交者");
        submitter.setDirection(Direction.COMPUTER_VISION);
        userMapper.insert(submitter);

        AssessmentTime time = new AssessmentTime();
        time.setId(102L);
        time.setDirection(Direction.COMPUTER_VISION);
        assessmentTimeMapper.insert(time);

        AssessmentQuestion question = new AssessmentQuestion();
        question.setId(102L);
        question.setAssessmentTimeId(102L);
        question.setQuestionNo(1);
        assessmentQuestionMapper.insert(question);

        AssessmentAnswer answer = new AssessmentAnswer();
        answer.setId(102L);
        answer.setUserId(104L);
        answer.setQuestionId(102L);
        assessmentAnswerMapper.insert(answer);

        FileVO workFileVO = createFileInMinio("work.zip", FileType.WORK);

        answer.setFileId(workFileVO.getId());
        assessmentAnswerMapper.updateById(answer);

        mockMvc.perform(get("/api/v1/file/download/" + workFileVO.getId())).andExpect(status().isOk());
    }

    @Test
    @DisplayName("下载考题附件 - 方向匹配应返回200")
    @WithUserVO(userId = 105L, studentId = "2024001105", username = "考生A", roleName = "CANDIDATE", direction = Direction.COMPUTER_VISION)
    void downloadAssessmentAttachment_asMatchingDirection_shouldReturn200() throws Exception {
        User user = new User();
        user.setId(105L);
        user.setStudentId("2024001105");
        user.setUsername("考生A");
        user.setDirection(Direction.COMPUTER_VISION);
        userMapper.insert(user);

        AssessmentTime time = new AssessmentTime();
        time.setId(103L);
        time.setDirection(Direction.COMPUTER_VISION);
        assessmentTimeMapper.insert(time);

        AssessmentQuestion question = new AssessmentQuestion();
        question.setId(103L);
        question.setAssessmentTimeId(103L);
        question.setQuestionNo(1);
        assessmentQuestionMapper.insert(question);

        FileVO attachmentFileVO = createFileInMinio("attachment.pdf", FileType.ASSESSMENT_ATTACHMENT);

        question.setAttachmentId(attachmentFileVO.getId());
        assessmentQuestionMapper.updateById(question);

        mockMvc.perform(get("/api/v1/file/download/" + attachmentFileVO.getId())).andExpect(status().isOk());
    }

    @Test
    @DisplayName("下载考题附件 - 方向不匹配应返回403")
    @WithUserVO(userId = 106L, studentId = "2024001106", username = "考生B", roleName = "CANDIDATE", direction = Direction.EMBEDDED)
    void downloadAssessmentAttachment_asDifferentDirection_shouldReturn403() throws Exception {
        User user = new User();
        user.setId(106L);
        user.setStudentId("2024001106");
        user.setUsername("考生B");
        user.setDirection(Direction.EMBEDDED);
        userMapper.insert(user);

        AssessmentTime time = new AssessmentTime();
        time.setId(104L);
        time.setDirection(Direction.COMPUTER_VISION);
        assessmentTimeMapper.insert(time);

        AssessmentQuestion question = new AssessmentQuestion();
        question.setId(104L);
        question.setAssessmentTimeId(104L);
        question.setQuestionNo(1);
        assessmentQuestionMapper.insert(question);

        FileVO attachmentFileVO = createFileInMinio("attachment.pdf", FileType.ASSESSMENT_ATTACHMENT);

        question.setAttachmentId(attachmentFileVO.getId());
        assessmentQuestionMapper.updateById(question);

        mockMvc.perform(get("/api/v1/file/download/" + attachmentFileVO.getId())).andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("下载头像文件 - 公开访问应返回200")
    void downloadAvatar_asPublic_shouldReturn200() throws Exception {
        FileVO avatarFileVO = createFileInMinio("avatar.png", FileType.AVATAR);

        mockMvc.perform(get("/api/v1/file/download/" + avatarFileVO.getId())).andExpect(status().isOk());
    }

    @Test
    @DisplayName("下载普通图片文件 - 公开访问应返回200")
    void downloadNormalImg_asPublic_shouldReturn200() throws Exception {
        FileVO normalImgFileVO = createFileInMinio("image.jpg", FileType.NORMAL_IMG);

        mockMvc.perform(get("/api/v1/file/download/" + normalImgFileVO.getId())).andExpect(status().isOk());
    }

    @Test
    @DisplayName("下载二维码文件 - 公开访问应返回200")
    void downloadQrcode_asPublic_shouldReturn200() throws Exception {
        FileVO qrcodeFileVO = createFileInMinio("qrcode.png", FileType.QRCODE);

        mockMvc.perform(get("/api/v1/file/download/" + qrcodeFileVO.getId())).andExpect(status().isOk());
    }

    @Test
    @DisplayName("下载考题附件 - 用户无方向应返回403")
    @WithUserVO(userId = 107L, studentId = "2024001107", username = "未报名考生", roleName = "CANDIDATE", noDirection = true)
    void downloadAssessmentAttachment_asNoDirection_shouldReturn403() throws Exception {
        User user = new User();
        user.setId(107L);
        user.setStudentId("2024001107");
        user.setUsername("未报名考生");
        user.setDirection(null);
        userMapper.insert(user);

        AssessmentTime time = new AssessmentTime();
        time.setId(105L);
        time.setDirection(Direction.COMPUTER_VISION);
        assessmentTimeMapper.insert(time);

        AssessmentQuestion question = new AssessmentQuestion();
        question.setId(105L);
        question.setAssessmentTimeId(105L);
        question.setQuestionNo(1);
        assessmentQuestionMapper.insert(question);

        FileVO attachmentFileVO = createFileInMinio("attachment.pdf", FileType.ASSESSMENT_ATTACHMENT);

        question.setAttachmentId(attachmentFileVO.getId());
        assessmentQuestionMapper.updateById(question);

        mockMvc.perform(get("/api/v1/file/download/" + attachmentFileVO.getId())).andExpect(status().isForbidden());
    }
}
