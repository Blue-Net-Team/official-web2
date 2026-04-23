package com.bluenet.web.domain.repository;

import com.bluenet.web.domain.model.entity.AssessmentSession;

import java.util.Optional;

/**
 * 考核会话仓库接口
 * <p>
 * 负责考核会话数据的持久化操作，只操作 Entity
 * </p>
 */
public interface AssessmentSessionRepository {
    /**
     * 保存新的考核会话记录。
     *
     * @param session
     *            考核会话实体。
     */
    void save(AssessmentSession session);

    /**
     * 按用户和考核场次查询对应记录。
     *
     * @param userId
     *            用户主键，用于限定用户范围。
     * @param assessmentTimeId
     *            考核场次主键。
     * @return 查询到的考核会话实体；不存在时为空。
     */
    Optional<AssessmentSession> findByUserIdAndAssessmentTimeId(Long userId, Long assessmentTimeId);
}
