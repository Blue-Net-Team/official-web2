package com.bluenet.web.application.service;

import com.bluenet.web.api.dto.learningpath.CreateLearningStepRequestDTO;
import com.bluenet.web.api.dto.learningpath.DirectionLearningPathDTO;
import com.bluenet.web.api.dto.learningpath.LearningStepDTO;
import com.bluenet.web.api.dto.learningpath.UpdateLearningStepRequestDTO;

/**
 * 学习路径应用服务接口
 * <p>
 * 提供学习路径相关的应用层服务，协调领域服务完成业务操作
 * </p>
 */
public interface LearningPathService {
    /**
     * 获取指定方向的学习路径
     *
     * @param slug
     *            方向标识（cv/embed/struct）
     * @return 方向学习路径DTO
     */
    DirectionLearningPathDTO getLearningPath(String slug);

    /**
     * 创建学习步骤
     *
     * @param slug
     *            方向标识
     * @param request
     *            创建请求DTO
     * @return 创建后的学习步骤DTO
     */
    LearningStepDTO createStep(String slug, CreateLearningStepRequestDTO request);

    /**
     * 更新学习步骤
     *
     * @param id
     *            步骤ID
     * @param request
     *            更新请求DTO
     * @return 更新后的学习步骤DTO
     */
    LearningStepDTO updateStep(Long id, UpdateLearningStepRequestDTO request);

    /**
     * 删除学习步骤
     *
     * @param id
     *            步骤ID
     */
    void deleteStep(Long id);
}
