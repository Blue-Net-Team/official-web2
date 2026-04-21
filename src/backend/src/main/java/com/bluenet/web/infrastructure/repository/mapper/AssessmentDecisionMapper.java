package com.bluenet.web.infrastructure.repository.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bluenet.web.infrastructure.repository.dataobject.AssessmentDecisionDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AssessmentDecisionMapper extends BaseMapper<AssessmentDecisionDO> {
    /**
     * 按条件查询考核最终决策 数据行。
     *
     * @param userId
     *            用户主键，用于限定用户范围。
     * @param assessmentTimeId
     *            考核场次主键。
     * @return 匹配条件的考核最终决策 数据行；不存在时为 null。
     */
    AssessmentDecisionDO selectByUserIdAndAssessmentTimeId(@Param("userId") Long userId,
            @Param("assessmentTimeId") Long assessmentTimeId);

    /**
     * 按条件查询考核最终决策 数据行。
     *
     * @param assessmentTimeId
     *            考核场次主键。
     * @return 满足条件的考核最终决策 结果集合。
     */
    List<AssessmentDecisionDO> selectByAssessmentTimeId(@Param("assessmentTimeId") Long assessmentTimeId);
}
