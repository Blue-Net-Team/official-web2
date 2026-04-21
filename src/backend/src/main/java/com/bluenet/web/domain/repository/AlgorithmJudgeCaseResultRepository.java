package com.bluenet.web.domain.repository;

import com.bluenet.web.domain.model.vo.AlgorithmJudgeCaseResultVO;

import java.util.List;

public interface AlgorithmJudgeCaseResultRepository {
    /**
     * 处理算法评测用例结果 仓储职责中的业务数据访问逻辑。
     *
     * @param results
     *            算法评测用例结果集合。
     */
    void saveAll(List<AlgorithmJudgeCaseResultVO> results);

    /**
     * 查询符合条件的算法评测用例结果 记录。
     *
     * @param judgeJobId
     *            算法评测任务主键。
     * @return 满足条件的算法评测用例结果 结果集合。
     */
    List<AlgorithmJudgeCaseResultVO> findByJudgeJobId(Long judgeJobId);
}
