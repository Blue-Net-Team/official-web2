package com.bluenet.web.domain.model.entity;

import com.bluenet.web.domain.model.enumerate.AchievementType;
import com.bluenet.web.domain.model.enumerate.AwardLevel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Achievement 领域实体单元测试。
 */
@DisplayName("Achievement 领域实体测试")
class AchievementTest {

    @Test
    @DisplayName("create: 应创建竞赛成就")
    void create_withCompetitionType_shouldCreateAchievement() {
        LocalDate achieveAt = LocalDate.of(2024, 5, 20);
        Achievement achievement = Achievement.create(
                "全国大学生竞赛一等奖",
                AchievementType.COMPETITION,
                "全国大学生计算机设计大赛",
                achieveAt,
                AwardLevel.NATIONAL,
                "一等奖",
                1L);

        assertThat(achievement.getId()).isNull();
        assertThat(achievement.getTitle()).isEqualTo("全国大学生竞赛一等奖");
        assertThat(achievement.getType()).isEqualTo(AchievementType.COMPETITION);
        assertThat(achievement.getRelateTo()).isEqualTo("全国大学生计算机设计大赛");
        assertThat(achievement.getAchieveAt()).isEqualTo(achieveAt);
        assertThat(achievement.getAwardLevel()).isEqualTo(AwardLevel.NATIONAL);
        assertThat(achievement.getAwardName()).isEqualTo("一等奖");
        assertThat(achievement.getFileId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("create: 非竞赛成就可以不指定奖项级别")
    void create_withNonCompetitionType_shouldAllowNullAwardLevel() {
        Achievement achievement = Achievement.create(
                "论文发表",
                AchievementType.PAPER,
                "核心期刊",
                LocalDate.of(2024, 6, 1),
                null,
                "已发表",
                null);

        assertThat(achievement.getType()).isEqualTo(AchievementType.PAPER);
        assertThat(achievement.getAwardLevel()).isNull();
    }

    @Test
    @DisplayName("create: 竞赛成就未指定奖项级别应抛异常")
    void create_withCompetitionTypeAndNullAwardLevel_shouldThrow() {
        assertThatThrownBy(
                () -> Achievement.create(
                        "竞赛成就",
                        AchievementType.COMPETITION,
                        "竞赛",
                        LocalDate.now(),
                        null,
                        "奖项",
                        null))
                                .isInstanceOf(IllegalArgumentException.class)
                                .hasMessageContaining("奖项级别");
    }

    @Test
    @DisplayName("reconstruct: 应保留所有字段")
    void reconstruct_shouldPreserveAllFields() {
        LocalDate achieveAt = LocalDate.of(2023, 12, 1);
        Achievement achievement = Achievement.reconstruct(
                100L,
                "专利授权",
                AchievementType.PATENT,
                "发明专利",
                achieveAt,
                AwardLevel.PROVINCIAL,
                "授权",
                2L);

        assertThat(achievement.getId()).isEqualTo(100L);
        assertThat(achievement.getTitle()).isEqualTo("专利授权");
        assertThat(achievement.getType()).isEqualTo(AchievementType.PATENT);
        assertThat(achievement.getRelateTo()).isEqualTo("发明专利");
        assertThat(achievement.getAchieveAt()).isEqualTo(achieveAt);
        assertThat(achievement.getAwardLevel()).isEqualTo(AwardLevel.PROVINCIAL);
        assertThat(achievement.getAwardName()).isEqualTo("授权");
        assertThat(achievement.getFileId()).isEqualTo(2L);
    }

    @Test
    @DisplayName("update: 应更新所有字段")
    void update_shouldUpdateAllFields() {
        Achievement achievement = Achievement.create(
                "旧标题",
                AchievementType.PAPER,
                "旧关联",
                LocalDate.of(2024, 1, 1),
                null,
                "旧奖项",
                1L);

        LocalDate newAchieveAt = LocalDate.of(2024, 8, 15);
        achievement.update(
                "新标题",
                AchievementType.COMPETITION,
                "新竞赛",
                newAchieveAt,
                AwardLevel.SCHOOL,
                "新奖项",
                3L);

        assertThat(achievement.getTitle()).isEqualTo("新标题");
        assertThat(achievement.getType()).isEqualTo(AchievementType.COMPETITION);
        assertThat(achievement.getRelateTo()).isEqualTo("新竞赛");
        assertThat(achievement.getAchieveAt()).isEqualTo(newAchieveAt);
        assertThat(achievement.getAwardLevel()).isEqualTo(AwardLevel.SCHOOL);
        assertThat(achievement.getAwardName()).isEqualTo("新奖项");
        assertThat(achievement.getFileId()).isEqualTo(3L);
    }

    @Test
    @DisplayName("update: 竞赛成就未指定奖项级别应抛异常")
    void update_withCompetitionTypeAndNullAwardLevel_shouldThrow() {
        Achievement achievement = Achievement.create(
                "标题",
                AchievementType.PAPER,
                "关联",
                LocalDate.now(),
                null,
                "奖项",
                null);

        assertThatThrownBy(
                () -> achievement.update(
                        "标题",
                        AchievementType.COMPETITION,
                        "关联",
                        LocalDate.now(),
                        null,
                        "奖项",
                        null))
                                .isInstanceOf(IllegalArgumentException.class)
                                .hasMessageContaining("奖项级别");
    }

    @Test
    @DisplayName("assignMembers: 应设置系统内成员和外部协作者")
    void assignMembers_shouldSetMembers() {
        Achievement achievement = Achievement.create(
                "团队成就",
                AchievementType.COMPETITION,
                "竞赛",
                LocalDate.of(2024, 5, 1),
                AwardLevel.NATIONAL,
                "一等奖",
                1L);

        achievement.assignMembers(List.of(1L, 2L), List.of("张三-外校", "李四-他队"));

        assertThat(achievement.getMemberIds()).containsExactly(1L, 2L);
        assertThat(achievement.getExternalMembers()).containsExactly("张三-外校", "李四-他队");
    }

    @Test
    @DisplayName("assignMembers: 成员ID应去重并忽略null")
    void assignMembers_shouldDeduplicateMemberIds() {
        Achievement achievement = Achievement.create(
                "团队成就",
                AchievementType.COMPETITION,
                "竞赛",
                LocalDate.of(2024, 5, 1),
                AwardLevel.NATIONAL,
                "一等奖",
                1L);

        achievement.assignMembers(java.util.Arrays.asList(1L, null, 1L, 2L), null);

        assertThat(achievement.getMemberIds()).containsExactly(1L, 2L);
        assertThat(achievement.getExternalMembers()).isEmpty();
    }

    @Test
    @DisplayName("assignMembers: 外部协作者应trim、去空、去重")
    void assignMembers_shouldNormalizeExternalMembers() {
        Achievement achievement = Achievement.create(
                "团队成就",
                AchievementType.COMPETITION,
                "竞赛",
                LocalDate.of(2024, 5, 1),
                AwardLevel.NATIONAL,
                "一等奖",
                1L);

        achievement.assignMembers(null, java.util.Arrays.asList(" 张三 ", null, "", "   ", "张三", "李四"));

        assertThat(achievement.getExternalMembers()).containsExactly("张三", "李四");
    }

    @Test
    @DisplayName("assignMembers: 外部协作者姓名超长应抛异常")
    void assignMembers_tooLongName_shouldThrow() {
        Achievement achievement = Achievement.create(
                "团队成就",
                AchievementType.COMPETITION,
                "竞赛",
                LocalDate.of(2024, 5, 1),
                AwardLevel.NATIONAL,
                "一等奖",
                1L);
        String longName = "长".repeat(101);

        assertThatThrownBy(() -> achievement.assignMembers(null, List.of(longName)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("外部协作者姓名不能超过100字符");
    }

    @Test
    @DisplayName("assignMembers: 全量替换旧关联")
    void assignMembers_shouldReplaceExisting() {
        Achievement achievement = Achievement.create(
                "团队成就",
                AchievementType.COMPETITION,
                "竞赛",
                LocalDate.of(2024, 5, 1),
                AwardLevel.NATIONAL,
                "一等奖",
                1L);
        achievement.assignMembers(List.of(1L, 2L), List.of("张三"));

        achievement.assignMembers(List.of(3L), List.of("李四"));

        assertThat(achievement.getMemberIds()).containsExactly(3L);
        assertThat(achievement.getExternalMembers()).containsExactly("李四");
    }
}
