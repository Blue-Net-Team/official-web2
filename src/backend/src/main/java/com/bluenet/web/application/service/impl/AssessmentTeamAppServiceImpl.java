package com.bluenet.web.application.service.impl;

import com.bluenet.web.application.TeamPreviewResult;
import com.bluenet.web.application.TeamResult;
import com.bluenet.web.application.service.AssessmentTeamAppService;
import com.bluenet.web.domain.exception.BadRequest;
import com.bluenet.web.domain.exception.DataNotFound;
import com.bluenet.web.domain.exception.Forbidden;
import com.bluenet.web.domain.model.entity.AssessmentTeam;
import com.bluenet.web.domain.model.entity.AssessmentTeamMember;
import com.bluenet.web.domain.model.entity.AssessmentTime;
import com.bluenet.web.domain.model.entity.User;
import com.bluenet.web.domain.repository.AssessmentAnswerRepository;
import com.bluenet.web.domain.repository.AssessmentJudgementRepository;
import com.bluenet.web.domain.repository.AssessmentTeamRepository;
import com.bluenet.web.domain.repository.AssessmentTimeRepository;
import com.bluenet.web.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
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

    private static final String INVITE_CODE_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int INVITE_CODE_LENGTH = 6;
    private final SecureRandom secureRandom = new SecureRandom();

    @Override
    @Transactional
    public TeamResult createTeam(Long userId, Long assessmentTimeId, String name) {
        AssessmentTime assessmentTime = assessmentTimeRepository.findById(assessmentTimeId)
                .orElseThrow(() -> new DataNotFound("考核时间不存在"));

        if (!Boolean.TRUE.equals(assessmentTime.getAllowTeam())) {
            throw new BadRequest("该考核不允许组队");
        }

        validateTimeNotEnded(assessmentTime);

        if (assessmentTeamRepository.existsByAssessmentTimeIdAndUserId(assessmentTimeId, userId)) {
            throw new BadRequest("您已加入该考核的队伍");
        }

        if (hasPersonalAnswer(assessmentTimeId, userId)) {
            throw new BadRequest("您已提交过个人答案，无法创建队伍");
        }

        String inviteCode = generateInviteCode();
        while (assessmentTeamRepository.findByInviteCode(inviteCode).isPresent()) {
            inviteCode = generateInviteCode();
        }

        AssessmentTeam team = AssessmentTeam.create(assessmentTimeId, userId, name, inviteCode);
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

        validateTimeNotEnded(assessmentTime);

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

        if (!team.isActive()) {
            throw new BadRequest("该队伍已解散");
        }

        AssessmentTime assessmentTime = assessmentTimeRepository.findById(team.getAssessmentTimeId())
                .orElseThrow(() -> new DataNotFound("考核时间不存在"));

        validateTimeNotEnded(assessmentTime);

        if (assessmentTeamRepository
                .existsByAssessmentTimeIdAndUserId(team.getAssessmentTimeId(), userId)) {
            throw new BadRequest("您已加入该考核的队伍");
        }

        if (hasPersonalAnswer(team.getAssessmentTimeId(), userId)) {
            throw new BadRequest("您已提交过个人答案，无法加入队伍");
        }

        if (hasTeamAnswer(team.getAssessmentTimeId(), userId)) {
            throw new BadRequest("您已有队伍答案，无法加入其他队伍");
        }

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
        if (teamOpt.isEmpty()) {
            return null;
        }

        return toTeamResult(teamOpt.get());
    }

    @Override
    @Transactional
    public void leaveTeam(Long userId, Long teamId) {
        AssessmentTeam team = assessmentTeamRepository.findById(teamId)
                .orElseThrow(() -> new DataNotFound("队伍不存在"));

        if (!team.isActive()) {
            throw new BadRequest("该队伍已解散");
        }

        if (team.isLeader(userId)) {
            throw new Forbidden("队长不能离开队伍，请先转让队长或解散队伍");
        }

        if (!assessmentTeamRepository.isMember(teamId, userId)) {
            throw new BadRequest("您不是该队伍的成员");
        }

        if (hasTeamSubmittedAnswer(teamId)) {
            throw new Forbidden("队伍已提交答案，无法退出");
        }

        assessmentTeamRepository.removeMember(teamId, userId);

        log.info("用户离开队伍成功，teamId: {}, userId: {}", teamId, userId);
    }

    @Override
    @Transactional
    public TeamResult transferLeader(Long userId, Long teamId, Long newLeaderId) {
        AssessmentTeam team = assessmentTeamRepository.findById(teamId)
                .orElseThrow(() -> new DataNotFound("队伍不存在"));

        if (!team.isActive()) {
            throw new BadRequest("该队伍已解散");
        }

        if (!team.isLeader(userId)) {
            throw new Forbidden("只有队长可以转让队长");
        }

        if (hasTeamSubmittedAnswer(teamId)) {
            throw new Forbidden("队伍已提交答案，无法转让队长");
        }

        if (!assessmentTeamRepository.isMember(teamId, newLeaderId)) {
            throw new BadRequest("新队长必须是队伍成员");
        }

        team.updateLeader(newLeaderId);
        assessmentTeamRepository.updateLeader(teamId, newLeaderId);

        log.info("转让队长成功，teamId: {}, newLeaderId: {}", teamId, newLeaderId);

        AssessmentTeam updatedTeam = assessmentTeamRepository.findById(teamId)
                .orElseThrow(() -> new DataNotFound("队伍不存在"));
        return toTeamResult(updatedTeam);
    }

    @Override
    @Transactional
    public void disbandTeam(Long userId, Long teamId) {
        AssessmentTeam team = assessmentTeamRepository.findById(teamId)
                .orElseThrow(() -> new DataNotFound("队伍不存在"));

        if (!team.isLeader(userId)) {
            throw new Forbidden("只有队长可以解散队伍");
        }

        if (hasTeamSubmittedAnswer(teamId)) {
            throw new Forbidden("队伍已提交答案，无法解散");
        }

        // Clean up answers and judgements before disbanding
        cleanupTeamAnswers(teamId);

        team.disband();
        assessmentTeamRepository.update(team);

        log.info("解散队伍成功，teamId: {}, leaderId: {}", teamId, userId);
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
