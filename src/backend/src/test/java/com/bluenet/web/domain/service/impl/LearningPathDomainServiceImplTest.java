package com.bluenet.web.domain.service.impl;

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

import com.bluenet.web.domain.model.enumerate.Direction;
import com.bluenet.web.domain.model.vo.LearningStepVO;
import com.bluenet.web.domain.repository.LearningPathRepository;

/**
 * LearningPathDomainServiceImpl单元测试
 */
@DisplayName("LearningPathDomainServiceImpl 单元测试")
@ExtendWith(MockitoExtension.class)
class LearningPathDomainServiceImplTest {

    @Mock
    private LearningPathRepository learningPathRepository;

    @InjectMocks
    private LearningPathDomainServiceImpl learningPathDomainService;

    private static final Long TEST_ID = 1L;
    private static final Direction TEST_DIRECTION = Direction.COMPUTER_VISION;
    private static final Integer TEST_STEP_NUMBER = 1;
    private static final String TEST_TITLE = "Python基础";
    private static final String TEST_VIDEO_URL = "https://example.com/video.mp4";

    private LearningStepVO createTestLearningStepVO() {
        return LearningStepVO.builder()
                .id(TEST_ID)
                .direction(TEST_DIRECTION)
                .stepNumber(TEST_STEP_NUMBER)
                .title(TEST_TITLE)
                .videoUrl(TEST_VIDEO_URL)
                .build();
    }

    // ==================== getLearningPath ====================

    @Test
    @DisplayName("获取学习路径：应返回指定方向的学习步骤列表")
    void getLearningPath_shouldReturnStepsForDirection() {
        // 准备
        List<LearningStepVO> expectedList = new ArrayList<>();
        expectedList.add(createTestLearningStepVO());

        when(learningPathRepository.findByDirection(TEST_DIRECTION)).thenReturn(expectedList);

        // 执行
        List<LearningStepVO> result = learningPathDomainService.getLearningPath(TEST_DIRECTION);

        // 验证
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(TEST_ID, result.get(0).getId());
        assertEquals(TEST_TITLE, result.get(0).getTitle());
        verify(learningPathRepository).findByDirection(TEST_DIRECTION);
    }

    @Test
    @DisplayName("获取学习路径：无步骤时应返回空列表")
    void getLearningPath_noSteps_shouldReturnEmptyList() {
        // 准备
        List<LearningStepVO> expectedList = new ArrayList<>();

        when(learningPathRepository.findByDirection(TEST_DIRECTION)).thenReturn(expectedList);

        // 执行
        List<LearningStepVO> result = learningPathDomainService.getLearningPath(TEST_DIRECTION);

        // 验证
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(learningPathRepository).findByDirection(TEST_DIRECTION);
    }

    // ==================== getStepById ====================

    @Test
    @DisplayName("根据ID获取步骤：应返回学习步骤")
    void getStepById_shouldReturnStep() {
        // 准备
        LearningStepVO expectedVO = createTestLearningStepVO();

        when(learningPathRepository.findById(TEST_ID)).thenReturn(Optional.of(expectedVO));

        // 执行
        Optional<LearningStepVO> result = learningPathDomainService.getStepById(TEST_ID);

        // 验证
        assertTrue(result.isPresent());
        assertEquals(TEST_ID, result.get().getId());
        assertEquals(TEST_TITLE, result.get().getTitle());
        verify(learningPathRepository).findById(TEST_ID);
    }

    @Test
    @DisplayName("根据ID获取步骤：步骤不存在时应返回空Optional")
    void getStepById_notFound_shouldReturnEmpty() {
        // 准备
        when(learningPathRepository.findById(TEST_ID)).thenReturn(Optional.empty());

        // 执行
        Optional<LearningStepVO> result = learningPathDomainService.getStepById(TEST_ID);

        // 验证
        assertTrue(result.isEmpty());
        verify(learningPathRepository).findById(TEST_ID);
    }

    // ==================== createStep ====================

    @Test
    @DisplayName("创建学习步骤：应成功创建并返回ID")
    void createStep_shouldCreateAndReturnId() {
        // 准备
        Long expectedId = 1L;

        when(learningPathRepository.save(any())).thenReturn(expectedId);

        // 执行
        Long result = learningPathDomainService.createStep(
                TEST_DIRECTION,
                TEST_STEP_NUMBER,
                TEST_TITLE,
                TEST_VIDEO_URL);

        // 验证
        assertEquals(expectedId, result);
        verify(learningPathRepository).save(
                argThat(
                        step -> step.getDirection().equals(TEST_DIRECTION)
                                && step.getStepNumber().equals(TEST_STEP_NUMBER)
                                && step.getTitle().equals(TEST_TITLE)
                                && step.getVideoUrl().equals(TEST_VIDEO_URL)));
    }

