package com.bluenet.web.infrastructure.repository.impl;

import com.bluenet.web.infrastructure.repository.dataobject.*;

import com.bluenet.web.testsupport.RepositoryTestObjects;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bluenet.web.domain.model.entity.AssessmentQuestion;
import com.bluenet.web.domain.model.enumerate.QuestionType;
import com.bluenet.web.domain.model.vo.AssessmentQuestionVO;
import com.bluenet.web.infrastructure.repository.mapper.AssessmentQuestionMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;

@DisplayName("AssessmentQuestionRepositoryImpl 单元测试")
@ExtendWith(MockitoExtension.class)
class AssessmentQuestionRepositoryImplTest {

    @Mock
    private AssessmentQuestionMapper assessmentQuestionMapper;

    @InjectMocks
    private AssessmentQuestionRepositoryImpl assessmentQuestionRepository;

    private static final Long TEST_ID = 1L;
    private static final Long TEST_TIME_ID = 100L;

    private AssessmentQuestion createTestEntity() {
        AssessmentQuestion entity = new AssessmentQuestion();
        entity.setId(TEST_ID);
        entity.setAssessmentTimeId(TEST_TIME_ID);
        entity.setQuestionNo(1);
        entity.setQuestionType(QuestionType.SINGLE_CHOICE);
        entity.setTitle("测试题目");
        entity.setScore(BigDecimal.TEN);
        return entity;
    }

    @Nested
    @DisplayName("findAllByTimeId 方法测试")
    class FindAllByTimeIdTests {

        @Test
        @DisplayName("正常分页查询：应返回按questionNo升序的分页结果")
        void findAllByTimeId_withData_shouldReturnPagedResult() {
            AssessmentQuestion q1 = createTestEntity();
            q1.setId(1L);
            q1.setQuestionNo(1);
            AssessmentQuestion q2 = createTestEntity();
            q2.setId(2L);
            q2.setQuestionNo(2);

            Page<AssessmentQuestionDO> mockPage = new Page<>(1, 10, 2);
            mockPage.setRecords(
                    List.of(
                            RepositoryTestObjects.toDataObject(q1, AssessmentQuestionDO.class),
                            RepositoryTestObjects.toDataObject(q2, AssessmentQuestionDO.class)));

            when(assessmentQuestionMapper.selectPageByAssessmentTimeId(any(Page.class), eq(TEST_TIME_ID)))
                    .thenReturn(mockPage);

            org.springframework.data.domain.Page<AssessmentQuestionVO> result = assessmentQuestionRepository
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

            org.springframework.data.domain.Page<AssessmentQuestionVO> result = assessmentQuestionRepository
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
            AssessmentQuestionVO vo = AssessmentQuestionVO.builder()
                    .id(TEST_ID)
                    .title("更新后的标题")
                    .score(BigDecimal.valueOf(20))
                    .build();

            when(assessmentQuestionMapper.updateById(any(AssessmentQuestionDO.class))).thenReturn(1);

            assessmentQuestionRepository.update(vo);

            verify(assessmentQuestionMapper).updateById(any(AssessmentQuestionDO.class));
        }

        @Test
        @DisplayName("更新不存在的考题：影响行数为0")
        void update_nonExistingQuestion_shouldReturnZero() {
            AssessmentQuestionVO vo = AssessmentQuestionVO.builder()
                    .id(999L)
                    .title("不存在的题目")
                    .build();

            when(assessmentQuestionMapper.updateById(any(AssessmentQuestionDO.class))).thenReturn(0);

            assessmentQuestionRepository.update(vo);

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

            Optional<AssessmentQuestionVO> result = assessmentQuestionRepository
                    .findByTimeIdAndQuestionNo(TEST_TIME_ID, 1);

            assertTrue(result.isPresent());
            assertEquals(TEST_TIME_ID, result.get().getAssessmentTimeId());
        }

        @Test
        @DisplayName("题号不存在：应返回空Optional")
        void findByTimeIdAndQuestionNo_notExisting_shouldReturnEmpty() {
            when(assessmentQuestionMapper.selectByAssessmentTimeIdAndQuestionNo(TEST_TIME_ID, 99)).thenReturn(null);

            Optional<AssessmentQuestionVO> result = assessmentQuestionRepository
                    .findByTimeIdAndQuestionNo(TEST_TIME_ID, 99);

            assertTrue(result.isEmpty());
        }
    }
}
