package com.bluenet.web.domain.service.impl;

import com.bluenet.web.domain.exception.BadRequest;
import com.bluenet.web.domain.exception.Forbidden;
import com.bluenet.web.domain.model.entity.AssessmentTeam;
import com.bluenet.web.domain.model.entity.AssessmentTime;
import com.bluenet.web.domain.repository.AssessmentAnswerRepository;
import com.bluenet.web.domain.repository.AssessmentTeamRepository;
import com.bluenet.web.domain.service.AssessmentTeamDomainService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;

/**
 * 考核队伍领域服务实现。
 * <p>
 * 只负责校验、生成/修改领域实体；所有写操作由应用服务在事务中统一执行。
 * </p>
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class AssessmentTeamDomainServiceImpl implements AssessmentTeamDomainService {

    private static final String INVITE_CODE_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int INVITE_CODE_LENGTH = 6;

    private final AssessmentTeamRepository assessmentTeamRepository;
    private final AssessmentAnswerRepository assessmentAnswerRepository;
    private final SecureRandom secureRandom = new SecureRandom();

    @Override
    public AssessmentTeam prepareNewTeam(Long userId, AssessmentTime assessmentTime, String name) {
        if (!Boolean.TRUE.equals(assessmentTime.getAllowTeam())) {
            throw new BadRequest("该考核不允许组队");
        }
        validateTimeNotEnded(assessmentTime);

        if (assessmentTeamRepository.existsByAssessmentTimeIdAndUserId(assessmentTime.getId(), userId)) {
            throw new BadRequest("您已加入该考核的队伍");
        }

        if (hasPersonalAnswer(assessmentTime.getId(), userId)) {
            throw new BadRequest("您已提交过个人答案，无法创建队伍");
        }

        String inviteCode = generateUniqueInviteCode();
        AssessmentTeam team = AssessmentTeam.create(assessmentTime.getId(), userId, name, inviteCode);
        log.info("准备创建队伍，assessmentTimeId: {}, leaderId: {}", assessmentTime.getId(), userId);
        return team;
    }

    @Override
    public void validateCanJoinTeam(Long userId, AssessmentTeam team, AssessmentTime assessmentTime) {
        if (!team.isActive()) {
            throw new BadRequest("该队伍已解散");
        }
        validateTimeNotEnded(assessmentTime);

        if (assessmentTeamRepository.existsByAssessmentTimeIdAndUserId(assessmentTime.getId(), userId)) {
            throw new BadRequest("您已加入该考核的队伍");
        }

        if (hasPersonalAnswer(assessmentTime.getId(), userId)) {
            throw new BadRequest("您已提交过个人答案，无法加入队伍");
        }

        if (hasTeamAnswer(assessmentTime.getId(), userId)) {
            throw new BadRequest("您已有队伍答案，无法加入其他队伍");
        }
    }

    @Override
    public void validateCanLeaveTeam(Long userId, AssessmentTeam team) {
        if (!team.isActive()) {
            throw new BadRequest("该队伍已解散");
        }
        if (team.isLeader(userId)) {
            throw new Forbidden("队长不能离开队伍，请先转让队长或解散队伍");
        }
        if (!assessmentTeamRepository.isMember(team.getId(), userId)) {
            throw new BadRequest("您不是该队伍的成员");
        }
        if (hasTeamSubmittedAnswer(team.getId())) {
            throw new Forbidden("队伍已提交答案，无法退出");
        }
    }

    @Override
    public AssessmentTeam prepareLeaderTransfer(Long userId, AssessmentTeam team, Long newLeaderId) {
        if (!team.isActive()) {
            throw new BadRequest("该队伍已解散");
        }
        if (!team.isLeader(userId)) {
            throw new Forbidden("只有队长可以转让队长");
        }
        if (hasTeamSubmittedAnswer(team.getId())) {
            throw new Forbidden("队伍已提交答案，无法转让队长");
        }
        if (!assessmentTeamRepository.isMember(team.getId(), newLeaderId)) {
            throw new BadRequest("新队长必须是队伍成员");
        }

        team.updateLeader(newLeaderId);
        log.info("准备转让队长，teamId: {}, newLeaderId: {}", team.getId(), newLeaderId);
        return team;
    }

    @Override
    public AssessmentTeam prepareDisband(Long userId, AssessmentTeam team) {
        if (!team.isLeader(userId)) {
            throw new Forbidden("只有队长可以解散队伍");
        }
        if (hasTeamSubmittedAnswer(team.getId())) {
            throw new Forbidden("队伍已提交答案，无法解散");
        }

        team.disband();
        log.info("准备解散队伍，teamId: {}, leaderId: {}", team.getId(), userId);
        return team;
    }

    @Override
    public void validateTeamPreviewable(AssessmentTeam team, AssessmentTime assessmentTime) {
        if (!team.isActive()) {
            throw new BadRequest("该队伍已解散");
        }
        validateTimeNotEnded(assessmentTime);
    }

    private String generateUniqueInviteCode() {
        String inviteCode = generateInviteCode();
        while (assessmentTeamRepository.findByInviteCode(inviteCode).isPresent()) {
            inviteCode = generateInviteCode();
        }
        return inviteCode;
    }

    private String generateInviteCode() {
        StringBuilder sb = new StringBuilder(INVITE_CODE_LENGTH);
        for (int i = 0; i < INVITE_CODE_LENGTH; i++) {
            int index = secureRandom.nextInt(INVITE_CODE_CHARS.length());
            sb.append(INVITE_CODE_CHARS.charAt(index));
        }
        return sb.toString();
    }

    private void validateTimeNotEnded(AssessmentTime time) {
        if (time.getEndTime() != null && LocalDateTime.now().isAfter(time.getEndTime())) {
            throw new BadRequest("考核时间已结束");
        }
    }

    private boolean hasPersonalAnswer(Long assessmentTimeId, Long userId) {
        return assessmentAnswerRepository.countPersonalAnswersByUserIdAndAssessmentTimeId(userId, assessmentTimeId) > 0;
    }

    private boolean hasTeamAnswer(Long assessmentTimeId, Long userId) {
        return assessmentAnswerRepository.countTeamAnswersByUserIdAndAssessmentTimeId(userId, assessmentTimeId) > 0;
    }

    private boolean hasTeamSubmittedAnswer(Long teamId) {
        return assessmentAnswerRepository.countByTeamId(teamId) > 0;
    }
}
