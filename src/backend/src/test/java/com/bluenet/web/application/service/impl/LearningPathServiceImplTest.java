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

import com.bluenet.web.api.dto.learningpath.CreateLearningStepRequestDTO;
import com.bluenet.web.api.dto.learningpath.DirectionLearningPathDTO;
import com.bluenet.web.api.dto.learningpath.LearningStepDTO;
import com.bluenet.web.api.dto.learningpath.UpdateLearningStepRequestDTO;
import com.bluenet.web.application.converter.LearningPathConverter;
import com.bluenet.web.domain.exception.GlobalException;
import com.bluenet.web.domain.model.enumerate.Direction;
import com.bluenet.web.domain.model.vo.LearningStepVO;
import com.bluenet.web.domain.service.LearningPathDomainService;

/**
 * LearningPathServiceImpl单元测试
 */
@DisplayName("LearningPathServiceImpl 单元测试")
@ExtendWith(MockitoExtension.class)
class LearningPathServiceImplTest {

    @Mock
    private LearningPathDomainService learningPathDomainService;

    @Mock
    private LearningPathConverter learningPathConverter;

    @InjectMocks
    private LearningPathServiceImpl learningPathService;

    private static final Long TEST_ID = 1L;
    private static final String TEST_SLUG = "cv";
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

    private LearningStepDTO createTestLearningStepDTO() {
        return LearningStepDTO.builder()
                .id(TEST_ID)
                .stepNumber(TEST_STEP_NUMBER)
                .title(TEST_TITLE)
                .videoLink(TEST_VIDEO_URL)
                .build();
    }

    // ==================== getLearningPath ====================

    @Test
    @DisplayName("获取学习路径：应返回方向学习路径DTO")
    void getLearningPath_shouldReturnDirectionLearningPathDTO() {
        // 准备
        List<LearningStepVO> voList = new ArrayList<>();
        voList.add(createTestLearningStepVO());

        DirectionLearningPathDTO expectedDTO = DirectionLearningPathDTO.builder()
                .direction(TEST_SLUG)
                .directionName("计算机视觉")
                .steps(List.of(createTestLearningStepDTO()))
                .build();

        when(learningPathDomainService.getLearningPath(TEST_DIRECTION)).thenReturn(voList);
        when(learningPathConverter.convertToDirectionLearningPathDTO(TEST_DIRECTION, voList)).thenReturn(expectedDTO);

        // 执行
        DirectionLearningPathDTO result = learningPathService.getLearningPath(TEST_SLUG);

        // 验证
        assertNotNull(result);
        assertEquals(TEST_SLUG, result.getDirection());
        verify(learningPathDomainService).getLearningPath(TEST_DIRECTION);
        verify(learningPathConverter).convertToDirectionLearningPathDTO(TEST_DIRECTION, voList);
    }

    @Test
    @DisplayName("获取学习路径：无效slug应抛出异常")
    void getLearningPath_invalidSlug_shouldThrowException() {
        // 执行 & 验证
        assertThrows(IllegalArgumentException.class, () -> learningPathService.getLearningPath("invalid"));
    }

    // ==================== createStep ====================

    @Test
    @DisplayName("创建学习步骤：应成功创建并返回DTO")
    void createStep_shouldCreateAndReturnDTO() {
        // 准备
        CreateLearningStepRequestDTO request = CreateLearningStepRequestDTO.builder()
                .stepNumber(TEST_STEP_NUMBER)
                .title(TEST_TITLE)
                .videoUrl(TEST_VIDEO_URL)
                .build();

        LearningStepVO createdVO = createTestLearningStepVO();
        LearningStepDTO expectedDTO = createTestLearningStepDTO();

        when(learningPathDomainService.existsByDirectionAndStepNumber(TEST_DIRECTION, TEST_STEP_NUMBER, null))
                .thenReturn(false);
        when(learningPathDomainService.createStep(TEST_DIRECTION, TEST_STEP_NUMBER, TEST_TITLE, TEST_VIDEO_URL))
                .thenReturn(TEST_ID);
        when(learningPathDomainService.getStepById(TEST_ID)).thenReturn(Optional.of(createdVO));
        when(learningPathConverter.convertToDTO(createdVO)).thenReturn(expectedDTO);

        // 执行
        LearningStepDTO result = learningPathService.createStep(TEST_SLUG, request);

        // 验证
        assertNotNull(result);
        assertEquals(TEST_ID, result.getId());
        verify(learningPathDomainService).createStep(TEST_DIRECTION, TEST_STEP_NUMBER, TEST_TITLE, TEST_VIDEO_URL);
    }

    @Test
    @DisplayName("创建学习步骤：步骤序号已存在应抛出异常")
    void createStep_existingStepNumber_shouldThrowException() {
        // 准备
        CreateLearningStepRequestDTO request = CreateLearningStepRequestDTO.builder()
                .stepNumber(TEST_STEP_NUMBER)
                .title(TEST_TITLE)
                .videoUrl(TEST_VIDEO_URL)
                .build();

        when(learningPathDomainService.existsByDirectionAndStepNumber(TEST_DIRECTION, TEST_STEP_NUMBER, null))
                .thenReturn(true);

        // 执行 & 验证
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> learningPathService.createStep(TEST_SLUG, request));

