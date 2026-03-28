package com.bluenet.web.domain.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.bluenet.web.domain.model.entity.IntroduceImage;
import com.bluenet.web.domain.model.enumerate.Direction;
import com.bluenet.web.domain.model.enumerate.ImageType;
import com.bluenet.web.domain.model.vo.IntroduceImageVO;
import com.bluenet.web.domain.repository.IntroduceImageRepository;

/**
 * IntroduceImageDomainServiceImpl单元测试
 */
@DisplayName("IntroduceImageDomainServiceImpl 单元测试")
@ExtendWith(MockitoExtension.class)
class IntroduceImageDomainServiceImplTest {

    @Mock
    private IntroduceImageRepository introduceImageRepository;

    @InjectMocks
    private IntroduceImageDomainServiceImpl introduceImageDomainService;

    private static final Long TEST_ID = 1L;
    private static final Long TEST_FILE_ID = 100L;
    private static final Long TEST_COMPETITION_ID = 10L;
    private static final String TEST_DESCRIPTION = "测试图片";
    private static final String TEST_FILE_URL = "http://example.com/image.jpg";
    private static final Direction TEST_DIRECTION = Direction.COMPUTER_VISION;

    private IntroduceImageVO createTestIntroduceImageVO() {
        return IntroduceImageVO.builder()
                .id(TEST_ID)
                .type(ImageType.LABORATORY)
                .description(TEST_DESCRIPTION)
                .fileId(TEST_FILE_ID)
                .fileUrl(TEST_FILE_URL)
                .direction(TEST_DIRECTION)
                .build();
    }

    /**
     * 获取介绍图片列表：应返回所有匹配的图片
     */
    @Test
    @DisplayName("获取介绍图片列表：应返回所有匹配的图片")
    void getIntroduceImages_shouldReturnMatchingImages() {
        // 准备
        ImageType type = ImageType.LABORATORY;
        Direction direction = null;
        List<IntroduceImageVO> expectedImages = new ArrayList<>();
        expectedImages.add(createTestIntroduceImageVO());

        when(introduceImageRepository.findByTypeAndDirection(type, direction)).thenReturn(expectedImages);

        // 执行
        List<IntroduceImageVO> result = introduceImageDomainService.getIntroduceImages(type, direction);

        // 验证
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(TEST_ID, result.get(0).getId());
        assertEquals(ImageType.LABORATORY, result.get(0).getType());
        verify(introduceImageRepository).findByTypeAndDirection(type, direction);
    }

    /**
     * 获取介绍图片列表：带方向参数应返回匹配的图片
     */
    @Test
    @DisplayName("获取介绍图片列表：带方向参数应返回匹配的图片")
    void getIntroduceImages_withDirection_shouldReturnMatchingImages() {
        // 准备
        ImageType type = ImageType.DIRECTION;
        Direction direction = TEST_DIRECTION;
        List<IntroduceImageVO> expectedImages = new ArrayList<>();
        expectedImages.add(createTestIntroduceImageVO());

        when(introduceImageRepository.findByTypeAndDirection(type, direction)).thenReturn(expectedImages);

        // 执行
        List<IntroduceImageVO> result = introduceImageDomainService.getIntroduceImages(type, direction);

        // 验证
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(TEST_DIRECTION, result.get(0).getDirection());
        verify(introduceImageRepository).findByTypeAndDirection(type, direction);
    }

    /**
     * 获取介绍图片列表：无匹配图片应返回空列表
     */
    @Test
    @DisplayName("获取介绍图片列表：无匹配图片应返回空列表")
    void getIntroduceImages_noMatchingImages_shouldReturnEmptyList() {
        // 准备
        ImageType type = ImageType.LABORATORY;
        Direction direction = null;
        List<IntroduceImageVO> expectedImages = new ArrayList<>();

        when(introduceImageRepository.findByTypeAndDirection(type, direction)).thenReturn(expectedImages);

        // 执行
        List<IntroduceImageVO> result = introduceImageDomainService.getIntroduceImages(type, direction);

        // 验证
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(introduceImageRepository).findByTypeAndDirection(type, direction);
    }

