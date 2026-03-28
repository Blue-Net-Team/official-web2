package com.bluenet.web.domain.service.impl;

import com.bluenet.web.domain.exception.DataNotFound;
import com.bluenet.web.domain.exception.Forbidden;
import com.bluenet.web.domain.model.enumerate.ExperienceType;
import com.bluenet.web.domain.model.vo.ExperienceVO;
import com.bluenet.web.domain.repository.UserExperienceRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * UserExperienceDomainService 单元测试
 */
@ExtendWith(MockitoExtension.class)
class UserExperienceDomainServiceImplTest {

    @Mock
    private UserExperienceRepository userExperienceRepository;

    @InjectMocks
    private UserExperienceDomainServiceImpl userExperienceDomainService;

    private static final Long USER_ID = 1L;
    private static final Long OTHER_USER_ID = 2L;
    private static final Long EXPERIENCE_ID = 100L;

    @Test
    void getExperiences_whenUserHasExperiences_returnsList() {
        // 准备
        List<ExperienceVO> experiences = Arrays.asList(
                createExperienceVO(EXPERIENCE_ID, ExperienceType.PROJECT, "项目1"),
                createExperienceVO(101L, ExperienceType.COMPETITION, "竞赛1"),
                createExperienceVO(102L, ExperienceType.INTERNSHIP, "实习1"));

        when(userExperienceRepository.findByUserId(USER_ID)).thenReturn(experiences);

        // 执行
        List<ExperienceVO> result = userExperienceDomainService.getExperiences(USER_ID);

        // 验证
        assertNotNull(result);
        assertEquals(3, result.size());
        verify(userExperienceRepository).findByUserId(USER_ID);
    }

    @Test
    void getExperiences_whenUserHasNoExperiences_returnsEmptyList() {
        // 准备
        when(userExperienceRepository.findByUserId(USER_ID)).thenReturn(Collections.emptyList());

        // 执行
        List<ExperienceVO> result = userExperienceDomainService.getExperiences(USER_ID);

        // 验证
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(userExperienceRepository).findByUserId(USER_ID);
    }

    @Test
    void getExperiencesByType_filtersByTypeCorrectly() {
        // 准备
        List<ExperienceVO> projectExperiences = Arrays.asList(
                createExperienceVO(EXPERIENCE_ID, ExperienceType.PROJECT, "项目1"),
                createExperienceVO(101L, ExperienceType.PROJECT, "项目2"));

        when(userExperienceRepository.findByUserIdAndType(USER_ID, ExperienceType.PROJECT))
                .thenReturn(projectExperiences);

        // 执行
        List<ExperienceVO> result = userExperienceDomainService.getExperiencesByType(USER_ID, ExperienceType.PROJECT);

        // 验证
        assertNotNull(result);
        assertEquals(2, result.size());
        result.forEach(exp -> assertEquals(ExperienceType.PROJECT, exp.getType()));
        verify(userExperienceRepository).findByUserIdAndType(USER_ID, ExperienceType.PROJECT);
    }

    @Test
    void getExperienceById_whenExistsAndOwnedByUser_returnsExperience() {
        // 准备
        ExperienceVO experience = createExperienceVO(EXPERIENCE_ID, ExperienceType.PROJECT, "项目1");
        when(userExperienceRepository.findById(EXPERIENCE_ID)).thenReturn(Optional.of(experience));
        when(userExperienceRepository.checkOwner(EXPERIENCE_ID, USER_ID)).thenReturn(true);

        // 执行
        Optional<ExperienceVO> result = userExperienceDomainService.getExperienceById(EXPERIENCE_ID, USER_ID);

        // 验证
        assertTrue(result.isPresent());
        assertEquals(EXPERIENCE_ID, result.get().getId());
        verify(userExperienceRepository).checkOwner(EXPERIENCE_ID, USER_ID);
    }

