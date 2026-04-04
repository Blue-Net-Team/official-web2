package com.bluenet.web.infrastructure.repository.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bluenet.web.domain.model.entity.AssessmentAnswer;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface AssessmentAnswerMapper extends BaseMapper<AssessmentAnswer> {
    @Select("SELECT COUNT(*) FROM tb_assessment_answer a "
            + "JOIN tb_assessment_question q ON a.question_id = q.id "
            + "WHERE q.assessment_time_id = #{assessmentTimeId} AND a.user_id = #{userId}")
    int countByUserIdAndAssessmentTimeId(@Param("userId") Long userId,
            @Param("assessmentTimeId") Long assessmentTimeId);
}
