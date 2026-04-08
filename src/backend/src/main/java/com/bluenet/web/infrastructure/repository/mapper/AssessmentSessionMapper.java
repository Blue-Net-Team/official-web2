package com.bluenet.web.infrastructure.repository.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bluenet.web.domain.model.entity.AssessmentSession;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface AssessmentSessionMapper extends BaseMapper<AssessmentSession> {
    @Select("SELECT * FROM tb_assessment_session WHERE user_id = #{userId} AND assessment_time_id = #{assessmentTimeId} LIMIT 1")
    AssessmentSession selectByUserIdAndAssessmentTimeId(@Param("userId") Long userId,
            @Param("assessmentTimeId") Long assessmentTimeId);
}
