package com.bluenet.web.infrastructure.repository.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bluenet.web.domain.model.entity.AssessmentDecision;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface AssessmentDecisionMapper extends BaseMapper<AssessmentDecision> {
    @Select("SELECT * FROM tb_assessment_decision WHERE user_id = #{userId} AND assessment_time_id = #{assessmentTimeId} LIMIT 1")
    AssessmentDecision selectByUserIdAndAssessmentTimeId(@Param("userId") Long userId,
            @Param("assessmentTimeId") Long assessmentTimeId);
}
