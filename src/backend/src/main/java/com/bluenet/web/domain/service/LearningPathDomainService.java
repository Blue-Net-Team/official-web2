package com.bluenet.web.domain.service;

import com.bluenet.web.domain.model.enumerate.Direction;
import com.bluenet.web.domain.model.vo.LearningStepVO;

import java.util.List;
import java.util.Optional;

/**
 * 学习路径领域服务接口
 * <p>
 * 提供学习路径相关的业务逻辑操作
 * </p>
 */
public interface LearningPathDomainService {
    /**
     * 获取指定方向的学习路径
     *
     * @param direction
     *            方向
     * @return 学习步骤列表，按步骤序号升序排列
     */
    List<LearningStepVO> getLearningPath(Direction direction);

    /**
     * 根据ID获取学习步骤
     *
     * @param id
     *            步骤ID
     * @return 学习步骤，如果不存在则返回Optional.empty()
     */
    Optional<LearningStepVO> getStepById(Long id);

    /**
     * 创建学习步骤
     *
     * @param direction
     *            方向
     * @param stepNumber
     *            步骤序号
     * @param title
     *            步骤标题
     * @param videoUrl
     *            视频链接URL
     * @return 创建后的步骤ID
     */
    Long createStep(Direction direction, Integer stepNumber, String title, String videoUrl);

    /**
     * 更新学习步骤
     *
     * @param id
     *            步骤ID
     * @param stepNumber
     *            步骤序号
     * @param title
     *            步骤标题
     * @param videoUrl
     *            视频链接URL
     */
    void updateStep(Long id, Integer stepNumber, String title, String videoUrl);

    /**
     * 删除学习步骤
     *
     * @param id
     *            步骤ID
     */
    void deleteStep(Long id);

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
