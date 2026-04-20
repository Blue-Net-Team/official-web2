package com.bluenet.web.infrastructure.repository.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bluenet.web.domain.model.entity.AssessmentDecision;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AssessmentDecisionMapper extends BaseMapper<AssessmentDecision> {
    /**
     * 查询考生在指定考核时间下的最终决策。
     */
    AssessmentDecision selectByUserIdAndAssessmentTimeId(@Param("userId") Long userId,
            @Param("assessmentTimeId") Long assessmentTimeId);

    /**
     * 查询指定考核时间下的全部录用决策。
     */
    List<AssessmentDecision> selectByAssessmentTimeId(@Param("assessmentTimeId") Long assessmentTimeId);
}
