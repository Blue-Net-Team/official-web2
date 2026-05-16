package com.bluenet.web.infrastructure.repository.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.List;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bluenet.web.domain.model.entity.AssessmentTime;
import com.bluenet.web.domain.model.enumerate.Direction;
import com.bluenet.web.infrastructure.repository.converter.AssessmentTimeRepositoryConverter;
import com.bluenet.web.infrastructure.repository.dataobject.AssessmentTimeDO;
import com.bluenet.web.infrastructure.repository.mapper.AssessmentQuestionMapper;
import com.bluenet.web.infrastructure.repository.mapper.AssessmentTimeMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;

@DisplayName("AssessmentTimeRepositoryImpl 单元测试")
@ExtendWith(MockitoExtension.class)
class AssessmentTimeRepositoryImplTest {

    @Mock
    private AssessmentTimeMapper assessmentTimeMapper;

    @Mock
    private AssessmentQuestionMapper assessmentQuestionMapper;

    @Spy
    private AssessmentTimeRepositoryConverter converter = new AssessmentTimeRepositoryConverter();

    @InjectMocks
    private AssessmentTimeRepositoryImpl assessmentTimeRepository;

    private static final Long TEST_USER_ID = 1L;
    private static final LocalDateTime START_TIME = LocalDateTime.of(2099, 1, 1, 9, 0);
    private static final LocalDateTime END_TIME = LocalDateTime.of(2099, 1, 1, 11, 0);

    private AssessmentTime createTestEntity(Long id, Direction direction, int epoch, int grade) {
        return AssessmentTime.reconstruct(id, direction, epoch, grade, START_TIME, END_TIME, false, null, null, false);
    }

    private AssessmentTimeDO toDataObject(AssessmentTime entity) {
        return AssessmentTimeDO.builder()
                .id(entity.getId())
                .direction(entity.getDirection())
                .epoch(entity.getEpoch())
                .grade(entity.getGrade())
                .startTime(entity.getStartTime())
                .endTime(entity.getEndTime())
                .timeLimit(entity.getTimeLimit())
                .timeLimitMinutes(entity.getTimeLimitMinutes())
                .build();
    }

    @Nested
    @DisplayName("update 方法测试")
    class UpdateTests {

        @Test
        @DisplayName("关闭限时时应清空timeLimitMinutes")
        void update_disableTimeLimit_shouldClearTimeLimitMinutes() {
            AssessmentTime entity = AssessmentTime.reconstruct(
                    1L,
                    Direction.COMPUTER_VISION,
                    1,
                    2024,
                    START_TIME,
                    END_TIME,
                    false,
                    null,
                    null,
                    false);

            assessmentTimeRepository.update(entity);

            verify(assessmentTimeMapper).updateById(
                    argThat(
                            (AssessmentTimeDO dataObject) -> dataObject.getId().equals(1L)
                                    && Boolean.FALSE.equals(dataObject.getTimeLimit())));
            verify(assessmentTimeMapper).clearTimeLimitMinutesById(1L);
        }
    }

    // ==================== findByUserParticipation 测试 ====================

    @Nested
    @DisplayName("findByUserParticipation 方法测试")
    class FindByUserParticipationTests {

        @Test
        @DisplayName("direction和enrollmentYear都存在：应使用组合条件+EXISTS查询")
        void findByUserParticipation_withDirectionAndYear_shouldReturnMatchingResults() {
            AssessmentTime at1 = createTestEntity(1L, Direction.COMPUTER_VISION, 1, 2024);
            AssessmentTime at2 = createTestEntity(2L, Direction.COMPUTER_VISION, 2, 2024);
            Page<AssessmentTimeDO> mockPage = new Page<>(1, 10, 2);
            mockPage.setRecords(
                    List.of(
                            toDataObject(at2),
                            toDataObject(at1)));

            when(
                    assessmentTimeMapper.selectPageByUserParticipation(
                            any(Page.class),
                            eq(TEST_USER_ID),
                            eq(Direction.COMPUTER_VISION),
                            eq(2024)))
                                    .thenReturn(mockPage);

            org.springframework.data.domain.Page<AssessmentTime> result = assessmentTimeRepository
                    .findByUserParticipation(
                            TEST_USER_ID,
                            Direction.COMPUTER_VISION,
                            2024,
                            Pageable.ofSize(10));

            assertEquals(2, result.getContent().size());
            verify(assessmentTimeMapper).selectPageByUserParticipation(
                    any(Page.class),
                    eq(TEST_USER_ID),
                    eq(Direction.COMPUTER_VISION),
                    eq(2024));
        }

