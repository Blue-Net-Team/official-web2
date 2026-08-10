package com.bluenet.web.domain.repository;

import com.bluenet.web.domain.model.entity.DirectionLearningStep;
import com.bluenet.web.domain.model.enumerate.Direction;

import java.util.List;
import java.util.Optional;

/**
 * 学习路径仓库接口
 * <p>
 * 负责学习路径数据的持久化操作，只操作 Entity，不暴露 VO 或 DTO
 * </p>
 */
public interface LearningPathRepository {
    /**
     * 按技术方向查询学习路径记录。
     *
     * @param direction
     *            技术方向过滤条件。
     * @return 满足条件的学习路径实体集合。
     */
    List<DirectionLearningStep> findByDirection(Direction direction);

    /**
     * 按主键查询学习路径记录。
     *
     * @param id
     *            业务记录主键。
     * @return 查询到的学习路径实体；不存在时为 Optional.empty()。
     */
    Optional<DirectionLearningStep> findById(Long id);

    /**
     * 保存新的学习路径记录。
     *
     * @param step
     *            学习路径步骤实体。
     */
    void save(DirectionLearningStep step);

    /**
     * 删除指定学习路径记录。
     *
     * @param id
     *            业务记录主键。
     */
    void deleteById(Long id);

    /**
     * 判断是否存在满足条件的学习路径记录。
     *
     * @param id
     *            业务记录主键。
     * @return 满足条件时返回 true，否则返回 false。
     */
    boolean existsById(Long id);

    /**
     * 判断是否存在满足条件的学习路径记录。
     *
     * @param direction
     *            技术方向过滤条件。
     * @param stepNumber
     *            学习路径步骤序号。
     * @param excludeId
     *            需要排除的当前记录主键。
     * @return 满足条件时返回 true，否则返回 false。
     */
    boolean existsByDirectionAndStepNumber(Direction direction, Integer stepNumber, Long excludeId);
}
