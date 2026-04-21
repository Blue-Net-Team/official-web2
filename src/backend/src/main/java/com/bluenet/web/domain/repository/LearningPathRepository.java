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
     * 按技术方向查询学习路径 记录。
     *
     * @param direction
     *            技术方向过滤条件。
     * @return 满足条件的学习路径 结果集合。
     */
    List<LearningStepVO> findByDirection(Direction direction);

    /**
     * 处理学习路径 仓储职责中的业务数据访问逻辑。
     *
     * @param id
     *            业务记录主键。
     * @return 学习步骤，如果不存在则返回Optional.empty()
     */
    Optional<LearningStepVO> findById(Long id);

    /**
     * 保存新的学习路径 记录。
     *
     * @param step
     *            学习路径步骤领域对象。
     * @return 新记录的主键。
     */
    Long save(com.bluenet.web.domain.model.entity.DirectionLearningStep step);

    /**
     * 更新已有学习路径 记录。
     *
     * @param step
     *            学习路径步骤领域对象。
     */
    void update(com.bluenet.web.domain.model.entity.DirectionLearningStep step);

    /**
     * 删除指定学习路径 记录。
     *
     * @param id
     *            业务记录主键。
     */
    void deleteById(Long id);

    /**
     * 判断是否存在满足条件的学习路径 记录。
     *
     * @param id
     *            业务记录主键。
     * @return 满足条件时返回 true，否则返回 false。
     */
    boolean existsById(Long id);

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
    boolean existsByDirectionAndStepNumber(Direction direction, Integer stepNumber, Long excludeId);
}
