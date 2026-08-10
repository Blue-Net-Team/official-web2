package com.bluenet.web.domain.service.impl;

import com.bluenet.web.domain.exception.BadRequest;
import com.bluenet.web.domain.exception.Forbidden;
import com.bluenet.web.domain.model.entity.AssessmentTeam;
import com.bluenet.web.domain.model.entity.AssessmentTime;
import com.bluenet.web.domain.repository.AssessmentAnswerRepository;
import com.bluenet.web.domain.repository.AssessmentTeamRepository;
import com.bluenet.web.testsupport.fixture.AssessmentFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * AssessmentTeamDomainServiceImpl 单元测试。
 */
@DisplayName("AssessmentTeamDomainServiceImpl 测试")
@ExtendWith(MockitoExtension.class)
class AssessmentTeamDomainServiceImplTest {

    @Mock
    private AssessmentTeamRepository assessmentTeamRepository;
    @Mock
    private AssessmentAnswerRepository assessmentAnswerRepository;

    private AssessmentTeamDomainServiceImpl domainService;

    @BeforeEach
    void setUp() {
        domainService = new AssessmentTeamDomainServiceImpl(assessmentTeamRepository, assessmentAnswerRepository);
    }

    @Test
    @DisplayName("prepareNewTeam: 有效请求应创建队伍")
    void prepareNewTeam_shouldCreateTeamWhenValid() {
        Long userId = 1L;
        AssessmentTime time = AssessmentFixture.timeBuilder().allowTeam().build();
        when(assessmentTeamRepository.existsByAssessmentTimeIdAndUserId(time.getId(), userId)).thenReturn(false);
        when(assessmentAnswerRepository.countPersonalAnswersByUserIdAndAssessmentTimeId(userId, time.getId()))
                .thenReturn(0);
        when(assessmentTeamRepository.findByInviteCode(anyString())).thenReturn(Optional.empty());

        AssessmentTeam team = domainService.prepareNewTeam(userId, time, "新队伍");

        assertNotNull(team);
        assertEquals(time.getId(), team.getAssessmentTimeId());
        assertEquals(userId, team.getLeaderId());
        assertEquals("新队伍", team.getName());
        assertTrue(team.isActive());
        assertNotNull(team.getInviteCode());
        assertEquals(6, team.getInviteCode().length());
        assertNotNull(team.getCreatedAt());
    }

    @Test
    @DisplayName("prepareNewTeam: 考核不允许组队应抛出 BadRequest")
    void prepareNewTeam_shouldThrowBadRequestWhenTeamNotAllowed() {
        Long userId = 1L;
        AssessmentTime time = AssessmentFixture.timeBuilder().build();

        BadRequest exception = assertThrows(
                BadRequest.class,
                () -> domainService.prepareNewTeam(userId, time, "新队伍"));
        assertEquals("该考核不允许组队", exception.getMessage());
        verify(assessmentTeamRepository, never()).existsByAssessmentTimeIdAndUserId(any(), any());
    }

    @Test
    @DisplayName("prepareNewTeam: 考核时间已结束应抛出 BadRequest")
    void prepareNewTeam_shouldThrowBadRequestWhenTimeEnded() {
        Long userId = 1L;
        AssessmentTime time = AssessmentFixture.timeBuilder().allowTeam().ended().build();

        BadRequest exception = assertThrows(
                BadRequest.class,
                () -> domainService.prepareNewTeam(userId, time, "新队伍"));
        assertEquals("考核时间已结束", exception.getMessage());
    }

    @Test
    @DisplayName("prepareNewTeam: 已加入队伍应抛出 BadRequest")
    void prepareNewTeam_shouldThrowBadRequestWhenAlreadyInTeam() {
        Long userId = 1L;
        AssessmentTime time = AssessmentFixture.timeBuilder().allowTeam().build();
        when(assessmentTeamRepository.existsByAssessmentTimeIdAndUserId(time.getId(), userId)).thenReturn(true);

        BadRequest exception = assertThrows(
                BadRequest.class,
                () -> domainService.prepareNewTeam(userId, time, "新队伍"));
        assertEquals("您已加入该考核的队伍", exception.getMessage());
    }

