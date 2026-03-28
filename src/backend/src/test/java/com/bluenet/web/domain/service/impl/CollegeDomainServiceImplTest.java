package com.bluenet.web.domain.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.bluenet.web.domain.model.vo.CollegeVO;
import com.bluenet.web.domain.repository.CollegeRepository;

/**
 * CollegeDomainServiceImpl 单元测试
 * <p>
 * 测试学院领域服务的业务逻辑
 * </p>
 */
@DisplayName("CollegeDomainServiceImpl 单元测试")
@ExtendWith(MockitoExtension.class)
class CollegeDomainServiceImplTest {

    @Mock
    private CollegeRepository collegeRepository;

    @InjectMocks
    private CollegeDomainServiceImpl collegeDomainService;

    private static final Long TEST_ID = 1L;
    private static final String TEST_NAME = "计算机科学与技术学院";
    private static final String TEST_NAME_2 = "软件学院";

    private CollegeVO createTestCollegeVO() {
        return CollegeVO.builder()
                .id(TEST_ID)
                .name(TEST_NAME)
                .build();
    }

    private CollegeVO createTestCollegeVO(Long id, String name) {
        return CollegeVO.builder()
                .id(id)
                .name(name)
                .build();
    }

    // ==================== getAllColleges 方法测试 ====================

    @Nested
    @DisplayName("getAllColleges 方法测试")
    class GetAllCollegesTests {

        @Test
        @DisplayName("正常情况：应返回所有学院列表")
        void getAllColleges_withColleges_shouldReturnList() {
            // 准备
            List<CollegeVO> voList = new ArrayList<>();
            voList.add(createTestCollegeVO(1L, "计算机学院"));
            voList.add(createTestCollegeVO(2L, "软件学院"));

            when(collegeRepository.findAll()).thenReturn(voList);

            // 执行
            List<CollegeVO> result = collegeDomainService.getAllColleges();

            // 验证
            assertNotNull(result);
            assertEquals(2, result.size());
            assertEquals(1L, result.get(0).getId());
            assertEquals("计算机学院", result.get(0).getName());
            verify(collegeRepository).findAll();
        }

        @Test
        @DisplayName("无学院数据：应返回空列表")
        void getAllColleges_noColleges_shouldReturnEmptyList() {
            // 准备
            when(collegeRepository.findAll()).thenReturn(new ArrayList<>());

            // 执行
            List<CollegeVO> result = collegeDomainService.getAllColleges();

            // 验证
            assertNotNull(result);
            assertTrue(result.isEmpty());
            verify(collegeRepository).findAll();
        }
    }

    // ==================== getCollegeById 方法测试 ====================

    @Nested
    @DisplayName("getCollegeById 方法测试")
    class GetCollegeByIdTests {

        @Test
        @DisplayName("正常情况：应返回学院VO")
        void getCollegeById_existingCollege_shouldReturnVO() {
            // 准备
            CollegeVO vo = createTestCollegeVO();
            when(collegeRepository.findById(TEST_ID)).thenReturn(Optional.of(vo));

            // 执行
            Optional<CollegeVO> result = collegeDomainService.getCollegeById(TEST_ID);

            // 验证
            assertTrue(result.isPresent());
            assertEquals(TEST_ID, result.get().getId());
            assertEquals(TEST_NAME, result.get().getName());
            verify(collegeRepository).findById(TEST_ID);
        }

        @Test
        @DisplayName("学院不存在：应返回空Optional")
        void getCollegeById_nonExistingCollege_shouldReturnEmpty() {
            // 准备
            when(collegeRepository.findById(TEST_ID)).thenReturn(Optional.empty());

            // 执行
            Optional<CollegeVO> result = collegeDomainService.getCollegeById(TEST_ID);

            // 验证
            assertTrue(result.isEmpty());
            verify(collegeRepository).findById(TEST_ID);
        }
    }

    // ==================== createCollege 方法测试 ====================

    @Nested
    @DisplayName("createCollege 方法测试")
    class CreateCollegeTests {

