package com.bluenet.web.application.service.impl;

import com.bluenet.web.BaseIntegrationTest;
import com.bluenet.web.application.result.team.TeamPreviewResult;
import com.bluenet.web.application.result.team.TeamResult;
import com.bluenet.web.application.service.AssessmentTeamAppService;
import com.bluenet.web.domain.exception.BadRequest;
import com.bluenet.web.domain.exception.Forbidden;
import com.bluenet.web.domain.model.entity.AssessmentQuestion;
import com.bluenet.web.domain.model.entity.AssessmentTeam;
import com.bluenet.web.domain.model.entity.AssessmentTime;
import com.bluenet.web.domain.model.entity.User;
import com.bluenet.web.domain.model.enumerate.Direction;
import com.bluenet.web.domain.repository.AssessmentAnswerRepository;
import com.bluenet.web.domain.repository.AssessmentQuestionRepository;
import com.bluenet.web.domain.repository.AssessmentTeamRepository;
import com.bluenet.web.domain.repository.AssessmentTimeRepository;
import com.bluenet.web.domain.repository.UserRepository;
import com.bluenet.web.domain.service.AssessmentTeamDomainService;
import com.bluenet.web.testsupport.fixture.AssessmentFixture;
import com.bluenet.web.testsupport.fixture.TimeFixture;
import com.bluenet.web.testsupport.fixture.UserFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

/**
 * AssessmentTeamAppServiceImpl 集成测试。
 *
 * <p>
 * 验证考核队伍应用服务的创建、加入、预览、离开、转让队长与解散逻辑。
 * </p>
 */
