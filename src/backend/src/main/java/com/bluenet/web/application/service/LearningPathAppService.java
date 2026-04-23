package com.bluenet.web.application.service;

import com.bluenet.web.application.LearningPathResult;
import com.bluenet.web.application.command.learningpath.LearningPathCommands;

import java.util.List;

/**
 * 学习路径应用服务接口。
 * <p>
 * 定义了学习路径聚合在应用层的所有业务操作。
 * </p>
 */
public interface LearningPathAppService {
    /**
     * 获取指定方向的学习路径
     *
     * @param slug
     *            方向标识（cv/embed/struct）
     * @return 学习步骤结果列表
     */
    List<LearningPathResult> getLearningPath(String slug);

    /**
     * 创建学习步骤
     *
     * @param command
     *            创建命令
     * @return 创建后的学习步骤结果
     */
    LearningPathResult createStep(LearningPathCommands.CreateLearningStepCommand command);

    /**
     * 更新学习步骤
     *
     * @param command
     *            更新命令
     * @return 更新后的学习步骤结果
     */
    LearningPathResult updateStep(LearningPathCommands.UpdateLearningStepCommand command);

    /**
     * 删除学习步骤
     *
     * @param id
     *            步骤ID
     */
    void deleteStep(Long id);
}
