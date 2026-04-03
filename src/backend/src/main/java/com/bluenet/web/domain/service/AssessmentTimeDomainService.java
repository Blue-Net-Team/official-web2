package com.bluenet.web.domain.service;

import com.bluenet.web.domain.model.vo.AssessmentTimeVO;

import java.util.Optional;

/**
 * 考核时间领域服务接口
 * <p>
 * 提供考核时间相关的业务逻辑操作
 * </p>
 */
public interface AssessmentTimeDomainService {
    /**
     * 根据ID获取考核时间
     *
     * @param id
     *            考核时间ID
     * @return 考核时间信息
     */
    Optional<AssessmentTimeVO> getById(Long id);

    /**
     * 创建考核时间
     *
     * @param assessmentTime
     *            考核时间信息
     * @return 创建后的考核时间ID
     * @throws IllegalArgumentException
     *             如果校验失败
     */
    Long create(AssessmentTimeVO assessmentTime);

    /**
     * 更新考核时间
     *
     * @param assessmentTime
     *            考核时间信息
     * @throws IllegalArgumentException
     *             如果校验失败
     */
    void update(AssessmentTimeVO assessmentTime);

    /**
     * 删除考核时间
     *
     * @param id
     *            考核时间ID
     * @throws IllegalArgumentException
     *             如果不存在或有关联题目
     */
    void delete(Long id);
}
