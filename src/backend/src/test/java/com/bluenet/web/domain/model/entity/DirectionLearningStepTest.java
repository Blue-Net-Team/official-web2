package com.bluenet.web.domain.model.entity;

import com.bluenet.web.domain.model.enumerate.Direction;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * DirectionLearningStep 领域实体单元测试。
 */
@DisplayName("DirectionLearningStep 领域实体测试")
class DirectionLearningStepTest {

    @Test
    @DisplayName("create: 应创建新学习步骤")
    void create_shouldCreateLearningStep() {
        DirectionLearningStep step = DirectionLearningStep.create(
                Direction.COMPUTER_VISION,
                1,
                "  OpenCV 基础  ",
                "https://example.com/video");

        assertThat(step.getId()).isNull();
        assertThat(step.getDirection()).isEqualTo(Direction.COMPUTER_VISION);
        assertThat(step.getSortOrder()).isEqualTo(1);
        assertThat(step.getTitle()).isEqualTo("OpenCV 基础");
        assertThat(step.getRelatedUrl()).isEqualTo("https://example.com/video");
    }

    @Test
    @DisplayName("create: 方向为空应抛异常")
    void create_withNullDirection_shouldThrow() {
        assertThatThrownBy(() -> DirectionLearningStep.create(null, 1, "标题", null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("方向不能为空");
    }

    @Test
    @DisplayName("create: 顺序值为空或小于1应抛异常")
    void create_withInvalidSortOrder_shouldThrow() {
        assertThatThrownBy(() -> DirectionLearningStep.create(Direction.EMBEDDED, null, "标题", null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("步骤顺序值");

        assertThatThrownBy(() -> DirectionLearningStep.create(Direction.EMBEDDED, 0, "标题", null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("步骤顺序值");
    }

    @Test
    @DisplayName("create: 标题为空应抛异常")
    void create_withBlankTitle_shouldThrow() {
        assertThatThrownBy(() -> DirectionLearningStep.create(Direction.STRUCTURAL_DESIGN, 1, "   ", null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("标题不能为空");
    }

    @Test
    @DisplayName("updateSortOrder: 应更新顺序值")
    void updateSortOrder_shouldUpdateSortOrder() {
        DirectionLearningStep step = DirectionLearningStep.create(Direction.EMBEDDED, 1, "标题", null);

        step.updateSortOrder(2);

        assertThat(step.getSortOrder()).isEqualTo(2);
    }

    @Test
    @DisplayName("updateSortOrder: 非法顺序值应抛异常")
    void updateSortOrder_withInvalidValue_shouldThrow() {
        DirectionLearningStep step = DirectionLearningStep.create(Direction.EMBEDDED, 1, "标题", null);

        assertThatThrownBy(() -> step.updateSortOrder(0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("步骤顺序值");
    }

    @Test
    @DisplayName("updateTitle: 应更新标题并去除空白")
    void updateTitle_shouldUpdateTitle() {
        DirectionLearningStep step = DirectionLearningStep.create(Direction.EMBEDDED, 1, "旧标题", null);

        step.updateTitle("  新标题  ");

        assertThat(step.getTitle()).isEqualTo("新标题");
    }

    @Test
    @DisplayName("updateTitle: 空标题应抛异常")
    void updateTitle_withBlankTitle_shouldThrow() {
        DirectionLearningStep step = DirectionLearningStep.create(Direction.EMBEDDED, 1, "标题", null);

        assertThatThrownBy(() -> step.updateTitle("   "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("标题不能为空");
    }

    @Test
    @DisplayName("updateRelatedUrl: 应更新相关链接")
    void updateRelatedUrl_shouldUpdateRelatedUrl() {
        DirectionLearningStep step = DirectionLearningStep.create(Direction.EMBEDDED, 1, "标题", null);

        step.updateRelatedUrl("https://new.example.com");

        assertThat(step.getRelatedUrl()).isEqualTo("https://new.example.com");
    }

    @Test
    @DisplayName("更新标题与相关链接: 不应改变顺序值")
    void updateContent_shouldNotChangeSortOrder() {
        DirectionLearningStep step = DirectionLearningStep
                .create(Direction.EMBEDDED, 7, "旧标题", "https://old.example.com");

        step.updateTitle("新标题");
        step.updateRelatedUrl("https://new.example.com");

        assertThat(step.getSortOrder()).isEqualTo(7);
    }

    @Test
    @DisplayName("reconstruct: 应保留所有字段")
    void reconstruct_shouldPreserveAllFields() {
        DirectionLearningStep step = DirectionLearningStep.reconstruct(
                10L,
                Direction.STRUCTURAL_DESIGN,
                3,
                "标题",
                "https://example.com");

        assertThat(step.getId()).isEqualTo(10L);
        assertThat(step.getDirection()).isEqualTo(Direction.STRUCTURAL_DESIGN);
        assertThat(step.getSortOrder()).isEqualTo(3);
        assertThat(step.getTitle()).isEqualTo("标题");
        assertThat(step.getRelatedUrl()).isEqualTo("https://example.com");
    }
}
