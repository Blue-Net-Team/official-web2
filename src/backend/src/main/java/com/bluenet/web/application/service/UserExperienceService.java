package com.bluenet.web.application.service;

import com.bluenet.web.api.dto.experience.*;

import java.util.List;

/**
 * 用户经历应用服务
 */
public interface UserExperienceService {
    /**
     * 获取当前用户的经历列表
     *
     * @param type
     *            经历类型（可选）
     * @return 经历列表
     */
    List<ExperienceDTO> getExperiences(String type);

    /**
     * 创建经历
     *
     * @param request
     *            创建请求
     * @return 创建的经历
     */
    ExperienceDTO createExperience(CreateExperienceRequestDTO request);

    /**
     * 更新经历
     *
     * @param experienceId
     *            经历ID
     * @param request
     *            更新请求
     * @return 更新后的经历
     */
    ExperienceDTO updateExperience(Long experienceId, UpdateExperienceRequestDTO request);

    /**
     * 删除经历
     *
     * @param experienceId
     *            经历ID
     */
    void deleteExperience(Long experienceId);
}
