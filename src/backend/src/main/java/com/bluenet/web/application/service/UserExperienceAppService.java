package com.bluenet.web.application.service;

import com.bluenet.web.application.result.user.UserExperienceResult;
import com.bluenet.web.application.command.userexperience.UserExperienceCommands;

import java.util.List;

/**
 * 用户经历应用服务接口。
 * <p>
 * 定义了用户经历聚合在应用层的所有业务操作。
 * </p>
 */
public interface UserExperienceAppService {

    /**
     * 获取当前用户的经历列表
     *
     * @param type
     *            经历类型（可选）
     * @return 经历结果列表
     */
    List<UserExperienceResult> getExperiences(String type);

    /**
     * 创建经历
     *
     * @param command
     *            创建命令
     * @return 创建后的经历结果
     */
    UserExperienceResult createExperience(UserExperienceCommands.CreateExperienceCommand command);

    /**
     * 更新经历
     *
     * @param command
     *            更新命令
     * @return 更新后的经历结果
     */
    UserExperienceResult updateExperience(UserExperienceCommands.UpdateExperienceCommand command);

    /**
     * 删除经历
     *
     * @param experienceId
     *            经历ID
     */
    void deleteExperience(Long experienceId);
}
