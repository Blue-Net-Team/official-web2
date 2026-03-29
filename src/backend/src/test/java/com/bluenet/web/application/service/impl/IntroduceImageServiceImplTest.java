package com.bluenet.web.application.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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

import com.bluenet.web.api.dto.introduce.IntroduceImageDTO;
import com.bluenet.web.application.converter.IntroduceImageConverter;
import com.bluenet.web.domain.model.enumerate.ImageType;
import com.bluenet.web.domain.model.vo.IntroduceImageVO;
import com.bluenet.web.domain.repository.IntroduceImageRepository;

/**
 * IntroduceImageServiceImpl单元测试
 */
@DisplayName("IntroduceImageServiceImpl 单元测试")
@ExtendWith(MockitoExtension.class)
class IntroduceImageServiceImplTest {

    @Mock
    private IntroduceImageRepository introduceImageRepository;

    @Mock
    private IntroduceImageConverter introduceImageConverter;

    @InjectMocks
    private IntroduceImageServiceImpl introduceImageService;

    private static final Long TEST_ID = 1L;
    private static final Long TEST_FILE_ID = 100L;
    private static final Long TEST_COMPETITION_ID = 10L;
    private static final String TEST_DESCRIPTION = "测试图片";
    private static final String TEST_FILE_URL = "http://example.com/image.jpg";

    private IntroduceImageVO createTestIntroduceImageVO() {
        return IntroduceImageVO.builder()
                .id(TEST_ID)
                .type(ImageType.COMPETITION)
                .description(TEST_DESCRIPTION)
                .fileId(TEST_FILE_ID)
                .fileUrl(TEST_FILE_URL)
                .competitionId(TEST_COMPETITION_ID)
                .build();
    }

    private IntroduceImageDTO createTestIntroduceImageDTO() {
        return IntroduceImageDTO.builder()
                .id(TEST_ID)
                .type(ImageType.COMPETITION)
                .description(TEST_DESCRIPTION)
                .fileId(TEST_FILE_ID)
                .fileUrl(TEST_FILE_URL)
                .build();
    }

    /**
     * 获取介绍图片列表：应返回转换后的DTO列表
     */
    @Test
    @DisplayName("获取介绍图片列表：应返回转换后的DTO列表")
    void getIntroduceImages_shouldReturnConvertedDTOs() {
        // 准备
        ImageType type = ImageType.COMPETITION;
        List<IntroduceImageVO> voList = new ArrayList<>();
        voList.add(createTestIntroduceImageVO());
        List<IntroduceImageDTO> expectedDTOs = new ArrayList<>();
        expectedDTOs.add(createTestIntroduceImageDTO());

        when(introduceImageRepository.findByType(type)).thenReturn(voList);
        when(introduceImageConverter.convertToIntroduceImageDTOList(voList)).thenReturn(expectedDTOs);

        // 执行
        List<IntroduceImageDTO> result = introduceImageService.getIntroduceImages(type);

        // 验证
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(TEST_ID, result.get(0).getId());
        assertEquals(ImageType.COMPETITION, result.get(0).getType());
        verify(introduceImageRepository).findByType(type);
        verify(introduceImageConverter).convertToIntroduceImageDTOList(voList);
    }

    /**
     * 获取介绍图片列表：无匹配图片应返回空列表
     */
    @Test
    @DisplayName("获取介绍图片列表：无匹配图片应返回空列表")
    void getIntroduceImages_noMatchingImages_shouldReturnEmptyList() {
        // 准备
        ImageType type = ImageType.COMPETITION;
        List<IntroduceImageVO> voList = new ArrayList<>();
        List<IntroduceImageDTO> expectedDTOs = new ArrayList<>();

        when(introduceImageRepository.findByType(type)).thenReturn(voList);
        when(introduceImageConverter.convertToIntroduceImageDTOList(voList)).thenReturn(expectedDTOs);

        // 执行
        List<IntroduceImageDTO> result = introduceImageService.getIntroduceImages(type);

        // 验证
        assertNotNull(result);
        assertEquals(0, result.size());
        verify(introduceImageRepository).findByType(type);
        verify(introduceImageConverter).convertToIntroduceImageDTOList(voList);
    }

}