    /**
     * 测试修复后的权限校验bug：用户尝试访问不属于自己经历时应返回空 这是针对原代码bug的回归测试： 原代码: if
     * (experience.isPresent() && !experience.get().getId().equals(experienceId))
     * 该条件永远为false，权限校验失效 修复后: if
     * (!userExperienceRepository.checkOwner(experienceId, userId))
     */
    @Test
    void getExperienceById_whenNotOwnedByUser_returnsEmpty() {
        // 准备
        ExperienceVO experience = createExperienceVO(EXPERIENCE_ID, ExperienceType.PROJECT, "项目1");
        when(userExperienceRepository.findById(EXPERIENCE_ID)).thenReturn(Optional.of(experience));
        when(userExperienceRepository.checkOwner(EXPERIENCE_ID, USER_ID)).thenReturn(false);

        // 执行
        Optional<ExperienceVO> result = userExperienceDomainService.getExperienceById(EXPERIENCE_ID, USER_ID);

        // 验证
        assertFalse(result.isPresent(), "用户不应能访问不属于自己经历");
        verify(userExperienceRepository).checkOwner(EXPERIENCE_ID, USER_ID);
    }

    @Test
    void getExperienceById_whenNotExists_returnsEmpty() {
        // 准备
        when(userExperienceRepository.findById(EXPERIENCE_ID)).thenReturn(Optional.empty());

        // 执行
        Optional<ExperienceVO> result = userExperienceDomainService.getExperienceById(EXPERIENCE_ID, USER_ID);

        // 验证
        assertFalse(result.isPresent());
    }

    @Test
    void createExperience_createsSuccessfully() {
        // 准备
        ExperienceVO createdExperience = createExperienceVO(EXPERIENCE_ID, ExperienceType.PROJECT, "新项目");
        String content = "{\"role\":\"开发者\",\"description\":\"项目描述\"}";

        when(
                userExperienceRepository.save(
                        eq(USER_ID),
                        eq(ExperienceType.PROJECT),
                        eq("新项目"),
                        anyString(),
                        anyString(),
                        eq(content))).thenReturn(createdExperience);

        // 执行
        ExperienceVO result = userExperienceDomainService.createExperience(
                USER_ID,
                ExperienceType.PROJECT,
                "新项目",
                "2024.01",
                "2024.06",
                content);

        // 验证
        assertNotNull(result);
        assertEquals(EXPERIENCE_ID, result.getId());
        verify(userExperienceRepository).save(
                eq(USER_ID),
                eq(ExperienceType.PROJECT),
                eq("新项目"),
                anyString(),
                anyString(),
                eq(content));
    }

    @Test
    void updateExperience_whenOwner_updatesSuccessfully() {
        // 准备
        ExperienceVO existingExperience = createExperienceVO(EXPERIENCE_ID, ExperienceType.PROJECT, "原项目");
        ExperienceVO updatedExperience = createExperienceVO(EXPERIENCE_ID, ExperienceType.PROJECT, "更新后的项目");
        String content = "{\"role\":\"负责人\",\"description\":\"更新后的描述\"}";

        when(userExperienceRepository.findById(EXPERIENCE_ID)).thenReturn(Optional.of(existingExperience));
        when(userExperienceRepository.checkOwner(EXPERIENCE_ID, USER_ID)).thenReturn(true);
        when(
                userExperienceRepository.update(
                        eq(EXPERIENCE_ID),
                        eq("更新后的项目"),
                        anyString(),
                        anyString(),
                        eq(content))).thenReturn(updatedExperience);

        // 执行
        ExperienceVO result = userExperienceDomainService.updateExperience(
                EXPERIENCE_ID,
                USER_ID,
                "更新后的项目",
                "2024.01",
                "2024.06",
                content);

        // 验证
        assertNotNull(result);
        assertEquals("更新后的项目", result.getTitle());
    }

    @Test
    void updateExperience_whenNotExists_throwsDataNotFound() {
        // 准备
        when(userExperienceRepository.findById(EXPERIENCE_ID)).thenReturn(Optional.empty());

        // 执行 & 验证
        assertThrows(
                DataNotFound.class,
                () -> userExperienceDomainService.updateExperience(
                        EXPERIENCE_ID,
                        USER_ID,
                        "更新后的项目",
                        "2024.01",
                        "2024.06",
                        "{}"));
    }

