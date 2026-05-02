package com.bluenet.web.infrastructure.repository.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bluenet.web.infrastructure.repository.dataobject.JudgeProblemConfigDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface JudgeProblemConfigMapper extends BaseMapper<JudgeProblemConfigDO> {
    /**
     * 新增或更新题目的当前判题配置，并返回配置主键。
     *
     * @param config
     *            判题配置数据对象。
     * @return 当前配置主键。
     */
    Long upsertCurrentConfig(@Param("config") JudgeProblemConfigDO config);

    /**
     * 根据题目查询当前判题配置。
     *
     * @param questionId
     *            算法题目主键。
     * @return 当前判题配置；不存在时为 null。
     */
    JudgeProblemConfigDO selectByQuestionId(@Param("questionId") Long questionId);

    /**
     * 根据题目查询当前判题配置主键。
     *
     * @param questionId
     *            算法题目主键。
     * @return 当前判题配置主键；不存在时为 null。
     */
    Long selectIdByQuestionId(@Param("questionId") Long questionId);

    /**
     * 更新当前配置的 manifest 对象信息。
     *
     * @param configId
     *            判题配置主键。
     * @param manifestObjectKey
     *            manifest OSS 对象键。
     * @param manifestObjectHash
     *            manifest SHA-256 哈希。
     * @return 无返回值。
     */
    void updateManifest(
            @Param("configId") Long configId,
            @Param("manifestObjectKey") String manifestObjectKey,
            @Param("manifestObjectHash") String manifestObjectHash);

    /**
     * 将当前配置标记为测试数据生成中。
     *
     * @param configId
     *            判题配置主键。
     * @return 无返回值。
     */
    void markGenerating(@Param("configId") Long configId);

    /**
     * 如果当前配置已经完成测试数据生成，则标记为可正式判题。
     *
     * @param configId
     *            判题配置主键。
     * @return 无返回值。
     */
    void markReadyIfGenerated(@Param("configId") Long configId);

    /**
     * 根据题目删除判题配置及其级联子表记录。
     *
     * @param questionId
     *            算法题目主键。
     * @return 无返回值。
     */
    void deleteByQuestionId(@Param("questionId") Long questionId);
}
