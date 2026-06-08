package com.bluenet.web.application.service;

import com.bluenet.web.application.AssessmentProgressResult;
import com.bluenet.web.application.AssessmentTimeResult;
import com.bluenet.web.application.command.assessment_time.AssessmentTimeCommands;
import org.springframework.data.domain.Page;

/**
 * 考核时间应用服务接口。
 * <p>
 * 定义了考核时间聚合在应用层的所有业务操作。
 * </p>
 */
public interface AssessmentTimeAppService {
    /**
     * 创建考核时间
     *
     * @param userId
     *            当前用户ID
     * @param command
     *            创建命令
     * @return 创建后的考核时间结果
     */
    AssessmentTimeResult createAssessmentTime(Long userId, AssessmentTimeCommands.CreateAssessmentTimeCommand command);

    /**
     * 更新考核时间
     *
     * @param userId
     *            当前用户ID
     * @param command
     *            更新命令
     * @return 更新后的考核时间结果
     */
    AssessmentTimeResult updateAssessmentTime(Long userId, AssessmentTimeCommands.UpdateAssessmentTimeCommand command);

    /**
     * 删除考核时间
     *
     * @param userId
     *            当前用户ID
     * @param id
     *            考核时间ID
     */
    void deleteAssessmentTime(Long userId, Long id);

    /**
     * 管理端分页查询考核时间（根据当前用户角色过滤）
     *
     * @param userId
     *            当前用户ID
     * @param page
     *            页码（从0开始）
     * @param size
     *            每页大小
     * @return 分页考核时间结果
     */
    Page<AssessmentTimeResult> listAssessmentTimes(Long userId, Integer page, Integer size);

    /**
     * 用户端查询考核时间（根据当前用户角色过滤）
     *
     * @param userId
     *            当前用户ID
     * @param page
     *            页码（从0开始）
     * @param size
     *            每页大小
     * @return 分页考核时间结果
     */
    Page<AssessmentTimeResult> listAssessmentTimesForUser(Long userId, Integer page, Integer size);

    /**
     * 查询指定考核时间的答题进度
     *
     * @param userId
     *            当前用户ID
     * @param assessmentTimeId
     *            考核时间ID
     * @return 进度信息
     */
    AssessmentProgressResult getAssessmentProgress(Long userId, Long assessmentTimeId);
}
