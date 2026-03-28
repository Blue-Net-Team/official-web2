package com.bluenet.web.domain.repository;

import com.bluenet.web.domain.model.enumerate.Direction;
import com.bluenet.web.domain.model.vo.LearningStepVO;

import java.util.List;
import java.util.Optional;

/**
 * 学习路径仓库接口
 * <p>
 * 负责学习路径数据的持久化操作
 * </p>
 */
public interface LearningPathRepository {
    /**
     * 根据方向查询学习步骤列表
     *
     * @param direction
     *            方向
     * @return 学习步骤列表，按步骤序号升序排列
     */
    List<LearningStepVO> findByDirection(Direction direction);

    /**
     * 根据ID查询学习步骤
     *
     * @param id
     *            步骤ID
     * @return 学习步骤，如果不存在则返回Optional.empty()
     */
    Optional<LearningStepVO> findById(Long id);

    /**
     * 保存学习步骤
     *
     * @param step
     *            学习步骤实体
     * @return 保存后的步骤ID
     */
    Long save(com.bluenet.web.domain.model.entity.DirectionLearningStep step);

    /**
     * 更新学习步骤
     *
     * @param step
     *            学习步骤实体
     */
    void update(com.bluenet.web.domain.model.entity.DirectionLearningStep step);

    /**
     * 根据ID删除学习步骤
     *
     * @param id
     *            步骤ID
     */
    void deleteById(Long id);

    /**
     * 检查学习步骤是否存在
     *
     * @param id
     *            步骤ID
     * @return 如果存在返回true，否则返回false
     */
    boolean existsById(Long id);

    /**
     * 检查同一方向内步骤序号是否已存在
     *
     * @param direction
     *            方向
     * @param stepNumber
     *            步骤序号
     * @param excludeId
     *            排除的步骤ID（用于更新时排除自身）
     * @return 如果存在返回true，否则返回false
     */
    boolean existsByDirectionAndStepNumber(Direction direction, Integer stepNumber, Long excludeId);
}
