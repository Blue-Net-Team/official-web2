package com.bluenet.web.domain.service.impl;

import com.bluenet.web.domain.exception.DataConflict;
import com.bluenet.web.domain.model.enumerate.Direction;
import com.bluenet.web.domain.model.vo.AssessmentTimeVO;
import com.bluenet.web.domain.repository.AssessmentTimeRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * AssessmentTimeDomainServiceImpl 单元测试
 * <p>
 * 测试考核时间领域服务的业务逻辑
 * </p>
 */
@DisplayName("AssessmentTimeDomainServiceImpl 单元测试")
@ExtendWith(MockitoExtension.class)
class AssessmentTimeDomainServiceImplTest {

    @Mock
    private AssessmentTimeRepository assessmentTimeRepository;

    @InjectMocks
    private AssessmentTimeDomainServiceImpl assessmentTimeDomainService;

    private static final Long TEST_ID = 1L;
    private static final Direction TEST_DIRECTION = Direction.COMPUTER_VISION;
    private static final Integer TEST_EPOCH = 1;
    private static final Integer TEST_GRADE = 1;

    private LocalDateTime futureStart = LocalDateTime.of(2099, 1, 1, 9, 0);
    private LocalDateTime futureEnd = LocalDateTime.of(2099, 1, 1, 11, 0);

    private AssessmentTimeVO createTestVO() {
        return AssessmentTimeVO.builder()
                .id(TEST_ID)
                .direction(TEST_DIRECTION)
                .epoch(TEST_EPOCH)
                .grade(TEST_GRADE)
                .startTime(futureStart)
                .endTime(futureEnd)
                .timeLimit(true)
                .timeLimitMinutes(120)
                .build();
    }

    // ==================== getById 方法测试 ====================

    @Nested
    @DisplayName("getById 方法测试")
    class GetByIdTests {

        @Test
        @DisplayName("考核时间存在：应返回VO")
        void getById_existing_shouldReturnVO() {
            AssessmentTimeVO vo = createTestVO();
            when(assessmentTimeRepository.findById(TEST_ID)).thenReturn(Optional.of(vo));

            Optional<AssessmentTimeVO> result = assessmentTimeDomainService.getById(TEST_ID);

            assertTrue(result.isPresent());
            assertEquals(TEST_ID, result.get().getId());
            verify(assessmentTimeRepository).findById(TEST_ID);
        }

        @Test
        @DisplayName("考核时间不存在：应返回空Optional")
        void getById_notExisting_shouldReturnEmpty() {
            when(assessmentTimeRepository.findById(TEST_ID)).thenReturn(Optional.empty());

            Optional<AssessmentTimeVO> result = assessmentTimeDomainService.getById(TEST_ID);

            assertTrue(result.isEmpty());
        }
    }

    // ==================== create 方法测试 ====================

    @Nested
    @DisplayName("create 方法测试")
    class CreateTests {

        @Test
        @DisplayName("正常创建：应返回ID")
        void create_valid_shouldReturnId() {
            AssessmentTimeVO vo = AssessmentTimeVO.builder()
                    .direction(TEST_DIRECTION)
                    .epoch(TEST_EPOCH)
                    .grade(TEST_GRADE)
                    .startTime(futureStart)
                    .endTime(futureEnd)
                    .timeLimit(true)
                    .timeLimitMinutes(120)
                    .build();

            when(assessmentTimeRepository.existsByDirectionAndEpochAndGrade(TEST_DIRECTION, TEST_EPOCH, TEST_GRADE))
                    .thenReturn(false);
            when(assessmentTimeRepository.save(any())).thenReturn(TEST_ID);

            Long result = assessmentTimeDomainService.create(vo);

            assertEquals(TEST_ID, result);
            verify(assessmentTimeRepository).save(vo);
        }

        @Test
        @DisplayName("开始时间不早于结束时间：应抛出IllegalArgumentException")
        void create_startTimeNotBeforeEndTime_shouldThrow() {
            AssessmentTimeVO vo = AssessmentTimeVO.builder()
                    .startTime(futureEnd) // end 作为 start
                    .endTime(futureStart) // start 作为 end
                    .build();

            IllegalArgumentException ex = assertThrows(
                    IllegalArgumentException.class,
                    () -> assessmentTimeDomainService.create(vo));
            assertEquals("开始时间必须早于结束时间", ex.getMessage());
        }

        @Test
        @DisplayName("开始时间等于结束时间：应抛出IllegalArgumentException")
        void create_startTimeEqualsEndTime_shouldThrow() {
            AssessmentTimeVO vo = AssessmentTimeVO.builder()
                    .startTime(futureStart)
                    .endTime(futureStart)
                    .build();

            IllegalArgumentException ex = assertThrows(
                    IllegalArgumentException.class,
                    () -> assessmentTimeDomainService.create(vo));
            assertEquals("开始时间必须早于结束时间", ex.getMessage());
        }

