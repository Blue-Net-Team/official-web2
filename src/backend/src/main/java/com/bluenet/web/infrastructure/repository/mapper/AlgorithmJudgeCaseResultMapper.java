package com.bluenet.web.infrastructure.repository.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bluenet.web.infrastructure.repository.dataobject.AlgorithmJudgeCaseResultDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AlgorithmJudgeCaseResultMapper extends BaseMapper<AlgorithmJudgeCaseResultDO> {
    /**
     * 按评测任务主键查询用例结果数据行列表。
     *
     * @param judgeJobId
     *            算法评测任务主键。
     * @return 满足条件的算法评测用例结果 结果集合。
     */
    List<AlgorithmJudgeCaseResultDO> selectByJudgeJobId(@Param("judgeJobId") Long judgeJobId);
}