    @Test
    @DisplayName("prepareNewTeam: 已提交个人答案应抛出 BadRequest")
    void prepareNewTeam_shouldThrowBadRequestWhenHasPersonalAnswer() {
        Long userId = 1L;
        AssessmentTime time = AssessmentFixture.timeBuilder().allowTeam().build();
        when(assessmentTeamRepository.existsByAssessmentTimeIdAndUserId(time.getId(), userId)).thenReturn(false);
        when(assessmentAnswerRepository.countPersonalAnswersByUserIdAndAssessmentTimeId(userId, time.getId()))
                .thenReturn(1);

        BadRequest exception = assertThrows(
                BadRequest.class,
                () -> domainService.prepareNewTeam(userId, time, "新队伍"));
        assertEquals("您已提交过个人答案，无法创建队伍", exception.getMessage());
    }

    @Test
    @DisplayName("validateCanJoinTeam: 有效请求应通过")
    void validateCanJoinTeam_shouldPassWhenValid() {
        Long userId = 2L;
        AssessmentTime time = AssessmentFixture.timeBuilder().allowTeam().build();
        AssessmentTeam team = AssessmentFixture.teamBuilder().assessmentTimeId(time.getId()).leaderId(1L).build();
        when(assessmentTeamRepository.existsByAssessmentTimeIdAndUserId(time.getId(), userId)).thenReturn(false);
        when(assessmentAnswerRepository.countPersonalAnswersByUserIdAndAssessmentTimeId(userId, time.getId()))
                .thenReturn(0);
        when(assessmentAnswerRepository.countTeamAnswersByUserIdAndAssessmentTimeId(userId, time.getId()))
                .thenReturn(0);

        domainService.validateCanJoinTeam(userId, team, time);
    }

    @Test
    @DisplayName("validateCanJoinTeam: 队伍已解散应抛出 BadRequest")
    void validateCanJoinTeam_shouldThrowBadRequestWhenTeamDisbanded() {
        Long userId = 2L;
        AssessmentTime time = AssessmentFixture.timeBuilder().allowTeam().build();
        AssessmentTeam team = AssessmentFixture.teamBuilder().assessmentTimeId(time.getId()).leaderId(1L).build();
        team.disband();

        BadRequest exception = assertThrows(
                BadRequest.class,
                () -> domainService.validateCanJoinTeam(userId, team, time));
        assertEquals("该队伍已解散", exception.getMessage());
    }

    @Test
    @DisplayName("validateCanJoinTeam: 考核时间已结束应抛出 BadRequest")
    void validateCanJoinTeam_shouldThrowBadRequestWhenTimeEnded() {
        Long userId = 2L;
        AssessmentTime time = AssessmentFixture.timeBuilder().allowTeam().ended().build();
        AssessmentTeam team = AssessmentFixture.teamBuilder().assessmentTimeId(time.getId()).leaderId(1L).build();

        BadRequest exception = assertThrows(
                BadRequest.class,
                () -> domainService.validateCanJoinTeam(userId, team, time));
        assertEquals("考核时间已结束", exception.getMessage());
    }

    @Test
    @DisplayName("validateCanJoinTeam: 已加入队伍应抛出 BadRequest")
    void validateCanJoinTeam_shouldThrowBadRequestWhenAlreadyInTeam() {
        Long userId = 2L;
        AssessmentTime time = AssessmentFixture.timeBuilder().allowTeam().build();
        AssessmentTeam team = AssessmentFixture.teamBuilder().assessmentTimeId(time.getId()).leaderId(1L).build();
        when(assessmentTeamRepository.existsByAssessmentTimeIdAndUserId(time.getId(), userId)).thenReturn(true);

        BadRequest exception = assertThrows(
                BadRequest.class,
                () -> domainService.validateCanJoinTeam(userId, team, time));
        assertEquals("您已加入该考核的队伍", exception.getMessage());
    }

