package com.bluenet.web.application.service;

import com.bluenet.web.api.dto.PageDTO;
import com.bluenet.web.api.dto.assessment_time.AssessmentProgressDTO;
import com.bluenet.web.api.dto.assessment_time.AssessmentTimeDTO;
import com.bluenet.web.application.AssessmentTimeResult;
import com.bluenet.web.application.command.assessment_time.AssessmentTimeCommands;

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
     * @param command
     *            创建命令
     * @return 创建后的考核时间结果
     */
    AssessmentTimeResult createAssessmentTime(AssessmentTimeCommands.CreateAssessmentTimeCommand command);

    /**
     * 更新考核时间
     *
     * @param command
     *            更新命令
     * @return 更新后的考核时间结果
     */
    AssessmentTimeResult updateAssessmentTime(AssessmentTimeCommands.UpdateAssessmentTimeCommand command);

    /**
     * 删除考核时间
     *
     * @param id
     *            考核时间ID
     */
    void deleteAssessmentTime(Long id);

    /**
     * 管理端分页查询考核时间（根据当前用户角色过滤）
     *
     * @param page
     *            页码（从0开始）
     * @param size
     *            每页大小
     * @return 分页考核时间DTO
     */
    PageDTO<AssessmentTimeDTO> listAssessmentTimes(Integer page, Integer size);

    /**
     * 用户端查询考核时间（根据当前用户角色过滤）
     *
     * @param page
     *            页码（从0开始）
     * @param size
     *            每页大小
     * @return 分页考核时间DTO
     */
    PageDTO<AssessmentTimeDTO> listAssessmentTimesForUser(Integer page, Integer size);

    /**
     * 查询指定考核时间的答题进度
     *
     * @param assessmentTimeId
     *            考核时间ID
     * @return 进度信息
     */
    AssessmentProgressDTO getAssessmentProgress(Long assessmentTimeId);
}
