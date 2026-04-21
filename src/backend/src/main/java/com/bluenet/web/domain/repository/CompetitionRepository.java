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
     * 按展示排序查询指定数量的竞赛视图。
     *
     * @param limit
     *            最大返回数量。
     * @return 竞赛简要信息列表
     */
    List<CompetitionVO> findCompetitionsWithLimit(int limit);

    /**
     * 按展示排序分页查询竞赛视图。
     *
     * @param pageable
     *            Spring 分页请求对象。
     * @return 分页后的竞赛 结果。
     */
    Page<CompetitionVO> findCompetitionsPage(Pageable pageable);

    /**
     * 保存新的竞赛 记录。
     *
     * @param competition
     *            竞赛领域对象。
     * @return 新记录的主键。
     */
    Long save(com.bluenet.web.domain.model.entity.Competition competition);

    /**
     * 更新已有竞赛 记录。
     *
     * @param competition
     *            竞赛领域对象。
     */
    void update(com.bluenet.web.domain.model.entity.Competition competition);

    /**
     * 删除指定竞赛 记录。
     *
     * @param id
     *            业务记录主键。
     */
    void deleteById(Long id);

    /**
     * 判断是否存在满足条件的竞赛 记录。
     *
     * @param id
     *            业务记录主键。
     * @return 满足条件时返回 true，否则返回 false。
     */
    boolean existsById(Long id);

    /**
     * 查询符合条件的竞赛 记录。
     *
     * @return 转换后的目标模型对象。
     */
    Integer findMaxSortOrder();

    /**
     * 批量更新竞赛 展示排序值。
     *
     * @param sortItems
     *            需要更新排序的条目集合。
     */
    void batchUpdateSortOrder(List<SortItem> sortItems);

    /**
     * 按主键查询竞赛 记录。
     *
     * @param id
     *            业务记录主键。
     * @return 查询或处理得到的竞赛 结果。
     */
    com.bluenet.web.domain.model.entity.Competition findById(Long id);

    /**
     * 查询当前竞赛相邻位置的竞赛记录，用于排序调整。
     *
     * @param sortOrder
     *            展示排序值。
     * @param direction
     *            技术方向过滤条件。
     * @return 查询或处理得到的竞赛 结果。
     */
    com.bluenet.web.domain.model.entity.Competition findAdjacent(Integer sortOrder, String direction);

    /**
     * 排序项记录
     */
    record SortItem(Long id, Integer sortOrder) {
    }
}
