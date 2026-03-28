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

import com.bluenet.web.api.dto.competition.*;
import com.bluenet.web.application.converter.CompetitionConverter;
import com.bluenet.web.domain.model.vo.CompetitionBriefVO;
import com.bluenet.web.domain.model.vo.CompetitionVO;
import com.bluenet.web.domain.model.vo.IntroduceImageVO;
import com.bluenet.web.domain.service.CompetitionDomainService;
import com.bluenet.web.domain.service.IntroduceImageDomainService;

/**
 * CompetitionServiceImpl单元测试
 */
@DisplayName("CompetitionServiceImpl 单元测试")
@ExtendWith(MockitoExtension.class)
class CompetitionServiceImplTest {

    @Mock
    private CompetitionDomainService competitionDomainService;

    @Mock
    private IntroduceImageDomainService introduceImageDomainService;

    @Mock
    private CompetitionConverter competitionConverter;

    @InjectMocks
    private CompetitionServiceImpl competitionService;

    private static final Long TEST_ID = 1L;
    private static final String TEST_NAME = "蓝桥杯";
    private static final String TEST_SHORT_NAME = "蓝桥杯";
    private static final String TEST_LOGO_URL = "http://example.com/logo.png";
    private static final Long TEST_LOGO_FILE_ID = 100L;
    private static final String TEST_SUMMARY = "全国软件和信息技术专业人才大赛";
    private static final String TEST_DETAIL = "蓝桥杯全国软件和信息技术专业人才大赛是由工业和信息化部人才交流中心举办的全国性IT学科赛事。";
    private static final Long TEST_FILE_ID = 100L;
    private static final String TEST_FILE_URL = "http://example.com/image.jpg";
    private static final String TEST_DESCRIPTION = "竞赛照片";

    private CompetitionBriefVO createTestCompetitionBriefVO() {
        return CompetitionBriefVO.builder()
                .id(TEST_ID)
                .name(TEST_NAME)
                .shortName(TEST_SHORT_NAME)
                .logoUrl(TEST_LOGO_URL)
                .logoFileId(TEST_LOGO_FILE_ID)
                .summary(TEST_SUMMARY)
                .build();
    }

    private CompetitionVO createTestCompetitionVO() {
        return CompetitionVO.builder()
                .id(TEST_ID)
                .name(TEST_NAME)
                .shortName(TEST_SHORT_NAME)
                .logoUrl(TEST_LOGO_URL)
                .logoFileId(TEST_LOGO_FILE_ID)
                .summary(TEST_SUMMARY)
                .detail(TEST_DETAIL)
                .sortOrder(0)
                .enabled(true)
                .build();
    }

    private CompetitionBriefDTO createTestCompetitionBriefDTO() {
        return CompetitionBriefDTO.builder()
                .id(TEST_ID)
                .name(TEST_NAME)
                .shortName(TEST_SHORT_NAME)
                .logoUrl(TEST_LOGO_URL)
                .logoFileId(TEST_LOGO_FILE_ID)
                .summary(TEST_SUMMARY)
                .build();
    }

    private CompetitionDetailDTO createTestCompetitionDetailDTO() {
        return CompetitionDetailDTO.builder()
                .id(TEST_ID)
                .name(TEST_NAME)
                .shortName(TEST_SHORT_NAME)
                .logoUrl(TEST_LOGO_URL)
                .logoFileId(TEST_LOGO_FILE_ID)
                .summary(TEST_SUMMARY)
                .detail(TEST_DETAIL)
                .images(new ArrayList<>())
                .build();
    }

    private IntroduceImageVO createTestIntroduceImageVO() {
        return IntroduceImageVO.builder()
                .id(1L)
                .fileId(TEST_FILE_ID)
                .fileUrl(TEST_FILE_URL)
                .description(TEST_DESCRIPTION)
                .build();
    }