    @Test
    @DisplayName("validateCanJoinTeam: 已提交个人答案应抛出 BadRequest")
    void validateCanJoinTeam_shouldThrowBadRequestWhenHasPersonalAnswer() {
        Long userId = 2L;
        AssessmentTime time = AssessmentFixture.timeBuilder().allowTeam().build();
        AssessmentTeam team = AssessmentFixture.teamBuilder().assessmentTimeId(time.getId()).leaderId(1L).build();
        when(assessmentTeamRepository.existsByAssessmentTimeIdAndUserId(time.getId(), userId)).thenReturn(false);
        when(assessmentAnswerRepository.countPersonalAnswersByUserIdAndAssessmentTimeId(userId, time.getId()))
                .thenReturn(1);

        BadRequest exception = assertThrows(
                BadRequest.class,
                () -> domainService.validateCanJoinTeam(userId, team, time));
        assertEquals("您已提交过个人答案，无法加入队伍", exception.getMessage());
    }

    @Test
    @DisplayName("validateCanJoinTeam: 已有队伍答案应抛出 BadRequest")
    void validateCanJoinTeam_shouldThrowBadRequestWhenHasTeamAnswer() {
        Long userId = 2L;
        AssessmentTime time = AssessmentFixture.timeBuilder().allowTeam().build();
        AssessmentTeam team = AssessmentFixture.teamBuilder().assessmentTimeId(time.getId()).leaderId(1L).build();
        when(assessmentTeamRepository.existsByAssessmentTimeIdAndUserId(time.getId(), userId)).thenReturn(false);
        when(assessmentAnswerRepository.countPersonalAnswersByUserIdAndAssessmentTimeId(userId, time.getId()))
                .thenReturn(0);
        when(assessmentAnswerRepository.countTeamAnswersByUserIdAndAssessmentTimeId(userId, time.getId()))
                .thenReturn(1);

        BadRequest exception = assertThrows(
                BadRequest.class,
                () -> domainService.validateCanJoinTeam(userId, team, time));
        assertEquals("您已有队伍答案，无法加入其他队伍", exception.getMessage());
    }

    @Test
    @DisplayName("validateCanLeaveTeam: 普通成员可以离开队伍")
    void validateCanLeaveTeam_shouldPassWhenValid() {
        Long leaderId = 1L;
        Long memberId = 2L;
        AssessmentTeam team = AssessmentFixture.teamBuilder().leaderId(leaderId).build();
        when(assessmentTeamRepository.isMember(team.getId(), memberId)).thenReturn(true);
        when(assessmentAnswerRepository.countByTeamId(team.getId())).thenReturn(0);

        domainService.validateCanLeaveTeam(memberId, team);
    }

    @Test
    @DisplayName("validateCanLeaveTeam: 队伍已解散应抛出 BadRequest")
    void validateCanLeaveTeam_shouldThrowBadRequestWhenTeamDisbanded() {
        Long leaderId = 1L;
        Long memberId = 2L;
        AssessmentTeam team = AssessmentFixture.teamBuilder().leaderId(leaderId).build();
        team.disband();

        BadRequest exception = assertThrows(
                BadRequest.class,
                () -> domainService.validateCanLeaveTeam(memberId, team));
        assertEquals("该队伍已解散", exception.getMessage());
    }

    @Test
    @DisplayName("validateCanLeaveTeam: 队长不能离开队伍")
    void validateCanLeaveTeam_shouldThrowForbiddenWhenLeader() {
        Long leaderId = 1L;
        AssessmentTeam team = AssessmentFixture.teamBuilder().leaderId(leaderId).build();

        Forbidden exception = assertThrows(
                Forbidden.class,
                () -> domainService.validateCanLeaveTeam(leaderId, team));
        assertEquals("队长不能离开队伍，请先转让队长或解散队伍", exception.getMessage());
        verify(assessmentTeamRepository, never()).isMember(team.getId(), leaderId);
    }

    @Test
    @DisplayName("validateCanLeaveTeam: 不是队伍成员应抛出 BadRequest")
    void validateCanLeaveTeam_shouldThrowBadRequestWhenNotMember() {
        Long leaderId = 1L;
        Long userId = 2L;
        AssessmentTeam team = AssessmentFixture.teamBuilder().leaderId(leaderId).build();
        when(assessmentTeamRepository.isMember(team.getId(), userId)).thenReturn(false);

        BadRequest exception = assertThrows(
                BadRequest.class,
                () -> domainService.validateCanLeaveTeam(userId, team));
        assertEquals("您不是该队伍的成员", exception.getMessage());
    }

