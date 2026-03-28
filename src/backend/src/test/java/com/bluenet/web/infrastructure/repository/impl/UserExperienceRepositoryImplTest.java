package com.bluenet.web.infrastructure.repository.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.bluenet.web.domain.model.entity.UserExperience;
import com.bluenet.web.domain.model.enumerate.ExperienceType;
import com.bluenet.web.domain.model.vo.ExperienceVO;
import com.bluenet.web.infrastructure.repository.mapper.UserExperienceMapper;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * UserExperienceRepositoryImpl 单元测试
 */
@DisplayName("UserExperienceRepositoryImpl 单元测试")
@ExtendWith(MockitoExtension.class)
class UserExperienceRepositoryImplTest {

    @Mock
    private UserExperienceMapper userExperienceMapper;

    @InjectMocks
    private UserExperienceRepositoryImpl userExperienceRepository;

    private static final Long USER_ID = 1L;
    private static final Long OTHER_USER_ID = 2L;
    private static final Long EXPERIENCE_ID = 100L;

    private UserExperience createTestExperience(Long id, Long userId, ExperienceType type, String title) {
        UserExperience experience = new UserExperience();
        experience.setId(id);
        experience.setUserId(userId);
        experience.setType(type);
        experience.setTitle(title);
        experience.setContent("{\"role\":\"开发者\",\"description\":\"测试内容\"}");
        experience.setStartTime(LocalDateTime.of(2024, 1, 1, 0, 0));
        experience.setEndTime(LocalDateTime.of(2024, 6, 1, 0, 0));
        return experience;
    }

    @Nested
    @DisplayName("findById 方法测试")
    class FindByIdTests {

        @Test
        @DisplayName("经历存在：应返回ExperienceVO")
        void findById_existingExperience_shouldReturnVO() {
            UserExperience experience = createTestExperience(EXPERIENCE_ID, USER_ID, ExperienceType.PROJECT, "项目1");

            when(userExperienceMapper.selectById(EXPERIENCE_ID)).thenReturn(experience);

            Optional<ExperienceVO> result = userExperienceRepository.findById(EXPERIENCE_ID);

            assertTrue(result.isPresent());
            assertEquals(EXPERIENCE_ID, result.get().getId());
            assertEquals(ExperienceType.PROJECT, result.get().getType());
            assertEquals("项目1", result.get().getTitle());
            verify(userExperienceMapper).selectById(EXPERIENCE_ID);
        }

        @Test
        @DisplayName("经历不存在：应返回空Optional")
        void findById_nonExistingExperience_shouldReturnEmpty() {
            when(userExperienceMapper.selectById(EXPERIENCE_ID)).thenReturn(null);

            Optional<ExperienceVO> result = userExperienceRepository.findById(EXPERIENCE_ID);

            assertFalse(result.isPresent());
            verify(userExperienceMapper).selectById(EXPERIENCE_ID);
        }
    }

    @Nested
    @DisplayName("findByUserId 方法测试")
    class FindByUserIdTests {

