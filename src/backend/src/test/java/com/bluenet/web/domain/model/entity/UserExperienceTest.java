package com.bluenet.web.domain.model.entity;

import com.bluenet.web.domain.model.enumerate.ExperienceType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * UserExperience 领域实体单元测试。
 */
@DisplayName("UserExperience 领域实体测试")
class UserExperienceTest {

    @Test
    @DisplayName("create: 应创建有效经历")
    void create_shouldCreateExperience() {
        LocalDateTime start = LocalDateTime.of(2024, 1, 1, 0, 0);
        LocalDateTime end = LocalDateTime.of(2024, 12, 31, 0, 0);
        UserExperience experience = UserExperience.create(
                1L,
                ExperienceType.PROJECT,
                "项目名称",
                "{}",
                start,
                end);

        assertNull(experience.getId());
        assertEquals(1L, experience.getUserId());
        assertEquals(ExperienceType.PROJECT, experience.getType());
        assertEquals("项目名称", experience.getTitle());
        assertEquals("{}", experience.getContent());
        assertEquals(start, experience.getStartTime());
        assertEquals(end, experience.getEndTime());
    }

    @Test
    @DisplayName("create: 用户ID为空应抛异常")
    void create_nullUserId_shouldThrow() {
        assertThrows(
                IllegalArgumentException.class,
                () -> UserExperience.create(null, ExperienceType.PROJECT, "名称", "{}", null, null));
    }

    @Test
    @DisplayName("create: 类型为空应抛异常")
    void create_nullType_shouldThrow() {
        assertThrows(
                IllegalArgumentException.class,
                () -> UserExperience.create(1L, null, "名称", "{}", null, null));
    }

    @Test
    @DisplayName("create: 名称为空应抛异常")
    void create_blankTitle_shouldThrow() {
        assertThrows(
                IllegalArgumentException.class,
                () -> UserExperience.create(1L, ExperienceType.PROJECT, "  ", "{}", null, null));
        assertThrows(
                IllegalArgumentException.class,
                () -> UserExperience.create(1L, ExperienceType.PROJECT, null, "{}", null, null));
    }

    @Test
    @DisplayName("create: 名称应去除前后空格")
    void create_shouldTrimTitle() {
        UserExperience experience = UserExperience.create(
                1L,
                ExperienceType.PROJECT,
                "  项目名称  ",
                "{}",
                null,
                null);
        assertEquals("项目名称", experience.getTitle());
    }

    @Test
    @DisplayName("updateDetails: 应更新详情")
    void updateDetails_shouldUpdateFields() {
        UserExperience experience = UserExperience.create(
                1L,
                ExperienceType.PROJECT,
                "旧名称",
                "{}",
                null,
                null);
        LocalDateTime start = LocalDateTime.of(2025, 1, 1, 0, 0);

        experience.updateDetails("新名称", "{\"key\":\"value\"}", start, null);

        assertEquals("新名称", experience.getTitle());
        assertEquals("{\"key\":\"value\"}", experience.getContent());
        assertEquals(start, experience.getStartTime());
        assertNull(experience.getEndTime());
    }

    @Test
    @DisplayName("updateDetails: 名称为空应抛异常")
    void updateDetails_blankTitle_shouldThrow() {
        UserExperience experience = UserExperience.create(
                1L,
                ExperienceType.PROJECT,
                "名称",
                "{}",
                null,
                null);
        assertThrows(
                IllegalArgumentException.class,
                () -> experience.updateDetails("   ", "{}", null, null));
    }

    @Test
    @DisplayName("reconstruct: 应保留所有字段")
    void reconstruct_shouldPreserveFields() {
        LocalDateTime start = LocalDateTime.of(2024, 6, 1, 0, 0);
        UserExperience experience = UserExperience.reconstruct(
                10L,
                2L,
                ExperienceType.INTERNSHIP,
                "公司",
                "{}",
                start,
                null);

        assertEquals(10L, experience.getId());
        assertEquals(2L, experience.getUserId());
        assertEquals(ExperienceType.INTERNSHIP, experience.getType());
        assertEquals("公司", experience.getTitle());
        assertEquals(start, experience.getStartTime());
    }
}
