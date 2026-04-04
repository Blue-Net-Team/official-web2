package com.bluenet.web.application.service;

import com.bluenet.web.api.dto.PageDTO;
import com.bluenet.web.api.dto.assessment_time.AssessmentProgressDTO;
import com.bluenet.web.api.dto.assessment_time.AssessmentTimeDTO;
import com.bluenet.web.api.dto.assessment_time.CreateAssessmentTimeRequestDTO;
import com.bluenet.web.api.dto.assessment_time.UpdateAssessmentTimeRequestDTO;

/**
 * 考核时间应用服务接口
 * <p>
 * 提供考核时间相关的应用层服务，协调领域服务完成业务操作
 * </p>
 */
public interface AssessmentTimeService {
    /**
     * 创建考核时间
     *
     * @param request
     *            创建请求DTO
     * @return 创建后的考核时间DTO
     */
    AssessmentTimeDTO createAssessmentTime(CreateAssessmentTimeRequestDTO request);

    /**
     * 更新考核时间
     *
     * @param id
     *            考核时间ID
     * @param request
     *            更新请求DTO
     * @return 更新后的考核时间DTO
     */
    AssessmentTimeDTO updateAssessmentTime(Long id, UpdateAssessmentTimeRequestDTO request);

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
