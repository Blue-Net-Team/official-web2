package com.bluenet.web.application.service.impl;

import com.bluenet.web.application.AssessmentSessionResult;
import com.bluenet.web.application.command.assessment_session.AssessmentSessionCommands;
import com.bluenet.web.application.service.AssessmentSessionAppService;
import com.bluenet.web.domain.model.entity.AssessmentSession;
import com.bluenet.web.domain.model.entity.AssessmentTime;
import com.bluenet.web.domain.repository.AssessmentSessionRepository;
import com.bluenet.web.domain.repository.AssessmentTimeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * 考核会话应用服务实现。
 * <p>
 * 实现考核会话聚合在应用层的业务逻辑编排。
 * </p>
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class AssessmentSessionAppServiceImpl implements AssessmentSessionAppService {
    private final AssessmentSessionRepository assessmentSessionRepository;
    private final AssessmentTimeRepository assessmentTimeRepository;

    /**
     * 获取或创建会话。
     *
     * @param command
     *            获取或创建会话命令
     * @return 考核会话结果
     */
    @Override
    @Transactional
    public AssessmentSessionResult getOrCreateSession(AssessmentSessionCommands.GetOrCreateSessionCommand command) {
        Long userId = command.userId();
        Long assessmentTimeId = command.assessmentTimeId();

        AssessmentTime time = assessmentTimeRepository.findById(assessmentTimeId)
                .orElseThrow(() -> new IllegalArgumentException("考核时间不存在"));

        if (!Boolean.TRUE.equals(time.getTimeLimit())) {
            log.debug(
                    "assessment time is not timed, skip session for userId: {}, assessmentTimeId: {}",
                    userId,
                    assessmentTimeId);
            return null;
        }
        if (time.getTimeLimitMinutes() == null || time.getTimeLimitMinutes() <= 0) {
            throw new IllegalArgumentException("限时考核必须设置有效的限时分钟数");
        }

        Optional<AssessmentSession> existing = assessmentSessionRepository
                .findByUserIdAndAssessmentTimeId(userId, assessmentTimeId);
        if (existing.isPresent()) {
            log.debug("session already exists for userId: {}, assessmentTimeId: {}", userId, assessmentTimeId);
            AssessmentSession session = existing.get();
            return new AssessmentSessionResult(
                    session.getId(),
                    session.getUserId(),
                    session.getAssessmentTimeId(),
                    session.getStartTime(),
                    session.getDeadline());
        }

        LocalDateTime startTime = LocalDateTime.now();
        LocalDateTime limitDeadline = startTime.plusMinutes(time.getTimeLimitMinutes());
        LocalDateTime deadline = limitDeadline.isBefore(time.getEndTime())
                ? limitDeadline
                : time.getEndTime();

        AssessmentSession entity = AssessmentSession.create(userId, assessmentTimeId, startTime, deadline);
        assessmentSessionRepository.save(entity);

        log.info(
                "created session for userId: {}, assessmentTimeId: {}, deadline: {}",
                userId,
                assessmentTimeId,
                deadline);

        return new AssessmentSessionResult(
                entity.getId(),
                entity.getUserId(),
                entity.getAssessmentTimeId(),
                entity.getStartTime(),
                entity.getDeadline());
    }
}
