package com.bluenet.web.domain.repository;

import com.bluenet.web.domain.model.entity.AlgorithmJudgeCaseResult;

import java.util.List;

public interface AlgorithmJudgeCaseResultRepository {
    /**
     * 保存算法评测用例结果集合。
     *
     * @param results
     *            算法评测用例结果实体集合。
     */
    void saveAll(List<AlgorithmJudgeCaseResult> results);

    /**
     * 查询符合条件的算法评测用例结果记录。
     *
     * @param judgeJobId
     *            算法评测任务主键。
     * @return 满足条件的算法评测用例结果实体集合。
     */
    List<AlgorithmJudgeCaseResult> findByJudgeJobId(Long judgeJobId);
}