    private CreateCompetitionRequestDTO createTestCreateRequest() {
        return CreateCompetitionRequestDTO.builder()
                .name(TEST_NAME)
                .shortName(TEST_SHORT_NAME)
                .logoFileId(TEST_FILE_ID)
                .summary(TEST_SUMMARY)
                .detail(TEST_DETAIL)
                .build();
    }

    private UpdateCompetitionRequestDTO createTestUpdateRequest() {
        return UpdateCompetitionRequestDTO.builder()
                .name(TEST_NAME)
                .shortName(TEST_SHORT_NAME)
                .logoFileId(TEST_FILE_ID)
                .summary(TEST_SUMMARY)
                .detail(TEST_DETAIL)
                .enabled(true)
                .build();
    }

    // ==================== getCompetitionList ====================

    /**
     * 获取竞赛列表：应返回转换后的DTO列表
     */
    @Test
    @DisplayName("获取竞赛列表：应返回转换后的DTO列表")
    void getCompetitionList_shouldReturnConvertedDTOs() {
        // 准备
        int limit = 10;
        List<CompetitionBriefVO> voList = new ArrayList<>();
        voList.add(createTestCompetitionBriefVO());
        List<CompetitionBriefDTO> expectedDTOs = new ArrayList<>();
        expectedDTOs.add(createTestCompetitionBriefDTO());

        when(competitionDomainService.getCompetitionList(limit)).thenReturn(voList);
        when(competitionConverter.convertToBriefDTOList(voList)).thenReturn(expectedDTOs);

        // 执行
        List<CompetitionBriefDTO> result = competitionService.getCompetitionList(limit);

        // 验证
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(TEST_ID, result.get(0).getId());
        verify(competitionDomainService).getCompetitionList(limit);
        verify(competitionConverter).convertToBriefDTOList(voList);
    }

    /**
     * 获取竞赛列表：limit小于1时应使用1
     */
    @Test
    @DisplayName("获取竞赛列表：limit小于1时应使用1")
    void getCompetitionList_withLimitLessThanOne_shouldUseOne() {
        // 准备
        int limit = 0;
        List<CompetitionBriefVO> voList = new ArrayList<>();
        List<CompetitionBriefDTO> expectedDTOs = new ArrayList<>();

        when(competitionDomainService.getCompetitionList(1)).thenReturn(voList);
        when(competitionConverter.convertToBriefDTOList(voList)).thenReturn(expectedDTOs);

        // 执行
        List<CompetitionBriefDTO> result = competitionService.getCompetitionList(limit);

        // 验证
        assertNotNull(result);
        verify(competitionDomainService).getCompetitionList(1);
    }

    /**
     * 获取竞赛列表：limit大于50时应使用50
     */
    @Test
    @DisplayName("获取竞赛列表：limit大于50时应使用50")
    void getCompetitionList_withLimitGreaterThan50_shouldUse50() {
        // 准备
        int limit = 100;
        List<CompetitionBriefVO> voList = new ArrayList<>();
        List<CompetitionBriefDTO> expectedDTOs = new ArrayList<>();

        when(competitionDomainService.getCompetitionList(50)).thenReturn(voList);
        when(competitionConverter.convertToBriefDTOList(voList)).thenReturn(expectedDTOs);

        // 执行
        List<CompetitionBriefDTO> result = competitionService.getCompetitionList(limit);

        // 验证
        assertNotNull(result);
        verify(competitionDomainService).getCompetitionList(50);
    }

