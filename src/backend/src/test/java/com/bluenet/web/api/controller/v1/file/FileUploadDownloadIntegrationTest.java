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
import com.bluenet.web.infrastructure.repository.mapper.QrcodeMapper;
import com.bluenet.web.infrastructure.repository.mapper.UserMapper;
import com.bluenet.web.infrastructure.security.WithUserVO;
import com.bluenet.web.infrastructure.security.util.UserCTX;
import com.bluenet.web.testcontainers.TestcontainersConfiguration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
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
    private QrcodeMapper qrcodeMapper;

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
        qrcodeMapper.delete(null);
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

    @Test
    @DisplayName("上传头像 - 应更新用户头像ID")
    @WithUserVO(userId = 1L, studentId = "2024001001", username = "张三", roleName = "CANDIDATE")
    void uploadAvatar_shouldUpdateUserAvatarId() throws Exception {
        User u = new User();
        u.setId(1L);
        u.setStudentId("2024001001");
        u.setUsername("张三");
        u.setDirection(Direction.COMPUTER_VISION);
        userMapper.insert(u);

        MockMultipartFile file = new MockMultipartFile("file", "a.png", "image/png",
                "png".getBytes(StandardCharsets.UTF_8));

        mockMvc.perform(multipart("/api/v1/file/upload/avatar").file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").isNumber());

        User after = userMapper.selectById(1L);
        assertNotNull(after.getAvatarId());
    }

    @Test
    @DisplayName("上传考题作品 - 应更新答题的文件ID和提交时间")
    @WithUserVO(userId = 2L, studentId = "2024001002", username = "李四", roleName = "CANDIDATE")
    void uploadAssessmentWork_shouldUpdateAnswerFileIdAndSubmitTime() throws Exception {
        User u = new User();
        u.setId(2L);
        u.setStudentId("2024001002");
        u.setUsername("李四");
        u.setDirection(Direction.COMPUTER_VISION);
        userMapper.insert(u);

        AssessmentTime time = new AssessmentTime();
        time.setId(10L);
        time.setDirection(Direction.COMPUTER_VISION);
        assessmentTimeMapper.insert(time);

        AssessmentQuestion q = new AssessmentQuestion();
        q.setId(20L);
        q.setAssessmentTimeId(10L);
        q.setQuestionNo(1);
        assessmentQuestionMapper.insert(q);

        AssessmentAnswer a = new AssessmentAnswer();
        a.setId(30L);
        a.setUserId(2L);
        a.setQuestionId(20L);
        assessmentAnswerMapper.insert(a);

        MockMultipartFile file = new MockMultipartFile("file", "work.zip", "application/zip",
                "zip".getBytes(StandardCharsets.UTF_8));

        mockMvc.perform(multipart("/api/v1/file/upload/assessment/work").file(file).param("answerId", "30"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        AssessmentAnswer after = assessmentAnswerMapper.selectById(30L);
        assertNotNull(after.getFileId());
        assertNotNull(after.getSubmitTime());
    }

    @Test
    @DisplayName("上传考题附件 - 应更新题目的附件ID")
    @WithUserVO(userId = 3L, studentId = "2024001003", username = "王五", roleName = "MEMBER")
    void uploadAssessmentAttachment_shouldUpdateQuestionAttachmentId() throws Exception {
        User u = new User();
        u.setId(3L);
        u.setStudentId("2024001003");
        u.setUsername("王五");
        u.setDirection(Direction.EMBEDDED);
        userMapper.insert(u);

        AssessmentTime time = new AssessmentTime();
        time.setId(11L);
        time.setDirection(Direction.EMBEDDED);
        assessmentTimeMapper.insert(time);

        AssessmentQuestion q = new AssessmentQuestion();
        q.setId(21L);
        q.setAssessmentTimeId(11L);
        q.setQuestionNo(1);
        assessmentQuestionMapper.insert(q);

        MockMultipartFile file = new MockMultipartFile("file", "a.pdf", "application/pdf",
                "%PDF".getBytes(StandardCharsets.UTF_8));

        mockMvc.perform(multipart("/api/v1/file/upload/assessment/attachment").file(file).param("questionId", "21"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        AssessmentQuestion after = assessmentQuestionMapper.selectById(21L);
        assertNotNull(after.getAttachmentId());
    }

    @Test
    @DisplayName("上传个人二维码 - 应创建二维码记录")
    @WithUserVO(userId = 4L, studentId = "2024001004", username = "赵六", roleName = "CANDIDATE")
    void uploadSelfQrcode_shouldCreateQrcodeRecord() throws Exception {
        User u = new User();
        u.setId(4L);
        u.setStudentId("2024001004");
        u.setUsername("赵六");
        u.setDirection(Direction.STRUCTURAL_DESIGN);
        userMapper.insert(u);

        MockMultipartFile file = new MockMultipartFile("file", "q.png", "image/png",
                "png".getBytes(StandardCharsets.UTF_8));

        mockMvc.perform(multipart("/api/v1/file/upload/qrcode/self").file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("上传群组二维码 - 应创建二维码记录")
    @WithUserVO(userId = 5L, studentId = "2024001005", username = "钱七", roleName = "MEMBER")
    void uploadGroupQrcode_shouldCreateQrcodeRecord() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "q.png", "image/png",
                "png".getBytes(StandardCharsets.UTF_8));

        mockMvc.perform(multipart("/api/v1/file/upload/qrcode/group").file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

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
