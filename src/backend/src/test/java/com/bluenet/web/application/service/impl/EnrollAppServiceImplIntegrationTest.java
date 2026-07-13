package com.bluenet.web.application.service.impl;

import com.bluenet.web.BaseIntegrationTest;
import com.bluenet.web.application.command.enroll.EnrollCommands;
import com.bluenet.web.application.message.MessageDispatcher;
import com.bluenet.web.application.message.MessageRequest;
import com.bluenet.web.application.query.enroll.GetEnrollmentListQuery;
import com.bluenet.web.application.result.enroll.EnrollResult;
import com.bluenet.web.application.service.EnrollAppService;
import com.bluenet.web.domain.exception.DataConflict;
import com.bluenet.web.domain.exception.DataNotFound;
import com.bluenet.web.domain.exception.GlobalException;
import com.bluenet.web.domain.model.entity.College;
import com.bluenet.web.domain.model.entity.Enroll;
import com.bluenet.web.domain.model.entity.File;
import com.bluenet.web.domain.model.entity.User;
import com.bluenet.web.domain.model.enumerate.Direction;
import com.bluenet.web.domain.model.enumerate.EnrollStatus;
import com.bluenet.web.domain.model.enumerate.FileType;
import com.bluenet.web.domain.model.enumerate.Gender;
import com.bluenet.web.domain.repository.CollegeRepository;
import com.bluenet.web.domain.repository.EnrollRepository;
import com.bluenet.web.domain.repository.FileRepository;
import com.bluenet.web.domain.repository.UserRepository;
import com.bluenet.web.infrastructure.security.principal.WithSecurityPrincipal;
import com.bluenet.web.infrastructure.security.util.UserCTX;
import com.bluenet.web.testsupport.fixture.CollegeFixture;
import com.bluenet.web.testsupport.fixture.FileFixture;
import com.bluenet.web.testsupport.fixture.UserFixture;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;

/**
 * EnrollAppServiceImpl 集成测试。
 *
 * <p>
 * 验证报名应用服务的创建、更新、查询、审核、拒绝及统计逻辑。
 * </p>
 */