    @Test
    void updateExperience_whenNotOwner_throwsForbidden() {
        // 准备
        ExperienceVO existingExperience = createExperienceVO(EXPERIENCE_ID, ExperienceType.PROJECT, "原项目");

        when(userExperienceRepository.findById(EXPERIENCE_ID)).thenReturn(Optional.of(existingExperience));
        when(userExperienceRepository.checkOwner(EXPERIENCE_ID, USER_ID)).thenReturn(false);

        // 执行 & 验证
        assertThrows(
                Forbidden.class,
                () -> userExperienceDomainService.updateExperience(
                        EXPERIENCE_ID,
                        USER_ID,
                        "更新后的项目",
                        "2024.01",
                        "2024.06",
                        "{}"));
    }

    @Test
    void deleteExperience_whenOwner_deletesSuccessfully() {
        // 准备
        ExperienceVO existingExperience = createExperienceVO(EXPERIENCE_ID, ExperienceType.PROJECT, "原项目");

        when(userExperienceRepository.findById(EXPERIENCE_ID)).thenReturn(Optional.of(existingExperience));
        when(userExperienceRepository.checkOwner(EXPERIENCE_ID, USER_ID)).thenReturn(true);
        when(userExperienceRepository.deleteById(EXPERIENCE_ID)).thenReturn(true);

        // 执行
        boolean result = userExperienceDomainService.deleteExperience(EXPERIENCE_ID, USER_ID);

        // 验证
        assertTrue(result);
        verify(userExperienceRepository).deleteById(EXPERIENCE_ID);
    }

    @Test
    void deleteExperience_whenNotExists_throwsDataNotFound() {
        // 准备
        when(userExperienceRepository.findById(EXPERIENCE_ID)).thenReturn(Optional.empty());

        // 执行 & 验证
        assertThrows(DataNotFound.class, () -> userExperienceDomainService.deleteExperience(EXPERIENCE_ID, USER_ID));
    }

    @Test
    void deleteExperience_whenNotOwner_throwsForbidden() {
        // 准备
        ExperienceVO existingExperience = createExperienceVO(EXPERIENCE_ID, ExperienceType.PROJECT, "原项目");

        when(userExperienceRepository.findById(EXPERIENCE_ID)).thenReturn(Optional.of(existingExperience));
        when(userExperienceRepository.checkOwner(EXPERIENCE_ID, USER_ID)).thenReturn(false);

        // 执行 & 验证
        assertThrows(Forbidden.class, () -> userExperienceDomainService.deleteExperience(EXPERIENCE_ID, USER_ID));
    }

    @Test
    void getTabCounts_returnsCorrectCounts() {
        // 准备
        when(userExperienceRepository.countByUserIdAndType(USER_ID, ExperienceType.PROJECT)).thenReturn(5);
        when(userExperienceRepository.countByUserIdAndType(USER_ID, ExperienceType.COMPETITION)).thenReturn(3);
        when(userExperienceRepository.countByUserIdAndType(USER_ID, ExperienceType.INTERNSHIP)).thenReturn(2);

        // 执行
        var result = userExperienceDomainService.getTabCounts(USER_ID);

        // 验证
        assertNotNull(result);
        assertEquals(5, result.projects());
        assertEquals(3, result.competitions());
        assertEquals(2, result.internships());
    }

    @Test
    void getTabCounts_whenNoExperiences_returnsZeroCounts() {
        // 准备
        when(userExperienceRepository.countByUserIdAndType(eq(USER_ID), any(ExperienceType.class))).thenReturn(0);

        // 执行
        var result = userExperienceDomainService.getTabCounts(USER_ID);

        // 验证
        assertNotNull(result);
        assertEquals(0, result.projects());
        assertEquals(0, result.competitions());
        assertEquals(0, result.internships());
    }

    /**
     * 创建测试用的ExperienceVO
     */
    private ExperienceVO createExperienceVO(Long id, ExperienceType type, String title) {
        return ExperienceVO.builder()
                .id(id)
                .type(type)
                .title(title)
                .startTime("2024.01")
                .endTime("2024.06")
                .content("{\"description\":\"测试内容\"}")
                .build();
    }

    // ==================== 权限边界测试 ====================

    @Nested
    @DisplayName("权限边界测试")
    class PermissionBoundaryTests {

