package com.bluenet.web.application.service.impl;

import com.bluenet.web.BaseIntegrationTest;
import com.bluenet.web.application.command.learningpath.LearningPathCommands;
import com.bluenet.web.application.result.learningpath.LearningPathResult;
import com.bluenet.web.application.service.LearningPathAppService;
import com.bluenet.web.domain.exception.DataNotFound;
import com.bluenet.web.domain.model.entity.DirectionLearningStep;
import com.bluenet.web.domain.model.enumerate.Direction;
import com.bluenet.web.domain.repository.LearningPathRepository;
import com.bluenet.web.infrastructure.security.principal.WithSecurityPrincipal;
import com.bluenet.web.infrastructure.security.util.UserCTX;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * LearningPathAppServiceImpl 集成测试。
 *
 * <p>
 * 验证学习路径应用服务的查询、创建（追加到末尾）、更新（不改变位置）、删除（不重排） 以及批量排序逻辑。
 * </p>
 */
@DisplayName("LearningPathAppServiceImpl 集成测试")
class LearningPathAppServiceImplIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private LearningPathAppService learningPathAppService;

    @Autowired
    private LearningPathRepository learningPathRepository;

    private static final String VALID_SLUG = "cv";
    private static final Direction VALID_DIRECTION = Direction.COMPUTER_VISION;

    @AfterEach
    void cleanupSecurityContext() {
        UserCTX.clear();
    }

    @Test
    @WithSecurityPrincipal(userId = 1L, roleType = "MEMBER")
    @DisplayName("getLearningPath: 有效方向标识应返回该方向的学习步骤")
    void getLearningPath_withValidSlug_shouldReturnStepsForDirection() {
        DirectionLearningStep step1 = DirectionLearningStep
                .create(VALID_DIRECTION, 10, "计算机视觉进阶", "http://example.com/cv-10");
        DirectionLearningStep step2 = DirectionLearningStep
                .create(VALID_DIRECTION, 20, "图像处理实战", "http://example.com/cv-20");
        learningPathRepository.save(step1);
        learningPathRepository.save(step2);

        List<LearningPathResult> result = learningPathAppService.getLearningPath(VALID_SLUG);

        assertThat(result).hasSize(6);
        assertThat(result)
                .extracting(LearningPathResult::direction)
                .containsOnly(VALID_DIRECTION);
        assertThat(result)
                .extracting(LearningPathResult::title)
                .contains("计算机视觉进阶", "图像处理实战");
    }

    @Test
    @WithSecurityPrincipal(userId = 1L, roleType = "MEMBER")
    @DisplayName("getLearningPath: 应按顺序值升序返回")
    void getLearningPath_shouldReturnAscendingOrder() {
        List<LearningPathResult> result = learningPathAppService.getLearningPath(VALID_SLUG);

        assertThat(result).extracting(LearningPathResult::sortOrder).isSorted();
    }

    @Test
    @WithSecurityPrincipal(userId = 1L, roleType = "MEMBER")
    @DisplayName("getLearningPath: 未知方向标识应抛 DataNotFound")
    void getLearningPath_withUnknownSlug_shouldThrowDataNotFound() {
        assertThatThrownBy(() -> learningPathAppService.getLearningPath("unknown-direction"))
                .isInstanceOf(DataNotFound.class)
                .hasMessageContaining("无效的方向标识");
    }

    @Test
    @WithSecurityPrincipal(userId = 1L, roleType = "SUPER_ADMIN")
    @DisplayName("createStep: 应创建学习步骤并追加到该方向末尾")
    void createStep_shouldAppendToEnd() {
        Integer maxBefore = learningPathRepository.findMaxSortOrder(VALID_DIRECTION);
        LearningPathCommands.CreateLearningStepCommand command = new LearningPathCommands.CreateLearningStepCommand(
                VALID_SLUG, "计算机视觉进阶", "http://example.com/cv-10");

        LearningPathResult result = learningPathAppService.createStep(command);

        assertThat(result).isNotNull();
        assertThat(result.id()).isNotNull();
        assertThat(result.direction()).isEqualTo(VALID_DIRECTION);
        assertThat(result.sortOrder()).isEqualTo(maxBefore + 1);
        assertThat(result.title()).isEqualTo("计算机视觉进阶");
        assertThat(result.relatedUrl()).isEqualTo("http://example.com/cv-10");
        assertThat(learningPathRepository.findById(result.id()))
                .isPresent()
                .hasValueSatisfying(step -> {
                    assertThat(step.getDirection()).isEqualTo(VALID_DIRECTION);
                    assertThat(step.getSortOrder()).isEqualTo(maxBefore + 1);
                    assertThat(step.getTitle()).isEqualTo("计算机视觉进阶");
                    assertThat(step.getRelatedUrl()).isEqualTo("http://example.com/cv-10");
                });
    }

    @Test
    @WithSecurityPrincipal(userId = 1L, roleType = "SUPER_ADMIN")
    @DisplayName("createStep: 空方向的首个步骤顺序值应为 1")
    void createStep_onEmptyDirection_shouldUseSortOrderOne() {
        learningPathRepository.findByDirection(Direction.STRUCTURAL_DESIGN)
                .forEach(step -> learningPathRepository.deleteById(step.getId()));
        LearningPathCommands.CreateLearningStepCommand command = new LearningPathCommands.CreateLearningStepCommand(
                "struct", "首个步骤", null);

        LearningPathResult result = learningPathAppService.createStep(command);

        assertThat(result.sortOrder()).isEqualTo(1);
    }

    @Test
    @WithSecurityPrincipal(userId = 1L, roleType = "SUPER_ADMIN")
    @DisplayName("createStep: 相同标题不应再受序号冲突限制")
    void createStep_withSameTitle_shouldNotBeRejected() {
        LearningPathCommands.CreateLearningStepCommand command = new LearningPathCommands.CreateLearningStepCommand(
                VALID_SLUG, "重复标题", null);

        LearningPathResult first = learningPathAppService.createStep(command);
        LearningPathResult second = learningPathAppService.createStep(command);

        assertThat(first.sortOrder()).isLessThan(second.sortOrder());
    }

    @Test
    @WithSecurityPrincipal(userId = 1L, roleType = "SUPER_ADMIN")
    @DisplayName("updateStep: 应更新标题和相关链接且不改变顺序值")
    void updateStep_shouldUpdateContentWithoutChangingOrder() {
        DirectionLearningStep step = DirectionLearningStep
                .create(VALID_DIRECTION, 10, "旧标题", "http://example.com/old");
        learningPathRepository.save(step);
        LearningPathCommands.UpdateLearningStepCommand command = new LearningPathCommands.UpdateLearningStepCommand(
                step.getId(), "新标题", "http://example.com/new");

        LearningPathResult result = learningPathAppService.updateStep(command);

        assertThat(result.sortOrder()).isEqualTo(10);
        assertThat(result.title()).isEqualTo("新标题");
        assertThat(result.relatedUrl()).isEqualTo("http://example.com/new");
        assertThat(learningPathRepository.findById(step.getId()))
                .isPresent()
                .hasValueSatisfying(updated -> {
                    assertThat(updated.getSortOrder()).isEqualTo(10);
                    assertThat(updated.getTitle()).isEqualTo("新标题");
                    assertThat(updated.getRelatedUrl()).isEqualTo("http://example.com/new");
                });
    }

    @Test
    @WithSecurityPrincipal(userId = 1L, roleType = "SUPER_ADMIN")
    @DisplayName("updateStep: 不存在的 id 应抛 IllegalArgumentException")
    void updateStep_withNonExistentId_shouldThrowIllegalArgument() {
        LearningPathCommands.UpdateLearningStepCommand command = new LearningPathCommands.UpdateLearningStepCommand(
                99999L, "任意标题", "http://example.com/any");

        assertThatThrownBy(() -> learningPathAppService.updateStep(command))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("学习步骤不存在");
    }

    @Test
    @WithSecurityPrincipal(userId = 1L, roleType = "SUPER_ADMIN")
    @DisplayName("deleteStep: 应删除学习步骤")
    void deleteStep_shouldDelete() {
        DirectionLearningStep step = DirectionLearningStep
                .create(VALID_DIRECTION, 10, "待删除步骤", "http://example.com/cv-delete");
        learningPathRepository.save(step);

        learningPathAppService.deleteStep(step.getId());

        assertThat(learningPathRepository.findById(step.getId())).isEmpty();
    }

    @Test
    @WithSecurityPrincipal(userId = 1L, roleType = "SUPER_ADMIN")
    @DisplayName("deleteStep: 不存在的 id 应抛 IllegalArgumentException")
    void deleteStep_withNonExistentId_shouldThrowIllegalArgument() {
        assertThatThrownBy(() -> learningPathAppService.deleteStep(99999L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("学习步骤不存在");
    }

    @Test
    @WithSecurityPrincipal(userId = 1L, roleType = "SUPER_ADMIN")
    @DisplayName("deleteStep: 删除中间步骤不应重排剩余步骤顺序值")
    void deleteStep_shouldNotRenumberRemainingSteps() {
        List<DirectionLearningStep> before = learningPathRepository.findByDirection(VALID_DIRECTION);
        assertThat(before).hasSizeGreaterThanOrEqualTo(3);

        DirectionLearningStep middle = before.get(1);
        Integer middleOrder = middle.getSortOrder();
        List<Integer> expected = before.stream()
                .map(DirectionLearningStep::getSortOrder)
                .filter(order -> !order.equals(middleOrder))
                .toList();

        learningPathAppService.deleteStep(middle.getId());

        assertThat(learningPathRepository.findByDirection(VALID_DIRECTION))
                .extracting(DirectionLearningStep::getSortOrder)
                .containsExactlyElementsOf(expected);
    }

    @Test
    @WithSecurityPrincipal(userId = 1L, roleType = "SUPER_ADMIN")
    @DisplayName("batchUpdateSortOrder: 应全量覆盖该方向顺序")
    void batchUpdateSortOrder_shouldOverwriteOrder() {
        List<DirectionLearningStep> steps = learningPathRepository.findByDirection(VALID_DIRECTION);
        assertThat(steps).hasSizeGreaterThanOrEqualTo(3);

        // 把最后一个步骤移到最前，其余依次后移
        List<LearningPathCommands.SortItemCommand> items = new ArrayList<>();
        items.add(new LearningPathCommands.SortItemCommand(steps.get(steps.size() - 1).getId(), 1));
        for (int i = 0; i < steps.size() - 1; i++) {
            items.add(new LearningPathCommands.SortItemCommand(steps.get(i).getId(), i + 2));
        }

        learningPathAppService.batchUpdateSortOrder(
                new LearningPathCommands.BatchUpdateSortOrderCommand(
                        VALID_SLUG, items));

        List<DirectionLearningStep> reloaded = learningPathRepository.findByDirection(VALID_DIRECTION);
        assertThat(reloaded.get(0).getId()).isEqualTo(steps.get(steps.size() - 1).getId());
        assertThat(reloaded).extracting(DirectionLearningStep::getSortOrder).isSorted();
    }

    @Test
    @WithSecurityPrincipal(userId = 1L, roleType = "SUPER_ADMIN")
    @DisplayName("batchUpdateSortOrder: 跨方向步骤 id 应被拒绝且不写入")
    void batchUpdateSortOrder_withForeignDirectionId_shouldReject() {
        List<DirectionLearningStep> cvSteps = learningPathRepository.findByDirection(VALID_DIRECTION);
        List<DirectionLearningStep> embedSteps = learningPathRepository.findByDirection(Direction.EMBEDDED);
        assertThat(embedSteps).isNotEmpty();
        List<Integer> orderBefore = cvSteps.stream().map(DirectionLearningStep::getSortOrder).toList();

        List<LearningPathCommands.SortItemCommand> items = new ArrayList<>();
        items.add(new LearningPathCommands.SortItemCommand(cvSteps.get(0).getId(), 1));
        items.add(new LearningPathCommands.SortItemCommand(embedSteps.get(0).getId(), 2));

        assertThatThrownBy(
                () -> learningPathAppService.batchUpdateSortOrder(
                        new LearningPathCommands.BatchUpdateSortOrderCommand(VALID_SLUG, items)))
                                .isInstanceOf(IllegalArgumentException.class);

        assertThat(learningPathRepository.findByDirection(VALID_DIRECTION))
                .extracting(DirectionLearningStep::getSortOrder)
                .containsExactlyElementsOf(orderBefore);
    }

    @Test
    @WithSecurityPrincipal(userId = 1L, roleType = "SUPER_ADMIN")
    @DisplayName("batchUpdateSortOrder: 不存在的步骤 id 应被拒绝")
    void batchUpdateSortOrder_withUnknownId_shouldReject() {
        List<DirectionLearningStep> cvSteps = learningPathRepository.findByDirection(VALID_DIRECTION);
        List<LearningPathCommands.SortItemCommand> items = List.of(
                new LearningPathCommands.SortItemCommand(cvSteps.get(0).getId(), 1),
                new LearningPathCommands.SortItemCommand(99999L, 2));

        assertThatThrownBy(
                () -> learningPathAppService.batchUpdateSortOrder(
                        new LearningPathCommands.BatchUpdateSortOrderCommand(VALID_SLUG, items)))
                                .isInstanceOf(IllegalArgumentException.class);
    }
}
