package com.bluenet.web.domain.service.impl;

import com.bluenet.web.domain.model.enumerate.Direction;
import com.bluenet.web.domain.model.vo.AssessmentSessionVO;
import com.bluenet.web.domain.model.vo.AssessmentTimeVO;
import com.bluenet.web.domain.repository.AssessmentSessionRepository;
import com.bluenet.web.domain.service.AssessmentTimeDomainService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("AssessmentSessionDomainServiceImpl 单元测试")
@ExtendWith(MockitoExtension.class)
class AssessmentSessionDomainServiceImplTest {

    @Mock
    private AssessmentSessionRepository assessmentSessionRepository;

    @Mock
    private AssessmentTimeDomainService assessmentTimeDomainService;

    @InjectMocks
    private AssessmentSessionDomainServiceImpl assessmentSessionDomainService;

    private static final Long TEST_USER_ID = 1L;
    private static final Long TEST_ASSESSMENT_TIME_ID = 20L;

    @Test
    @DisplayName("不限时考核：不创建会话并返回null")
    void getOrCreateSession_nonTimed_shouldNotCreateSession() {
        AssessmentTimeVO timeVO = AssessmentTimeVO.builder()
                .id(TEST_ASSESSMENT_TIME_ID)
                .direction(Direction.COMPUTER_VISION)
                .startTime(LocalDateTime.now().minusHours(1))
                .endTime(LocalDateTime.now().plusHours(2))
                .timeLimit(false)
                .timeLimitMinutes(90)
                .build();

        when(assessmentTimeDomainService.getById(TEST_ASSESSMENT_TIME_ID)).thenReturn(Optional.of(timeVO));

        AssessmentSessionVO result = assessmentSessionDomainService
                .getOrCreateSession(TEST_USER_ID, TEST_ASSESSMENT_TIME_ID);

        assertNull(result);
        verify(assessmentSessionRepository, never())
                .findByUserIdAndAssessmentTimeId(TEST_USER_ID, TEST_ASSESSMENT_TIME_ID);
        verify(assessmentSessionRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    @DisplayName("限时考核已有会话：返回已有会话")
    void getOrCreateSession_timedWithExistingSession_shouldReturnExistingSession() {
        AssessmentTimeVO timeVO = AssessmentTimeVO.builder()
                .id(TEST_ASSESSMENT_TIME_ID)
                .direction(Direction.COMPUTER_VISION)
                .startTime(LocalDateTime.now().minusHours(1))
                .endTime(LocalDateTime.now().plusHours(2))
                .timeLimit(true)
                .timeLimitMinutes(90)
                .build();
        AssessmentSessionVO existing = AssessmentSessionVO.builder()
                .id(1L)
                .userId(TEST_USER_ID)
                .assessmentTimeId(TEST_ASSESSMENT_TIME_ID)
                .startTime(LocalDateTime.now().minusMinutes(10))
                .deadline(LocalDateTime.now().plusMinutes(80))
                .build();

        when(assessmentTimeDomainService.getById(TEST_ASSESSMENT_TIME_ID)).thenReturn(Optional.of(timeVO));
        when(assessmentSessionRepository.findByUserIdAndAssessmentTimeId(TEST_USER_ID, TEST_ASSESSMENT_TIME_ID))
                .thenReturn(Optional.of(existing));

        AssessmentSessionVO result = assessmentSessionDomainService
                .getOrCreateSession(TEST_USER_ID, TEST_ASSESSMENT_TIME_ID);

        assertSame(existing, result);
        verify(assessmentSessionRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }
}
