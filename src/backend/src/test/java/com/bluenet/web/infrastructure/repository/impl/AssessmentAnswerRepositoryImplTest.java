package com.bluenet.web.infrastructure.repository.impl;

import com.bluenet.web.domain.model.entity.AssessmentAnswer;
import com.bluenet.web.domain.model.enumerate.ProgrammingLanguage;
import com.bluenet.web.infrastructure.repository.converter.AssessmentAnswerRepositoryConverter;
import com.bluenet.web.infrastructure.repository.dataobject.AssessmentAnswerCountResult;
import com.bluenet.web.infrastructure.repository.mapper.AssessmentAnswerMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * AssessmentAnswerRepositoryImpl 单元测试。
 */
@DisplayName("AssessmentAnswerRepositoryImpl 单元测试")
@ExtendWith(MockitoExtension.class)
class AssessmentAnswerRepositoryImplTest {

    @Mock
    private AssessmentAnswerMapper assessmentAnswerMapper;

    @Spy
    private AssessmentAnswerRepositoryConverter converter = new AssessmentAnswerRepositoryConverter();

    @InjectMocks
    private AssessmentAnswerRepositoryImpl assessmentAnswerRepository;

    private static final Long TEST_USER_ID = 1L;
    private static final Long TEST_TIME_ID = 100L;

    private AssessmentAnswer createTestEntity(Long id) {
        return AssessmentAnswer.reconstruct(
                id,
                TEST_USER_ID,
                10L,
                "测试答案",
                ProgrammingLanguage.JAVA,
                null,
                LocalDateTime.now(),
                null);
    }

    @Nested
    @DisplayName("countByUserIdAndAssessmentTimeIds 方法测试")
    class CountByUserIdAndAssessmentTimeIdsTests {

        @Test
        @DisplayName("空列表：应返回空映射")
        void countByUserIdAndAssessmentTimeIds_emptyList_shouldReturnEmptyMap() {
            Map<Long, Integer> result = assessmentAnswerRepository
                    .countByUserIdAndAssessmentTimeIds(TEST_USER_ID, Collections.emptyList());

            assertTrue(result.isEmpty());
            verify(assessmentAnswerMapper, never()).countByUserIdAndAssessmentTimeIds(anyLong(), anyList());
        }

        @Test
        @DisplayName("userId为null：应返回空映射")
        void countByUserIdAndAssessmentTimeIds_nullUserId_shouldReturnEmptyMap() {
            Map<Long, Integer> result = assessmentAnswerRepository
                    .countByUserIdAndAssessmentTimeIds(null, List.of(TEST_TIME_ID));

            assertTrue(result.isEmpty());
            verify(assessmentAnswerMapper, never()).countByUserIdAndAssessmentTimeIds(any(), anyList());
        }

        @Test
        @DisplayName("部分命中：应返回对应计数")
        void countByUserIdAndAssessmentTimeIds_partialHits_shouldReturnCounts() {
            when(assessmentAnswerMapper.countByUserIdAndAssessmentTimeIds(TEST_USER_ID, List.of(1L, 2L, 3L)))
                    .thenReturn(
                            List.of(
                                    new AssessmentAnswerCountResult(1L, 5L),
                                    new AssessmentAnswerCountResult(3L, 8L)));

            Map<Long, Integer> result = assessmentAnswerRepository
                    .countByUserIdAndAssessmentTimeIds(TEST_USER_ID, List.of(1L, 2L, 3L));

            assertEquals(2, result.size());
            assertEquals(5, result.get(1L));
            assertEquals(8, result.get(3L));
            assertNull(result.get(2L));
        }

        @Test
        @DisplayName("全部命中：应返回所有计数")
        void countByUserIdAndAssessmentTimeIds_allHits_shouldReturnAllCounts() {
            when(assessmentAnswerMapper.countByUserIdAndAssessmentTimeIds(TEST_USER_ID, List.of(1L, 2L)))
                    .thenReturn(
                            List.of(
                                    new AssessmentAnswerCountResult(1L, 5L),
                                    new AssessmentAnswerCountResult(2L, 10L)));

            Map<Long, Integer> result = assessmentAnswerRepository
                    .countByUserIdAndAssessmentTimeIds(TEST_USER_ID, List.of(1L, 2L));

            assertEquals(2, result.size());
            assertEquals(5, result.get(1L));
            assertEquals(10, result.get(2L));
        }
    }
}
