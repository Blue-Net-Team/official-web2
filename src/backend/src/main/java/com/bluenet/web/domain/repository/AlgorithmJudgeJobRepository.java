package com.bluenet.web.domain.repository;

import com.bluenet.web.domain.model.entity.AlgorithmJudgeJob;

import java.util.Optional;

public interface AlgorithmJudgeJobRepository {
    /**
     * 保存新的算法评测任务记录。
     *
     * @param job
     *            算法评测任务领域对象。
     */
    void save(AlgorithmJudgeJob job);

    /**
     * 按主键查询算法评测任务记录。
     *
     * @param id
     *            业务记录主键。
     * @return 查询到的算法评测任务实体；不存在时为空。
     */
    Optional<AlgorithmJudgeJob> findById(Long id);

}
