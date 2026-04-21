package com.bluenet.web.domain.repository;

import com.bluenet.web.domain.model.entity.AlgorithmJudgeJob;
import com.bluenet.web.domain.model.vo.AlgorithmJudgeJobVO;

import java.util.Optional;

public interface AlgorithmJudgeJobRepository {
    /**
     * 保存新的算法评测任务 记录。
     *
     * @param job
     *            算法评测任务领域对象。
     */
    void save(AlgorithmJudgeJob job);

    /**
     * 按主键查询算法评测任务 记录。
     *
     * @param id
     *            业务记录主键。
     * @return 查询到的算法评测任务 结果；不存在时为空。
     */
    Optional<AlgorithmJudgeJobVO> findById(Long id);

    /**
     * 更新已有算法评测任务 记录。
     *
     * @param job
     *            算法评测任务领域对象。
     */
    void update(AlgorithmJudgeJobVO job);
}