        @Test
        @DisplayName("不同用户尝试访问同一经历：非所有者应收到空Optional")
        void getExperienceById_differentUsers_nonOwnerReceivesEmpty() {
            // 准备 - 用户1的经历
            ExperienceVO experience = createExperienceVO(EXPERIENCE_ID, ExperienceType.PROJECT, "用户1的项目");

            when(userExperienceRepository.findById(EXPERIENCE_ID)).thenReturn(Optional.of(experience));
            // 用户2尝试访问用户1的经历
            when(userExperienceRepository.checkOwner(EXPERIENCE_ID, OTHER_USER_ID)).thenReturn(false);

            // 执行
            Optional<ExperienceVO> result = userExperienceDomainService.getExperienceById(EXPERIENCE_ID, OTHER_USER_ID);

            // 验证
            assertFalse(result.isPresent(), "非所有者不应能获取经历详情");
        }

        @Test
        @DisplayName("不同用户尝试更新同一经历：非所有者应抛出Forbidden异常")
        void updateExperience_differentUsers_nonOwnerThrowsForbidden() {
            // 准备
            ExperienceVO existingExperience = createExperienceVO(EXPERIENCE_ID, ExperienceType.PROJECT, "原项目");

            when(userExperienceRepository.findById(EXPERIENCE_ID)).thenReturn(Optional.of(existingExperience));
            when(userExperienceRepository.checkOwner(EXPERIENCE_ID, OTHER_USER_ID)).thenReturn(false);

            // 执行 & 验证
            assertThrows(
                    Forbidden.class,
                    () -> userExperienceDomainService.updateExperience(
                            EXPERIENCE_ID,
                            OTHER_USER_ID,
                            "尝试更新",
                            "2024.01",
                            "2024.06",
                            "{}"));
        }

        @Test
        @DisplayName("不同用户尝试删除同一经历：非所有者应抛出Forbidden异常")
        void deleteExperience_differentUsers_nonOwnerThrowsForbidden() {
            // 准备
            ExperienceVO existingExperience = createExperienceVO(EXPERIENCE_ID, ExperienceType.PROJECT, "原项目");

            when(userExperienceRepository.findById(EXPERIENCE_ID)).thenReturn(Optional.of(existingExperience));
            when(userExperienceRepository.checkOwner(EXPERIENCE_ID, OTHER_USER_ID)).thenReturn(false);

            // 执行 & 验证
            assertThrows(
                    Forbidden.class,
                    () -> userExperienceDomainService.deleteExperience(EXPERIENCE_ID, OTHER_USER_ID));
        }

        @Test
        @DisplayName("边界条件：ID为null时应正确处理")
        void getExperienceById_nullId_handlesGracefully() {
            // 准备
            when(userExperienceRepository.findById(null)).thenReturn(Optional.empty());

            // 执行
            Optional<ExperienceVO> result = userExperienceDomainService.getExperienceById(null, USER_ID);

            // 验证
            assertFalse(result.isPresent());
        }

        @Test
        @DisplayName("边界条件：用户ID为null时更新经历应抛出异常")
        void updateExperience_nullUserId_throwsException() {
            // 准备
            ExperienceVO existingExperience = createExperienceVO(EXPERIENCE_ID, ExperienceType.PROJECT, "原项目");
            when(userExperienceRepository.findById(EXPERIENCE_ID)).thenReturn(Optional.of(existingExperience));
            when(userExperienceRepository.checkOwner(EXPERIENCE_ID, null)).thenReturn(false);

            // 执行 & 验证
            assertThrows(
                    Forbidden.class,
                    () -> userExperienceDomainService.updateExperience(
                            EXPERIENCE_ID,
                            null,
                            "更新",
                            "2024.01",
                            "2024.06",
                            "{}"));
        }

        @Test
        @DisplayName("边界条件：用户ID为null时删除经历应抛出异常")
        void deleteExperience_nullUserId_throwsException() {
            // 准备
            ExperienceVO existingExperience = createExperienceVO(EXPERIENCE_ID, ExperienceType.PROJECT, "原项目");
            when(userExperienceRepository.findById(EXPERIENCE_ID)).thenReturn(Optional.of(existingExperience));
            when(userExperienceRepository.checkOwner(EXPERIENCE_ID, null)).thenReturn(false);

            // 执行 & 验证
            assertThrows(
                    Forbidden.class,
                    () -> userExperienceDomainService.deleteExperience(EXPERIENCE_ID, null));
        }
    }
}
