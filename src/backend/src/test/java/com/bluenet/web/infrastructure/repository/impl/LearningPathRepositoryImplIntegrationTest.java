package com.bluenet.web.infrastructure.repository.impl;

import com.bluenet.web.BaseIntegrationTest;
import com.bluenet.web.domain.model.entity.DirectionLearningStep;
import com.bluenet.web.domain.model.enumerate.Direction;
import com.bluenet.web.domain.repository.LearningPathRepository;
import com.bluenet.web.infrastructure.repository.dataobject.DirectionLearningStepDO;
import com.bluenet.web.infrastructure.repository.mapper.LearningPathMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

/**
 * LearningPathRepositoryImpl 集成测试。
 */
@DisplayName("LearningPathRepositoryImpl 集成测试")
class LearningPathRepositoryImplIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private LearningPathRepository learningPathRepository;

    @Autowired
    private LearningPathMapper learningPathMapper;

    private DirectionLearningStep createStep(Direction direction, Integer sortOrder, String title) {
        DirectionLearningStep step = DirectionLearningStep
                .create(direction, sortOrder, title, "http://example.com/" + title);
        learningPathRepository.save(step);
        return step;
    }

    @Test
    @DisplayName("save: 新学习步骤应插入并回写ID")
    void save_newStep_shouldInsertAndReturnId() {
        DirectionLearningStep step = createStep(Direction.COMPUTER_VISION, 100, "测试步骤");

        assertThat(step.getId()).isNotNull();
        DirectionLearningStepDO dataObject = learningPathMapper.selectById(step.getId());
        assertThat(dataObject).isNotNull();
        assertThat(dataObject.getTitle()).isEqualTo("测试步骤");
        assertThat(dataObject.getDirection()).isEqualTo(Direction.COMPUTER_VISION);
        assertThat(dataObject.getSortOrder()).isEqualTo(100);
    }

    @Test
    @DisplayName("save: 已有学习步骤应更新字段")
    void save_existingStep_shouldUpdateFields() {
        DirectionLearningStep step = createStep(Direction.EMBEDDED, 200, "旧标题");
        step.updateTitle("新标题");
        step.updateSortOrder(250);

        learningPathRepository.save(step);

        DirectionLearningStepDO updated = learningPathMapper.selectById(step.getId());
        assertThat(updated.getTitle()).isEqualTo("新标题");
        assertThat(updated.getSortOrder()).isEqualTo(250);
    }

    @Test
    @DisplayName("findById: 存在返回实体，不存在返回空")
    void findById_shouldReturnOptional() {
        DirectionLearningStep step = createStep(Direction.STRUCTURAL_DESIGN, 300, "按ID查询");

        Optional<DirectionLearningStep> found = learningPathRepository.findById(step.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getTitle()).isEqualTo("按ID查询");

        assertThat(learningPathRepository.findById(-1L)).isEmpty();
    }

    @Test
    @DisplayName("findByDirection: 应按顺序返回该方向学习步骤，且不包含其它方向")
    void findByDirection_shouldReturnOrderedSteps() {
        List<DirectionLearningStep> steps = learningPathRepository.findByDirection(Direction.COMPUTER_VISION);

        assertThat(steps).isNotEmpty();
        assertThat(steps).extracting(DirectionLearningStep::getDirection).containsOnly(Direction.COMPUTER_VISION);
        assertThat(steps).extracting(DirectionLearningStep::getSortOrder).isSorted();
    }

    @Test
    @DisplayName("existsById: 应正确判断学习步骤是否存在")
    void existsById_shouldWork() {
        DirectionLearningStep step = createStep(Direction.EMBEDDED, 600, "存在步骤");

        assertThat(learningPathRepository.existsById(step.getId())).isTrue();
        assertThat(learningPathRepository.existsById(-1L)).isFalse();
    }

    @Test
    @DisplayName("findMaxSortOrder: 应按方向返回最大顺序值")
    void findMaxSortOrder_shouldReturnMaxWithinDirection() {
        Integer initialMax = learningPathRepository.findMaxSortOrder(Direction.STRUCTURAL_DESIGN);
        assertThat(initialMax).isNotNull();

        createStep(Direction.STRUCTURAL_DESIGN, initialMax + 10, "更大的顺序值");

        assertThat(learningPathRepository.findMaxSortOrder(Direction.STRUCTURAL_DESIGN))
                .isEqualTo(initialMax + 10);
    }

    @Test
    @DisplayName("findMaxSortOrder: 应按方向隔离")
    void findMaxSortOrder_shouldBeScopedToDirection() {
        Integer cvMax = learningPathRepository.findMaxSortOrder(Direction.COMPUTER_VISION);
        createStep(Direction.EMBEDDED, cvMax + 50, "嵌入式大步长");

        assertThat(learningPathRepository.findMaxSortOrder(Direction.COMPUTER_VISION)).isEqualTo(cvMax);
    }

    @Test
    @DisplayName("findMaxSortOrder: 方向下无步骤时应返回 null")
    void findMaxSortOrder_onEmptyDirection_shouldReturnNull() {
        learningPathRepository.findByDirection(Direction.STRUCTURAL_DESIGN)
                .forEach(step -> learningPathRepository.deleteById(step.getId()));

        assertThat(learningPathRepository.findMaxSortOrder(Direction.STRUCTURAL_DESIGN)).isNull();
    }

    @Test
    @DisplayName("batchUpdateSortOrder: 应一次性写入非连续顺序值")
    void batchUpdateSortOrder_shouldAcceptNonContiguousValues() {
        List<DirectionLearningStep> steps = learningPathRepository.findByDirection(Direction.COMPUTER_VISION);
        List<String> titleOrderBefore = steps.stream().map(DirectionLearningStep::getTitle).toList();

        List<String> reversedTitles = new java.util.ArrayList<>(titleOrderBefore);
        java.util.Collections.reverse(reversedTitles);

        List<LearningPathRepository.SortItem> items = new java.util.ArrayList<>();
        int value = 40;
        for (DirectionLearningStep step : steps) {
            items.add(new LearningPathRepository.SortItem(step.getId(), value));
            value -= 10;
        }

        learningPathRepository.batchUpdateSortOrder(Direction.COMPUTER_VISION, items);

        List<DirectionLearningStep> reloaded = learningPathRepository.findByDirection(Direction.COMPUTER_VISION);
        assertThat(reloaded).extracting(DirectionLearningStep::getTitle).containsExactlyElementsOf(reversedTitles);
        assertThat(reloaded).extracting(DirectionLearningStep::getSortOrder).containsExactly(10, 20, 30, 40);
    }

    @Test
    @DisplayName("batchUpdateSortOrder: 瞬时重复顺序值不应触发唯一约束错误")
    void batchUpdateSortOrder_shouldTolerateDuplicateValues() {
        List<DirectionLearningStep> steps = learningPathRepository.findByDirection(Direction.EMBEDDED);
        assertThat(steps).hasSizeGreaterThanOrEqualTo(3);

        List<LearningPathRepository.SortItem> items = List.of(
                new LearningPathRepository.SortItem(steps.get(0).getId(), 5),
                new LearningPathRepository.SortItem(steps.get(1).getId(), 5),
                new LearningPathRepository.SortItem(steps.get(2).getId(), 6));

        assertThatCode(() -> learningPathRepository.batchUpdateSortOrder(Direction.EMBEDDED, items))
                .doesNotThrowAnyException();

        assertThat(learningPathMapper.selectById(steps.get(0).getId()).getSortOrder()).isEqualTo(5);
        assertThat(learningPathMapper.selectById(steps.get(1).getId()).getSortOrder()).isEqualTo(5);
        assertThat(learningPathMapper.selectById(steps.get(2).getId()).getSortOrder()).isEqualTo(6);
    }

    @Test
    @DisplayName("deleteById: 应删除学习步骤")
    void deleteById_shouldRemoveStep() {
        DirectionLearningStep step = createStep(Direction.COMPUTER_VISION, 800, "待删除步骤");
        Long stepId = step.getId();

        learningPathRepository.deleteById(stepId);

        assertThat(learningPathMapper.selectById(stepId)).isNull();
    }
}