        @Test
        @DisplayName("enrollmentYear为null：应仅按EXISTS查询")
        void findByUserParticipation_nullYear_shouldQueryByExistsOnly() {
            AssessmentTime at1 = createTestEntity(1L, Direction.STRUCTURAL_DESIGN, 1, 2024);
            Page<AssessmentTimeDO> mockPage = new Page<>(1, 10, 1);
            mockPage.setRecords(List.of(toDataObject(at1)));

            when(
                    assessmentTimeMapper.selectPageByUserParticipation(
                            any(Page.class),
                            eq(TEST_USER_ID),
                            eq(Direction.COMPUTER_VISION),
                            isNull()))
                                    .thenReturn(mockPage);

            org.springframework.data.domain.Page<AssessmentTime> result = assessmentTimeRepository
                    .findByUserParticipation(
                            TEST_USER_ID,
                            Direction.COMPUTER_VISION,
                            null,
                            Pageable.ofSize(10));

            assertEquals(1, result.getContent().size());
        }

        @Test
        @DisplayName("direction和enrollmentYear都为null：应仅按EXISTS查询")
        void findByUserParticipation_bothNull_shouldQueryByExistsOnly() {
            Page<AssessmentTimeDO> mockPage = new Page<>(1, 10, 0);
            mockPage.setRecords(List.of());

            when(
                    assessmentTimeMapper.selectPageByUserParticipation(
                            any(Page.class),
                            eq(TEST_USER_ID),
                            isNull(),
                            isNull()))
                                    .thenReturn(mockPage);

            org.springframework.data.domain.Page<AssessmentTime> result = assessmentTimeRepository
                    .findByUserParticipation(TEST_USER_ID, null, null, Pageable.ofSize(10));

            assertTrue(result.getContent().isEmpty());
        }

        @Test
        @DisplayName("无匹配数据：应返回空分页")
        void findByUserParticipation_noMatch_shouldReturnEmptyPage() {
            Page<AssessmentTimeDO> mockPage = new Page<>(1, 10, 0);
            mockPage.setRecords(List.of());

            when(
                    assessmentTimeMapper.selectPageByUserParticipation(
                            any(Page.class),
                            eq(TEST_USER_ID),
                            eq(Direction.EMBEDDED),
                            eq(2025)))
                                    .thenReturn(mockPage);

            org.springframework.data.domain.Page<AssessmentTime> result = assessmentTimeRepository
                    .findByUserParticipation(
                            TEST_USER_ID,
                            Direction.EMBEDDED,
                            2025,
                            Pageable.ofSize(10));

            assertTrue(result.getContent().isEmpty());
            assertEquals(0, result.getTotalElements());
        }

        @Test
        @DisplayName("结果按id降序排列")
        void findByUserParticipation_shouldOrderByDesc() {
            AssessmentTime at1 = createTestEntity(1L, Direction.COMPUTER_VISION, 1, 2024);
            AssessmentTime at2 = createTestEntity(2L, Direction.COMPUTER_VISION, 1, 2024);
            Page<AssessmentTimeDO> mockPage = new Page<>(1, 10, 2);
            mockPage.setRecords(
                    List.of(
                            toDataObject(at2),
                            toDataObject(at1)));

            when(
                    assessmentTimeMapper.selectPageByUserParticipation(
                            any(Page.class),
                            eq(TEST_USER_ID),
                            eq(Direction.COMPUTER_VISION),
                            eq(2024)))
                                    .thenReturn(mockPage);

            org.springframework.data.domain.Page<AssessmentTime> result = assessmentTimeRepository
                    .findByUserParticipation(
                            TEST_USER_ID,
                            Direction.COMPUTER_VISION,
                            2024,
                            Pageable.ofSize(10));

            assertEquals(2, result.getContent().get(0).getId());
            assertEquals(1, result.getContent().get(1).getId());
        }

        @Test
        @DisplayName("分页参数正确传递")
        void findByUserParticipation_shouldRespectPagination() {
            Page<AssessmentTimeDO> mockPage = new Page<>(1, 5, 20);
            mockPage.setRecords(List.of());

            when(
                    assessmentTimeMapper.selectPageByUserParticipation(
                            any(Page.class),
                            eq(TEST_USER_ID),
                            eq(Direction.COMPUTER_VISION),
                            eq(2024)))
                                    .thenReturn(mockPage);

            assessmentTimeRepository.findByUserParticipation(
                    TEST_USER_ID,
                    Direction.COMPUTER_VISION,
                    2024,
                    Pageable.ofSize(5));

            verify(assessmentTimeMapper).selectPageByUserParticipation(
                    any(Page.class),
                    eq(TEST_USER_ID),
                    eq(Direction.COMPUTER_VISION),
                    eq(2024));
        }
    }
}
