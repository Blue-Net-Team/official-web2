package com.bluenet.web.infrastructure.repository.impl;

import com.bluenet.web.infrastructure.repository.dataobject.*;

import com.bluenet.web.testsupport.RepositoryTestObjects;

import com.bluenet.web.domain.model.entity.UserExperience;
import com.bluenet.web.domain.model.enumerate.ExperienceType;
import com.bluenet.web.infrastructure.repository.converter.UserExperienceRepositoryConverter;
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

    @Mock
    private UserExperienceRepositoryConverter converter;

    @InjectMocks
    private UserExperienceRepositoryImpl userExperienceRepository;

    private static final Long USER_ID = 1L;
    private static final Long OTHER_USER_ID = 2L;
    private static final Long EXPERIENCE_ID = 100L;

    private UserExperience createTestExperience(Long id, Long userId, ExperienceType type, String title) {
        return UserExperience.reconstruct(
                id,
                userId,
                type,
                title,
                "{\"role\":\"开发者\",\"description\":\"测试内容\"}",
                LocalDateTime.of(2024, 1, 1, 0, 0),
                LocalDateTime.of(2024, 6, 1, 0, 0));
    }

    @Nested
    @DisplayName("findById 方法测试")
    class FindByIdTests {

        @Test
        @DisplayName("经历存在：应返回Entity")
        void findById_existingExperience_shouldReturnEntity() {
            UserExperience experience = createTestExperience(EXPERIENCE_ID, USER_ID, ExperienceType.PROJECT, "项目1");
            UserExperienceDO dataObject = RepositoryTestObjects.toDataObject(experience, UserExperienceDO.class);

            when(userExperienceMapper.selectById(EXPERIENCE_ID)).thenReturn(dataObject);
            when(converter.toEntity(dataObject)).thenReturn(experience);

            Optional<UserExperience> result = userExperienceRepository.findById(EXPERIENCE_ID);

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

            Optional<UserExperience> result = userExperienceRepository.findById(EXPERIENCE_ID);

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
            List<UserExperienceDO> experiences = Arrays.asList(
                    RepositoryTestObjects.toDataObject(
                            createTestExperience(100L, USER_ID, ExperienceType.PROJECT, "项目1"),
                            UserExperienceDO.class),
                    RepositoryTestObjects.toDataObject(
                            createTestExperience(101L, USER_ID, ExperienceType.COMPETITION, "竞赛1"),
                            UserExperienceDO.class));

            when(userExperienceMapper.selectByUserId(USER_ID)).thenReturn(experiences);
            when(converter.toEntityList(experiences)).thenReturn(
                    Arrays.asList(
                            createTestExperience(100L, USER_ID, ExperienceType.PROJECT, "项目1"),
                            createTestExperience(101L, USER_ID, ExperienceType.COMPETITION, "竞赛1")));

            List<UserExperience> result = userExperienceRepository.findByUserId(USER_ID);

            assertNotNull(result);
            assertEquals(2, result.size());
            verify(userExperienceMapper).selectByUserId(USER_ID);
        }

        @Test
        @DisplayName("用户无经历：应返回空列表")
        void findByUserId_userHasNoExperiences_shouldReturnEmptyList() {
            when(userExperienceMapper.selectByUserId(USER_ID)).thenReturn(Collections.emptyList());

            List<UserExperience> result = userExperienceRepository.findByUserId(USER_ID);

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
            List<UserExperienceDO> experiences = Arrays.asList(
                    RepositoryTestObjects.toDataObject(
                            createTestExperience(100L, USER_ID, ExperienceType.PROJECT, "项目1"),
                            UserExperienceDO.class),
                    RepositoryTestObjects.toDataObject(
                            createTestExperience(101L, USER_ID, ExperienceType.PROJECT, "项目2"),
                            UserExperienceDO.class));

            when(userExperienceMapper.selectByUserIdAndType(USER_ID, ExperienceType.PROJECT)).thenReturn(experiences);
            when(converter.toEntityList(experiences)).thenReturn(
                    Arrays.asList(
                            createTestExperience(100L, USER_ID, ExperienceType.PROJECT, "项目1"),
                            createTestExperience(101L, USER_ID, ExperienceType.PROJECT, "项目2")));

            List<UserExperience> result = userExperienceRepository.findByUserIdAndType(USER_ID, ExperienceType.PROJECT);

            assertNotNull(result);
            assertEquals(2, result.size());
            result.forEach(exp -> assertEquals(ExperienceType.PROJECT, exp.getType()));
        }

        @Test
        @DisplayName("无匹配类型：应返回空列表")
        void findByUserIdAndType_noMatch_shouldReturnEmptyList() {
            when(userExperienceMapper.selectByUserIdAndType(USER_ID, ExperienceType.INTERNSHIP))
                    .thenReturn(Collections.emptyList());

            List<UserExperience> result = userExperienceRepository
                    .findByUserIdAndType(USER_ID, ExperienceType.INTERNSHIP);

            assertNotNull(result);
            assertTrue(result.isEmpty());
        }
    }

    @Nested
    @DisplayName("save 方法测试")
    class SaveTests {

        @Test
        @DisplayName("保存经历：应成功保存并设置ID")
        void save_validData_shouldSaveAndSetId() {
            UserExperience experience = createTestExperience(null, USER_ID, ExperienceType.PROJECT, "新项目");

            when(converter.toDataObject(any(UserExperience.class))).thenAnswer(invocation -> {
                UserExperience entity = invocation.getArgument(0);
                return UserExperienceDO.builder()
                        .id(entity.getId())
                        .userId(entity.getUserId())
                        .type(entity.getType())
                        .title(entity.getTitle())
                        .content(entity.getContent())
                        .startTime(entity.getStartTime())
                        .endTime(entity.getEndTime())
                        .build();
            });

            when(userExperienceMapper.insert(any(UserExperienceDO.class))).thenAnswer(invocation -> {
                UserExperienceDO exp = invocation.getArgument(0);
                exp.setId(EXPERIENCE_ID);
                return 1;
            });

            userExperienceRepository.save(experience);

            assertEquals(EXPERIENCE_ID, experience.getId());
            verify(userExperienceMapper).insert(any(UserExperienceDO.class));
        }
    }

    @Nested
    @DisplayName("update 方法测试")
    class UpdateTests {

        @Test
        @DisplayName("更新经历：应成功更新")
        void update_existingExperience_shouldUpdate() {
            UserExperience existing = createTestExperience(EXPERIENCE_ID, USER_ID, ExperienceType.PROJECT, "原项目");
            existing.setTitle("更新后的项目");

            when(converter.toDataObject(any(UserExperience.class))).thenAnswer(invocation -> {
                UserExperience entity = invocation.getArgument(0);
                return UserExperienceDO.builder()
                        .id(entity.getId())
                        .userId(entity.getUserId())
                        .type(entity.getType())
                        .title(entity.getTitle())
                        .content(entity.getContent())
                        .startTime(entity.getStartTime())
                        .endTime(entity.getEndTime())
                        .build();
            });

            when(userExperienceMapper.updateById(any(UserExperienceDO.class))).thenReturn(1);

            userExperienceRepository.update(existing);

            verify(userExperienceMapper).updateById(any(UserExperienceDO.class));
        }
    }

    @Nested
    @DisplayName("deleteById 方法测试")
    class DeleteByIdTests {

        @Test
        @DisplayName("删除成功：应调用mapper")
        void deleteById_existingExperience_shouldCallMapper() {
            when(userExperienceMapper.deleteById(EXPERIENCE_ID)).thenReturn(1);

            userExperienceRepository.deleteById(EXPERIENCE_ID);

            verify(userExperienceMapper).deleteById(EXPERIENCE_ID);
        }
    }

    @Nested
    @DisplayName("countByUserIdAndType 方法测试")
    class CountByUserIdAndTypeTests {

        @Test
        @DisplayName("统计数量：应返回正确数量")
        void countByUserIdAndType_shouldReturnCorrectCount() {
            when(userExperienceMapper.countByUserIdAndType(USER_ID, ExperienceType.PROJECT)).thenReturn(5L);

            int result = userExperienceRepository.countByUserIdAndType(USER_ID, ExperienceType.PROJECT);

            assertEquals(5, result);
        }

        @Test
        @DisplayName("无数据：应返回0")
        void countByUserIdAndType_noData_shouldReturnZero() {
            when(userExperienceMapper.countByUserIdAndType(USER_ID, ExperienceType.PROJECT)).thenReturn(0L);

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

            when(userExperienceMapper.selectById(EXPERIENCE_ID))
                    .thenReturn(RepositoryTestObjects.toDataObject(experience, UserExperienceDO.class));

            boolean result = userExperienceRepository.checkOwner(EXPERIENCE_ID, USER_ID);

            assertTrue(result);
        }

        @Test
        @DisplayName("不是所有者：应返回false")
        void checkOwner_notOwner_shouldReturnFalse() {
            UserExperience experience = createTestExperience(EXPERIENCE_ID, USER_ID, ExperienceType.PROJECT, "项目1");

            when(userExperienceMapper.selectById(EXPERIENCE_ID))
                    .thenReturn(RepositoryTestObjects.toDataObject(experience, UserExperienceDO.class));

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