    // ==================== addIntroduceImage 测试 ====================

    /**
     * 添加介绍图片：应成功创建并返回ID
     */
    @Test
    @DisplayName("添加介绍图片：应成功创建并返回ID")
    void addIntroduceImage_shouldCreateAndReturnId() {
        // 准备
        Long expectedId = 1L;
        when(introduceImageRepository.save(any(IntroduceImage.class))).thenReturn(expectedId);

        // 执行
        Long result = introduceImageDomainService.addIntroduceImage(
                ImageType.LABORATORY,
                TEST_FILE_ID,
                null,
                TEST_DESCRIPTION);

        // 验证
        assertEquals(expectedId, result);
        verify(introduceImageRepository).save(any(IntroduceImage.class));
    }

    /**
     * 添加方向介绍图片：应成功创建并设置方向
     */
    @Test
    @DisplayName("添加方向介绍图片：应成功创建并设置方向")
    void addIntroduceImage_withDirection_shouldCreateWithDirection() {
        // 准备
        Long expectedId = 2L;
        when(introduceImageRepository.save(any(IntroduceImage.class))).thenReturn(expectedId);

        // 执行
        Long result = introduceImageDomainService.addIntroduceImage(
                ImageType.DIRECTION,
                TEST_FILE_ID,
                TEST_DIRECTION,
                TEST_DESCRIPTION);

        // 验证
        assertEquals(expectedId, result);
        verify(introduceImageRepository).save(any(IntroduceImage.class));
    }

    /**
     * 添加介绍图片：无描述时应成功创建
     */
    @Test
    @DisplayName("添加介绍图片：无描述时应成功创建")
    void addIntroduceImage_withoutDescription_shouldCreateSuccessfully() {
        // 准备
        Long expectedId = 3L;
        when(introduceImageRepository.save(any(IntroduceImage.class))).thenReturn(expectedId);

        // 执行
        Long result = introduceImageDomainService.addIntroduceImage(
                ImageType.EQUIPMENT,
                TEST_FILE_ID,
                null,
                null);

        // 验证
        assertEquals(expectedId, result);
        verify(introduceImageRepository).save(any(IntroduceImage.class));
    }

    // ==================== addCompetitionImage 测试 ====================

    /**
     * 添加竞赛图片：应成功创建并返回ID
     */
    @Test
    @DisplayName("添加竞赛图片：应成功创建并返回ID")
    void addCompetitionImage_shouldCreateAndReturnId() {
        // 准备
        Long expectedId = 10L;
        when(introduceImageRepository.save(any(IntroduceImage.class))).thenReturn(expectedId);

        // 执行
        Long result = introduceImageDomainService.addCompetitionImage(
                TEST_COMPETITION_ID,
                TEST_FILE_ID,
                TEST_DESCRIPTION);

        // 验证
        assertEquals(expectedId, result);
        verify(introduceImageRepository).save(any(IntroduceImage.class));
    }

    /**
     * 添加竞赛图片：无描述时应成功创建
     */
    @Test
    @DisplayName("添加竞赛图片：无描述时应成功创建")
    void addCompetitionImage_withoutDescription_shouldCreateSuccessfully() {
        // 准备
        Long expectedId = 11L;
        when(introduceImageRepository.save(any(IntroduceImage.class))).thenReturn(expectedId);

        // 执行
        Long result = introduceImageDomainService.addCompetitionImage(
                TEST_COMPETITION_ID,
                TEST_FILE_ID,
                null);

        // 验证
        assertEquals(expectedId, result);
        verify(introduceImageRepository).save(any(IntroduceImage.class));
    }

    // ==================== countCompetitionImages 测试 ====================

