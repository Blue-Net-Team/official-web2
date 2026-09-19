package com.bluenet.web.infrastructure.repository.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bluenet.web.domain.repository.LearningPathRepository;
import com.bluenet.web.infrastructure.repository.dataobject.DirectionLearningStepDO;
import com.bluenet.web.domain.model.enumerate.Direction;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 学习路径Mapper接口
 * <p>
 * 负责学习路径数据的数据库操作
 * </p>
 */
@Mapper
public interface LearningPathMapper extends BaseMapper<DirectionLearningStepDO> {
    /**
     * 按条件查询学习路径 数据行。
     *
     * @param direction
     *            技术方向过滤条件。
     * @return 满足条件的学习路径 结果集合。
     */
    List<DirectionLearningStepDO> selectByDirection(@Param("direction") Direction direction);

    /**
     * 查询学习路径 数据行。
     *
     * @param id
     *            业务记录主键。
     * @return 匹配条件的学习路径 数据行；不存在时为 null。
     */
    DirectionLearningStepDO selectLearningStepById(@Param("id") Long id);

    /**
     * 查询某方向当前最大排序值。
     *
     * @param direction
     *            技术方向过滤条件。
     * @return 当前最大排序值；该方向尚无步骤时为 null。
     */
    Integer selectMaxSortOrder(@Param("direction") Direction direction);

    /**
     * 批量更新学习步骤排序值。
     * <p>
     * 使用单条 {@code CASE WHEN} SQL 一次性更新多条记录，并限定在同一方向内。
     * </p>
     *
     * @param direction
     *            技术方向过滤条件。
     * @param items
     *            排序项列表（id 与目标排序值）。
     */
    void batchUpdateSortOrder(
            @Param("direction") Direction direction,
            @Param("items") List<LearningPathRepository.SortItem> items);
}