        @Test
        @DisplayName("限时考核未设置限时分钟数：应抛出IllegalArgumentException")
        void create_timeLimitWithoutMinutes_shouldThrow() {
            AssessmentTimeVO vo = AssessmentTimeVO.builder()
                    .startTime(futureStart)
                    .endTime(futureEnd)
                    .timeLimit(true)
                    .timeLimitMinutes(null)
                    .build();

            IllegalArgumentException ex = assertThrows(
                    IllegalArgumentException.class,
                    () -> assessmentTimeDomainService.create(vo));
            assertEquals("限时考核必须设置有效的限时分钟数", ex.getMessage());
        }

        @Test
        @DisplayName("限时考核限时分钟数为0：应抛出IllegalArgumentException")
        void create_timeLimitWithZeroMinutes_shouldThrow() {
            AssessmentTimeVO vo = AssessmentTimeVO.builder()
                    .startTime(futureStart)
                    .endTime(futureEnd)
                    .timeLimit(true)
                    .timeLimitMinutes(0)
                    .build();

            IllegalArgumentException ex = assertThrows(
                    IllegalArgumentException.class,
                    () -> assessmentTimeDomainService.create(vo));
            assertEquals("限时考核必须设置有效的限时分钟数", ex.getMessage());
        }

        @Test
        @DisplayName("方向轮次年级组合已存在：应抛出IllegalArgumentException")
        void create_duplicateCombination_shouldThrow() {
            AssessmentTimeVO vo = AssessmentTimeVO.builder()
                    .direction(TEST_DIRECTION)
                    .epoch(TEST_EPOCH)
                    .grade(TEST_GRADE)
                    .startTime(futureStart)
                    .endTime(futureEnd)
                    .timeLimit(false)
                    .build();

            when(assessmentTimeRepository.existsByDirectionAndEpochAndGrade(TEST_DIRECTION, TEST_EPOCH, TEST_GRADE))
                    .thenReturn(true);

            IllegalArgumentException ex = assertThrows(
                    IllegalArgumentException.class,
                    () -> assessmentTimeDomainService.create(vo));
            assertEquals("该方向轮次年级的考核时间已存在", ex.getMessage());
            verify(assessmentTimeRepository, never()).save(any());
        }

        @Test
        @DisplayName("不限时考核不设置限时分钟数：应正常创建")
        void create_noTimeLimitWithoutMinutes_shouldSucceed() {
            AssessmentTimeVO vo = AssessmentTimeVO.builder()
                    .direction(Direction.STRUCTURAL_DESIGN)
                    .epoch(2)
                    .grade(2)
                    .startTime(futureStart)
                    .endTime(futureEnd)
                    .timeLimit(false)
                    .build();

            when(assessmentTimeRepository.existsByDirectionAndEpochAndGrade(Direction.STRUCTURAL_DESIGN, 2, 2))
                    .thenReturn(false);
            when(assessmentTimeRepository.save(any())).thenReturn(2L);

            Long result = assessmentTimeDomainService.create(vo);
            assertEquals(2L, result);
        }
    }

    // ==================== update 方法测试 ====================

    @Nested
    @DisplayName("update 方法测试")
    class UpdateTests {

        @Test
        @DisplayName("正常更新：应成功")
        void update_valid_shouldSucceed() {
            AssessmentTimeVO existing = createTestVO();
            // futureStart = 2099，所以 isAfter(now) = true，允许修改
            AssessmentTimeVO updateVO = AssessmentTimeVO.builder()
                    .id(TEST_ID)
                    .startTime(futureStart)
                    .endTime(futureEnd)
                    .timeLimit(true)
                    .timeLimitMinutes(90)
                    .build();

            when(assessmentTimeRepository.findById(TEST_ID)).thenReturn(Optional.of(existing));

            assessmentTimeDomainService.update(updateVO);

            verify(assessmentTimeRepository).update(updateVO);
        }

        @Test
        @DisplayName("考核时间不存在：应抛出IllegalArgumentException")
        void update_notExisting_shouldThrow() {
            AssessmentTimeVO updateVO = AssessmentTimeVO.builder()
                    .id(TEST_ID)
                    .build();

            when(assessmentTimeRepository.findById(TEST_ID)).thenReturn(Optional.empty());

            IllegalArgumentException ex = assertThrows(
                    IllegalArgumentException.class,
                    () -> assessmentTimeDomainService.update(updateVO));
            assertEquals("考核时间不存在", ex.getMessage());
        }