        @Test
        @DisplayName("用户有经历：应返回经历列表")
        void findByUserId_userHasExperiences_shouldReturnList() {
            List<UserExperience> experiences = Arrays.asList(
                    createTestExperience(100L, USER_ID, ExperienceType.PROJECT, "项目1"),
                    createTestExperience(101L, USER_ID, ExperienceType.COMPETITION, "竞赛1"));

            when(userExperienceMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(experiences);

            List<ExperienceVO> result = userExperienceRepository.findByUserId(USER_ID);

            assertNotNull(result);
            assertEquals(2, result.size());
            verify(userExperienceMapper).selectList(any(LambdaQueryWrapper.class));
        }

        @Test
        @DisplayName("用户无经历：应返回空列表")
        void findByUserId_userHasNoExperiences_shouldReturnEmptyList() {
            when(userExperienceMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(Collections.emptyList());

            List<ExperienceVO> result = userExperienceRepository.findByUserId(USER_ID);

            assertNotNull(result);
            assertTrue(result.isEmpty());
        }
    }

    @Nested
    @DisplayName("findByUserIdAndType 方法测试")
    class FindByUserIdAndTypeTests {

        @Test
        @DisplayName("按类型筛选：应返回指定类型的经历")
        void findByUserIdAndType_shouldFilterByType() {
            List<UserExperience> experiences = Arrays.asList(
                    createTestExperience(100L, USER_ID, ExperienceType.PROJECT, "项目1"),
                    createTestExperience(101L, USER_ID, ExperienceType.PROJECT, "项目2"));

            when(userExperienceMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(experiences);

            List<ExperienceVO> result = userExperienceRepository.findByUserIdAndType(USER_ID, ExperienceType.PROJECT);

            assertNotNull(result);
            assertEquals(2, result.size());
            result.forEach(vo -> assertEquals(ExperienceType.PROJECT, vo.getType()));
        }

        @Test
        @DisplayName("无匹配类型：应返回空列表")
        void findByUserIdAndType_noMatch_shouldReturnEmptyList() {
            when(userExperienceMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(Collections.emptyList());

            List<ExperienceVO> result = userExperienceRepository
                    .findByUserIdAndType(USER_ID, ExperienceType.INTERNSHIP);

            assertNotNull(result);
            assertTrue(result.isEmpty());
        }
    }

    @Nested
    @DisplayName("save 方法测试")
    class SaveTests {

        @Test
        @DisplayName("保存经历：应成功保存并返回VO")
        void save_validData_shouldSaveAndReturnVO() {
            when(userExperienceMapper.insert(any(UserExperience.class))).thenAnswer(invocation -> {
                UserExperience experience = invocation.getArgument(0);
                experience.setId(EXPERIENCE_ID);
                return 1;
            });

            ExperienceVO result = userExperienceRepository.save(
                    USER_ID,
                    ExperienceType.PROJECT,
                    "新项目",
                    "2024.01",
                    "2024.06",
                    "{\"role\":\"开发者\"}");

            assertNotNull(result);
            assertEquals(EXPERIENCE_ID, result.getId());
            assertEquals("新项目", result.getTitle());
            verify(userExperienceMapper).insert(any(UserExperience.class));
        }
    }

    @Nested
    @DisplayName("update 方法测试")
    class UpdateTests {

        @Test
        @DisplayName("更新经历：应成功更新并返回VO")
        void update_existingExperience_shouldUpdateAndReturnVO() {
            UserExperience existingExperience = createTestExperience(
                    EXPERIENCE_ID,
                    USER_ID,
                    ExperienceType.PROJECT,
                    "原项目");

            when(userExperienceMapper.selectById(EXPERIENCE_ID)).thenReturn(existingExperience);
            when(userExperienceMapper.updateById(any(UserExperience.class))).thenReturn(1);

            ExperienceVO result = userExperienceRepository.update(
                    EXPERIENCE_ID,
                    "更新后的项目",
                    "2024.01",
                    "2024.12",
                    "{\"role\":\"负责人\"}");

            assertNotNull(result);
            assertEquals("更新后的项目", result.getTitle());
            verify(userExperienceMapper).updateById(any(UserExperience.class));
        }

        @Test
        @DisplayName("经历不存在：应抛出IllegalArgumentException")
        void update_nonExistingExperience_shouldThrowException() {
            when(userExperienceMapper.selectById(EXPERIENCE_ID)).thenReturn(null);

            assertThrows(
                    IllegalArgumentException.class,
                    () -> userExperienceRepository.update(EXPERIENCE_ID, "更新", "2024.01", "2024.06", "{}"));
        }
    }

    @Nested
    @DisplayName("deleteById 方法测试")
    class DeleteByIdTests {

        @Test
        @DisplayName("删除成功：应返回true")
        void deleteById_existingExperience_shouldReturnTrue() {
            when(userExperienceMapper.deleteById(EXPERIENCE_ID)).thenReturn(1);

            boolean result = userExperienceRepository.deleteById(EXPERIENCE_ID);

            assertTrue(result);
            verify(userExperienceMapper).deleteById(EXPERIENCE_ID);
        }

        @Test
        @DisplayName("删除失败：应返回false")
        void deleteById_nonExistingExperience_shouldReturnFalse() {
            when(userExperienceMapper.deleteById(EXPERIENCE_ID)).thenReturn(0);

            boolean result = userExperienceRepository.deleteById(EXPERIENCE_ID);

            assertFalse(result);
        }
    }

    @Nested
    @DisplayName("countByUserIdAndType 方法测试")
    class CountByUserIdAndTypeTests {

        @Test
        @DisplayName("统计数量：应返回正确数量")
        void countByUserIdAndType_shouldReturnCorrectCount() {
            when(userExperienceMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(5L);

            int result = userExperienceRepository.countByUserIdAndType(USER_ID, ExperienceType.PROJECT);

            assertEquals(5, result);
        }

        @Test
        @DisplayName("无数据：应返回0")
        void countByUserIdAndType_noData_shouldReturnZero() {
            when(userExperienceMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);

            int result = userExperienceRepository.countByUserIdAndType(USER_ID, ExperienceType.PROJECT);

            assertEquals(0, result);
        }
    }

    @Nested
    @DisplayName("checkOwner 方法测试")
    class CheckOwnerTests {

        @Test
        @DisplayName("是所有者：应返回true")
        void checkOwner_isOwner_shouldReturnTrue() {
            UserExperience experience = createTestExperience(EXPERIENCE_ID, USER_ID, ExperienceType.PROJECT, "项目1");

            when(userExperienceMapper.selectById(EXPERIENCE_ID)).thenReturn(experience);

            boolean result = userExperienceRepository.checkOwner(EXPERIENCE_ID, USER_ID);

            assertTrue(result);
        }

        @Test
        @DisplayName("不是所有者：应返回false")
        void checkOwner_notOwner_shouldReturnFalse() {
            UserExperience experience = createTestExperience(EXPERIENCE_ID, USER_ID, ExperienceType.PROJECT, "项目1");

            when(userExperienceMapper.selectById(EXPERIENCE_ID)).thenReturn(experience);

            boolean result = userExperienceRepository.checkOwner(EXPERIENCE_ID, OTHER_USER_ID);

            assertFalse(result);
        }

        @Test
        @DisplayName("经历不存在：应返回false")
        void checkOwner_experienceNotExists_shouldReturnFalse() {
            when(userExperienceMapper.selectById(EXPERIENCE_ID)).thenReturn(null);

            boolean result = userExperienceRepository.checkOwner(EXPERIENCE_ID, USER_ID);

            assertFalse(result);
        }
    }
}
