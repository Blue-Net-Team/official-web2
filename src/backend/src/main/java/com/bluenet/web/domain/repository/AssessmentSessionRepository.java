package com.bluenet.web.domain.repository;

import com.bluenet.web.domain.model.entity.AssessmentSession;
import com.bluenet.web.domain.model.vo.AssessmentSessionVO;

import java.util.Optional;

public interface AssessmentSessionRepository {
    /**
     * 保存新的考核会话 记录。
     *
     * @param session
     *            考核会话领域对象。
     */
    void save(AssessmentSession session);

    /**
     * 按用户和考核场次查询对应记录。
     *
     * @param userId
     *            用户主键，用于限定用户范围。
     * @param assessmentTimeId
     *            考核场次主键。
     * @return 查询到的考核会话 结果；不存在时为空。
     */
    Optional<AssessmentSessionVO> findByUserIdAndAssessmentTimeId(Long userId, Long assessmentTimeId);
}
