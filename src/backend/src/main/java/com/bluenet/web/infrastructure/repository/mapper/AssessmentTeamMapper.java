package com.bluenet.web.infrastructure.repository.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bluenet.web.infrastructure.repository.dataobject.AssessmentTeamDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface AssessmentTeamMapper extends BaseMapper<AssessmentTeamDO> {

    AssessmentTeamDO selectByInviteCode(@Param("inviteCode") String inviteCode);

    AssessmentTeamDO selectByAssessmentTimeIdAndUserId(@Param("assessmentTimeId") Long assessmentTimeId,
            @Param("userId") Long userId);

    @Update("UPDATE tb_assessment_team SET leader_id = #{newLeaderId} WHERE id = #{teamId}")
    int updateLeader(@Param("teamId") Long teamId, @Param("newLeaderId") Long newLeaderId);

    List<Long> selectMemberUserIdsByTeamId(@Param("teamId") Long teamId);
}