        @Test
        @DisplayName("已开始的考核修改开始时间：应抛出IllegalArgumentException")
        void update_startedModifyStartTime_shouldThrow() {
            // 开始时间在过去，表示已开始
            AssessmentTimeVO existing = AssessmentTimeVO.builder()
                    .id(TEST_ID)
                    .direction(TEST_DIRECTION)
                    .epoch(TEST_EPOCH)
                    .grade(TEST_GRADE)
                    .startTime(LocalDateTime.of(2020, 1, 1, 9, 0)) // 过去
                    .endTime(LocalDateTime.of(2099, 1, 1, 11, 0))
                    .timeLimit(false)
                    .build();

            AssessmentTimeVO updateVO = AssessmentTimeVO.builder()
                    .id(TEST_ID)
                    .startTime(LocalDateTime.of(2025, 6, 1, 9, 0)) // 尝试修改
                    .build();

            when(assessmentTimeRepository.findById(TEST_ID)).thenReturn(Optional.of(existing));

            IllegalArgumentException ex = assertThrows(
                    IllegalArgumentException.class,
                    () -> assessmentTimeDomainService.update(updateVO));
            assertEquals("已开始的考核不允许修改开始时间", ex.getMessage());
        }

        @Test
        @DisplayName("已开始的考核修改结束时间：应成功")
        void update_startedModifyEndTime_shouldSucceed() {
            AssessmentTimeVO existing = AssessmentTimeVO.builder()
                    .id(TEST_ID)
                    .direction(TEST_DIRECTION)
                    .epoch(TEST_EPOCH)
                    .grade(TEST_GRADE)
                    .startTime(LocalDateTime.of(2020, 1, 1, 9, 0)) // 过去
                    .endTime(LocalDateTime.of(2099, 1, 1, 11, 0))
                    .timeLimit(false)
                    .build();

            LocalDateTime newEndTime = LocalDateTime.of(2099, 12, 31, 23, 59);
            AssessmentTimeVO updateVO = AssessmentTimeVO.builder()
                    .id(TEST_ID)
                    .endTime(newEndTime)
                    .build();

            when(assessmentTimeRepository.findById(TEST_ID)).thenReturn(Optional.of(existing));

            assessmentTimeDomainService.update(updateVO);

            verify(assessmentTimeRepository).update(updateVO);
        }

        @Test
        @DisplayName("方向轮次年级组合已被其他记录使用：应抛出IllegalArgumentException")
        void update_duplicateCombination_shouldThrow() {
            AssessmentTimeVO existing = createTestVO();
            AssessmentTimeVO updateVO = AssessmentTimeVO.builder()
                    .id(TEST_ID)
                    .direction(Direction.STRUCTURAL_DESIGN)
                    .epoch(2)
                    .grade(2)
                    .build();

            when(assessmentTimeRepository.findById(TEST_ID)).thenReturn(Optional.of(existing));
            when(
                    assessmentTimeRepository.existsByDirectionAndEpochAndGradeAndIdNot(
                            Direction.STRUCTURAL_DESIGN,
                            2,
                            2,
                            TEST_ID)).thenReturn(true);

            IllegalArgumentException ex = assertThrows(
                    IllegalArgumentException.class,
                    () -> assessmentTimeDomainService.update(updateVO));
            assertEquals("该方向轮次年级的考核时间已存在", ex.getMessage());
        }
    }

    // ==================== delete 方法测试 ====================

    @Nested
    @DisplayName("delete 方法测试")
    class DeleteTests {

        @Test
        @DisplayName("正常删除：应成功")
        void delete_valid_shouldSucceed() {
            when(assessmentTimeRepository.existsById(TEST_ID)).thenReturn(true);
            when(assessmentTimeRepository.hasAssociatedQuestions(TEST_ID)).thenReturn(false);

            assessmentTimeDomainService.delete(TEST_ID);

            verify(assessmentTimeRepository).deleteById(TEST_ID);
        }

        @Test
        @DisplayName("考核时间不存在：应抛出IllegalArgumentException")
        void delete_notExisting_shouldThrow() {
            when(assessmentTimeRepository.existsById(TEST_ID)).thenReturn(false);

            IllegalArgumentException ex = assertThrows(
                    IllegalArgumentException.class,
                    () -> assessmentTimeDomainService.delete(TEST_ID));
            assertEquals("考核时间不存在", ex.getMessage());
            verify(assessmentTimeRepository, never()).deleteById(any());
        }

        @Test
        @DisplayName("有关联题目：应抛出DataConflict")
        void delete_withAssociatedQuestions_shouldThrow() {
            when(assessmentTimeRepository.existsById(TEST_ID)).thenReturn(true);
            when(assessmentTimeRepository.hasAssociatedQuestions(TEST_ID)).thenReturn(true);

            DataConflict ex = assertThrows(
                    DataConflict.class,
                    () -> assessmentTimeDomainService.delete(TEST_ID));
            assertEquals("存在关联的考核题目，需先删除相关题目", ex.getMessage());
            verify(assessmentTimeRepository, never()).deleteById(any());
        }
    }
}
