package com.bluenet.web.domain.model.entity;

import com.bluenet.web.domain.model.enumerate.AwardLevel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Competition 领域实体单元测试。
 */
@DisplayName("Competition 领域实体测试")
class CompetitionTest {

    @Test
    @DisplayName("create: 应创建新竞赛并默认省级级别")
    void create_shouldCreateCompetitionWithDefaultLevel() {
        Competition competition = Competition.create(
                "  全国大学生计算机设计大赛  ",
                "计算机设计大赛",
                1L,
                2L,
                "竞赛简介",
                null,
                "2024-05",
                "教育部",
                10);

        assertThat(competition.getId()).isNull();
        assertThat(competition.getName()).isEqualTo("全国大学生计算机设计大赛");
        assertThat(competition.getShortName()).isEqualTo("计算机设计大赛");
        assertThat(competition.getLogoFileId()).isEqualTo(1L);
        assertThat(competition.getCoverFileId()).isEqualTo(2L);
        assertThat(competition.getSummary()).isEqualTo("竞赛简介");
        assertThat(competition.getLevel()).isEqualTo(AwardLevel.PROVINCIAL);
        assertThat(competition.getMonth()).isEqualTo("2024-05");
        assertThat(competition.getOrganizer()).isEqualTo("教育部");
        assertThat(competition.getSortOrder()).isEqualTo(10);
    }

    @Test
    @DisplayName("create: 指定级别时应保留")
    void create_withExplicitLevel_shouldPreserveLevel() {
        Competition competition = Competition.create(
                "竞赛",
                "简称",
                null,
                null,
                null,
                AwardLevel.NATIONAL,
                null,
                null,
                null);

        assertThat(competition.getLevel()).isEqualTo(AwardLevel.NATIONAL);
    }

    @Test
    @DisplayName("create: 名称为空应抛异常")
    void create_withBlankName_shouldThrow() {
        assertThatThrownBy(
                () -> Competition.create(
                        "   ",
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null))
                                .isInstanceOf(IllegalArgumentException.class)
                                .hasMessageContaining("竞赛名称不能为空");
    }

    @Test
    @DisplayName("update: 应更新字段")
    void update_shouldUpdateFields() {
        Competition competition = Competition.create(
                "旧名称",
                "旧简称",
                1L,
                2L,
                "旧简介",
                AwardLevel.SCHOOL,
                "2024-01",
                "旧主办方",
                5);

        competition.update(
                "  新名称  ",
                "新简称",
                3L,
                4L,
                "新简介",
                AwardLevel.NATIONAL,
                "2024-06",
                "新主办方");

        assertThat(competition.getName()).isEqualTo("新名称");
        assertThat(competition.getShortName()).isEqualTo("新简称");
        assertThat(competition.getLogoFileId()).isEqualTo(3L);
        assertThat(competition.getCoverFileId()).isEqualTo(4L);
        assertThat(competition.getSummary()).isEqualTo("新简介");
        assertThat(competition.getLevel()).isEqualTo(AwardLevel.NATIONAL);
        assertThat(competition.getMonth()).isEqualTo("2024-06");
        assertThat(competition.getOrganizer()).isEqualTo("新主办方");
        assertThat(competition.getSortOrder()).isEqualTo(5);
    }

    @Test
    @DisplayName("update: 名称为空应抛异常")
    void update_withBlankName_shouldThrow() {
        Competition competition = Competition.create("名称", null, null, null, null, null, null, null, null);

        assertThatThrownBy(() -> competition.update("   ", null, null, null, null, null, null, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("竞赛名称不能为空");
    }

    @Test
    @DisplayName("updateSortOrder: 应更新排序值")
    void updateSortOrder_shouldUpdateSortOrder() {
        Competition competition = Competition.create("名称", null, null, null, null, null, null, null, 1);

        competition.updateSortOrder(99);

        assertThat(competition.getSortOrder()).isEqualTo(99);
    }

    @Test
    @DisplayName("reconstruct: 应保留所有字段")
    void reconstruct_shouldPreserveAllFields() {
        Competition competition = Competition.reconstruct(
                100L,
                "竞赛",
                "简称",
                1L,
                2L,
                "简介",
                AwardLevel.NATIONAL,
                "2024-05",
                "主办方",
                20);

        assertThat(competition.getId()).isEqualTo(100L);
        assertThat(competition.getName()).isEqualTo("竞赛");
        assertThat(competition.getShortName()).isEqualTo("简称");
        assertThat(competition.getLogoFileId()).isEqualTo(1L);
        assertThat(competition.getCoverFileId()).isEqualTo(2L);
        assertThat(competition.getSummary()).isEqualTo("简介");
        assertThat(competition.getLevel()).isEqualTo(AwardLevel.NATIONAL);
        assertThat(competition.getMonth()).isEqualTo("2024-05");
        assertThat(competition.getOrganizer()).isEqualTo("主办方");
        assertThat(competition.getSortOrder()).isEqualTo(20);
    }
}
