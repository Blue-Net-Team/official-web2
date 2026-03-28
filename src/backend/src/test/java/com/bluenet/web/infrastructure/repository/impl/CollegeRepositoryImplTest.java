package com.bluenet.web.infrastructure.repository.impl;

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

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.bluenet.web.domain.model.entity.College;
import com.bluenet.web.domain.model.vo.CollegeVO;
import com.bluenet.web.infrastructure.repository.mapper.CollegeMapper;
import com.bluenet.web.infrastructure.repository.mapper.EnrollMapper;
import com.bluenet.web.infrastructure.repository.mapper.UserMapper;

/**
 * CollegeRepositoryImpl 单元测试
 * <p>
 * 测试学院仓库层的数据操作
 * </p>
 */
@DisplayName("CollegeRepositoryImpl 单元测试")
@ExtendWith(MockitoExtension.class)
class CollegeRepositoryImplTest {

    @Mock
    private CollegeMapper collegeMapper;

    @Mock
    private UserMapper userMapper;

    @Mock
    private EnrollMapper enrollMapper;

    @InjectMocks
    private CollegeRepositoryImpl collegeRepository;

    private static final Long TEST_ID = 1L;
    private static final String TEST_NAME = "计算机科学与技术学院";
    private static final String TEST_NAME_2 = "软件学院";

    private College createTestCollege() {
        College college = new College();
        college.setId(TEST_ID);
        college.setName(TEST_NAME);
        return college;
    }

    private College createTestCollege(Long id, String name) {
        College college = new College();
        college.setId(id);
        college.setName(name);
        return college;
    }

    // ==================== findAll 方法测试 ====================

    @Nested
    @DisplayName("findAll 方法测试")
    class FindAllTests {

        @Test
        @DisplayName("正常情况：应返回所有学院VO列表")
        void findAll_withColleges_shouldReturnVOList() {
            // 准备
            List<College> collegeList = new ArrayList<>();
            collegeList.add(createTestCollege(1L, "计算机学院"));
            collegeList.add(createTestCollege(2L, "软件学院"));

            when(collegeMapper.selectList(null)).thenReturn(collegeList);

            // 执行
            List<CollegeVO> result = collegeRepository.findAll();

            // 验证
            assertNotNull(result);
            assertEquals(2, result.size());
            assertEquals(1L, result.get(0).getId());
            assertEquals("计算机学院", result.get(0).getName());
            assertEquals(2L, result.get(1).getId());
            assertEquals("软件学院", result.get(1).getName());
            verify(collegeMapper).selectList(null);
        }

        @Test
        @DisplayName("无学院数据：应返回空列表")
        void findAll_noColleges_shouldReturnEmptyList() {
            // 准备
            when(collegeMapper.selectList(null)).thenReturn(new ArrayList<>());

            // 执行
            List<CollegeVO> result = collegeRepository.findAll();

            // 验证
            assertNotNull(result);
            assertTrue(result.isEmpty());
            verify(collegeMapper).selectList(null);
        }
    }

    // ==================== findById 方法测试 ====================

    @Nested
    @DisplayName("findById 方法测试")
    class FindByIdTests {

        @Test
        @DisplayName("正常情况：应返回学院VO")
        void findById_existingCollege_shouldReturnVO() {
            // 准备
            College college = createTestCollege();
            when(collegeMapper.selectById(TEST_ID)).thenReturn(college);

            // 执行
            Optional<CollegeVO> result = collegeRepository.findById(TEST_ID);

            // 验证
            assertTrue(result.isPresent());
            assertEquals(TEST_ID, result.get().getId());
            assertEquals(TEST_NAME, result.get().getName());
            verify(collegeMapper).selectById(TEST_ID);
        }

        @Test
        @DisplayName("学院不存在：应返回空Optional")
        void findById_nonExistingCollege_shouldReturnEmpty() {
            // 准备
            when(collegeMapper.selectById(TEST_ID)).thenReturn(null);

            // 执行
            Optional<CollegeVO> result = collegeRepository.findById(TEST_ID);

            // 验证
            assertTrue(result.isEmpty());
            verify(collegeMapper).selectById(TEST_ID);
        }

        @Test
        @DisplayName("ID为null：应调用mapper并返回空")
        void findById_nullId_shouldHandleGracefully() {
            // 准备
            when(collegeMapper.selectById(null)).thenReturn(null);

            // 执行
            Optional<CollegeVO> result = collegeRepository.findById(null);

            // 验证
            assertTrue(result.isEmpty());
        }
    }

    // ==================== save 方法测试 ====================

    @Nested
    @DisplayName("save 方法测试")
    class SaveTests {

        @Test
        @DisplayName("正常保存：应返回生成的ID")
        void save_validName_shouldReturnId() {
            // 准备
            when(collegeMapper.insert(any(College.class))).thenAnswer(invocation -> {
                College college = invocation.getArgument(0, College.class);
                college.setId(TEST_ID);
                return 1;
            });

            // 执行
            Long result = collegeRepository.save(TEST_NAME);

            // 验证
            assertEquals(TEST_ID, result);
            verify(collegeMapper).insert(any(College.class));
        }

        @Test
        @DisplayName("保存时名称应正确设置")
        void save_shouldSetNameCorrectly() {
            // 准备
            when(collegeMapper.insert(any(College.class))).thenAnswer(invocation -> {
                College college = invocation.getArgument(0);
                college.setId(TEST_ID);
                assertEquals(TEST_NAME, college.getName());
                return 1;
            });

            // 执行
            collegeRepository.save(TEST_NAME);

            // 验证
            verify(collegeMapper).insert(any(College.class));
        }
    }

