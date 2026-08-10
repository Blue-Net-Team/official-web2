package com.bluenet.web.infrastructure.repository.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bluenet.web.infrastructure.repository.dataobject.AssessmentTeamDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AssessmentTeamMapper extends BaseMapper<AssessmentTeamDO> {

    AssessmentTeamDO selectByInviteCode(@Param("inviteCode") String inviteCode);

    AssessmentTeamDO selectByAssessmentTimeIdAndUserId(@Param("assessmentTimeId") Long assessmentTimeId,
            @Param("userId") Long userId);

    List<Long> selectMemberUserIdsByTeamId(@Param("teamId") Long teamId);
}
