package com.bluenet.web.application.service.impl;

import com.bluenet.web.BaseIntegrationTest;
import com.bluenet.web.application.command.assessment_question.AssessmentQuestionCommands;
import com.bluenet.web.application.result.assessment.AssessmentQuestionResult;
import com.bluenet.web.application.result.user.UserQuestionListResult;
import com.bluenet.web.application.service.AssessmentQuestionAppService;
import com.bluenet.web.domain.exception.BadRequest;
import com.bluenet.web.domain.exception.DataConflict;
import com.bluenet.web.domain.exception.DataNotFound;
import com.bluenet.web.domain.model.entity.AssessmentQuestion;
import com.bluenet.web.domain.model.entity.AssessmentTime;
import com.bluenet.web.domain.model.entity.File;
import com.bluenet.web.domain.model.entity.User;
import com.bluenet.web.domain.model.enumerate.Direction;
import com.bluenet.web.domain.model.enumerate.FileType;
import com.bluenet.web.domain.model.enumerate.QuestionType;
import com.bluenet.web.domain.repository.AssessmentAnswerRepository;
import com.bluenet.web.domain.repository.AssessmentQuestionRepository;
import com.bluenet.web.domain.repository.AssessmentTimeRepository;
import com.bluenet.web.domain.repository.FileRepository;
import com.bluenet.web.domain.repository.UserRepository;
import com.bluenet.web.domain.service.AssessmentDecisionDomainService;
import com.bluenet.web.infrastructure.security.principal.RoleTypeResolver;
import com.bluenet.web.infrastructure.security.principal.SecurityPrincipal;
import com.bluenet.web.infrastructure.security.util.UserCTX;
import com.bluenet.web.testsupport.fixture.AssessmentFixture;
import com.bluenet.web.testsupport.fixture.FileFixture;
import com.bluenet.web.testsupport.fixture.TimeFixture;
import com.bluenet.web.testsupport.fixture.UserFixture;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

/**
 * AssessmentQuestionAppServiceImpl 集成测试。
 *
 * <p>
 * 验证考核题目应用服务的创建、更新、删除、列表、用户查询及附件更新。
 * </p>
 */
