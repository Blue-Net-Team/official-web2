package com.bluenet.web.domain.repository;

import com.bluenet.web.domain.model.entity.JudgeProblemConfig;

import java.util.Optional;

/**
 * 算法题判题配置仓储接口。
 */
public interface JudgeProblemConfigRepository {
    /**
     * 新增或替换题目的当前判题配置，并返回配置主键。
     *
     * @param config
     *            判题配置领域实体。
     * @return 当前配置主键。
     */
    Long upsertCurrentConfig(JudgeProblemConfig config);

    /**
     * 根据题目查询当前判题配置。
     *
     * @param questionId
     *            算法题目主键。
     * @return 当前判题配置；不存在时为空。
     */
    Optional<JudgeProblemConfig> findByQuestionId(Long questionId);

    /**
     * 根据题目查询当前判题配置主键。
     *
     * @param questionId
     *            算法题目主键。
     * @return 当前判题配置主键；不存在时为空。
     */
    Optional<Long> findIdByQuestionId(Long questionId);

    /**
     * 更新当前配置的 manifest 对象信息。
     *
     * @param configId
     *            判题配置主键。
     * @param manifestObjectKey
     *            manifest OSS 对象键。
     * @param manifestObjectHash
     *            manifest SHA-256 哈希。
     */
    void updateManifest(Long configId, String manifestObjectKey, String manifestObjectHash);

    /**
     * 将当前配置标记为测试数据生成中。
     *
     * @param configId
     *            判题配置主键。
     */
    void markGenerating(Long configId);

    /**
     * 如果当前配置已经完成测试数据生成，则标记为可正式判题。
     *
     * @param configId
     *            判题配置主键。
     */
    void markReadyIfGenerated(Long configId);

    /**
     * 根据题目删除判题配置。
     *
     * @param questionId
     *            算法题目主键。
     */
    void deleteByQuestionId(Long questionId);
}
