package com.bluenet.web.domain.repository;

import com.bluenet.web.domain.model.entity.JudgeTestcaseConfig;

import java.util.List;

/**
 * 算法题测试用例生成配置仓储接口。
 */
public interface JudgeTestcaseConfigRepository {
    /**
     * 查询某个判题配置下的全部测试用例生成配置。
     *
     * @param configId
     *            判题配置主键。
     * @return 测试用例生成配置领域实体列表。
     */
    List<JudgeTestcaseConfig> findByConfigId(Long configId);

    /**
     * 删除某个判题配置下的全部测试用例生成配置。
     *
     * @param configId
     *            判题配置主键。
     */
    void deleteByConfigId(Long configId);

    /**
     * 保存测试用例生成配置。
     *
     * @param config
     *            测试用例生成配置领域实体。
     */
    void save(JudgeTestcaseConfig config);
}