    @Test
    @DisplayName("validateCanLeaveTeam: 队伍已提交答案应抛出 Forbidden")
    void validateCanLeaveTeam_shouldThrowForbiddenWhenTeamSubmittedAnswer() {
        Long leaderId = 1L;
        Long memberId = 2L;
        AssessmentTeam team = AssessmentFixture.teamBuilder().leaderId(leaderId).build();
        when(assessmentTeamRepository.isMember(team.getId(), memberId)).thenReturn(true);
        when(assessmentAnswerRepository.countByTeamId(team.getId())).thenReturn(1);

        Forbidden exception = assertThrows(
                Forbidden.class,
                () -> domainService.validateCanLeaveTeam(memberId, team));
        assertEquals("队伍已提交答案，无法退出", exception.getMessage());
    }

    @Test
    @DisplayName("prepareLeaderTransfer: 队长转让给成员应成功")
    void prepareLeaderTransfer_shouldTransferWhenValid() {
        Long leaderId = 1L;
        Long newLeaderId = 2L;
        AssessmentTeam team = AssessmentFixture.teamBuilder().leaderId(leaderId).build();
        when(assessmentAnswerRepository.countByTeamId(team.getId())).thenReturn(0);
        when(assessmentTeamRepository.isMember(team.getId(), newLeaderId)).thenReturn(true);

        AssessmentTeam result = domainService.prepareLeaderTransfer(leaderId, team, newLeaderId);

        assertEquals(newLeaderId, result.getLeaderId());
        assertTrue(result.isActive());
    }

    @Test
    @DisplayName("prepareLeaderTransfer: 队伍已解散应抛出 BadRequest")
    void prepareLeaderTransfer_shouldThrowBadRequestWhenTeamDisbanded() {
        Long leaderId = 1L;
        Long newLeaderId = 2L;
        AssessmentTeam team = AssessmentFixture.teamBuilder().leaderId(leaderId).build();
        team.disband();

        BadRequest exception = assertThrows(
                BadRequest.class,
                () -> domainService.prepareLeaderTransfer(leaderId, team, newLeaderId));
        assertEquals("该队伍已解散", exception.getMessage());
    }

    @Test
    @DisplayName("prepareLeaderTransfer: 非队长不能转让")
    void prepareLeaderTransfer_shouldThrowForbiddenWhenNotLeader() {
        Long leaderId = 1L;
        Long nonLeaderId = 3L;
        Long newLeaderId = 2L;
        AssessmentTeam team = AssessmentFixture.teamBuilder().leaderId(leaderId).build();

        Forbidden exception = assertThrows(
                Forbidden.class,
                () -> domainService.prepareLeaderTransfer(nonLeaderId, team, newLeaderId));
        assertEquals("只有队长可以转让队长", exception.getMessage());
        verify(assessmentAnswerRepository, never()).countByTeamId(team.getId());
    }

    @Test
    @DisplayName("prepareLeaderTransfer: 队伍已提交答案应抛出 Forbidden")
    void prepareLeaderTransfer_shouldThrowForbiddenWhenTeamSubmittedAnswer() {
        Long leaderId = 1L;
        Long newLeaderId = 2L;
        AssessmentTeam team = AssessmentFixture.teamBuilder().leaderId(leaderId).build();
        when(assessmentAnswerRepository.countByTeamId(team.getId())).thenReturn(1);

        Forbidden exception = assertThrows(
                Forbidden.class,
                () -> domainService.prepareLeaderTransfer(leaderId, team, newLeaderId));
        assertEquals("队伍已提交答案，无法转让队长", exception.getMessage());
        verify(assessmentTeamRepository, never()).isMember(team.getId(), newLeaderId);
    }

