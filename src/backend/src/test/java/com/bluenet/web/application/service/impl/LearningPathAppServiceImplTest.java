package com.bluenet.web.application.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.bluenet.web.application.LearningPathResult;
import com.bluenet.web.application.command.learningpath.LearningPathCommands;
import com.bluenet.web.domain.model.entity.DirectionLearningStep;
import com.bluenet.web.domain.model.enumerate.Direction;
import com.bluenet.web.domain.repository.LearningPathRepository;

/**
 * LearningPathAppServiceImpl 单元测试
 */
@DisplayName("LearningPathAppServiceImpl 单元测试")
@ExtendWith(MockitoExtension.class)
class LearningPathAppServiceImplTest {

    @Mock
    private LearningPathRepository learningPathRepository;

    @InjectMocks
    private LearningPathAppServiceImpl learningPathAppService;

    private static final Long TEST_ID = 1L;
    private static final String TEST_SLUG = "cv";
    private static final Direction TEST_DIRECTION = Direction.COMPUTER_VISION;
    private static final Integer TEST_STEP_NUMBER = 1;
    private static final String TEST_TITLE = "Python基础";
    private static final String TEST_VIDEO_URL = "https://example.com/video.mp4";

    private DirectionLearningStep createTestStep() {
        return DirectionLearningStep.reconstruct(TEST_ID, TEST_DIRECTION, TEST_STEP_NUMBER, TEST_TITLE, TEST_VIDEO_URL);
    }

    // ==================== getLearningPath ====================

    @Test
    @DisplayName("获取学习路径：应返回学习步骤结果列表")
    void getLearningPath_shouldReturnStepResults() {
        List<DirectionLearningStep> steps = new ArrayList<>();
        steps.add(createTestStep());

        when(learningPathRepository.findByDirection(TEST_DIRECTION)).thenReturn(steps);

        List<LearningPathResult> result = learningPathAppService.getLearningPath(TEST_SLUG);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(TEST_ID, result.get(0).id());
        verify(learningPathRepository).findByDirection(TEST_DIRECTION);
    }

    @Test
    @DisplayName("获取学习路径：无效slug应抛出异常")
    void getLearningPath_invalidSlug_shouldThrowException() {
        assertThrows(IllegalArgumentException.class, () -> learningPathAppService.getLearningPath("invalid"));
    }

    // ==================== createStep ====================

    @Test
    @DisplayName("创建学习步骤：应成功创建并返回结果")
    void createStep_shouldCreateAndReturnResult() {
        LearningPathCommands.CreateLearningStepCommand command = new LearningPathCommands.CreateLearningStepCommand(
                TEST_SLUG, TEST_STEP_NUMBER, TEST_TITLE, TEST_VIDEO_URL);

        when(learningPathRepository.existsByDirectionAndStepNumber(TEST_DIRECTION, TEST_STEP_NUMBER, null))
                .thenReturn(false);

        LearningPathResult result = learningPathAppService.createStep(command);

        assertNotNull(result);
        assertEquals(TEST_STEP_NUMBER, result.stepNumber());
        assertEquals(TEST_TITLE, result.title());
        verify(learningPathRepository).save(any(DirectionLearningStep.class));
    }

    @Test
    @DisplayName("创建学习步骤：步骤序号已存在应抛出异常")
    void createStep_existingStepNumber_shouldThrowException() {
        LearningPathCommands.CreateLearningStepCommand command = new LearningPathCommands.CreateLearningStepCommand(
                TEST_SLUG, TEST_STEP_NUMBER, TEST_TITLE, TEST_VIDEO_URL);

        when(learningPathRepository.existsByDirectionAndStepNumber(TEST_DIRECTION, TEST_STEP_NUMBER, null))
                .thenReturn(true);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> learningPathAppService.createStep(command));

        assertTrue(exception.getMessage().contains("步骤序号已存在"));
    }

    // ==================== updateStep ====================

    @Test
    @DisplayName("更新学习步骤：应成功更新并返回结果")
    void updateStep_shouldUpdateAndReturnResult() {
        LearningPathCommands.UpdateLearningStepCommand command = new LearningPathCommands.UpdateLearningStepCommand(
                TEST_ID, TEST_STEP_NUMBER, TEST_TITLE, TEST_VIDEO_URL);

        DirectionLearningStep existingStep = createTestStep();

        when(learningPathRepository.findById(TEST_ID)).thenReturn(Optional.of(existingStep));
        when(learningPathRepository.existsByDirectionAndStepNumber(TEST_DIRECTION, TEST_STEP_NUMBER, TEST_ID))
                .thenReturn(false);

        LearningPathResult result = learningPathAppService.updateStep(command);

        assertNotNull(result);
        assertEquals(TEST_ID, result.id());
        verify(learningPathRepository).update(any(DirectionLearningStep.class));
    }

    @Test
    @DisplayName("更新学习步骤：步骤不存在应抛出异常")
    void updateStep_notFound_shouldThrowException() {
        LearningPathCommands.UpdateLearningStepCommand command = new LearningPathCommands.UpdateLearningStepCommand(
                TEST_ID, TEST_STEP_NUMBER, TEST_TITLE, TEST_VIDEO_URL);

        when(learningPathRepository.findById(TEST_ID)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> learningPathAppService.updateStep(command));

        assertTrue(exception.getMessage().contains("学习步骤不存在"));
    }

    @Test
    @DisplayName("更新学习步骤：步骤序号冲突应抛出异常")
    void updateStep_stepNumberConflict_shouldThrowException() {
        LearningPathCommands.UpdateLearningStepCommand command = new LearningPathCommands.UpdateLearningStepCommand(
                TEST_ID, 2, TEST_TITLE, TEST_VIDEO_URL);

        DirectionLearningStep existingStep = createTestStep();

        when(learningPathRepository.findById(TEST_ID)).thenReturn(Optional.of(existingStep));
        when(learningPathRepository.existsByDirectionAndStepNumber(TEST_DIRECTION, 2, TEST_ID))
                .thenReturn(true);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> learningPathAppService.updateStep(command));

        assertTrue(exception.getMessage().contains("步骤序号已存在"));
    }

    // ==================== deleteStep ====================

    @Test
    @DisplayName("删除学习步骤：应成功删除")
    void deleteStep_shouldDeleteSuccessfully() {
        when(learningPathRepository.existsById(TEST_ID)).thenReturn(true);

        learningPathAppService.deleteStep(TEST_ID);

        verify(learningPathRepository).deleteById(TEST_ID);
    }

    @Test
    @DisplayName("删除学习步骤：步骤不存在应抛出异常")
    void deleteStep_notFound_shouldThrowException() {
        when(learningPathRepository.existsById(TEST_ID)).thenReturn(false);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> learningPathAppService.deleteStep(TEST_ID));

        assertTrue(exception.getMessage().contains("学习步骤不存在"));
    }
}
