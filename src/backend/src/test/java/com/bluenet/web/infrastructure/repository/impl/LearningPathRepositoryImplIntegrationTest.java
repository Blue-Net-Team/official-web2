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

/**
 * LearningPathRepositoryImpl 集成测试。
 */
@DisplayName("LearningPathRepositoryImpl 集成测试")
class LearningPathRepositoryImplIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private LearningPathRepository learningPathRepository;

    @Autowired
    private LearningPathMapper learningPathMapper;

    private DirectionLearningStep createStep(Direction direction, Integer stepNumber, String title) {
        DirectionLearningStep step = DirectionLearningStep
                .create(direction, stepNumber, title, "http://example.com/" + title);
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
    }

    @Test
    @DisplayName("save: 已有学习步骤应更新字段")
    void save_existingStep_shouldUpdateFields() {
        DirectionLearningStep step = createStep(Direction.EMBEDDED, 200, "旧标题");
        step.updateTitle("新标题");
        step.updateStepNumber(250);

        learningPathRepository.save(step);

        DirectionLearningStepDO updated = learningPathMapper.selectById(step.getId());
        assertThat(updated.getTitle()).isEqualTo("新标题");
        assertThat(updated.getStepNumber()).isEqualTo(250);
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
    @DisplayName("findByDirection: 应按方向返回学习步骤")
    void findByDirection_shouldReturnSteps() {
        createStep(Direction.COMPUTER_VISION, 400, "CV步骤1");
        createStep(Direction.COMPUTER_VISION, 401, "CV步骤2");
        createStep(Direction.EMBEDDED, 500, "嵌入式步骤");

        List<DirectionLearningStep> steps = learningPathRepository.findByDirection(Direction.COMPUTER_VISION);

        assertThat(steps)
                .extracting(DirectionLearningStep::getTitle)
                .contains("CV步骤1", "CV步骤2")
                .doesNotContain("嵌入式步骤");
    }

    @Test
    @DisplayName("existsById: 应正确判断学习步骤是否存在")
    void existsById_shouldWork() {
        DirectionLearningStep step = createStep(Direction.EMBEDDED, 600, "存在步骤");

        assertThat(learningPathRepository.existsById(step.getId())).isTrue();
        assertThat(learningPathRepository.existsById(-1L)).isFalse();
    }

    @Test
    @DisplayName("existsByDirectionAndStepNumber: 应正确判断步骤序号冲突")
    void existsByDirectionAndStepNumber_shouldWork() {
        DirectionLearningStep step = createStep(Direction.STRUCTURAL_DESIGN, 700, "唯一序号");

        assertThat(
                learningPathRepository.existsByDirectionAndStepNumber(Direction.STRUCTURAL_DESIGN, 700, step.getId()))
                        .isFalse();
        assertThat(learningPathRepository.existsByDirectionAndStepNumber(Direction.STRUCTURAL_DESIGN, 700, -1L))
                .isTrue();
        assertThat(
                learningPathRepository.existsByDirectionAndStepNumber(Direction.STRUCTURAL_DESIGN, 701, step.getId()))
                        .isFalse();
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