    // ==================== update 方法测试 ====================

    @Nested
    @DisplayName("update 方法测试")
    class UpdateTests {

        @Test
        @DisplayName("正常更新：应调用mapper更新")
        void update_validData_shouldCallMapperUpdate() {
            // 准备
            when(collegeMapper.updateById(any(College.class))).thenAnswer(invocation -> 1);

            // 执行
            collegeRepository.update(TEST_ID, TEST_NAME_2);

            // 验证
            verify(collegeMapper)
                    .updateById(argThat((College c) -> TEST_ID.equals(c.getId()) && TEST_NAME_2.equals(c.getName())));
        }
    }

    // ==================== deleteById 方法测试 ====================

    @Nested
    @DisplayName("deleteById 方法测试")
    class DeleteByIdTests {

        @Test
        @DisplayName("正常删除：应调用mapper删除")
        void deleteById_existingId_shouldCallMapperDelete() {
            // 准备
            when(collegeMapper.deleteById(TEST_ID)).thenReturn(1);

            // 执行
            collegeRepository.deleteById(TEST_ID);

            // 验证
            verify(collegeMapper).deleteById(TEST_ID);
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
            when(collegeMapper.selectById(TEST_ID)).thenReturn(createTestCollege());

            // 执行
            boolean result = collegeRepository.existsById(TEST_ID);

            // 验证
            assertTrue(result);
            verify(collegeMapper).selectById(TEST_ID);
        }

        @Test
        @DisplayName("学院不存在：应返回false")
        void existsById_nonExistingCollege_shouldReturnFalse() {
            // 准备
            when(collegeMapper.selectById(TEST_ID)).thenReturn(null);

            // 执行
            boolean result = collegeRepository.existsById(TEST_ID);

            // 验证
            assertFalse(result);
            verify(collegeMapper).selectById(TEST_ID);
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
            when(collegeMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(1L);

            // 执行
            boolean result = collegeRepository.existsByName(TEST_NAME);

            // 验证
            assertTrue(result);
            verify(collegeMapper).selectCount(any(LambdaQueryWrapper.class));
        }

        @Test
        @DisplayName("名称不存在：应返回false")
        void existsByName_nonExistingName_shouldReturnFalse() {
            // 准备
            when(collegeMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);

            // 执行
            boolean result = collegeRepository.existsByName(TEST_NAME);

            // 验证
            assertFalse(result);
            verify(collegeMapper).selectCount(any(LambdaQueryWrapper.class));
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
            when(collegeMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(1L);

            // 执行
            boolean result = collegeRepository.existsByNameAndIdNot(TEST_NAME, TEST_ID);

            // 验证
            assertTrue(result);
            verify(collegeMapper).selectCount(any(LambdaQueryWrapper.class));
        }

        @Test
        @DisplayName("排除自身后名称不存在：应返回false")
        void existsByNameAndIdNot_nonExistingName_shouldReturnFalse() {
            // 准备
            when(collegeMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);

            // 执行
            boolean result = collegeRepository.existsByNameAndIdNot(TEST_NAME, TEST_ID);

            // 验证
            assertFalse(result);
            verify(collegeMapper).selectCount(any(LambdaQueryWrapper.class));
        }
    }

    // ==================== hasAssociatedUsers 方法测试 ====================

    @Nested
    @DisplayName("hasAssociatedUsers 方法测试")
    class HasAssociatedUsersTests {

        @Test
        @DisplayName("有关联用户：应返回true")
        void hasAssociatedUsers_withUsers_shouldReturnTrue() {
            // 准备
            when(userMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(5L);

            // 执行
            boolean result = collegeRepository.hasAssociatedUsers(TEST_ID);

            // 验证
            assertTrue(result);
            verify(userMapper).selectCount(any(LambdaQueryWrapper.class));
        }

        @Test
        @DisplayName("无关联用户：应返回false")
        void hasAssociatedUsers_noUsers_shouldReturnFalse() {
            // 准备
            when(userMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);

            // 执行
            boolean result = collegeRepository.hasAssociatedUsers(TEST_ID);

            // 验证
            assertFalse(result);
            verify(userMapper).selectCount(any(LambdaQueryWrapper.class));
        }
    }

    // ==================== hasAssociatedEnrolls 方法测试 ====================

    @Nested
    @DisplayName("hasAssociatedEnrolls 方法测试")
    class HasAssociatedEnrollsTests {

        @Test
        @DisplayName("有关联报名记录：应返回true")
        void hasAssociatedEnrolls_withEnrolls_shouldReturnTrue() {
            // 准备
            when(enrollMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(10L);

            // 执行
            boolean result = collegeRepository.hasAssociatedEnrolls(TEST_ID);

            // 验证
            assertTrue(result);
            verify(enrollMapper).selectCount(any(LambdaQueryWrapper.class));
        }

        @Test
        @DisplayName("无关联报名记录：应返回false")
        void hasAssociatedEnrolls_noEnrolls_shouldReturnFalse() {
            // 准备
            when(enrollMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);

            // 执行
            boolean result = collegeRepository.hasAssociatedEnrolls(TEST_ID);

            // 验证
            assertFalse(result);
            verify(enrollMapper).selectCount(any(LambdaQueryWrapper.class));
        }
    }
}
