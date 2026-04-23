package com.bluenet.web.infrastructure.repository.impl;

import com.bluenet.web.domain.model.entity.AssessmentSession;
import com.bluenet.web.domain.repository.AssessmentSessionRepository;
import com.bluenet.web.infrastructure.repository.converter.AssessmentSessionRepositoryConverter;
import com.bluenet.web.infrastructure.repository.dataobject.AssessmentSessionDO;
import com.bluenet.web.infrastructure.repository.mapper.AssessmentSessionMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 考核会话仓库实现类
 * <p>
 * 实现考核会话数据的持久化操作，使用显式转换器替代 BeanUtils
 * </p>
 */
@Repository
@Slf4j
@RequiredArgsConstructor
public class AssessmentSessionRepositoryImpl implements AssessmentSessionRepository {
    private final AssessmentSessionMapper assessmentSessionMapper;
    private final AssessmentSessionRepositoryConverter converter;

    @Override
    public void save(AssessmentSession session) {
        log.info(
                "save assessment session for userId: {}, assessmentTimeId: {}",
                session.getUserId(),
                session.getAssessmentTimeId());
        AssessmentSessionDO dataObject = converter.toDataObject(session);
        assessmentSessionMapper.insert(dataObject);
        session.setId(dataObject.getId());
    }

    @Override
    public Optional<AssessmentSession> findByUserIdAndAssessmentTimeId(Long userId, Long assessmentTimeId) {
        AssessmentSessionDO dataObject = assessmentSessionMapper
                .selectByUserIdAndAssessmentTimeId(userId, assessmentTimeId);
        return Optional.ofNullable(converter.toEntity(dataObject));
    }
}