        assertTrue(exception.getMessage().contains("步骤序号已存在"));
    }

    @Test
    @DisplayName("创建学习步骤：创建失败应抛出异常")
    void createStep_creationFailed_shouldThrowException() {
        // 准备
        CreateLearningStepRequestDTO request = CreateLearningStepRequestDTO.builder()
                .stepNumber(TEST_STEP_NUMBER)
                .title(TEST_TITLE)
                .videoUrl(TEST_VIDEO_URL)
                .build();

        when(learningPathDomainService.existsByDirectionAndStepNumber(TEST_DIRECTION, TEST_STEP_NUMBER, null))
                .thenReturn(false);
        when(learningPathDomainService.createStep(TEST_DIRECTION, TEST_STEP_NUMBER, TEST_TITLE, TEST_VIDEO_URL))
                .thenReturn(TEST_ID);
        when(learningPathDomainService.getStepById(TEST_ID)).thenReturn(Optional.empty());

        // 执行 & 验证
        GlobalException exception = assertThrows(
                GlobalException.class,
                () -> learningPathService.createStep(TEST_SLUG, request));

        assertTrue(exception.getMessage().contains("创建学习步骤失败"));
    }

    // ==================== updateStep ====================

    @Test
    @DisplayName("更新学习步骤：应成功更新并返回DTO")
    void updateStep_shouldUpdateAndReturnDTO() {
        // 准备
        UpdateLearningStepRequestDTO request = UpdateLearningStepRequestDTO.builder()
                .stepNumber(TEST_STEP_NUMBER)
                .title(TEST_TITLE)
                .videoUrl(TEST_VIDEO_URL)
                .build();

        LearningStepVO existingVO = createTestLearningStepVO();
        LearningStepVO updatedVO = createTestLearningStepVO();
        LearningStepDTO expectedDTO = createTestLearningStepDTO();

        when(learningPathDomainService.existsById(TEST_ID)).thenReturn(true);
        when(learningPathDomainService.getStepById(TEST_ID)).thenReturn(Optional.of(existingVO));
        when(learningPathDomainService.existsByDirectionAndStepNumber(TEST_DIRECTION, TEST_STEP_NUMBER, TEST_ID))
                .thenReturn(false);
        when(learningPathDomainService.getStepById(TEST_ID)).thenReturn(Optional.of(updatedVO));
        when(learningPathConverter.convertToDTO(updatedVO)).thenReturn(expectedDTO);

        // 执行
        LearningStepDTO result = learningPathService.updateStep(TEST_ID, request);

        // 验证
        assertNotNull(result);
        assertEquals(TEST_ID, result.getId());
        verify(learningPathDomainService).updateStep(TEST_ID, TEST_STEP_NUMBER, TEST_TITLE, TEST_VIDEO_URL);
    }

    @Test
    @DisplayName("更新学习步骤：步骤不存在应抛出异常")
    void updateStep_notFound_shouldThrowException() {
        // 准备
        UpdateLearningStepRequestDTO request = UpdateLearningStepRequestDTO.builder()
                .stepNumber(TEST_STEP_NUMBER)
                .title(TEST_TITLE)
                .videoUrl(TEST_VIDEO_URL)
                .build();

        when(learningPathDomainService.existsById(TEST_ID)).thenReturn(false);

        // 执行 & 验证
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> learningPathService.updateStep(TEST_ID, request));

        assertTrue(exception.getMessage().contains("学习步骤不存在"));
    }

    @Test
    @DisplayName("更新学习步骤：步骤序号冲突应抛出异常")
    void updateStep_stepNumberConflict_shouldThrowException() {
        // 准备
        UpdateLearningStepRequestDTO request = UpdateLearningStepRequestDTO.builder()
                .stepNumber(TEST_STEP_NUMBER)
                .title(TEST_TITLE)
                .videoUrl(TEST_VIDEO_URL)
                .build();

        LearningStepVO existingVO = createTestLearningStepVO();

        when(learningPathDomainService.existsById(TEST_ID)).thenReturn(true);
        when(learningPathDomainService.getStepById(TEST_ID)).thenReturn(Optional.of(existingVO));
        when(learningPathDomainService.existsByDirectionAndStepNumber(TEST_DIRECTION, TEST_STEP_NUMBER, TEST_ID))
                .thenReturn(true);

        // 执行 & 验证
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> learningPathService.updateStep(TEST_ID, request));

        assertTrue(exception.getMessage().contains("步骤序号已存在"));
    }

    // ==================== deleteStep ====================

    @Test
    @DisplayName("删除学习步骤：应成功删除")
    void deleteStep_shouldDeleteSuccessfully() {
        // 准备
        when(learningPathDomainService.existsById(TEST_ID)).thenReturn(true);

        // 执行
        learningPathService.deleteStep(TEST_ID);

        // 验证
        verify(learningPathDomainService).deleteStep(TEST_ID);
    }

    @Test
    @DisplayName("删除学习步骤：步骤不存在应抛出异常")
    void deleteStep_notFound_shouldThrowException() {
        // 准备
        when(learningPathDomainService.existsById(TEST_ID)).thenReturn(false);

        // 执行 & 验证
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> learningPathService.deleteStep(TEST_ID));

        assertTrue(exception.getMessage().contains("学习步骤不存在"));
    }
}
