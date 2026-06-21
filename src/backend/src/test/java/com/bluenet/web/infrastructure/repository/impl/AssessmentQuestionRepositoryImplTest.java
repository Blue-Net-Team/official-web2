package com.bluenet.web.infrastructure.repository.impl;

import com.bluenet.web.infrastructure.repository.converter.AssessmentQuestionRepositoryConverter;
import com.bluenet.web.infrastructure.repository.dataobject.*;

import com.bluenet.web.testsupport.RepositoryTestObjects;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bluenet.web.domain.model.entity.AssessmentQuestion;
import com.bluenet.web.domain.model.enumerate.QuestionType;
import com.bluenet.web.infrastructure.repository.mapper.AssessmentQuestionMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;

@DisplayName("AssessmentQuestionRepositoryImpl 单元测试")
@ExtendWith(MockitoExtension.class)
class AssessmentQuestionRepositoryImplTest {

    @Mock
    private AssessmentQuestionMapper assessmentQuestionMapper;

    @Spy
    private AssessmentQuestionRepositoryConverter assessmentQuestionRepositoryConverter = new AssessmentQuestionRepositoryConverter();

    @InjectMocks
    private AssessmentQuestionRepositoryImpl assessmentQuestionRepository;

    private static final Long TEST_ID = 1L;
    private static final Long TEST_TIME_ID = 100L;

    private AssessmentQuestion createTestEntity() {
        return AssessmentQuestion.reconstruct(
                TEST_ID,
                TEST_TIME_ID,
                1,
                QuestionType.SINGLE_CHOICE,
                "测试题目",
                null,
                null,
                BigDecimal.TEN);
    }

    @Nested
    @DisplayName("countByAssessmentTimeIds 方法测试")
    class CountByAssessmentTimeIdsTests {

        @Test
        @DisplayName("空列表：应返回空映射")
        void countByAssessmentTimeIds_emptyList_shouldReturnEmptyMap() {
            Map<Long, Integer> result = assessmentQuestionRepository.countByAssessmentTimeIds(Collections.emptyList());

            assertTrue(result.isEmpty());
            verify(assessmentQuestionMapper, never()).countByAssessmentTimeIds(anyList());
        }

        @Test
        @DisplayName("部分命中：应返回对应计数")
        void countByAssessmentTimeIds_partialHits_shouldReturnCounts() {
            when(assessmentQuestionMapper.countByAssessmentTimeIds(List.of(1L, 2L, 3L)))
                    .thenReturn(
                            List.of(
                                    new AssessmentQuestionCountResult(1L, 5L),
                                    new AssessmentQuestionCountResult(3L, 8L)));

            Map<Long, Integer> result = assessmentQuestionRepository.countByAssessmentTimeIds(List.of(1L, 2L, 3L));

            assertEquals(2, result.size());
            assertEquals(5, result.get(1L));
            assertEquals(8, result.get(3L));
            assertNull(result.get(2L));
        }

        @Test
        @DisplayName("全部命中：应返回所有计数")
        void countByAssessmentTimeIds_allHits_shouldReturnAllCounts() {
            when(assessmentQuestionMapper.countByAssessmentTimeIds(List.of(1L, 2L)))
                    .thenReturn(
                            List.of(
                                    new AssessmentQuestionCountResult(1L, 5L),
                                    new AssessmentQuestionCountResult(2L, 10L)));

            Map<Long, Integer> result = assessmentQuestionRepository.countByAssessmentTimeIds(List.of(1L, 2L));

            assertEquals(2, result.size());
            assertEquals(5, result.get(1L));
            assertEquals(10, result.get(2L));
        }
    }

    @Nested
    @DisplayName("findAllByTimeId 方法测试")
    class FindAllByTimeIdTests {

        @Test
        @DisplayName("正常分页查询：应返回按questionNo升序的分页结果")
        void findAllByTimeId_withData_shouldReturnPagedResult() {
            AssessmentQuestion q1 = createTestEntity();
            AssessmentQuestion q2 = AssessmentQuestion.reconstruct(
                    2L,
                    TEST_TIME_ID,
                    2,
                    QuestionType.SINGLE_CHOICE,
                    "测试题目2",
                    null,
                    null,
                    BigDecimal.TEN);

            Page<AssessmentQuestionDO> mockPage = new Page<>(1, 10, 2);
            mockPage.setRecords(
                    List.of(
                            RepositoryTestObjects.toDataObject(q1, AssessmentQuestionDO.class),
                            RepositoryTestObjects.toDataObject(q2, AssessmentQuestionDO.class)));

            when(assessmentQuestionMapper.selectPageByAssessmentTimeId(any(Page.class), eq(TEST_TIME_ID)))
                    .thenReturn(mockPage);

            org.springframework.data.domain.Page<AssessmentQuestion> result = assessmentQuestionRepository
                    .findAllByTimeId(TEST_TIME_ID, Pageable.ofSize(10));

            assertEquals(2, result.getContent().size());
            assertEquals(2, result.getTotalElements());
            assertEquals(1, result.getContent().get(0).getQuestionNo());
        }

