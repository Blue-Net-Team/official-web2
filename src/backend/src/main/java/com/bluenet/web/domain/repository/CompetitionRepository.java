package com.bluenet.web.domain.repository;

import com.bluenet.web.domain.model.vo.CompetitionVO;

import java.util.List;

/**
 * 竞赛仓库接口
 * <p>
 * 负责竞赛数据的持久化操作，包括增删改查等基本操作
 * </p>
 */
public interface CompetitionRepository {
    /**
     * 查询竞赛列表（按排序号降序）
     *
     * @param limit
     *            限制返回数量，如果为0则返回全部
     * @return 竞赛简要信息列表
     */
    List<CompetitionVO> findCompetitionsWithLimit(int limit);

    /**
     * 保存竞赛
     *
     * @param competition
     *            竞赛实体
     * @return 保存后的竞赛ID
     */
    Long save(com.bluenet.web.domain.model.entity.Competition competition);

    /**
     * 更新竞赛
     *
     * @param competition
     *            竞赛实体
     */
    void update(com.bluenet.web.domain.model.entity.Competition competition);

    /**
     * 根据ID删除竞赛
     *
     * @param id
     *            竞赛ID
     */
    void deleteById(Long id);

    /**
     * 检查竞赛是否存在
     *
     * @param id
     *            竞赛ID
     * @return 如果存在返回true，否则返回false
     */
    boolean existsById(Long id);
}
