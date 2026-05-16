package com.bluenet.web.infrastructure.repository.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bluenet.web.infrastructure.repository.dataobject.AssessmentTeamMemberDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AssessmentTeamMemberMapper extends BaseMapper<AssessmentTeamMemberDO> {

    List<AssessmentTeamMemberDO> selectByTeamId(@Param("teamId") Long teamId);

    int deleteByTeamIdAndUserId(@Param("teamId") Long teamId, @Param("userId") Long userId);

    boolean existsByTeamIdAndUserId(@Param("teamId") Long teamId, @Param("userId") Long userId);
}
