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

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * LearningPathAppServiceImpl 集成测试。
 *
 * <p>
 * 验证学习路径应用服务的查询、创建、更新、删除逻辑，以及同方向步骤序号唯一性约束。
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
                .extracting(LearningPathResult::stepNumber)
                .contains(10, 20);
        assertThat(result)
                .extracting(LearningPathResult::title)
                .contains("计算机视觉进阶", "图像处理实战");
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
    @DisplayName("createStep: 应创建学习步骤并持久化")
    void createStep_shouldCreateAndPersist() {
        LearningPathCommands.CreateLearningStepCommand command = new LearningPathCommands.CreateLearningStepCommand(
                VALID_SLUG, 10, "计算机视觉进阶", "http://example.com/cv-10");

        LearningPathResult result = learningPathAppService.createStep(command);

        assertThat(result).isNotNull();
        assertThat(result.id()).isNotNull();
        assertThat(result.direction()).isEqualTo(VALID_DIRECTION);
        assertThat(result.stepNumber()).isEqualTo(10);
        assertThat(result.title()).isEqualTo("计算机视觉进阶");
        assertThat(result.relatedUrl()).isEqualTo("http://example.com/cv-10");
        assertThat(learningPathRepository.findById(result.id()))
                .isPresent()
                .hasValueSatisfying(step -> {
                    assertThat(step.getDirection()).isEqualTo(VALID_DIRECTION);
                    assertThat(step.getStepNumber()).isEqualTo(10);
                    assertThat(step.getTitle()).isEqualTo("计算机视觉进阶");
                    assertThat(step.getRelatedUrl()).isEqualTo("http://example.com/cv-10");
                });
    }

    @Test
    @WithSecurityPrincipal(userId = 1L, roleType = "SUPER_ADMIN")
    @DisplayName("createStep: 同一方向重复步骤序号应抛 IllegalArgumentException")
    void createStep_withDuplicateStepNumber_shouldThrowIllegalArgument() {
        DirectionLearningStep existing = DirectionLearningStep
                .create(VALID_DIRECTION, 10, "已有步骤", "http://example.com/cv-existing");
        learningPathRepository.save(existing);
        LearningPathCommands.CreateLearningStepCommand command = new LearningPathCommands.CreateLearningStepCommand(
                VALID_SLUG, 10, "重复步骤", "http://example.com/cv-duplicate");

        assertThatThrownBy(() -> learningPathAppService.createStep(command))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("该方向的步骤序号已存在");
    }

    @Test
    @WithSecurityPrincipal(userId = 1L, roleType = "SUPER_ADMIN")
    @DisplayName("updateStep: 应更新步骤序号、标题和相关链接")
    void updateStep_shouldUpdateStepNumberTitleAndRelatedUrl() {
        DirectionLearningStep step = DirectionLearningStep
                .create(VALID_DIRECTION, 10, "旧标题", "http://example.com/old");
        learningPathRepository.save(step);
        LearningPathCommands.UpdateLearningStepCommand command = new LearningPathCommands.UpdateLearningStepCommand(
                step.getId(), 20, "新标题", "http://example.com/new");

        LearningPathResult result = learningPathAppService.updateStep(command);

        assertThat(result.stepNumber()).isEqualTo(20);
        assertThat(result.title()).isEqualTo("新标题");
        assertThat(result.relatedUrl()).isEqualTo("http://example.com/new");
        assertThat(learningPathRepository.findById(step.getId()))
                .isPresent()
                .hasValueSatisfying(updated -> {
                    assertThat(updated.getStepNumber()).isEqualTo(20);
                    assertThat(updated.getTitle()).isEqualTo("新标题");
                    assertThat(updated.getRelatedUrl()).isEqualTo("http://example.com/new");
                });
    }

    @Test
    @WithSecurityPrincipal(userId = 1L, roleType = "SUPER_ADMIN")
    @DisplayName("updateStep: 不存在的 id 应抛 IllegalArgumentException")
    void updateStep_withNonExistentId_shouldThrowIllegalArgument() {
        LearningPathCommands.UpdateLearningStepCommand command = new LearningPathCommands.UpdateLearningStepCommand(
                99999L, 1, "任意标题", "http://example.com/any");

        assertThatThrownBy(() -> learningPathAppService.updateStep(command))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("学习步骤不存在");
    }

    @Test
    @WithSecurityPrincipal(userId = 1L, roleType = "SUPER_ADMIN")
    @DisplayName("updateStep: 与同一方向其他步骤重复序号应抛 IllegalArgumentException")
    void updateStep_withDuplicateStepNumber_shouldThrowIllegalArgument() {
        DirectionLearningStep step1 = DirectionLearningStep
                .create(VALID_DIRECTION, 10, "步骤一", "http://example.com/cv-10");
        DirectionLearningStep step2 = DirectionLearningStep
                .create(VALID_DIRECTION, 20, "步骤二", "http://example.com/cv-20");
        learningPathRepository.save(step1);
        learningPathRepository.save(step2);
        LearningPathCommands.UpdateLearningStepCommand command = new LearningPathCommands.UpdateLearningStepCommand(
                step2.getId(), 10, "步骤二改名", "http://example.com/cv-20-new");

        assertThatThrownBy(() -> learningPathAppService.updateStep(command))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("该方向的步骤序号已存在");
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
}
