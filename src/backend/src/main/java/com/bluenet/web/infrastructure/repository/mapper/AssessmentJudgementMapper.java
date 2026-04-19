package com.bluenet.web.infrastructure.repository.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bluenet.web.domain.model.entity.AssessmentJudgement;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface AssessmentJudgementMapper extends BaseMapper<AssessmentJudgement> {
    @Select("SELECT * FROM tb_assessment_judgement WHERE answer_id = #{answerId} ORDER BY updated_at DESC, id DESC LIMIT 1")
    AssessmentJudgement selectLatestByAnswerId(@Param("answerId") Long answerId);

    @Select("SELECT * FROM tb_assessment_judgement WHERE question_id = #{questionId} AND user_id = #{userId} ORDER BY updated_at DESC, id DESC LIMIT 1")
    AssessmentJudgement selectLatestByQuestionIdAndUserId(@Param("questionId") Long questionId,
            @Param("userId") Long userId);

    @Select("SELECT * FROM tb_assessment_judgement WHERE question_id = #{questionId} ORDER BY user_id ASC, updated_at DESC, id DESC")
    List<AssessmentJudgement> selectAllByQuestionId(@Param("questionId") Long questionId);

    @Select("""
            SELECT DISTINCT ON (user_id) *
            FROM tb_assessment_judgement
            WHERE question_id = #{questionId}
              AND source = 'AUTO'
              AND status = 'JUDGED'
              AND result_code IS NOT NULL
            ORDER BY user_id, updated_at DESC, id DESC
            """)
    List<AssessmentJudgement> selectLatestObjectiveByQuestionId(@Param("questionId") Long questionId);
}