    /**
     * 获取竞赛列表：无竞赛时应返回空列表
     */
    @Test
    @DisplayName("获取竞赛列表：无竞赛时应返回空列表")
    void getCompetitionList_noCompetitions_shouldReturnEmptyList() {
        // 准备
        int limit = 10;
        List<CompetitionBriefVO> voList = new ArrayList<>();
        List<CompetitionBriefDTO> expectedDTOs = new ArrayList<>();

        when(competitionDomainService.getCompetitionList(limit)).thenReturn(voList);
        when(competitionConverter.convertToBriefDTOList(voList)).thenReturn(expectedDTOs);

        // 执行
        List<CompetitionBriefDTO> result = competitionService.getCompetitionList(limit);

        // 验证
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // ==================== getCompetitionDetail ====================

    /**
     * 获取竞赛详情：应返回竞赛详情DTO
     */
    @Test
    @DisplayName("获取竞赛详情：应返回竞赛详情DTO")
    void getCompetitionDetail_shouldReturnDetailDTO() {
        // 准备
        CompetitionVO competitionVO = createTestCompetitionVO();
        List<IntroduceImageVO> images = new ArrayList<>();
        CompetitionDetailDTO expectedDTO = createTestCompetitionDetailDTO();

        when(competitionDomainService.getCompetitionById(TEST_ID)).thenReturn(Optional.of(competitionVO));
        when(introduceImageDomainService.getCompetitionImages(TEST_ID)).thenReturn(images);
        when(competitionConverter.convertToDetailDTO(competitionVO, images)).thenReturn(expectedDTO);

        // 执行
        CompetitionDetailDTO result = competitionService.getCompetitionDetail(TEST_ID);

        // 验证
        assertNotNull(result);
        assertEquals(TEST_ID, result.getId());
        assertEquals(TEST_NAME, result.getName());
        verify(competitionDomainService).getCompetitionById(TEST_ID);
        verify(introduceImageDomainService).getCompetitionImages(TEST_ID);
        verify(competitionConverter).convertToDetailDTO(competitionVO, images);
    }

    /**
     * 获取竞赛详情：竞赛不存在时应抛出异常
     */
    @Test
    @DisplayName("获取竞赛详情：竞赛不存在时应抛出异常")
    void getCompetitionDetail_notFound_shouldThrowException() {
        // 准备
        when(competitionDomainService.getCompetitionById(TEST_ID)).thenReturn(Optional.empty());

        // 执行 & 验证
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> competitionService.getCompetitionDetail(TEST_ID));
        assertEquals("竞赛不存在", exception.getMessage());
    }

    // ==================== createCompetition ====================

    /**
     * 创建竞赛：应成功创建并返回DTO
     */
    @Test
    @DisplayName("创建竞赛：应成功创建并返回DTO")
    void createCompetition_shouldCreateAndReturnDTO() {
        // 准备
        CreateCompetitionRequestDTO request = createTestCreateRequest();
        CompetitionVO createdVO = createTestCompetitionVO();
        CompetitionBriefDTO expectedDTO = createTestCompetitionBriefDTO();
        List<IntroduceImageVO> images = new ArrayList<>();

        when(competitionDomainService.createCompetition(anyString(), anyString(), anyLong(), anyString(), anyString()))
                .thenReturn(TEST_ID);
        when(competitionDomainService.getCompetitionById(TEST_ID)).thenReturn(Optional.of(createdVO));
        when(introduceImageDomainService.getCompetitionImages(TEST_ID)).thenReturn(images);
        when(competitionConverter.convertToBriefDTO(any(CompetitionBriefVO.class))).thenReturn(expectedDTO);

        // 执行
        CompetitionBriefDTO result = competitionService.createCompetition(request);

        // 验证
        assertNotNull(result);
        assertEquals(TEST_ID, result.getId());
        assertEquals(TEST_NAME, result.getName());
        verify(competitionDomainService).createCompetition(
                TEST_NAME,
                TEST_SHORT_NAME,
                TEST_FILE_ID,
                TEST_SUMMARY,
                TEST_DETAIL);
        verify(competitionDomainService).getCompetitionById(TEST_ID);
        verify(competitionConverter).convertToBriefDTO(any(CompetitionBriefVO.class));
    }