@DisplayName("EnrollAppServiceImpl 集成测试")
class EnrollAppServiceImplIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private EnrollAppService enrollAppService;

    @Autowired
    private EnrollRepository enrollRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CollegeRepository collegeRepository;

    @Autowired
    private FileRepository fileRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @MockitoBean
    private MessageDispatcher messageDispatcher;

    @AfterEach
    void cleanup() {
        UserCTX.clear();
        reset(messageDispatcher);
    }

    private College createCollege() {
        return CollegeFixture.saveCollege(collegeRepository, "计算机学院");
    }

    private EnrollCommands.CreateEnrollmentCommand createCommand(String studentId, Long collegeId,
            Direction direction) {
        return new EnrollCommands.CreateEnrollmentCommand(
                "报名" + studentId,
                studentId,
                studentId + "@example.com",
                collegeId,
                "计算机科学与技术",
                Gender.MALE,
                direction,
                null,
                "自我介绍",
                null,
                false);
    }

    private EnrollCommands.CreateEnrollmentCommand createCommand(String studentId, Long collegeId,
            Direction direction, Long avatarId) {
        return new EnrollCommands.CreateEnrollmentCommand(
                "报名" + studentId,
                studentId,
                studentId + "@example.com",
                collegeId,
                "计算机科学与技术",
                Gender.MALE,
                direction,
                avatarId,
                "自我介绍",
                null,
                false);
    }

    private EnrollCommands.UpdateEnrollmentCommand updateCommand(String studentId, Long collegeId,
            Direction direction) {
        return new EnrollCommands.UpdateEnrollmentCommand(
                studentId,
                "更新" + studentId,
                "updated-" + studentId + "@example.com",
                collegeId,
                "软件工程",
                Gender.FEMALE,
                direction,
                null,
                "更新后的自我介绍",
                null);
    }

    @Test
    @WithSecurityPrincipal(userId = 1L, roleType = "SUPER_ADMIN")
    @DisplayName("createEnrollment: 应创建报名并返回 PENDING 状态")
    void createEnrollment_shouldCreateAndReturnPending() {
        College college = createCollege();
        EnrollCommands.CreateEnrollmentCommand command = createCommand(
                "2024003001",
                college.getId(),
                Direction.COMPUTER_VISION);

        EnrollResult.Enrollment result = enrollAppService.createEnrollment(command);

        assertThat(result).isNotNull();
        assertThat(result.id()).isNotNull();
        assertThat(result.studentId()).isEqualTo("2024003001");
        assertThat(result.status()).isEqualTo(EnrollStatus.PENDING);
        assertThat(result.created()).isTrue();
        assertThat(enrollRepository.findById(result.id()))
                .isPresent()
                .hasValueSatisfying(enroll -> assertThat(enroll.getStatus()).isEqualTo(EnrollStatus.PENDING));
    }

    @Test
    @WithSecurityPrincipal(userId = 1L, roleType = "SUPER_ADMIN")
    @DisplayName("createEnrollment: 重复学号应抛 DataConflict 并提示学号已存在")
    void createEnrollment_duplicateStudentId_shouldThrowDataConflict() {
        College college = createCollege();
        EnrollCommands.CreateEnrollmentCommand command = createCommand(
                "2024003002",
                college.getId(),
                Direction.COMPUTER_VISION);
        enrollAppService.createEnrollment(command);

        assertThatThrownBy(() -> enrollAppService.createEnrollment(command))
                .isInstanceOf(DataConflict.class)
                .hasMessageContaining("学号已存在");
    }

    @Test
    @WithSecurityPrincipal(userId = 1L, roleType = "SUPER_ADMIN")
    @DisplayName("createEnrollment: forceUpdate=true 时应更新已存在的报名")
    void createEnrollment_forceUpdate_shouldUpdateExistingEnrollment() {
        College college = createCollege();
        EnrollCommands.CreateEnrollmentCommand first = createCommand(
                "2024003003",
                college.getId(),
                Direction.COMPUTER_VISION);
        EnrollResult.Enrollment created = enrollAppService.createEnrollment(first);

        EnrollCommands.CreateEnrollmentCommand second = new EnrollCommands.CreateEnrollmentCommand(
                "更新名",
                "2024003003",
                "2024003003@example.com",
                college.getId(),
                "软件工程",
                Gender.FEMALE,
                Direction.STRUCTURAL_DESIGN,
                null,
                "更新介绍",
                null,
                true);

        EnrollResult.Enrollment result = enrollAppService.createEnrollment(second);

        assertThat(result.id()).isEqualTo(created.id());
        assertThat(result.username()).isEqualTo("更新名");
        assertThat(result.direction()).isEqualTo(Direction.STRUCTURAL_DESIGN);
        assertThat(result.created()).isFalse();
        assertThat(enrollRepository.findById(result.id()))
                .isPresent()
                .hasValueSatisfying(enroll -> {
                    assertThat(enroll.getUsername()).isEqualTo("更新名");
                    assertThat(enroll.getDirection()).isEqualTo(Direction.STRUCTURAL_DESIGN);
                });
    }

    @Test
    @WithSecurityPrincipal(userId = 1L, roleType = "SUPER_ADMIN")
    @DisplayName("createEnrollment: 非头像文件应抛 GlobalException")
    void createEnrollment_invalidAvatarType_shouldThrowGlobalException() {
        College college = createCollege();
        File normalImg = FileFixture.save(fileRepository, "normal-img", FileType.NORMAL_IMG);
        EnrollCommands.CreateEnrollmentCommand command = createCommand(
                "2024003004",
                college.getId(),
                Direction.COMPUTER_VISION,
                normalImg.getId());

        assertThatThrownBy(() -> enrollAppService.createEnrollment(command))
                .isInstanceOf(GlobalException.class)
                .hasMessageContaining("文件类型不是头像");
    }

    @Test
    @WithSecurityPrincipal(userId = 1L, roleType = "SUPER_ADMIN")
    @DisplayName("createEnrollment: 不存在的头像文件应抛 GlobalException")
    void createEnrollment_nonExistentAvatar_shouldThrowGlobalException() {
        College college = createCollege();
        EnrollCommands.CreateEnrollmentCommand command = createCommand(
                "2024003005",
                college.getId(),
                Direction.COMPUTER_VISION,
                99999L);

        assertThatThrownBy(() -> enrollAppService.createEnrollment(command))
                .isInstanceOf(GlobalException.class)
                .hasMessageContaining("头像文件不存在");
    }

    @Test
    @WithSecurityPrincipal(userId = 1L, roleType = "SUPER_ADMIN")
    @DisplayName("updateEnrollment: PENDING 状态的报名应更新成功")
    void updateEnrollment_pendingStatus_shouldUpdateSuccessfully() {
        College college = createCollege();
        EnrollCommands.CreateEnrollmentCommand createCommand = createCommand(
                "2024003006",
                college.getId(),
                Direction.COMPUTER_VISION);
        EnrollResult.Enrollment created = enrollAppService.createEnrollment(createCommand);
        EnrollCommands.UpdateEnrollmentCommand updateCommand = updateCommand(
                "2024003006",
                college.getId(),
                Direction.EMBEDDED);

        EnrollResult.Enrollment result = enrollAppService.updateEnrollment(updateCommand);

        assertThat(result.id()).isEqualTo(created.id());
        assertThat(result.username()).isEqualTo("更新2024003006");
        assertThat(result.direction()).isEqualTo(Direction.EMBEDDED);
        assertThat(result.status()).isEqualTo(EnrollStatus.PENDING);
        assertThat(enrollRepository.findById(result.id()))
                .isPresent()
                .hasValueSatisfying(enroll -> assertThat(enroll.getDirection()).isEqualTo(Direction.EMBEDDED));
    }

    @Test
    @WithSecurityPrincipal(userId = 1L, roleType = "SUPER_ADMIN")
    @DisplayName("updateEnrollment: 不存在的学号应抛 DataNotFound")
    void updateEnrollment_notFound_shouldThrowDataNotFound() {
        College college = createCollege();
        EnrollCommands.UpdateEnrollmentCommand command = updateCommand(
                "2024999999",
                college.getId(),
                Direction.COMPUTER_VISION);

        assertThatThrownBy(() -> enrollAppService.updateEnrollment(command))
                .isInstanceOf(DataNotFound.class)
                .hasMessageContaining("报名记录不存在");
    }

    @Test
    @WithSecurityPrincipal(userId = 1L, roleType = "SUPER_ADMIN")
    @DisplayName("getEnrollmentList: 应支持分页及关键字、状态、方向过滤")
    void getEnrollmentList_shouldSupportPagingAndFilters() {
        College college = createCollege();
        EnrollResult.Enrollment enrollA = enrollAppService
                .createEnrollment(createCommand("2024003010", college.getId(), Direction.COMPUTER_VISION));
        EnrollResult.Enrollment enrollB = enrollAppService
                .createEnrollment(createCommand("2024003011", college.getId(), Direction.STRUCTURAL_DESIGN));
        EnrollResult.Enrollment enrollC = enrollAppService
                .createEnrollment(createCommand("2024003012", college.getId(), Direction.EMBEDDED));
        enrollAppService.approveEnrollment(enrollA.id());
        enrollAppService.rejectEnrollment(enrollC.id(), new EnrollCommands.RejectEnrollmentCommand("不适合"));

        Page<EnrollResult.Brief> allPage = enrollAppService
                .getEnrollmentList(new GetEnrollmentListQuery(0, 20, null, null, null));
        assertThat(allPage.getTotalElements()).isEqualTo(3);
        assertThat(allPage.getContent()).hasSize(3);

        Page<EnrollResult.Brief> approvedPage = enrollAppService
                .getEnrollmentList(new GetEnrollmentListQuery(0, 20, null, EnrollStatus.APPROVED, null));
        assertThat(approvedPage.getTotalElements()).isEqualTo(1);
        assertThat(approvedPage.getContent().get(0).id()).isEqualTo(enrollA.id());

        Page<EnrollResult.Brief> structPage = enrollAppService
                .getEnrollmentList(new GetEnrollmentListQuery(0, 20, null, null, Direction.STRUCTURAL_DESIGN));
        assertThat(structPage.getTotalElements()).isEqualTo(1);
        assertThat(structPage.getContent().get(0).id()).isEqualTo(enrollB.id());

        Page<EnrollResult.Brief> keywordPage = enrollAppService
                .getEnrollmentList(new GetEnrollmentListQuery(0, 20, "2024003012", null, null));
        assertThat(keywordPage.getTotalElements()).isEqualTo(1);
        assertThat(keywordPage.getContent().get(0).id()).isEqualTo(enrollC.id());
    }

    @Test
    @WithSecurityPrincipal(userId = 1L, roleType = "SUPER_ADMIN")
    @DisplayName("getEnrollmentDetail: 应返回报名详情")
    void getEnrollmentDetail_shouldReturnDetail() {
        College college = createCollege();
        EnrollResult.Enrollment enroll = enrollAppService
                .createEnrollment(createCommand("2024003013", college.getId(), Direction.COMPUTER_VISION));

        EnrollResult.Detail detail = enrollAppService.getEnrollmentDetail(enroll.id());

        assertThat(detail).isNotNull();
        assertThat(detail.id()).isEqualTo(enroll.id());
        assertThat(detail.studentId()).isEqualTo("2024003013");
        assertThat(detail.status()).isEqualTo(EnrollStatus.PENDING);
    }

    @Test
    @WithSecurityPrincipal(userId = 1L, roleType = "SUPER_ADMIN")
    @DisplayName("getEnrollmentDetail: 不存在的 id 应抛 DataNotFound")
    void getEnrollmentDetail_notFound_shouldThrowDataNotFound() {
        assertThatThrownBy(() -> enrollAppService.getEnrollmentDetail(99999L))
                .isInstanceOf(DataNotFound.class)
                .hasMessageContaining("报名记录不存在");
    }

    @Test
    @WithSecurityPrincipal(userId = 1L, roleType = "SUPER_ADMIN")
    @DisplayName("approveEnrollment: 学号不存在时应创建用户并发送凭据邮件")
    void approveEnrollment_newStudent_shouldCreateUserAndDispatchEmail() {
        College college = createCollege();
        EnrollResult.Enrollment enroll = enrollAppService
                .createEnrollment(createCommand("2024003020", college.getId(), Direction.COMPUTER_VISION));

        EnrollResult.Approval result = enrollAppService.approveEnrollment(enroll.id());

        assertThat(result.status()).isEqualTo(EnrollStatus.APPROVED);
        assertThat(result.createdUserId()).isNotNull();
        User createdUser = userRepository.findByStudentId("2024003020").orElseThrow();
        assertThat(createdUser.getId()).isEqualTo(result.createdUserId());
        verify(messageDispatcher).dispatchAsync(any(MessageRequest.class));
    }

    @Test
    @WithSecurityPrincipal(userId = 1L, roleType = "SUPER_ADMIN")
    @DisplayName("approveEnrollment: 学号已存在时应跳过用户创建")
    void approveEnrollment_existingStudent_shouldSkipUserCreation() {
        College college = createCollege();
        User existingUser = UserFixture.candidate("2024003021")
                .withCollegeId(college.getId())
                .save(userRepository, passwordEncoder);
        EnrollResult.Enrollment enroll = enrollAppService
                .createEnrollment(createCommand("2024003021", college.getId(), Direction.COMPUTER_VISION));

        EnrollResult.Approval result = enrollAppService.approveEnrollment(enroll.id());

        assertThat(result.status()).isEqualTo(EnrollStatus.APPROVED);
        assertThat(result.createdUserId()).isEqualTo(existingUser.getId());
        verify(messageDispatcher, never()).dispatchAsync(any(MessageRequest.class));
    }

    @Test
    @WithSecurityPrincipal(userId = 1L, roleType = "SUPER_ADMIN")
    @DisplayName("approveEnrollment: 非 PENDING 状态应抛 DataConflict")
    void approveEnrollment_nonPending_shouldThrowDataConflict() {
        College college = createCollege();
        EnrollResult.Enrollment enroll = enrollAppService
                .createEnrollment(createCommand("2024003022", college.getId(), Direction.COMPUTER_VISION));
        enrollAppService.approveEnrollment(enroll.id());

        assertThatThrownBy(() -> enrollAppService.approveEnrollment(enroll.id()))
                .isInstanceOf(DataConflict.class)
                .hasMessageContaining("只能审核待审核状态的报名");
    }

    @Test
    @WithSecurityPrincipal(userId = 1L, roleType = "SUPER_ADMIN")
    @DisplayName("rejectEnrollment: 应更新状态为 REJECTED 并发送拒绝邮件")
    void rejectEnrollment_shouldRejectAndDispatchEmail() {
        College college = createCollege();
        EnrollResult.Enrollment enroll = enrollAppService
                .createEnrollment(createCommand("2024003023", college.getId(), Direction.COMPUTER_VISION));

        EnrollResult.Approval result = enrollAppService.rejectEnrollment(
                enroll.id(),
                new EnrollCommands.RejectEnrollmentCommand("不符合要求"));

        assertThat(result.status()).isEqualTo(EnrollStatus.REJECTED);
        assertThat(result.createdUserId()).isNull();
        Enroll updated = enrollRepository.findById(enroll.id()).orElseThrow();
        assertThat(updated.getStatus()).isEqualTo(EnrollStatus.REJECTED);
        verify(messageDispatcher).dispatchAsync(any(MessageRequest.class));
    }

    @Test
    @WithSecurityPrincipal(userId = 1L, roleType = "SUPER_ADMIN")
    @DisplayName("getStatistics: 应返回总数、按状态统计及按方向统计")
    void getStatistics_shouldReturnTotalStatusAndDirectionCounts() {
        College college = createCollege();
        EnrollResult.Enrollment pending = enrollAppService
                .createEnrollment(createCommand("2024003030", college.getId(), Direction.COMPUTER_VISION));
        EnrollResult.Enrollment approved = enrollAppService
                .createEnrollment(createCommand("2024003031", college.getId(), Direction.STRUCTURAL_DESIGN));
        EnrollResult.Enrollment rejected = enrollAppService
                .createEnrollment(createCommand("2024003032", college.getId(), Direction.EMBEDDED));
        enrollAppService.approveEnrollment(approved.id());
        enrollAppService.rejectEnrollment(rejected.id(), new EnrollCommands.RejectEnrollmentCommand("不适合"));

        EnrollResult.Statistics statistics = enrollAppService.getStatistics();

        assertThat(statistics.total()).isEqualTo(3L);
        assertThat(statistics.byStatus())
                .containsEntry(EnrollStatus.PENDING.getValue(), 1L)
                .containsEntry(EnrollStatus.APPROVED.getValue(), 1L)
                .containsEntry(EnrollStatus.REJECTED.getValue(), 1L);
        assertThat(statistics.byDirection())
                .containsEntry(Direction.COMPUTER_VISION, 1L)
                .containsEntry(Direction.STRUCTURAL_DESIGN, 1L)
                .containsEntry(Direction.EMBEDDED, 1L);
    }
}
