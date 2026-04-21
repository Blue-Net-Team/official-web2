package com.bluenet.web.infrastructure.repository.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
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
     * 判断是否存在满足条件的学习路径 记录。
     *
     * @param direction
     *            技术方向过滤条件。
     * @param stepNumber
     *            学习路径步骤序号。
     * @param excludeId
     *            需要排除的当前记录主键。
     * @return 满足条件时返回 true，否则返回 false。
     */
    boolean existsByDirectionAndStepNumber(
            @Param("direction") Direction direction,
            @Param("stepNumber") Integer stepNumber,
            @Param("excludeId") Long excludeId);
}
