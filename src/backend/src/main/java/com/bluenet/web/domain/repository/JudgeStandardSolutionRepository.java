package com.bluenet.web.domain.repository;

import com.bluenet.web.domain.model.entity.JudgeStandardSolution;

import java.util.List;

/**
 * 算法题标准解仓储接口。
 */
public interface JudgeStandardSolutionRepository {
    /**
     * 查询某个判题配置下的全部标准解。
     *
     * @param configId
     *            判题配置主键。
     * @return 标准解领域实体列表。
     */
    List<JudgeStandardSolution> findByConfigId(Long configId);

    /**
     * 删除某个判题配置下的全部标准解。
     *
     * @param configId
     *            判题配置主键。
     */
    void deleteByConfigId(Long configId);

    /**
     * 保存标准解。
     *
     * @param solution
     *            标准解领域实体。
     */
    void save(JudgeStandardSolution solution);
}