        @Test
        @DisplayName("无数据时：应返回空分页结果")
        void findAllByTimeId_noData_shouldReturnEmptyPage() {
            Page<AssessmentQuestionDO> mockPage = new Page<>(1, 10, 0);
            mockPage.setRecords(List.of());

            when(assessmentQuestionMapper.selectPageByAssessmentTimeId(any(Page.class), eq(TEST_TIME_ID)))
                    .thenReturn(mockPage);

            org.springframework.data.domain.Page<AssessmentQuestion> result = assessmentQuestionRepository
                    .findAllByTimeId(TEST_TIME_ID, Pageable.ofSize(10));

            assertTrue(result.getContent().isEmpty());
            assertEquals(0, result.getTotalElements());
        }
    }

    @Nested
    @DisplayName("update 方法测试")
    class UpdateTests {

        @Test
        @DisplayName("正常更新：应调用mapper更新并返回影响行数")
        void update_withExistingQuestion_shouldUpdateSuccessfully() {
            AssessmentQuestion entity = AssessmentQuestion.reconstruct(
                    TEST_ID,
                    TEST_TIME_ID,
                    1,
                    QuestionType.SINGLE_CHOICE,
                    "更新后的标题",
                    null,
                    null,
                    BigDecimal.valueOf(20));

            when(assessmentQuestionMapper.updateById(any(AssessmentQuestionDO.class))).thenReturn(1);

            assessmentQuestionRepository.update(entity);

            verify(assessmentQuestionMapper).updateById(any(AssessmentQuestionDO.class));
        }

        @Test
        @DisplayName("更新不存在的考题：影响行数为0")
        void update_nonExistingQuestion_shouldReturnZero() {
            AssessmentQuestion entity = AssessmentQuestion.reconstruct(
                    999L,
                    TEST_TIME_ID,
                    1,
                    QuestionType.SINGLE_CHOICE,
                    "不存在的题目",
                    null,
                    null,
                    null);

            when(assessmentQuestionMapper.updateById(any(AssessmentQuestionDO.class))).thenReturn(0);

            assessmentQuestionRepository.update(entity);

            verify(assessmentQuestionMapper).updateById(any(AssessmentQuestionDO.class));
        }
    }

    @Nested
    @DisplayName("deleteById 方法测试")
    class DeleteByIdTests {

        @Test
        @DisplayName("正常删除：应调用mapper删除")
        void deleteById_existingQuestion_shouldDeleteSuccessfully() {
            when(assessmentQuestionMapper.deleteById(TEST_ID)).thenReturn(1);

            assessmentQuestionRepository.deleteById(TEST_ID);

            verify(assessmentQuestionMapper).deleteById(TEST_ID);
        }

        @Test
        @DisplayName("删除不存在的考题：影响行数为0")
        void deleteById_nonExistingQuestion_shouldReturnZero() {
            when(assessmentQuestionMapper.deleteById(999L)).thenReturn(0);

            assessmentQuestionRepository.deleteById(999L);

            verify(assessmentQuestionMapper).deleteById(999L);
        }
    }

    @Nested
    @DisplayName("existsById 方法测试")
    class ExistsByIdTests {

        @Test
        @DisplayName("存在时：应返回true")
        void existsById_existingQuestion_shouldReturnTrue() {
            when(assessmentQuestionMapper.selectById(TEST_ID))
                    .thenReturn(RepositoryTestObjects.toDataObject(createTestEntity(), AssessmentQuestionDO.class));

            assertTrue(assessmentQuestionRepository.existsById(TEST_ID));
        }

        @Test
        @DisplayName("不存在时：应返回false")
        void existsById_nonExistingQuestion_shouldReturnFalse() {
            when(assessmentQuestionMapper.selectById(999L))
                    .thenReturn(RepositoryTestObjects.toDataObject(null, AssessmentQuestionDO.class));

            assertFalse(assessmentQuestionRepository.existsById(999L));
        }
    }

    @Nested
    @DisplayName("findByTimeIdAndQuestionNo 方法测试")
    class FindByTimeIdAndQuestionNoTests {

        @Test
        @DisplayName("题号已存在：应返回Optional包含结果")
        void findByTimeIdAndQuestionNo_existing_shouldReturnPresent() {
            when(assessmentQuestionMapper.selectByAssessmentTimeIdAndQuestionNo(TEST_TIME_ID, 1))
                    .thenReturn(RepositoryTestObjects.toDataObject(createTestEntity(), AssessmentQuestionDO.class));

            Optional<AssessmentQuestion> result = assessmentQuestionRepository
                    .findByTimeIdAndQuestionNo(TEST_TIME_ID, 1);

            assertTrue(result.isPresent());
            assertEquals(TEST_TIME_ID, result.get().getAssessmentTimeId());
        }

        @Test
        @DisplayName("题号不存在：应返回空Optional")
        void findByTimeIdAndQuestionNo_notExisting_shouldReturnEmpty() {
            when(assessmentQuestionMapper.selectByAssessmentTimeIdAndQuestionNo(TEST_TIME_ID, 99)).thenReturn(null);

            Optional<AssessmentQuestion> result = assessmentQuestionRepository
                    .findByTimeIdAndQuestionNo(TEST_TIME_ID, 99);

            assertTrue(result.isEmpty());
        }
    }
}
