package com.bluenet.web.infrastructure.repository.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bluenet.web.domain.model.entity.DirectionLearningStep;
import com.bluenet.web.domain.model.enumerate.Direction;
import com.bluenet.web.domain.model.vo.LearningStepVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

/**
 * 学习路径Mapper接口
 * <p>
 * 负责学习路径数据的数据库操作
 * </p>
 */
@Mapper
public interface LearningPathMapper extends BaseMapper<DirectionLearningStep> {
    /**
     * 根据方向查询学习步骤列表
     *
     * @param direction
     *            方向
     * @return 学习步骤列表
     */
    List<LearningStepVO> selectByDirection(@Param("direction") Direction direction);

    /**
     * 根据ID查询学习步骤
     *
     * @param id
     *            步骤ID
     * @return 学习步骤
     */
    Optional<LearningStepVO> selectById(@Param("id") Long id);

    /**
     * 检查同一方向内步骤序号是否已存在
     *
     * @param direction
     *            方向
     * @param stepNumber
     *            步骤序号
     * @param excludeId
     *            排除的步骤ID
     * @return 如果存在返回true，否则返回false
     */
    boolean existsByDirectionAndStepNumber(
            @Param("direction") Direction direction,
            @Param("stepNumber") Integer stepNumber,
            @Param("excludeId") Long excludeId);
}
