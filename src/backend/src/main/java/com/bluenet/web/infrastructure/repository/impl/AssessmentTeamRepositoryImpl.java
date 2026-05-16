package com.bluenet.web.infrastructure.repository.impl;

import com.bluenet.web.domain.model.entity.AssessmentTeam;
import com.bluenet.web.domain.model.entity.AssessmentTeamMember;
import com.bluenet.web.domain.repository.AssessmentTeamRepository;
import com.bluenet.web.infrastructure.repository.converter.AssessmentTeamRepositoryConverter;
import com.bluenet.web.infrastructure.repository.dataobject.AssessmentTeamDO;
import com.bluenet.web.infrastructure.repository.dataobject.AssessmentTeamMemberDO;
import com.bluenet.web.infrastructure.repository.mapper.AssessmentTeamMapper;
import com.bluenet.web.infrastructure.repository.mapper.AssessmentTeamMemberMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 考核队伍仓库实现类
 */
@Repository
@RequiredArgsConstructor
public class AssessmentTeamRepositoryImpl implements AssessmentTeamRepository {

    private final AssessmentTeamMapper assessmentTeamMapper;
    private final AssessmentTeamMemberMapper assessmentTeamMemberMapper;
    private final AssessmentTeamRepositoryConverter converter;

    @Override
    public Optional<AssessmentTeam> findById(Long id) {
        AssessmentTeamDO dataObject = assessmentTeamMapper.selectById(id);
        return Optional.ofNullable(converter.toEntity(dataObject));
    }

    @Override
    public Optional<AssessmentTeam> findByInviteCode(String inviteCode) {
        AssessmentTeamDO dataObject = assessmentTeamMapper.selectByInviteCode(inviteCode);
        return Optional.ofNullable(converter.toEntity(dataObject));
    }

    @Override
    public Optional<AssessmentTeam> findByAssessmentTimeIdAndUserId(Long assessmentTimeId, Long userId) {
        AssessmentTeamDO dataObject = assessmentTeamMapper.selectByAssessmentTimeIdAndUserId(assessmentTimeId, userId);
        return Optional.ofNullable(converter.toEntity(dataObject));
    }

    @Override
    public boolean existsByAssessmentTimeIdAndUserId(Long assessmentTimeId, Long userId) {
        return assessmentTeamMapper.selectByAssessmentTimeIdAndUserId(assessmentTimeId, userId) != null;
    }

    @Override
    public void save(AssessmentTeam team) {
        AssessmentTeamDO dataObject = converter.toDataObject(team);
        assessmentTeamMapper.insert(dataObject);
        team.setId(dataObject.getId());

        AssessmentTeamMemberDO leaderMember = AssessmentTeamMemberDO.builder()
                .teamId(dataObject.getId())
                .userId(team.getLeaderId())
                .build();
        assessmentTeamMemberMapper.insert(leaderMember);
    }

    @Override
    public void update(AssessmentTeam team) {
        AssessmentTeamDO dataObject = converter.toDataObject(team);
        assessmentTeamMapper.updateById(dataObject);
    }

    @Override
    public void deleteById(Long id) {
        assessmentTeamMemberMapper.delete(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<AssessmentTeamMemberDO>()
                        .eq(AssessmentTeamMemberDO::getTeamId, id));
        assessmentTeamMapper.deleteById(id);
    }

    @Override
    public void updateLeader(Long teamId, Long newLeaderId) {
        assessmentTeamMapper.updateLeader(teamId, newLeaderId);
    }

    @Override
    public void addMember(Long teamId, Long userId) {
        AssessmentTeamMemberDO member = AssessmentTeamMemberDO.builder()
                .teamId(teamId)
                .userId(userId)
                .build();
        assessmentTeamMemberMapper.insert(member);
    }

    @Override
    public void removeMember(Long teamId, Long userId) {
        assessmentTeamMemberMapper.deleteByTeamIdAndUserId(teamId, userId);
    }

    @Override
    public List<AssessmentTeamMember> findMembersByTeamId(Long teamId) {
        List<AssessmentTeamMemberDO> dataObjects = assessmentTeamMemberMapper.selectByTeamId(teamId);
        return converter.toMemberEntityList(dataObjects);
    }

    @Override
    public boolean isMember(Long teamId, Long userId) {
        return assessmentTeamMemberMapper.existsByTeamIdAndUserId(teamId, userId);
    }
}