    /**
     * 创建竞赛：创建后查询为空应抛出异常
     */
    @Test
    @DisplayName("创建竞赛：创建后查询为空应抛出异常")
    void createCompetition_createFailed_shouldThrowException() {
        // 准备
        CreateCompetitionRequestDTO request = createTestCreateRequest();

        when(competitionDomainService.createCompetition(anyString(), anyString(), anyLong(), anyString(), anyString()))
                .thenReturn(TEST_ID);
        when(competitionDomainService.getCompetitionById(TEST_ID)).thenReturn(Optional.empty());

        // 执行 & 验证
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> competitionService.createCompetition(request));
        assertEquals("创建竞赛失败", exception.getMessage());
    }

    // ==================== updateCompetition ====================

    /**
     * 更新竞赛：应成功更新并返回DTO
     */
    @Test
    @DisplayName("更新竞赛：应成功更新并返回DTO")
    void updateCompetition_shouldUpdateAndReturnDTO() {
        // 准备
        UpdateCompetitionRequestDTO request = createTestUpdateRequest();
        CompetitionVO updatedVO = createTestCompetitionVO();
        CompetitionBriefDTO expectedDTO = createTestCompetitionBriefDTO();

        when(competitionDomainService.existsById(TEST_ID)).thenReturn(true);
        when(competitionDomainService.getCompetitionById(TEST_ID)).thenReturn(Optional.of(updatedVO));
        when(competitionConverter.convertToBriefDTO(any(CompetitionBriefVO.class))).thenReturn(expectedDTO);

        // 执行
        CompetitionBriefDTO result = competitionService.updateCompetition(TEST_ID, request);

        // 验证
        assertNotNull(result);
        assertEquals(TEST_ID, result.getId());
        assertEquals(TEST_NAME, result.getName());
        verify(competitionDomainService).updateCompetition(
                TEST_ID,
                TEST_NAME,
                TEST_SHORT_NAME,
                TEST_FILE_ID,
                TEST_SUMMARY,
                TEST_DETAIL,
                true);
        verify(competitionConverter).convertToBriefDTO(any(CompetitionBriefVO.class));
    }

    /**
     * 更新竞赛：竞赛不存在应抛出异常
     */
    @Test
    @DisplayName("更新竞赛：竞赛不存在应抛出异常")
    void updateCompetition_notFound_shouldThrowException() {
        // 准备
        UpdateCompetitionRequestDTO request = createTestUpdateRequest();

        when(competitionDomainService.existsById(TEST_ID)).thenReturn(false);

        // 执行 & 验证
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> competitionService.updateCompetition(TEST_ID, request));
        assertEquals("竞赛不存在", exception.getMessage());
    }

    /**
     * 更新竞赛：更新后查询为空应抛出异常
     */
    @Test
    @DisplayName("更新竞赛：更新后查询为空应抛出异常")
    void updateCompetition_updateFailed_shouldThrowException() {
        // 准备
        UpdateCompetitionRequestDTO request = createTestUpdateRequest();

        when(competitionDomainService.existsById(TEST_ID)).thenReturn(true);
        when(competitionDomainService.getCompetitionById(TEST_ID)).thenReturn(Optional.empty());

        // 执行 & 验证
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> competitionService.updateCompetition(TEST_ID, request));
        assertEquals("更新竞赛失败", exception.getMessage());
    }

    // ==================== deleteCompetition ====================

    /**
     * 删除竞赛：应成功删除竞赛和关联图片
     */
    @Test
    @DisplayName("删除竞赛：应成功删除竞赛和关联图片")
    void deleteCompetition_shouldDeleteCompetitionAndImages() {
        // 准备
        List<IntroduceImageVO> images = new ArrayList<>();
        IntroduceImageVO image = createTestIntroduceImageVO();
        images.add(image);

        when(competitionDomainService.existsById(TEST_ID)).thenReturn(true);
        when(introduceImageDomainService.getCompetitionImages(TEST_ID)).thenReturn(images);

        // 执行
        competitionService.deleteCompetition(TEST_ID);

        // 验证
        verify(competitionDomainService).existsById(TEST_ID);
        verify(introduceImageDomainService).getCompetitionImages(TEST_ID);
        verify(introduceImageDomainService).removeCompetitionImage(image.getId());
        verify(competitionDomainService).deleteCompetition(TEST_ID);
    }

    /**
     * 删除竞赛：竞赛不存在应抛出异常
     */
    @Test
    @DisplayName("删除竞赛：竞赛不存在应抛出异常")
    void deleteCompetition_notFound_shouldThrowException() {
        // 准备
        when(competitionDomainService.existsById(TEST_ID)).thenReturn(false);

        // 执行 & 验证
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> competitionService.deleteCompetition(TEST_ID));
        assertEquals("竞赛不存在", exception.getMessage());
    }

    /**
     * 删除竞赛：无关联图片时应成功删除
     */
    @Test
    @DisplayName("删除竞赛：无关联图片时应成功删除")
    void deleteCompetition_noImages_shouldDeleteSuccessfully() {
        // 准备
        List<IntroduceImageVO> images = new ArrayList<>();

        when(competitionDomainService.existsById(TEST_ID)).thenReturn(true);
        when(introduceImageDomainService.getCompetitionImages(TEST_ID)).thenReturn(images);

        // 执行
        competitionService.deleteCompetition(TEST_ID);

        // 验证
        verify(competitionDomainService).deleteCompetition(TEST_ID);
        verify(introduceImageDomainService, never()).removeCompetitionImage(any());
    }

    // ==================== updateSortOrder ====================

    /**
     * 更新排序：应成功更新排序权重
     */
    @Test
    @DisplayName("更新排序：应成功更新排序权重")
    void updateSortOrder_shouldUpdateSuccessfully() {
        // 准备
        UpdateSortOrderRequestDTO request = UpdateSortOrderRequestDTO.builder().sortOrder(100).build();

        when(competitionDomainService.existsById(TEST_ID)).thenReturn(true);

        // 执行
        competitionService.updateSortOrder(TEST_ID, request);

        // 验证
        verify(competitionDomainService).updateSortOrder(TEST_ID, 100);
    }

    /**
     * 更新排序：竞赛不存在应抛出异常
     */
    @Test
    @DisplayName("更新排序：竞赛不存在应抛出异常")
    void updateSortOrder_notFound_shouldThrowException() {
        // 准备
        UpdateSortOrderRequestDTO request = UpdateSortOrderRequestDTO.builder().sortOrder(100).build();

        when(competitionDomainService.existsById(TEST_ID)).thenReturn(false);

        // 执行 & 验证
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> competitionService.updateSortOrder(TEST_ID, request));
        assertEquals("竞赛不存在", exception.getMessage());
    }

    // ==================== addCompetitionImage ====================

    /**
     * 添加竞赛照片：应成功添加并返回DTO
     */
    @Test
    @DisplayName("添加竞赛照片：应成功添加并返回DTO")
    void addCompetitionImage_shouldAddAndReturnDTO() {
        // 准备
        AddCompetitionImageRequestDTO request = AddCompetitionImageRequestDTO.builder()
                .fileId(TEST_FILE_ID)
                .description(TEST_DESCRIPTION)
                .build();
        Long imageId = 1L;

        when(competitionDomainService.existsById(TEST_ID)).thenReturn(true);
        when(introduceImageDomainService.countCompetitionImages(TEST_ID)).thenReturn(0);
        when(introduceImageDomainService.addCompetitionImage(TEST_ID, TEST_FILE_ID, TEST_DESCRIPTION))
                .thenReturn(imageId);

        // 执行
        CompetitionImageDTO result = competitionService.addCompetitionImage(TEST_ID, request);

        // 验证
        assertNotNull(result);
        assertEquals(imageId, result.getId());
        assertEquals(TEST_DESCRIPTION, result.getDescription());
    }

    /**
     * 添加竞赛照片：竞赛不存在应抛出异常
     */
    @Test
    @DisplayName("添加竞赛照片：竞赛不存在应抛出异常")
    void addCompetitionImage_competitionNotFound_shouldThrowException() {
        // 准备
        AddCompetitionImageRequestDTO request = AddCompetitionImageRequestDTO.builder()
                .fileId(TEST_FILE_ID)
                .description(TEST_DESCRIPTION)
                .build();

        when(competitionDomainService.existsById(TEST_ID)).thenReturn(false);

        // 执行 & 验证
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> competitionService.addCompetitionImage(TEST_ID, request));
        assertEquals("竞赛不存在", exception.getMessage());
    }

    /**
     * 添加竞赛照片：超过20张限制应抛出异常
     */
    @Test
    @DisplayName("添加竞赛照片：超过20张限制应抛出异常")
    void addCompetitionImage_exceedLimit_shouldThrowException() {
        // 准备
        AddCompetitionImageRequestDTO request = AddCompetitionImageRequestDTO.builder()
                .fileId(TEST_FILE_ID)
                .description(TEST_DESCRIPTION)
                .build();

        when(competitionDomainService.existsById(TEST_ID)).thenReturn(true);
        when(introduceImageDomainService.countCompetitionImages(TEST_ID)).thenReturn(20);

        // 执行 & 验证
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> competitionService.addCompetitionImage(TEST_ID, request));
        assertEquals("每个竞赛最多关联20张照片", exception.getMessage());
    }

    /**
     * 添加竞赛照片：已有19张时应允许添加
     */
    @Test
    @DisplayName("添加竞赛照片：已有19张时应允许添加")
    void addCompetitionImage_with19Images_shouldAllowAdd() {
        // 准备
        AddCompetitionImageRequestDTO request = AddCompetitionImageRequestDTO.builder()
                .fileId(TEST_FILE_ID)
                .description(TEST_DESCRIPTION)
                .build();
        Long imageId = 1L;

        when(competitionDomainService.existsById(TEST_ID)).thenReturn(true);
        when(introduceImageDomainService.countCompetitionImages(TEST_ID)).thenReturn(19);
        when(introduceImageDomainService.addCompetitionImage(TEST_ID, TEST_FILE_ID, TEST_DESCRIPTION))
                .thenReturn(imageId);

        // 执行
        CompetitionImageDTO result = competitionService.addCompetitionImage(TEST_ID, request);

        // 验证
        assertNotNull(result);
        assertEquals(imageId, result.getId());
    }

    // ==================== removeCompetitionImage ====================

    /**
     * 删除竞赛照片：应成功删除
     */
    @Test
    @DisplayName("删除竞赛照片：应成功删除")
    void removeCompetitionImage_shouldRemoveSuccessfully() {
        // 准备
        Long imageId = 1L;

        when(competitionDomainService.existsById(TEST_ID)).thenReturn(true);
        when(introduceImageDomainService.existsById(imageId)).thenReturn(true);

        // 执行
        competitionService.removeCompetitionImage(TEST_ID, imageId);

        // 验证
        verify(introduceImageDomainService).removeCompetitionImage(imageId);
    }

    /**
     * 删除竞赛照片：竞赛不存在应抛出异常
     */
    @Test
    @DisplayName("删除竞赛照片：竞赛不存在应抛出异常")
    void removeCompetitionImage_competitionNotFound_shouldThrowException() {
        // 准备
        Long imageId = 1L;

        when(competitionDomainService.existsById(TEST_ID)).thenReturn(false);

        // 执行 & 验证
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> competitionService.removeCompetitionImage(TEST_ID, imageId));
        assertEquals("竞赛不存在", exception.getMessage());
    }

    /**
     * 删除竞赛照片：图片不存在应抛出异常
     */
    @Test
    @DisplayName("删除竞赛照片：图片不存在应抛出异常")
    void removeCompetitionImage_imageNotFound_shouldThrowException() {
        // 准备
        Long imageId = 1L;

        when(competitionDomainService.existsById(TEST_ID)).thenReturn(true);
        when(introduceImageDomainService.existsById(imageId)).thenReturn(false);

        // 执行 & 验证
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> competitionService.removeCompetitionImage(TEST_ID, imageId));
        assertEquals("图片不存在", exception.getMessage());
    }
}
