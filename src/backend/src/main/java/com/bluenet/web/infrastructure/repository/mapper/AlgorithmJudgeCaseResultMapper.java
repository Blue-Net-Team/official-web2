package com.bluenet.web.infrastructure.repository.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bluenet.web.domain.model.entity.AlgorithmJudgeCaseResult;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface AlgorithmJudgeCaseResultMapper extends BaseMapper<AlgorithmJudgeCaseResult> {
    @Select("SELECT * FROM tb_algorithm_judge_case_result WHERE judge_job_id = #{judgeJobId} ORDER BY case_no ASC, id ASC")
    List<AlgorithmJudgeCaseResult> selectByJudgeJobId(@Param("judgeJobId") Long judgeJobId);
}
