package com.bluenet.web.domain.repository;

import com.bluenet.web.domain.model.entity.AssessmentTeam;
import com.bluenet.web.domain.model.entity.AssessmentTeamMember;

import java.util.List;
import java.util.Optional;

/**
 * 考核队伍仓库接口
 * <p>
 * 负责考核队伍数据的持久化操作
 * </p>
 */
public interface AssessmentTeamRepository {

    Optional<AssessmentTeam> findById(Long id);

    Optional<AssessmentTeam> findByInviteCode(String inviteCode);

    Optional<AssessmentTeam> findByAssessmentTimeIdAndUserId(Long assessmentTimeId, Long userId);

    boolean existsByAssessmentTimeIdAndUserId(Long assessmentTimeId, Long userId);

    void save(AssessmentTeam team);

    void update(AssessmentTeam team);

    void deleteById(Long id);

    void updateLeader(Long teamId, Long newLeaderId);

    void addMember(Long teamId, Long userId);

    void removeMember(Long teamId, Long userId);

    List<AssessmentTeamMember> findMembersByTeamId(Long teamId);

    boolean isMember(Long teamId, Long userId);
}
