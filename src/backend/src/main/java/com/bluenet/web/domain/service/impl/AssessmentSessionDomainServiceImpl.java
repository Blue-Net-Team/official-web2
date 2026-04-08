package com.bluenet.web.domain.service.impl;

import com.bluenet.web.domain.model.entity.AssessmentSession;
import com.bluenet.web.domain.model.vo.AssessmentSessionVO;
import com.bluenet.web.domain.model.vo.AssessmentTimeVO;
import com.bluenet.web.domain.repository.AssessmentSessionRepository;
import com.bluenet.web.domain.service.AssessmentSessionDomainService;
import com.bluenet.web.domain.service.AssessmentTimeDomainService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class AssessmentSessionDomainServiceImpl implements AssessmentSessionDomainService {
    private final AssessmentSessionRepository assessmentSessionRepository;
    private final AssessmentTimeDomainService assessmentTimeDomainService;

    @Override
    @Transactional
    public AssessmentSessionVO getOrCreateSession(Long userId, Long assessmentTimeId) {
        // 尝试查找已有会话
        Optional<AssessmentSessionVO> existing = assessmentSessionRepository
                .findByUserIdAndAssessmentTimeId(userId, assessmentTimeId);
        if (existing.isPresent()) {
            log.debug("session already exists for userId: {}, assessmentTimeId: {}", userId, assessmentTimeId);
            return existing.get();
        }

        // 获取考核时间信息
        AssessmentTimeVO timeVO = assessmentTimeDomainService.getById(assessmentTimeId)
                .orElseThrow(() -> new IllegalArgumentException("考核时间不存在"));

        LocalDateTime startTime = LocalDateTime.now();
        LocalDateTime limitDeadline = startTime.plusMinutes(timeVO.getTimeLimitMinutes());
        // deadline = min(start_time + timeLimitMinutes, endTime)
        LocalDateTime deadline = limitDeadline.isBefore(timeVO.getEndTime())
                ? limitDeadline
                : timeVO.getEndTime();

        AssessmentSession entity = new AssessmentSession();
        entity.setUserId(userId);
        entity.setAssessmentTimeId(assessmentTimeId);
        entity.setStartTime(startTime);
        entity.setDeadline(deadline);

        assessmentSessionRepository.save(entity);

        log.info(
                "created session for userId: {}, assessmentTimeId: {}, deadline: {}",
                userId,
                assessmentTimeId,
                deadline);

        return AssessmentSessionVO.builder()
                .id(entity.getId())
                .userId(entity.getUserId())
                .assessmentTimeId(entity.getAssessmentTimeId())
                .startTime(entity.getStartTime())
                .deadline(entity.getDeadline())
                .build();
    }
}
