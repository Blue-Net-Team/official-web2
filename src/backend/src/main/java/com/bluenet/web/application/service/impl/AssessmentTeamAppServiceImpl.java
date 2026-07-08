package com.bluenet.web.application.service.impl;

import com.bluenet.web.application.TeamPreviewResult;
import com.bluenet.web.application.TeamResult;
import com.bluenet.web.application.service.AssessmentTeamAppService;
import com.bluenet.web.domain.exception.DataNotFound;
import com.bluenet.web.domain.model.entity.AssessmentTeam;
import com.bluenet.web.domain.model.entity.AssessmentTeamMember;
import com.bluenet.web.domain.model.entity.AssessmentTime;
import com.bluenet.web.domain.model.entity.User;
import com.bluenet.web.domain.repository.AssessmentAnswerRepository;
import com.bluenet.web.domain.repository.AssessmentJudgementRepository;
import com.bluenet.web.domain.repository.AssessmentTeamRepository;
import com.bluenet.web.domain.repository.AssessmentTimeRepository;
import com.bluenet.web.domain.repository.UserRepository;
import com.bluenet.web.domain.service.AssessmentTeamDomainService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * 考核队伍应用服务实现。
 * <p>
 * 实现考核队伍聚合在应用层的业务逻辑编排。
 * </p>
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class AssessmentTeamAppServiceImpl implements AssessmentTeamAppService {

    private final AssessmentTeamRepository assessmentTeamRepository;
    private final AssessmentTimeRepository assessmentTimeRepository;
    private final AssessmentAnswerRepository assessmentAnswerRepository;
    private final AssessmentJudgementRepository assessmentJudgementRepository;
    private final UserRepository userRepository;
    private final AssessmentTeamDomainService assessmentTeamDomainService;

    @Override
    @Transactional
    public TeamResult createTeam(Long userId, Long assessmentTimeId, String name) {
        AssessmentTime assessmentTime = assessmentTimeRepository.findById(assessmentTimeId)
                .orElseThrow(() -> new DataNotFound("考核时间不存在"));

        AssessmentTeam team = assessmentTeamDomainService.prepareNewTeam(userId, assessmentTime, name);
        assessmentTeamRepository.save(team);

        log.info(
                "创建队伍成功，teamId: {}, assessmentTimeId: {}, leaderId: {}",
                team.getId(),
                assessmentTimeId,
                userId);

        return toTeamResult(team);
    }

    @Override
    @Transactional(readOnly = true)
    public TeamPreviewResult previewTeam(String inviteCode) {
        AssessmentTeam team = assessmentTeamRepository.findByInviteCode(inviteCode)
                .orElseThrow(() -> new DataNotFound("邀请码无效"));

        AssessmentTime assessmentTime = assessmentTimeRepository.findById(team.getAssessmentTimeId())
                .orElseThrow(() -> new DataNotFound("考核时间不存在"));

        assessmentTeamDomainService.validateTeamPreviewable(team, assessmentTime);

        List<AssessmentTeamMember> members = assessmentTeamRepository.findMembersByTeamId(team.getId());
        List<String> memberUsernames = new ArrayList<>();
        String leaderUsername = "";

        for (AssessmentTeamMember member : members) {
            Optional<User> userOpt = userRepository.findById(member.getUserId());
            if (userOpt.isPresent()) {
                String username = userOpt.get().getUsername();
                memberUsernames.add(username);
                if (member.getUserId().equals(team.getLeaderId())) {
                    leaderUsername = username;
                }
            }
        }

        return new TeamPreviewResult(
                team.getId(),
                team.getAssessmentTimeId(),
                leaderUsername,
                team.getName(),
                team.getStatus(),
                team.getCreatedAt(),
                members.size(),
                memberUsernames);
    }

    @Override
    @Transactional
    public TeamResult joinTeam(Long userId, String inviteCode) {
        AssessmentTeam team = assessmentTeamRepository.findByInviteCode(inviteCode)
                .orElseThrow(() -> new DataNotFound("邀请码无效"));

        AssessmentTime assessmentTime = assessmentTimeRepository.findById(team.getAssessmentTimeId())
                .orElseThrow(() -> new DataNotFound("考核时间不存在"));

        assessmentTeamDomainService.validateCanJoinTeam(userId, team, assessmentTime);
        assessmentTeamRepository.addMember(team.getId(), userId);

        log.info("用户加入队伍成功，teamId: {}, userId: {}", team.getId(), userId);

        AssessmentTeam updatedTeam = assessmentTeamRepository.findById(team.getId())
                .orElseThrow(() -> new DataNotFound("队伍不存在"));
        return toTeamResult(updatedTeam);
    }

    @Override
    @Transactional(readOnly = true)
    public TeamResult getMyTeam(Long userId, Long assessmentTimeId) {
        Optional<AssessmentTeam> teamOpt = assessmentTeamRepository
                .findByAssessmentTimeIdAndUserId(assessmentTimeId, userId);
        return teamOpt.map(this::toTeamResult).orElse(null);
    }

    @Override
    @Transactional
    public void leaveTeam(Long userId, Long teamId) {
        AssessmentTeam team = assessmentTeamRepository.findById(teamId)
                .orElseThrow(() -> new DataNotFound("队伍不存在"));

        assessmentTeamDomainService.validateCanLeaveTeam(userId, team);
        assessmentTeamRepository.removeMember(teamId, userId);

        log.info("用户离开队伍成功，teamId: {}, userId: {}", teamId, userId);
    }

    @Override
    @Transactional
    public TeamResult transferLeader(Long userId, Long teamId, Long newLeaderId) {
        AssessmentTeam team = assessmentTeamRepository.findById(teamId)
                .orElseThrow(() -> new DataNotFound("队伍不存在"));

        AssessmentTeam updatedTeam = assessmentTeamDomainService.prepareLeaderTransfer(userId, team, newLeaderId);
        assessmentTeamRepository.save(updatedTeam);

        log.info("转让队长成功，teamId: {}, newLeaderId: {}", teamId, newLeaderId);

        return toTeamResult(updatedTeam);
    }

    @Override
    @Transactional
    public void disbandTeam(Long userId, Long teamId) {
        AssessmentTeam team = assessmentTeamRepository.findById(teamId)
                .orElseThrow(() -> new DataNotFound("队伍不存在"));

        AssessmentTeam disbandedTeam = assessmentTeamDomainService.prepareDisband(userId, team);
        cleanupTeamAnswers(teamId);
        assessmentTeamRepository.save(disbandedTeam);

        log.info("解散队伍成功，teamId: {}, leaderId: {}", teamId, userId);
    }

    private void cleanupTeamAnswers(Long teamId) {
        List<Long> answerIds = assessmentAnswerRepository.findAnswerIdsByTeamId(teamId);
        if (!answerIds.isEmpty()) {
            assessmentJudgementRepository.deleteByAnswerIds(answerIds);
        }
        assessmentAnswerRepository.deleteByTeamId(teamId);
    }

    private TeamResult toTeamResult(AssessmentTeam team) {
        List<AssessmentTeamMember> members = assessmentTeamRepository.findMembersByTeamId(team.getId());
        List<TeamResult.TeamMemberResult> memberResults = new ArrayList<>();

        for (AssessmentTeamMember member : members) {
            Optional<User> userOpt = userRepository.findById(member.getUserId());
            String username = userOpt.map(User::getUsername).orElse("未知用户");
            String direction = userOpt.map(u -> u.getDirection() != null ? u.getDirection().getDescription() : null)
                    .orElse(null);
            Long avatarFileId = userOpt.map(User::getAvatarId).orElse(null);

            memberResults.add(
                    new TeamResult.TeamMemberResult(
                            member.getId(),
                            member.getUserId(),
                            username,
                            direction,
                            avatarFileId,
                            member.getJoinedAt(),
                            member.getUserId().equals(team.getLeaderId())));
        }

        return new TeamResult(
                team.getId(),
                team.getAssessmentTimeId(),
                team.getLeaderId(),
                team.getName(),
                team.getInviteCode(),
                team.getStatus(),
                team.getCreatedAt(),
                memberResults);
    }
}
