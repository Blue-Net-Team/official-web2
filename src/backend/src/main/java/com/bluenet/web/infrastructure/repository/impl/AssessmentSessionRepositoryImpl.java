package com.bluenet.web.infrastructure.repository.impl;

import com.bluenet.web.infrastructure.repository.support.RepositoryObjectConverter;

import com.bluenet.web.infrastructure.repository.dataobject.*;

import com.bluenet.web.domain.model.entity.AssessmentSession;
import com.bluenet.web.domain.model.vo.AssessmentSessionVO;
import com.bluenet.web.domain.repository.AssessmentSessionRepository;
import com.bluenet.web.infrastructure.repository.mapper.AssessmentSessionMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@Slf4j
@RequiredArgsConstructor
public class AssessmentSessionRepositoryImpl implements AssessmentSessionRepository {
    private final AssessmentSessionMapper assessmentSessionMapper;

    /**
     * 保存新的考核会话 记录。
     *
     * @param session
     *            考核会话领域对象。
     */
    @Override
    public void save(AssessmentSession session) {
        log.info(
                "save assessment session for userId: {}, assessmentTimeId: {}",
                session.getUserId(),
                session.getAssessmentTimeId());
        RepositoryObjectConverter.insert(assessmentSessionMapper, session, AssessmentSessionDO.class);
    }

    /**
     * 按用户和考核场次查询对应记录。
     *
     * @param userId
     *            用户主键，用于限定用户范围。
     * @param assessmentTimeId
     *            考核场次主键。
     * @return 查询到的考核会话 结果；不存在时为空。
     */
    @Override
    public Optional<AssessmentSessionVO> findByUserIdAndAssessmentTimeId(Long userId, Long assessmentTimeId) {
        AssessmentSession session = RepositoryObjectConverter.toDomain(
                assessmentSessionMapper.selectByUserIdAndAssessmentTimeId(userId, assessmentTimeId),
                AssessmentSession.class);
        if (session == null) {
            return Optional.empty();
        }
        return Optional.of(convertToVO(session));
    }

    /**
     * 在考核会话 的持久层对象、领域对象和视图对象之间转换。
     *
     * @param session
     *            考核会话领域对象。
     * @return 转换后的目标模型对象。
     */
    private AssessmentSessionVO convertToVO(AssessmentSession session) {
        return AssessmentSessionVO.builder()
                .id(session.getId())
                .userId(session.getUserId())
                .assessmentTimeId(session.getAssessmentTimeId())
                .startTime(session.getStartTime())
                .deadline(session.getDeadline())
                .build();
    }
}