    /**
     * 统计竞赛图片数量：应返回正确数量
     */
    @Test
    @DisplayName("统计竞赛图片数量：应返回正确数量")
    void countCompetitionImages_shouldReturnCorrectCount() {
        // 准备
        int expectedCount = 5;
        when(introduceImageRepository.countByTypeAndCompetitionId(ImageType.COMPETITION, TEST_COMPETITION_ID))
                .thenReturn(expectedCount);

        // 执行
        int result = introduceImageDomainService.countCompetitionImages(TEST_COMPETITION_ID);

        // 验证
        assertEquals(expectedCount, result);
        verify(introduceImageRepository).countByTypeAndCompetitionId(ImageType.COMPETITION, TEST_COMPETITION_ID);
    }

    /**
     * 统计竞赛图片数量：无图片时应返回0
     */
    @Test
    @DisplayName("统计竞赛图片数量：无图片时应返回0")
    void countCompetitionImages_noImages_shouldReturnZero() {
        // 准备
        when(introduceImageRepository.countByTypeAndCompetitionId(ImageType.COMPETITION, TEST_COMPETITION_ID))
                .thenReturn(0);

        // 执行
        int result = introduceImageDomainService.countCompetitionImages(TEST_COMPETITION_ID);

        // 验证
        assertEquals(0, result);
    }

    // ==================== getCompetitionImages 测试 ====================

    /**
     * 获取竞赛图片列表：应返回所有竞赛图片
     */
    @Test
    @DisplayName("获取竞赛图片列表：应返回所有竞赛图片")
    void getCompetitionImages_shouldReturnCompetitionImages() {
        // 准备
        List<IntroduceImageVO> expectedImages = new ArrayList<>();
        expectedImages.add(
                IntroduceImageVO.builder()
                        .id(TEST_ID)
                        .type(ImageType.COMPETITION)
                        .competitionId(TEST_COMPETITION_ID)
                        .fileId(TEST_FILE_ID)
                        .fileUrl(TEST_FILE_URL)
                        .build());

        when(introduceImageRepository.findByTypeAndCompetitionId(ImageType.COMPETITION, TEST_COMPETITION_ID))
                .thenReturn(expectedImages);

        // 执行
        List<IntroduceImageVO> result = introduceImageDomainService.getCompetitionImages(TEST_COMPETITION_ID);

        // 验证
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(ImageType.COMPETITION, result.get(0).getType());
        assertEquals(TEST_COMPETITION_ID, result.get(0).getCompetitionId());
        verify(introduceImageRepository).findByTypeAndCompetitionId(ImageType.COMPETITION, TEST_COMPETITION_ID);
    }

    // ==================== removeCompetitionImage 测试 ====================

    /**
     * 删除竞赛图片：应成功删除
     */
    @Test
    @DisplayName("删除竞赛图片：应成功删除")
    void removeCompetitionImage_shouldDeleteSuccessfully() {
        // 准备
        doNothing().when(introduceImageRepository).deleteById(TEST_ID);

        // 执行
        introduceImageDomainService.removeCompetitionImage(TEST_ID);

        // 验证
        verify(introduceImageRepository).deleteById(TEST_ID);
    }

    // ==================== existsById 测试 ====================

    /**
     * 检查图片存在：图片存在时应返回true
     */
    @Test
    @DisplayName("检查图片存在：图片存在时应返回true")
    void existsById_existingImage_shouldReturnTrue() {
        // 准备
        when(introduceImageRepository.existsById(TEST_ID)).thenReturn(true);

        // 执行
        boolean result = introduceImageDomainService.existsById(TEST_ID);

        // 验证
        assertTrue(result);
        verify(introduceImageRepository).existsById(TEST_ID);
    }

    /**
     * 检查图片存在：图片不存在时应返回false
     */
    @Test
    @DisplayName("检查图片存在：图片不存在时应返回false")
    void existsById_nonExistingImage_shouldReturnFalse() {
        // 准备
        when(introduceImageRepository.existsById(TEST_ID)).thenReturn(false);

        // 执行
        boolean result = introduceImageDomainService.existsById(TEST_ID);

        // 验证
        assertFalse(result);
        verify(introduceImageRepository).existsById(TEST_ID);
    }
}