@DisplayName("AssessmentQuestionAppServiceImpl 集成测试")
class AssessmentQuestionAppServiceImplIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private AssessmentQuestionAppService assessmentQuestionAppService;

    @Autowired
    private AssessmentQuestionRepository assessmentQuestionRepository;

    @Autowired
    private AssessmentTimeRepository assessmentTimeRepository;

    @Autowired
    private AssessmentAnswerRepository assessmentAnswerRepository;

    @Autowired
    private FileRepository fileRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private RoleTypeResolver roleTypeResolver;

    @MockitoBean
    private AssessmentDecisionDomainService assessmentDecisionDomainService;

    private long sequence = 1000;

    private String nextStudentId(String prefix) {
        return prefix + (++sequence);
    }

    private User createCandidate(Direction direction, Integer gradeYear) {
        return UserFixture.candidate(nextStudentId("SC"))
                .withDirection(direction)
                .withAssessmentGradeYear(gradeYear)
                .save(userRepository, passwordEncoder);
    }

    private AssessmentTime createTime(Direction direction, Integer grade) {
        return AssessmentFixture.timeBuilder()
                .direction(direction)
                .grade(grade)
                .withinNow()
                .save(assessmentTimeRepository);
    }

    private void loginAs(User user) {
        SecurityPrincipal principal = new SecurityPrincipal(
                user,
                roleTypeResolver.resolve(user.getRoleId()),
                Collections.emptySet());
        UserCTX.setPrincipal(principal);
    }

    @AfterEach
    void cleanup() {
        UserCTX.clear();
    }

    @Test
    @DisplayName("createQuestion: 应创建文件上传题")
    void createQuestion_shouldCreateFileUploadQuestion() {
        AssessmentTime time = createTime(Direction.COMPUTER_VISION, 2024);
        AssessmentQuestionCommands.CreateAssessmentQuestionCommand command = new AssessmentQuestionCommands.CreateAssessmentQuestionCommand(
                time.getId(),
                1,
                QuestionType.FILE_UPLOAD,
                "题目一",
                AssessmentFixture.questionBuilder().build().getContent(),
                null,
                new BigDecimal("100"));

        AssessmentQuestionResult result = assessmentQuestionAppService.createQuestion(command);

        assertNotNull(result);
        assertEquals(time.getId(), result.assessmentTimeId());
        assertEquals(1, result.questionNo());
        assertEquals(QuestionType.FILE_UPLOAD, result.questionType());
    }

    @Test
    @DisplayName("createQuestion: 考核时间不存在应抛异常")
    void createQuestion_timeNotFound_shouldThrow() {
        AssessmentQuestionCommands.CreateAssessmentQuestionCommand command = new AssessmentQuestionCommands.CreateAssessmentQuestionCommand(
                -1L,
                1,
                QuestionType.FILE_UPLOAD,
                "题目一",
                AssessmentFixture.questionBuilder().build().getContent(),
                null,
                new BigDecimal("100"));

        assertThrows(IllegalArgumentException.class, () -> assessmentQuestionAppService.createQuestion(command));
    }

    @Test
    @DisplayName("createQuestion: 同考核同题号重复应抛 DataConflict")
    void createQuestion_duplicateQuestionNo_shouldThrowDataConflict() {
        AssessmentTime time = createTime(Direction.COMPUTER_VISION, 2024);
        AssessmentQuestionCommands.CreateAssessmentQuestionCommand command = new AssessmentQuestionCommands.CreateAssessmentQuestionCommand(
                time.getId(),
                1,
                QuestionType.FILE_UPLOAD,
                "题目一",
                AssessmentFixture.questionBuilder().build().getContent(),
                null,
                new BigDecimal("100"));
        assessmentQuestionAppService.createQuestion(command);

        assertThrows(DataConflict.class, () -> assessmentQuestionAppService.createQuestion(command));
    }

    @Test
    @DisplayName("createQuestion: 算法题内容不合法应抛异常")
    void createQuestion_invalidAlgorithmContent_shouldThrow() {
        AssessmentTime time = createTime(Direction.COMPUTER_VISION, 2024);
        AssessmentQuestionCommands.CreateAssessmentQuestionCommand command = new AssessmentQuestionCommands.CreateAssessmentQuestionCommand(
                time.getId(),
                1,
                QuestionType.ALGORITHM,
                "算法题",
                AssessmentFixture.questionBuilder().fileUpload().build().getContent(),
                null,
                new BigDecimal("100"));

        assertThrows(IllegalArgumentException.class, () -> assessmentQuestionAppService.createQuestion(command));
    }

    @Test
    @DisplayName("updateQuestion: 应更新题目标题和分数")
    void updateQuestion_shouldUpdateTitleAndScore() {
        AssessmentTime time = createTime(Direction.COMPUTER_VISION, 2024);
        AssessmentQuestion question = AssessmentFixture.questionBuilder()
                .assessmentTime(time)
                .title("旧标题")
                .score(new BigDecimal("50"))
                .save(assessmentQuestionRepository);

        AssessmentQuestionCommands.UpdateAssessmentQuestionCommand command = new AssessmentQuestionCommands.UpdateAssessmentQuestionCommand(
                question.getId(),
                null,
                null,
                "新标题",
                null,
                null,
                new BigDecimal("80"));

        AssessmentQuestionResult result = assessmentQuestionAppService.updateQuestion(command);

        assertEquals("新标题", result.title());
        assertEquals(new BigDecimal("80"), result.score());
    }

    @Test
    @DisplayName("updateQuestion: 修改为题号冲突应抛 DataConflict")
    void updateQuestion_duplicateQuestionNo_shouldThrowDataConflict() {
        AssessmentTime time = createTime(Direction.COMPUTER_VISION, 2024);
        AssessmentFixture.questionBuilder().assessmentTime(time).questionNo(1).save(assessmentQuestionRepository);
        AssessmentQuestion question2 = AssessmentFixture.questionBuilder()
                .assessmentTime(time)
                .questionNo(2)
                .save(assessmentQuestionRepository);

        AssessmentQuestionCommands.UpdateAssessmentQuestionCommand command = new AssessmentQuestionCommands.UpdateAssessmentQuestionCommand(
                question2.getId(),
                1,
                null,
                null,
                null,
                null,
                null);

        assertThrows(DataConflict.class, () -> assessmentQuestionAppService.updateQuestion(command));
    }

    @Test
    @DisplayName("updateQuestion: 题目不存在应抛 DataNotFound")
    void updateQuestion_questionNotFound_shouldThrow() {
        AssessmentQuestionCommands.UpdateAssessmentQuestionCommand command = new AssessmentQuestionCommands.UpdateAssessmentQuestionCommand(
                -1L,
                null,
                null,
                "新标题",
                null,
                null,
                null);

        assertThrows(DataNotFound.class, () -> assessmentQuestionAppService.updateQuestion(command));
    }

    @Test
    @DisplayName("deleteQuestion: 应删除非算法题")
    void deleteQuestion_shouldDelete() {
        AssessmentTime time = createTime(Direction.COMPUTER_VISION, 2024);
        AssessmentQuestion question = AssessmentFixture.questionBuilder()
                .assessmentTime(time)
                .save(assessmentQuestionRepository);

        assessmentQuestionAppService.deleteQuestion(question.getId());

        assertFalse(assessmentQuestionRepository.existsById(question.getId()));
    }

    @Test
    @DisplayName("deleteQuestion: 题目不存在应抛 DataNotFound")
    void deleteQuestion_notFound_shouldThrow() {
        assertThrows(DataNotFound.class, () -> assessmentQuestionAppService.deleteQuestion(-1L));
    }

    @Test
    @DisplayName("listQuestionsForAdmin: 应返回指定考核时间下的题目")
    void listQuestionsForAdmin_shouldReturnQuestions() {
        AssessmentTime time = createTime(Direction.COMPUTER_VISION, 2024);
        AssessmentFixture.questionBuilder().assessmentTime(time).questionNo(1).save(assessmentQuestionRepository);
        AssessmentFixture.questionBuilder().assessmentTime(time).questionNo(2).save(assessmentQuestionRepository);

        Page<AssessmentQuestionResult> result = assessmentQuestionAppService.listQuestionsForAdmin(time.getId(), 0, 10);

        assertEquals(2, result.getTotalElements());
    }

    @Test
    @DisplayName("listQuestionsForUser: 考生应能看到本方向本年级题目及作答状态")
    void listQuestionsForUser_candidate_shouldReturnWithAnswered() {
        AssessmentTime time = createTime(Direction.COMPUTER_VISION, 2024);
        User candidate = createCandidate(Direction.COMPUTER_VISION, 2024);
        AssessmentQuestion question = AssessmentFixture.questionBuilder()
                .assessmentTime(time)
                .questionNo(1)
                .save(assessmentQuestionRepository);
        AssessmentFixture.answerBuilder().user(candidate).question(question).save(assessmentAnswerRepository);

        loginAs(candidate);

        UserQuestionListResult result = assessmentQuestionAppService.listQuestionsForUser(time.getId(), 0, 10);

        assertEquals(1, result.questions().getTotalElements());
        assertTrue(result.questions().getContent().get(0).answered());
        assertFalse(result.ended());
    }

    @Test
    @DisplayName("listQuestionsForUser: 考生方向不匹配应抛安全异常")
    void listQuestionsForUser_candidateWrongDirection_shouldThrowSecurityException() {
        AssessmentTime time = createTime(Direction.COMPUTER_VISION, 2024);
        User candidate = createCandidate(Direction.STRUCTURAL_DESIGN, 2024);
        AssessmentFixture.questionBuilder().assessmentTime(time).save(assessmentQuestionRepository);

        loginAs(candidate);

        assertThrows(
                SecurityException.class,
                () -> assessmentQuestionAppService.listQuestionsForUser(time.getId(), 0, 10));
    }

    @Test
    @DisplayName("listQuestionsForUser: 考核未开始应抛安全异常")
    void listQuestionsForUser_beforeStart_shouldThrowSecurityException() {
        AssessmentTime time = AssessmentFixture.timeBuilder()
                .direction(Direction.COMPUTER_VISION)
                .grade(2024)
                .startTime(TimeFixture.plusMinutes(10))
                .endTime(TimeFixture.plusMinutes(70))
                .save(assessmentTimeRepository);
        User candidate = createCandidate(Direction.COMPUTER_VISION, 2024);
        AssessmentFixture.questionBuilder().assessmentTime(time).save(assessmentQuestionRepository);

        loginAs(candidate);

        assertThrows(
                SecurityException.class,
                () -> assessmentQuestionAppService.listQuestionsForUser(time.getId(), 0, 10));
    }

    @Test
    @DisplayName("getQuestionDetailForUser: 考生应能查看题目详情")
    void getQuestionDetailForUser_candidate_shouldReturnDetail() {
        AssessmentTime time = createTime(Direction.COMPUTER_VISION, 2024);
        User candidate = createCandidate(Direction.COMPUTER_VISION, 2024);
        AssessmentQuestion question = AssessmentFixture.questionBuilder()
                .assessmentTime(time)
                .title("详情题")
                .save(assessmentQuestionRepository);

        loginAs(candidate);

        AssessmentQuestionResult result = assessmentQuestionAppService.getQuestionDetailForUser(question.getId());

        assertEquals(question.getId(), result.id());
        assertEquals("详情题", result.title());
    }

    @Test
    @DisplayName("updateAttachment: 应更新题目附件")
    void updateAttachment_shouldUpdateAttachmentId() {
        AssessmentTime time = createTime(Direction.COMPUTER_VISION, 2024);
        AssessmentQuestion question = AssessmentFixture.questionBuilder()
                .assessmentTime(time)
                .save(assessmentQuestionRepository);
        File attachment = FileFixture.save(fileRepository, "attachment.pdf", FileType.ASSESSMENT_ATTACHMENT);

        assessmentQuestionAppService.updateAttachment(question.getId(), attachment.getId());

        AssessmentQuestion updated = assessmentQuestionRepository.findById(question.getId()).orElseThrow();
        assertEquals(attachment.getId(), updated.getAttachmentId());
    }

    @Test
    @DisplayName("updateAttachment: 文件类型不匹配应抛 BadRequest")
    void updateAttachment_wrongFileType_shouldThrow() {
        AssessmentTime time = createTime(Direction.COMPUTER_VISION, 2024);
        AssessmentQuestion question = AssessmentFixture.questionBuilder()
                .assessmentTime(time)
                .save(assessmentQuestionRepository);
        File avatar = FileFixture.save(fileRepository, "avatar.png", FileType.AVATAR);

        assertThrows(
                BadRequest.class,
                () -> assessmentQuestionAppService.updateAttachment(question.getId(), avatar.getId()));
    }
}