@DisplayName("AssessmentTeamAppServiceImpl 集成测试")
class AssessmentTeamAppServiceImplIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private AssessmentTeamAppService assessmentTeamAppService;

    @Autowired
    private AssessmentTeamRepository assessmentTeamRepository;

    @Autowired
    private AssessmentTimeRepository assessmentTimeRepository;

    @Autowired
    private AssessmentQuestionRepository assessmentQuestionRepository;

    @Autowired
    private AssessmentAnswerRepository assessmentAnswerRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @MockitoBean
    private AssessmentTeamDomainService assessmentTeamDomainService;

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

    private AssessmentTime createTeamAllowedTime(Direction direction, Integer grade) {
        return AssessmentFixture.timeBuilder()
                .direction(direction)
                .grade(grade)
                .withinNow()
                .allowTeam()
                .save(assessmentTimeRepository);
    }

    private void stubPrepareNewTeam(Long userId, AssessmentTime time, String name, String inviteCode) {
        AssessmentTeam team = AssessmentTeam.create(time.getId(), userId, name, inviteCode);
        when(assessmentTeamDomainService.prepareNewTeam(eq(userId), any(AssessmentTime.class), eq(name)))
                .thenReturn(team);
    }

    private void stubPrepareNewTeamThrows(Long userId, AssessmentTime time, String name, RuntimeException exception) {
        when(assessmentTeamDomainService.prepareNewTeam(eq(userId), any(AssessmentTime.class), eq(name)))
                .thenThrow(exception);
    }

    private void stubPrepareLeaderTransfer(Long userId, Long newLeaderId) {
        when(assessmentTeamDomainService.prepareLeaderTransfer(eq(userId), any(AssessmentTeam.class), eq(newLeaderId)))
                .thenAnswer(invocation -> {
                    AssessmentTeam team = invocation.getArgument(1);
                    team.updateLeader(newLeaderId);
                    return team;
                });
    }

    private void stubPrepareLeaderTransferThrows(Long userId, Long newLeaderId, RuntimeException exception) {
        doThrow(exception).when(assessmentTeamDomainService)
                .prepareLeaderTransfer(eq(userId), any(AssessmentTeam.class), eq(newLeaderId));
    }

    private void stubPrepareDisband(Long userId) {
        when(assessmentTeamDomainService.prepareDisband(eq(userId), any(AssessmentTeam.class)))
                .thenAnswer(invocation -> {
                    AssessmentTeam team = invocation.getArgument(1);
                    team.disband();
                    return team;
                });
    }

    private void stubPrepareDisbandThrows(Long userId, RuntimeException exception) {
        doThrow(exception).when(assessmentTeamDomainService)
                .prepareDisband(eq(userId), any(AssessmentTeam.class));
    }

    private void stubValidateCanJoinTeamThrows(Long userId, RuntimeException exception) {
        doThrow(exception).when(assessmentTeamDomainService)
                .validateCanJoinTeam(eq(userId), any(AssessmentTeam.class), any(AssessmentTime.class));
    }

    private void stubValidateCanLeaveTeamThrows(Long userId, RuntimeException exception) {
        doThrow(exception).when(assessmentTeamDomainService)
                .validateCanLeaveTeam(eq(userId), any(AssessmentTeam.class));
    }

    private void stubValidateTeamPreviewableThrows(RuntimeException exception) {
        doThrow(exception).when(assessmentTeamDomainService)
                .validateTeamPreviewable(any(AssessmentTeam.class), any(AssessmentTime.class));
    }

    @Test
    @DisplayName("createTeam: 允许组队的考核应成功创建队伍")
    void createTeam_shouldCreate() {
        User leader = createCandidate(Direction.COMPUTER_VISION, 2024);
        AssessmentTime time = createTeamAllowedTime(Direction.COMPUTER_VISION, 2024);
        stubPrepareNewTeam(leader.getId(), time, "无敌队", "CODE01");

        TeamResult result = assessmentTeamAppService.createTeam(leader.getId(), time.getId(), "无敌队");

        assertNotNull(result);
        assertEquals("无敌队", result.name());
        assertEquals(leader.getId(), result.leaderId());
        assertEquals(1, result.members().size());
        assertTrue(result.members().get(0).isLeader());
    }

    @Test
    @DisplayName("createTeam: 不允许组队的考核应抛 BadRequest")
    void createTeam_notAllowTeam_shouldThrowBadRequest() {
        User leader = createCandidate(Direction.COMPUTER_VISION, 2024);
        AssessmentTime time = AssessmentFixture.timeBuilder()
                .direction(Direction.COMPUTER_VISION)
                .grade(2024)
                .withinNow()
                .save(assessmentTimeRepository);
        stubPrepareNewTeamThrows(leader.getId(), time, "队", new BadRequest("该考核不允许组队"));

        assertThrows(
                BadRequest.class,
                () -> assessmentTeamAppService.createTeam(leader.getId(), time.getId(), "队"));
    }

    @Test
    @DisplayName("createTeam: 已加入队伍的用户不能再创建队伍")
    void createTeam_alreadyInTeam_shouldThrowBadRequest() {
        User leader = createCandidate(Direction.COMPUTER_VISION, 2024);
        AssessmentTime time = createTeamAllowedTime(Direction.COMPUTER_VISION, 2024);
        stubPrepareNewTeam(leader.getId(), time, "队一", "CODE01");
        stubPrepareNewTeamThrows(leader.getId(), time, "队二", new BadRequest("您已加入该考核的队伍"));

        assessmentTeamAppService.createTeam(leader.getId(), time.getId(), "队一");

        assertThrows(
                BadRequest.class,
                () -> assessmentTeamAppService.createTeam(leader.getId(), time.getId(), "队二"));
    }

    @Test
    @DisplayName("createTeam: 已提交个人答案的用户不能创建队伍")
    void createTeam_hasPersonalAnswer_shouldThrowBadRequest() {
        User leader = createCandidate(Direction.COMPUTER_VISION, 2024);
        AssessmentTime time = createTeamAllowedTime(Direction.COMPUTER_VISION, 2024);
        AssessmentQuestion question = AssessmentFixture.questionBuilder()
                .assessmentTime(time)
                .save(assessmentQuestionRepository);
        AssessmentFixture.answerBuilder().user(leader).question(question).save(assessmentAnswerRepository);
        stubPrepareNewTeamThrows(leader.getId(), time, "队", new BadRequest("您已提交过个人答案，无法创建队伍"));

        assertThrows(
                BadRequest.class,
                () -> assessmentTeamAppService.createTeam(leader.getId(), time.getId(), "队"));
    }

    @Test
    @DisplayName("createTeam: 考核已结束不能创建队伍")
    void createTeam_afterEnd_shouldThrowBadRequest() {
        User leader = createCandidate(Direction.COMPUTER_VISION, 2024);
        AssessmentTime time = AssessmentFixture.timeBuilder()
                .direction(Direction.COMPUTER_VISION)
                .grade(2024)
                .startTime(TimeFixture.minusMinutes(60))
                .endTime(TimeFixture.minusMinutes(5))
                .allowTeam()
                .save(assessmentTimeRepository);
        stubPrepareNewTeamThrows(leader.getId(), time, "队", new BadRequest("考核时间已结束"));

        assertThrows(
                BadRequest.class,
                () -> assessmentTeamAppService.createTeam(leader.getId(), time.getId(), "队"));
    }

    @Test
    @DisplayName("previewTeam: 应通过邀请码预览队伍")
    void previewTeam_shouldReturnPreview() {
        User leader = createCandidate(Direction.COMPUTER_VISION, 2024);
        AssessmentTime time = createTeamAllowedTime(Direction.COMPUTER_VISION, 2024);
        stubPrepareNewTeam(leader.getId(), time, "预览队", "CODE02");
        TeamResult team = assessmentTeamAppService.createTeam(leader.getId(), time.getId(), "预览队");

        TeamPreviewResult result = assessmentTeamAppService.previewTeam(team.inviteCode());

        assertNotNull(result);
        assertEquals(team.id(), result.id());
        assertEquals("预览队", result.name());
        assertEquals(1, result.memberCount());
        assertEquals(leader.getUsername(), result.leaderUsername());
    }

    @Test
    @DisplayName("previewTeam: 队伍已解散应抛 BadRequest")
    void previewTeam_disbanded_shouldThrowBadRequest() {
        User leader = createCandidate(Direction.COMPUTER_VISION, 2024);
        AssessmentTime time = createTeamAllowedTime(Direction.COMPUTER_VISION, 2024);
        stubPrepareNewTeam(leader.getId(), time, "解散队", "CODE03");
        stubPrepareDisband(leader.getId());
        stubValidateTeamPreviewableThrows(new BadRequest("该队伍已解散"));
        TeamResult team = assessmentTeamAppService.createTeam(leader.getId(), time.getId(), "解散队");
        assessmentTeamAppService.disbandTeam(leader.getId(), team.id());

        assertThrows(BadRequest.class, () -> assessmentTeamAppService.previewTeam(team.inviteCode()));
    }

    @Test
    @DisplayName("joinTeam: 成员应能通过邀请码加入队伍")
    void joinTeam_shouldJoin() {
        User leader = createCandidate(Direction.COMPUTER_VISION, 2024);
        User member = createCandidate(Direction.COMPUTER_VISION, 2024);
        AssessmentTime time = createTeamAllowedTime(Direction.COMPUTER_VISION, 2024);
        stubPrepareNewTeam(leader.getId(), time, "扩容队", "CODE04");
        TeamResult team = assessmentTeamAppService.createTeam(leader.getId(), time.getId(), "扩容队");

        TeamResult result = assessmentTeamAppService.joinTeam(member.getId(), team.inviteCode());

        assertEquals(2, result.members().size());
        assertTrue(assessmentTeamRepository.isMember(team.id(), member.getId()));
    }

    @Test
    @DisplayName("joinTeam: 已加入该考核队伍的用户不能再加入")
    void joinTeam_alreadyInTeam_shouldThrowBadRequest() {
        User leader = createCandidate(Direction.COMPUTER_VISION, 2024);
        User member = createCandidate(Direction.COMPUTER_VISION, 2024);
        AssessmentTime time = createTeamAllowedTime(Direction.COMPUTER_VISION, 2024);
        stubPrepareNewTeam(leader.getId(), time, "队一", "CODE05");
        TeamResult team = assessmentTeamAppService.createTeam(leader.getId(), time.getId(), "队一");
        assessmentTeamAppService.joinTeam(member.getId(), team.inviteCode());
        stubValidateCanJoinTeamThrows(member.getId(), new BadRequest("您已加入该考核的队伍"));

        assertThrows(
                BadRequest.class,
                () -> assessmentTeamAppService.joinTeam(member.getId(), team.inviteCode()));
    }

    @Test
    @DisplayName("leaveTeam: 成员应能离开队伍")
    void leaveTeam_shouldLeave() {
        User leader = createCandidate(Direction.COMPUTER_VISION, 2024);
        User member = createCandidate(Direction.COMPUTER_VISION, 2024);
        AssessmentTime time = createTeamAllowedTime(Direction.COMPUTER_VISION, 2024);
        stubPrepareNewTeam(leader.getId(), time, "离队队", "CODE06");
        TeamResult team = assessmentTeamAppService.createTeam(leader.getId(), time.getId(), "离队队");
        assessmentTeamAppService.joinTeam(member.getId(), team.inviteCode());

        assessmentTeamAppService.leaveTeam(member.getId(), team.id());

        assertNull(assessmentTeamAppService.getMyTeam(member.getId(), time.getId()));
        assertFalse(assessmentTeamRepository.isMember(team.id(), member.getId()));
    }

    @Test
    @DisplayName("leaveTeam: 队长不能离开队伍")
    void leaveTeam_leaderCannotLeave_shouldThrowForbidden() {
        User leader = createCandidate(Direction.COMPUTER_VISION, 2024);
        AssessmentTime time = createTeamAllowedTime(Direction.COMPUTER_VISION, 2024);
        stubPrepareNewTeam(leader.getId(), time, "队", "CODE07");
        TeamResult team = assessmentTeamAppService.createTeam(leader.getId(), time.getId(), "队");
        stubValidateCanLeaveTeamThrows(leader.getId(), new Forbidden("队长不能离开队伍，请先转让队长或解散队伍"));

        assertThrows(Forbidden.class, () -> assessmentTeamAppService.leaveTeam(leader.getId(), team.id()));
    }

    @Test
    @DisplayName("transferLeader: 队长应能转让队长")
    void transferLeader_shouldTransfer() {
        User leader = createCandidate(Direction.COMPUTER_VISION, 2024);
        User member = createCandidate(Direction.COMPUTER_VISION, 2024);
        AssessmentTime time = createTeamAllowedTime(Direction.COMPUTER_VISION, 2024);
        stubPrepareNewTeam(leader.getId(), time, "转让队", "CODE08");
        TeamResult team = assessmentTeamAppService.createTeam(leader.getId(), time.getId(), "转让队");
        assessmentTeamAppService.joinTeam(member.getId(), team.inviteCode());
        stubPrepareLeaderTransfer(leader.getId(), member.getId());

        TeamResult result = assessmentTeamAppService.transferLeader(leader.getId(), team.id(), member.getId());

        assertEquals(member.getId(), result.leaderId());
    }

    @Test
    @DisplayName("transferLeader: 非队长不能转让队长")
    void transferLeader_nonLeader_shouldThrowForbidden() {
        User leader = createCandidate(Direction.COMPUTER_VISION, 2024);
        User member = createCandidate(Direction.COMPUTER_VISION, 2024);
        AssessmentTime time = createTeamAllowedTime(Direction.COMPUTER_VISION, 2024);
        stubPrepareNewTeam(leader.getId(), time, "队", "CODE09");
        TeamResult team = assessmentTeamAppService.createTeam(leader.getId(), time.getId(), "队");
        assessmentTeamAppService.joinTeam(member.getId(), team.inviteCode());
        stubPrepareLeaderTransferThrows(member.getId(), leader.getId(), new Forbidden("只有队长可以转让队长"));

        assertThrows(
                Forbidden.class,
                () -> assessmentTeamAppService.transferLeader(member.getId(), team.id(), leader.getId()));
    }

    @Test
    @DisplayName("disbandTeam: 队长应能解散队伍")
    void disbandTeam_shouldDisband() {
        User leader = createCandidate(Direction.COMPUTER_VISION, 2024);
        AssessmentTime time = createTeamAllowedTime(Direction.COMPUTER_VISION, 2024);
        stubPrepareNewTeam(leader.getId(), time, "解散队", "CODE10");
        TeamResult team = assessmentTeamAppService.createTeam(leader.getId(), time.getId(), "解散队");
        stubPrepareDisband(leader.getId());

        assessmentTeamAppService.disbandTeam(leader.getId(), team.id());

        AssessmentTeam updated = assessmentTeamRepository.findById(team.id()).orElseThrow();
        assertEquals(AssessmentTeam.TeamStatus.DISBANDED, updated.getStatus());
    }

    @Test
    @DisplayName("disbandTeam: 非队长不能解散队伍")
    void disbandTeam_nonLeader_shouldThrowForbidden() {
        User leader = createCandidate(Direction.COMPUTER_VISION, 2024);
        User member = createCandidate(Direction.COMPUTER_VISION, 2024);
        AssessmentTime time = createTeamAllowedTime(Direction.COMPUTER_VISION, 2024);
        stubPrepareNewTeam(leader.getId(), time, "队", "CODE11");
        TeamResult team = assessmentTeamAppService.createTeam(leader.getId(), time.getId(), "队");
        assessmentTeamAppService.joinTeam(member.getId(), team.inviteCode());
        stubPrepareDisbandThrows(member.getId(), new Forbidden("只有队长可以解散队伍"));

        assertThrows(Forbidden.class, () -> assessmentTeamAppService.disbandTeam(member.getId(), team.id()));
    }

    @Test
    @DisplayName("getMyTeam: 应返回当前用户的队伍")
    void getMyTeam_shouldReturnTeam() {
        User leader = createCandidate(Direction.COMPUTER_VISION, 2024);
        AssessmentTime time = createTeamAllowedTime(Direction.COMPUTER_VISION, 2024);
        stubPrepareNewTeam(leader.getId(), time, "我的队", "CODE12");
        TeamResult team = assessmentTeamAppService.createTeam(leader.getId(), time.getId(), "我的队");

        TeamResult result = assessmentTeamAppService.getMyTeam(leader.getId(), time.getId());

        assertNotNull(result);
        assertEquals(team.id(), result.id());
    }

    @Test
    @DisplayName("getMyTeam: 用户未加入队伍应返回 null")
    void getMyTeam_notInTeam_shouldReturnNull() {
        User user = createCandidate(Direction.COMPUTER_VISION, 2024);
        AssessmentTime time = createTeamAllowedTime(Direction.COMPUTER_VISION, 2024);

        TeamResult result = assessmentTeamAppService.getMyTeam(user.getId(), time.getId());

        assertNull(result);
    }
}
