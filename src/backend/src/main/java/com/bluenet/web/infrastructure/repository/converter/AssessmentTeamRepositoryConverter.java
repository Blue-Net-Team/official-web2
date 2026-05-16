package com.bluenet.web.infrastructure.repository.converter;

import com.bluenet.web.domain.model.entity.AssessmentTeam;
import com.bluenet.web.domain.model.entity.AssessmentTeamMember;
import com.bluenet.web.infrastructure.repository.dataobject.AssessmentTeamDO;
import com.bluenet.web.infrastructure.repository.dataobject.AssessmentTeamMemberDO;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 考核队伍仓储转换器
 * <p>
 * 负责 AssessmentTeam 的 DO 与 Entity 之间的显式字段映射
 * </p>
 */
@Component
public class AssessmentTeamRepositoryConverter {

    public AssessmentTeamDO toDataObject(AssessmentTeam entity) {
        if (entity == null) {
            return null;
        }
        return AssessmentTeamDO.builder()
                .id(entity.getId())
                .assessmentTimeId(entity.getAssessmentTimeId())
                .leaderId(entity.getLeaderId())
                .name(entity.getName())
                .inviteCode(entity.getInviteCode())
                .status(entity.getStatus() != null ? entity.getStatus().name() : null)
                .createdAt(entity.getCreatedAt())
                .build();
    }

    public AssessmentTeam toEntity(AssessmentTeamDO dataObject) {
        if (dataObject == null) {
            return null;
        }
        return AssessmentTeam.reconstruct(
                dataObject.getId(),
                dataObject.getAssessmentTimeId(),
                dataObject.getLeaderId(),
                dataObject.getName(),
                dataObject.getInviteCode(),
                dataObject.getStatus() != null ? AssessmentTeam.TeamStatus.valueOf(dataObject.getStatus()) : null,
                dataObject.getCreatedAt());
    }

    public AssessmentTeamMemberDO toMemberDataObject(AssessmentTeamMember entity) {
        if (entity == null) {
            return null;
        }
        return AssessmentTeamMemberDO.builder()
                .id(entity.getId())
                .teamId(entity.getTeamId())
                .userId(entity.getUserId())
                .joinedAt(entity.getJoinedAt())
                .build();
    }

    public AssessmentTeamMember toMemberEntity(AssessmentTeamMemberDO dataObject) {
        if (dataObject == null) {
            return null;
        }
        return AssessmentTeamMember.reconstruct(
                dataObject.getId(),
                dataObject.getTeamId(),
                dataObject.getUserId(),
                dataObject.getJoinedAt());
    }

    public List<AssessmentTeamMember> toMemberEntityList(List<AssessmentTeamMemberDO> dataObjects) {
        if (dataObjects == null) {
            return List.of();
        }
        return dataObjects.stream()
                .map(this::toMemberEntity)
                .toList();
    }
}