    @Test
    @DisplayName("创建学习步骤：videoUrl为null时应成功创建")
    void createStep_withNullVideoUrl_shouldCreateSuccessfully() {
        // 准备
        Long expectedId = 1L;

        when(learningPathRepository.save(any())).thenReturn(expectedId);

        // 执行
        Long result = learningPathDomainService.createStep(
                TEST_DIRECTION,
                TEST_STEP_NUMBER,
                TEST_TITLE,
                null);

        // 验证
        assertEquals(expectedId, result);
        verify(learningPathRepository).save(argThat(step -> step.getVideoUrl() == null));
    }

    // ==================== updateStep ====================

    @Test
    @DisplayName("更新学习步骤：应成功更新步骤信息")
    void updateStep_shouldUpdateSuccessfully() {
        // 执行
        learningPathDomainService.updateStep(
                TEST_ID,
                TEST_STEP_NUMBER,
                TEST_TITLE,
                TEST_VIDEO_URL);

        // 验证
        verify(learningPathRepository).update(
                argThat(
                        step -> step.getId().equals(TEST_ID)
                                && step.getStepNumber().equals(TEST_STEP_NUMBER)
                                && step.getTitle().equals(TEST_TITLE)
                                && step.getVideoUrl().equals(TEST_VIDEO_URL)));
    }

    // ==================== deleteStep ====================

    @Test
    @DisplayName("删除学习步骤：应成功删除步骤")
    void deleteStep_shouldDeleteSuccessfully() {
        // 执行
        learningPathDomainService.deleteStep(TEST_ID);

        // 验证
        verify(learningPathRepository).deleteById(TEST_ID);
    }

    // ==================== existsById ====================

    @Test
    @DisplayName("检查存在：步骤存在时应返回true")
    void existsById_existingStep_shouldReturnTrue() {
        // 准备
        when(learningPathRepository.existsById(TEST_ID)).thenReturn(true);

        // 执行
        boolean result = learningPathDomainService.existsById(TEST_ID);

        // 验证
        assertTrue(result);
        verify(learningPathRepository).existsById(TEST_ID);
    }

    @Test
    @DisplayName("检查存在：步骤不存在时应返回false")
    void existsById_nonExistingStep_shouldReturnFalse() {
        // 准备
        when(learningPathRepository.existsById(TEST_ID)).thenReturn(false);

        // 执行
        boolean result = learningPathDomainService.existsById(TEST_ID);

        // 验证
        assertFalse(result);
        verify(learningPathRepository).existsById(TEST_ID);
    }

    // ==================== existsByDirectionAndStepNumber ====================

    @Test
    @DisplayName("检查步骤序号存在：序号已存在时应返回true")
    void existsByDirectionAndStepNumber_existingStepNumber_shouldReturnTrue() {
        // 准备
        when(learningPathRepository.existsByDirectionAndStepNumber(TEST_DIRECTION, TEST_STEP_NUMBER, null))
                .thenReturn(true);

        // 执行
        boolean result = learningPathDomainService.existsByDirectionAndStepNumber(
                TEST_DIRECTION,
                TEST_STEP_NUMBER,
                null);

        // 验证
        assertTrue(result);
        verify(learningPathRepository).existsByDirectionAndStepNumber(TEST_DIRECTION, TEST_STEP_NUMBER, null);
    }

    @Test
    @DisplayName("检查步骤序号存在：序号不存在时应返回false")
    void existsByDirectionAndStepNumber_nonExistingStepNumber_shouldReturnFalse() {
        // 准备
        when(learningPathRepository.existsByDirectionAndStepNumber(TEST_DIRECTION, TEST_STEP_NUMBER, null))
                .thenReturn(false);

        // 执行
        boolean result = learningPathDomainService.existsByDirectionAndStepNumber(
                TEST_DIRECTION,
                TEST_STEP_NUMBER,
                null);

        // 验证
        assertFalse(result);
        verify(learningPathRepository).existsByDirectionAndStepNumber(TEST_DIRECTION, TEST_STEP_NUMBER, null);
    }

    @Test
    @DisplayName("检查步骤序号存在：排除自身时应返回false")
    void existsByDirectionAndStepNumber_excludeSelf_shouldReturnFalse() {
        // 准备
        Long excludeId = TEST_ID;
        when(learningPathRepository.existsByDirectionAndStepNumber(TEST_DIRECTION, TEST_STEP_NUMBER, excludeId))
                .thenReturn(false);

        // 执行
        boolean result = learningPathDomainService.existsByDirectionAndStepNumber(
                TEST_DIRECTION,
                TEST_STEP_NUMBER,
                excludeId);

        // 验证
        assertFalse(result);
        verify(learningPathRepository).existsByDirectionAndStepNumber(TEST_DIRECTION, TEST_STEP_NUMBER, excludeId);
    }
}
