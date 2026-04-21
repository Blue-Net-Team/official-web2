package com.bluenet.web.infrastructure.repository.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bluenet.web.infrastructure.repository.dataobject.CompetitionDO;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CompetitionMapper extends BaseMapper<CompetitionDO> {
    /**
     * 查询竞赛 数据行。
     *
     * @param limit
     *            最大返回数量。
     * @return 满足条件的竞赛 结果集合。
     */
    List<CompetitionDO> selectCompetitionsWithLimit(@Param("limit") int limit);

    /**
     * 查询竞赛 数据行。
     *
     * @param page
     *            分页请求或 MyBatis-Plus 分页对象。
     * @return 分页后的竞赛 结果。
     */
    IPage<CompetitionDO> selectCompetitionsPage(Page<CompetitionDO> page);

    /**
     * 查询竞赛 数据行。
     *
     * @return 转换后的目标模型对象。
     */
    Integer selectMaxSortOrder();

    /**
     * 处理竞赛 仓储职责中的业务数据访问逻辑。
     *
     * @param id
     *            业务记录主键。
     * @param sortOrder
     *            展示排序值。
     */
    void updateSortOrderById(@Param("id") Long id, @Param("sortOrder") Integer sortOrder);

    /**
     * 查询竞赛 数据行。
     *
     * @param sortOrder
     *            展示排序值。
     * @return 匹配条件的竞赛 数据行；不存在时为 null。
     */
    CompetitionDO selectAdjacentUp(@Param("sortOrder") Integer sortOrder);

    /**
     * 查询竞赛 数据行。
     *
     * @param sortOrder
     *            展示排序值。
     * @return 匹配条件的竞赛 数据行；不存在时为 null。
     */
    CompetitionDO selectAdjacentDown(@Param("sortOrder") Integer sortOrder);

    /**
     * 按条件查询竞赛 数据行。
     *
     * @param names
     *            名称集合。
     * @return 满足条件的竞赛 结果集合。
     */
    List<CompetitionDO> selectByNames(@Param("names") List<String> names);
}