    @Test
    @DisplayName("prepareLeaderTransfer: 新队长不是成员应抛出 BadRequest")
    void prepareLeaderTransfer_shouldThrowBadRequestWhenNewLeaderNotMember() {
        Long leaderId = 1L;
        Long newLeaderId = 2L;
        AssessmentTeam team = AssessmentFixture.teamBuilder().leaderId(leaderId).build();
        when(assessmentAnswerRepository.countByTeamId(team.getId())).thenReturn(0);
        when(assessmentTeamRepository.isMember(team.getId(), newLeaderId)).thenReturn(false);

        BadRequest exception = assertThrows(
                BadRequest.class,
                () -> domainService.prepareLeaderTransfer(leaderId, team, newLeaderId));
        assertEquals("新队长必须是队伍成员", exception.getMessage());
    }

    @Test
    @DisplayName("prepareDisband: 队长可以解散队伍")
    void prepareDisband_shouldDisbandWhenLeader() {
        Long leaderId = 1L;
        AssessmentTeam team = AssessmentFixture.teamBuilder().leaderId(leaderId).build();
        when(assessmentAnswerRepository.countByTeamId(team.getId())).thenReturn(0);

        AssessmentTeam result = domainService.prepareDisband(leaderId, team);

        assertFalse(result.isActive());
        assertEquals(AssessmentTeam.TeamStatus.DISBANDED, result.getStatus());
    }

    @Test
    @DisplayName("prepareDisband: 非队长不能解散队伍")
    void prepareDisband_shouldThrowForbiddenWhenNotLeader() {
        Long leaderId = 1L;
        Long nonLeaderId = 2L;
        AssessmentTeam team = AssessmentFixture.teamBuilder().leaderId(leaderId).build();

        Forbidden exception = assertThrows(
                Forbidden.class,
                () -> domainService.prepareDisband(nonLeaderId, team));
        assertEquals("只有队长可以解散队伍", exception.getMessage());
        verify(assessmentAnswerRepository, never()).countByTeamId(team.getId());
    }

    @Test
    @DisplayName("prepareDisband: 队伍已提交答案应抛出 Forbidden")
    void prepareDisband_shouldThrowForbiddenWhenTeamSubmittedAnswer() {
        Long leaderId = 1L;
        AssessmentTeam team = AssessmentFixture.teamBuilder().leaderId(leaderId).build();
        when(assessmentAnswerRepository.countByTeamId(team.getId())).thenReturn(1);

        Forbidden exception = assertThrows(
                Forbidden.class,
                () -> domainService.prepareDisband(leaderId, team));
        assertEquals("队伍已提交答案，无法解散", exception.getMessage());
    }

    @Test
    @DisplayName("validateTeamPreviewable: 有效队伍应通过")
    void validateTeamPreviewable_shouldPassWhenValid() {
        AssessmentTime time = AssessmentFixture.timeBuilder().allowTeam().build();
        AssessmentTeam team = AssessmentFixture.teamBuilder().assessmentTimeId(time.getId()).leaderId(1L).build();

        domainService.validateTeamPreviewable(team, time);
    }

    @Test
    @DisplayName("validateTeamPreviewable: 队伍已解散应抛出 BadRequest")
    void validateTeamPreviewable_shouldThrowBadRequestWhenTeamDisbanded() {
        AssessmentTime time = AssessmentFixture.timeBuilder().allowTeam().build();
        AssessmentTeam team = AssessmentFixture.teamBuilder().assessmentTimeId(time.getId()).leaderId(1L).build();
        team.disband();

        BadRequest exception = assertThrows(
                BadRequest.class,
                () -> domainService.validateTeamPreviewable(team, time));
        assertEquals("该队伍已解散", exception.getMessage());
    }

    @Test
    @DisplayName("validateTeamPreviewable: 考核时间已结束应抛出 BadRequest")
    void validateTeamPreviewable_shouldThrowBadRequestWhenTimeEnded() {
        AssessmentTime time = AssessmentFixture.timeBuilder().allowTeam().ended().build();
        AssessmentTeam team = AssessmentFixture.teamBuilder().assessmentTimeId(time.getId()).leaderId(1L).build();

        BadRequest exception = assertThrows(
                BadRequest.class,
                () -> domainService.validateTeamPreviewable(team, time));
        assertEquals("考核时间已结束", exception.getMessage());
    }
}