        @Test
        @DisplayName("正常创建：应返回新学院ID")
        void createCollege_validName_shouldReturnId() {
            // 准备
            when(collegeRepository.existsByName(TEST_NAME)).thenReturn(false);
            when(collegeRepository.save(TEST_NAME)).thenReturn(TEST_ID);

            // 执行
            Long result = collegeDomainService.createCollege(TEST_NAME);

            // 验证
            assertEquals(TEST_ID, result);
            verify(collegeRepository).existsByName(TEST_NAME);
            verify(collegeRepository).save(TEST_NAME);
        }

        @Test
        @DisplayName("名称已存在：应抛出IllegalArgumentException")
        void createCollege_duplicateName_shouldThrowException() {
            // 准备
            when(collegeRepository.existsByName(TEST_NAME)).thenReturn(true);

            // 执行 & 验证
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> collegeDomainService.createCollege(TEST_NAME));
            assertEquals("学院名称已存在", exception.getMessage());

            verify(collegeRepository).existsByName(TEST_NAME);
            verify(collegeRepository, never()).save(any());
        }
    }

    // ==================== updateCollege 方法测试 ====================

    @Nested
    @DisplayName("updateCollege 方法测试")
    class UpdateCollegeTests {

        @Test
        @DisplayName("正常更新：应成功更新学院名称")
        void updateCollege_validData_shouldUpdateSuccessfully() {
            // 准备
            when(collegeRepository.existsById(TEST_ID)).thenReturn(true);
            when(collegeRepository.existsByNameAndIdNot(TEST_NAME_2, TEST_ID)).thenReturn(false);

            // 执行
            collegeDomainService.updateCollege(TEST_ID, TEST_NAME_2);

            // 验证
            verify(collegeRepository).existsById(TEST_ID);
            verify(collegeRepository).existsByNameAndIdNot(TEST_NAME_2, TEST_ID);
            verify(collegeRepository).update(TEST_ID, TEST_NAME_2);
        }

        @Test
        @DisplayName("学院不存在：应抛出IllegalArgumentException")
        void updateCollege_nonExistingCollege_shouldThrowException() {
            // 准备
            when(collegeRepository.existsById(TEST_ID)).thenReturn(false);

            // 执行 & 验证
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> collegeDomainService.updateCollege(TEST_ID, TEST_NAME_2));
            assertEquals("学院不存在", exception.getMessage());

            verify(collegeRepository).existsById(TEST_ID);
            verify(collegeRepository, never()).update(any(), any());
        }

        @Test
        @DisplayName("新名称已被其他学院使用：应抛出IllegalArgumentException")
        void updateCollege_duplicateName_shouldThrowException() {
            // 准备
            when(collegeRepository.existsById(TEST_ID)).thenReturn(true);
            when(collegeRepository.existsByNameAndIdNot(TEST_NAME_2, TEST_ID)).thenReturn(true);

            // 执行 & 验证
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> collegeDomainService.updateCollege(TEST_ID, TEST_NAME_2));
            assertEquals("学院名称已存在", exception.getMessage());

            verify(collegeRepository).existsById(TEST_ID);
            verify(collegeRepository).existsByNameAndIdNot(TEST_NAME_2, TEST_ID);
            verify(collegeRepository, never()).update(any(), any());
        }

        @Test
        @DisplayName("更新为相同名称：应允许更新（名称不变）")
        void updateCollege_sameName_shouldAllowUpdate() {
            // 准备
            when(collegeRepository.existsById(TEST_ID)).thenReturn(true);
            when(collegeRepository.existsByNameAndIdNot(TEST_NAME, TEST_ID)).thenReturn(false);

            // 执行
            collegeDomainService.updateCollege(TEST_ID, TEST_NAME);

            // 验证
            verify(collegeRepository).update(TEST_ID, TEST_NAME);
        }
    }

    // ==================== deleteCollege 方法测试 ====================

    @Nested
    @DisplayName("deleteCollege 方法测试")
    class DeleteCollegeTests {

        @Test
        @DisplayName("正常删除：应成功删除学院")
        void deleteCollege_validId_shouldDeleteSuccessfully() {
            // 准备
            when(collegeRepository.existsById(TEST_ID)).thenReturn(true);
            when(collegeRepository.hasAssociatedUsers(TEST_ID)).thenReturn(false);
            when(collegeRepository.hasAssociatedEnrolls(TEST_ID)).thenReturn(false);

            // 执行
            collegeDomainService.deleteCollege(TEST_ID);

            // 验证
            verify(collegeRepository).existsById(TEST_ID);
            verify(collegeRepository).hasAssociatedUsers(TEST_ID);
            verify(collegeRepository).hasAssociatedEnrolls(TEST_ID);
            verify(collegeRepository).deleteById(TEST_ID);
        }

        @Test
        @DisplayName("学院不存在：应抛出IllegalArgumentException")
        void deleteCollege_nonExistingCollege_shouldThrowException() {
            // 准备
            when(collegeRepository.existsById(TEST_ID)).thenReturn(false);

            // 执行 & 验证
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> collegeDomainService.deleteCollege(TEST_ID));
            assertEquals("学院不存在", exception.getMessage());

            verify(collegeRepository).existsById(TEST_ID);
            verify(collegeRepository, never()).deleteById(any());
        }

        @Test
        @DisplayName("有关联用户：应抛出IllegalArgumentException")
        void deleteCollege_withAssociatedUsers_shouldThrowException() {
            // 准备
            when(collegeRepository.existsById(TEST_ID)).thenReturn(true);
            when(collegeRepository.hasAssociatedUsers(TEST_ID)).thenReturn(true);

            // 执行 & 验证
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> collegeDomainService.deleteCollege(TEST_ID));
            assertEquals("该学院下存在关联用户，无法删除", exception.getMessage());

            verify(collegeRepository).existsById(TEST_ID);
            verify(collegeRepository).hasAssociatedUsers(TEST_ID);
            verify(collegeRepository, never()).deleteById(any());
        }

        @Test
        @DisplayName("有关联报名记录：应抛出IllegalArgumentException")
        void deleteCollege_withAssociatedEnrolls_shouldThrowException() {
            // 准备
            when(collegeRepository.existsById(TEST_ID)).thenReturn(true);
            when(collegeRepository.hasAssociatedUsers(TEST_ID)).thenReturn(false);
            when(collegeRepository.hasAssociatedEnrolls(TEST_ID)).thenReturn(true);

            // 执行 & 验证
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> collegeDomainService.deleteCollege(TEST_ID));
            assertEquals("该学院下存在关联报名记录，无法删除", exception.getMessage());

            verify(collegeRepository).existsById(TEST_ID);
            verify(collegeRepository).hasAssociatedUsers(TEST_ID);
            verify(collegeRepository).hasAssociatedEnrolls(TEST_ID);
            verify(collegeRepository, never()).deleteById(any());
        }
    }

    // ==================== existsById 方法测试 ====================

    @Nested
    @DisplayName("existsById 方法测试")
    class ExistsByIdTests {

        @Test
        @DisplayName("学院存在：应返回true")
        void existsById_existingCollege_shouldReturnTrue() {
            // 准备
            when(collegeRepository.existsById(TEST_ID)).thenReturn(true);

            // 执行
            boolean result = collegeDomainService.existsById(TEST_ID);

            // 验证
            assertTrue(result);
            verify(collegeRepository).existsById(TEST_ID);
        }

        @Test
        @DisplayName("学院不存在：应返回false")
        void existsById_nonExistingCollege_shouldReturnFalse() {
            // 准备
            when(collegeRepository.existsById(TEST_ID)).thenReturn(false);

            // 执行
            boolean result = collegeDomainService.existsById(TEST_ID);

            // 验证
            assertFalse(result);
            verify(collegeRepository).existsById(TEST_ID);
        }
    }

    // ==================== existsByName 方法测试 ====================

    @Nested
    @DisplayName("existsByName 方法测试")
    class ExistsByNameTests {

        @Test
        @DisplayName("名称已存在：应返回true")
        void existsByName_existingName_shouldReturnTrue() {
            // 准备
            when(collegeRepository.existsByName(TEST_NAME)).thenReturn(true);

            // 执行
            boolean result = collegeDomainService.existsByName(TEST_NAME);

            // 验证
            assertTrue(result);
            verify(collegeRepository).existsByName(TEST_NAME);
        }

        @Test
        @DisplayName("名称不存在：应返回false")
        void existsByName_nonExistingName_shouldReturnFalse() {
            // 准备
            when(collegeRepository.existsByName(TEST_NAME)).thenReturn(false);

            // 执行
            boolean result = collegeDomainService.existsByName(TEST_NAME);

            // 验证
            assertFalse(result);
            verify(collegeRepository).existsByName(TEST_NAME);
        }
    }

    // ==================== existsByNameAndIdNot 方法测试 ====================

    @Nested
    @DisplayName("existsByNameAndIdNot 方法测试")
    class ExistsByNameAndIdNotTests {

        @Test
        @DisplayName("排除自身后名称已存在：应返回true")
        void existsByNameAndIdNot_existingNameWithDifferentId_shouldReturnTrue() {
            // 准备
            when(collegeRepository.existsByNameAndIdNot(TEST_NAME, TEST_ID)).thenReturn(true);

            // 执行
            boolean result = collegeDomainService.existsByNameAndIdNot(TEST_NAME, TEST_ID);

            // 验证
            assertTrue(result);
            verify(collegeRepository).existsByNameAndIdNot(TEST_NAME, TEST_ID);
        }

        @Test
        @DisplayName("排除自身后名称不存在：应返回false")
        void existsByNameAndIdNot_nonExistingName_shouldReturnFalse() {
            // 准备
            when(collegeRepository.existsByNameAndIdNot(TEST_NAME, TEST_ID)).thenReturn(false);

            // 执行
            boolean result = collegeDomainService.existsByNameAndIdNot(TEST_NAME, TEST_ID);

            // 验证
            assertFalse(result);
            verify(collegeRepository).existsByNameAndIdNot(TEST_NAME, TEST_ID);
        }
    }

    // ==================== canDelete 方法测试 ====================

    @Nested
    @DisplayName("canDelete 方法测试")
    class CanDeleteTests {

        @Test
        @DisplayName("学院存在且无关联：应返回true")
        void canDelete_noAssociations_shouldReturnTrue() {
            // 准备
            when(collegeRepository.existsById(TEST_ID)).thenReturn(true);
            when(collegeRepository.hasAssociatedUsers(TEST_ID)).thenReturn(false);
            when(collegeRepository.hasAssociatedEnrolls(TEST_ID)).thenReturn(false);

            // 执行
            boolean result = collegeDomainService.canDelete(TEST_ID);

            // 验证
            assertTrue(result);
        }

        @Test
        @DisplayName("学院不存在：应返回false")
        void canDelete_nonExistingCollege_shouldReturnFalse() {
            // 准备
            when(collegeRepository.existsById(TEST_ID)).thenReturn(false);

            // 执行
            boolean result = collegeDomainService.canDelete(TEST_ID);

            // 验证
            assertFalse(result);
        }

        @Test
        @DisplayName("有关联用户：应返回false")
        void canDelete_withAssociatedUsers_shouldReturnFalse() {
            // 准备
            when(collegeRepository.existsById(TEST_ID)).thenReturn(true);
            when(collegeRepository.hasAssociatedUsers(TEST_ID)).thenReturn(true);

            // 执行
            boolean result = collegeDomainService.canDelete(TEST_ID);

            // 验证
            assertFalse(result);
        }

        @Test
        @DisplayName("有关联报名记录：应返回false")
        void canDelete_withAssociatedEnrolls_shouldReturnFalse() {
            // 准备
            when(collegeRepository.existsById(TEST_ID)).thenReturn(true);
            when(collegeRepository.hasAssociatedUsers(TEST_ID)).thenReturn(false);
            when(collegeRepository.hasAssociatedEnrolls(TEST_ID)).thenReturn(true);

            // 执行
            boolean result = collegeDomainService.canDelete(TEST_ID);

            // 验证
            assertFalse(result);
        }
    }
}
