package com.bluenet.web.domain.repository;

import com.bluenet.web.domain.model.vo.CompetitionVO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * 竞赛仓库接口
 * <p>
 * 负责竞赛数据的持久化操作，包括增删改查等基本操作
 * </p>
 */
public interface CompetitionRepository {
    /**
     * 查询竞赛列表（按排序号升序）
     *
     * @param limit
     *            限制返回数量，如果为0则返回全部
     * @return 竞赛简要信息列表
     */
    List<CompetitionVO> findCompetitionsWithLimit(int limit);

    /**
     * 分页查询竞赛列表
     *
     * @param pageable
     *            分页参数
     * @return 竞赛分页数据
     */
    Page<CompetitionVO> findCompetitionsPage(Pageable pageable);

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

    /**
     * 查询当前最大的排序号
     *
     * @return 最大排序号，若无记录返回null
     */
    Integer findMaxSortOrder();

    /**
     * 批量更新竞赛排序号
     *
     * @param sortItems
     *            竞赛ID与新排序号的列表
     */
    void batchUpdateSortOrder(List<SortItem> sortItems);

    /**
     * 根据ID查询竞赛
     *
     * @param id
     *            竞赛ID
     * @return 竞赛实体，不存在返回null
     */
    com.bluenet.web.domain.model.entity.Competition findById(Long id);

    /**
     * 查找排序号相邻的竞赛
     *
     * @param sortOrder
     *            当前排序号
     * @param direction
     *            方向：UP 查找比当前小的最大值，DOWN 查找比当前大的最小值
     * @return 相邻的竞赛实体，不存在返回null
     */
    com.bluenet.web.domain.model.entity.Competition findAdjacent(Integer sortOrder, String direction);

    /**
     * 排序项记录
     */
    record SortItem(Long id, Integer sortOrder) {
    }
}
