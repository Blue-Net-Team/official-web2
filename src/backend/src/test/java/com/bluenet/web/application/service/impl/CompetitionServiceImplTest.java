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
import com.bluenet.web.domain.exception.BadRequest;
import com.bluenet.web.domain.exception.DataNotFound;
import com.bluenet.web.domain.exception.GlobalException;
import com.bluenet.web.domain.model.enumerate.FileType;
import com.bluenet.web.domain.model.vo.CompetitionBriefVO;
import com.bluenet.web.domain.model.vo.CompetitionVO;
import com.bluenet.web.domain.model.vo.FileVO;
import com.bluenet.web.domain.service.CompetitionDomainService;
import com.bluenet.web.domain.service.FileDomainService;

/**
 * CompetitionServiceImpl单元测试
 */
@DisplayName("CompetitionServiceImpl 单元测试")
@ExtendWith(MockitoExtension.class)
class CompetitionServiceImplTest {

    @Mock
    private CompetitionDomainService competitionDomainService;

    @Mock
    private FileDomainService fileDomainService;

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
                .build();
    }

    private CreateCompetitionRequestDTO createTestCreateRequest() {
        return CreateCompetitionRequestDTO.builder()
                .name(TEST_NAME)
                .shortName(TEST_SHORT_NAME)
                .logoFileId(TEST_FILE_ID)
                .summary(TEST_SUMMARY)
                .detail(TEST_DETAIL)
                .level("国家级")
                .month("4月")
                .organizer("工信部")
                .build();
    }

    private UpdateCompetitionRequestDTO createTestUpdateRequest() {
        return UpdateCompetitionRequestDTO.builder()
                .name(TEST_NAME)
                .shortName(TEST_SHORT_NAME)
                .logoFileId(TEST_FILE_ID)
                .summary(TEST_SUMMARY)
                .detail(TEST_DETAIL)
                .level("国家级")
                .month("4月")
                .organizer("工信部")
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

    // ==================== getCompetitionResponseList ====================

    /**
     * 获取竞赛响应列表：应返回响应DTO列表
     */
    @Test
    @DisplayName("获取竞赛响应列表：应返回响应DTO列表")
    void getCompetitionResponseList_shouldReturnResponseDTOs() {
        // 准备
        int limit = 10;
        List<CompetitionBriefVO> voList = new ArrayList<>();
        voList.add(createTestCompetitionBriefVO());

        List<CompetitionResponseDTO> expectedDTOs = new ArrayList<>();
        expectedDTOs.add(
                CompetitionResponseDTO.builder()
                        .id(TEST_ID)
                        .name(TEST_NAME)
                        .summary(TEST_SUMMARY)
                        .build());

        when(competitionDomainService.getCompetitionList(limit)).thenReturn(voList);
        when(competitionConverter.convertToResponseDTOList(voList)).thenReturn(expectedDTOs);

        // 执行
        List<CompetitionResponseDTO> result = competitionService.getCompetitionResponseList(limit);

        // 验证
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(TEST_ID, result.get(0).getId());
        verify(competitionDomainService).getCompetitionList(limit);
        verify(competitionConverter).convertToResponseDTOList(voList);
    }

    /**
     * 获取竞赛响应列表：limit 参数校验 - 小于 1 时应使用 1
     */
    @Test
    @DisplayName("获取竞赛响应列表：limit 小于 1 时应使用 1")
    void getCompetitionResponseList_limitLessThanOne_shouldUseOne() {
        // 准备
        int limit = 0;
        List<CompetitionBriefVO> voList = new ArrayList<>();
        List<CompetitionResponseDTO> expectedDTOs = new ArrayList<>();

        when(competitionDomainService.getCompetitionList(1)).thenReturn(voList);
        when(competitionConverter.convertToResponseDTOList(voList)).thenReturn(expectedDTOs);

        // 执行
        List<CompetitionResponseDTO> result = competitionService.getCompetitionResponseList(limit);

        // 验证
        assertNotNull(result);
        verify(competitionDomainService).getCompetitionList(1);
    }

    /**
     * 获取竞赛响应列表：limit 参数校验 - 大于 50 时应使用 50
     */
    @Test
    @DisplayName("获取竞赛响应列表：limit 大于 50 时应使用 50")
    void getCompetitionResponseList_limitGreaterThan50_shouldUse50() {
        // 准备
        int limit = 100;
        List<CompetitionBriefVO> voList = new ArrayList<>();
        List<CompetitionResponseDTO> expectedDTOs = new ArrayList<>();

        when(competitionDomainService.getCompetitionList(50)).thenReturn(voList);
        when(competitionConverter.convertToResponseDTOList(voList)).thenReturn(expectedDTOs);

        // 执行
        List<CompetitionResponseDTO> result = competitionService.getCompetitionResponseList(limit);

        // 验证
        assertNotNull(result);
        verify(competitionDomainService).getCompetitionList(50);
    }

    /**
     * 获取竞赛响应列表：空列表时应返回空列表
     */
    @Test
    @DisplayName("获取竞赛响应列表：空列表时应返回空列表")
    void getCompetitionResponseList_emptyList_shouldReturnEmptyList() {
        // 准备
        int limit = 10;
        List<CompetitionBriefVO> voList = new ArrayList<>();
        List<CompetitionResponseDTO> expectedDTOs = new ArrayList<>();

        when(competitionDomainService.getCompetitionList(limit)).thenReturn(voList);
        when(competitionConverter.convertToResponseDTOList(voList)).thenReturn(expectedDTOs);

        // 执行
        List<CompetitionResponseDTO> result = competitionService.getCompetitionResponseList(limit);

        // 验证
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    /**
     * 获取竞赛响应列表：多个竞赛时应全部转换
     */
    @Test
    @DisplayName("获取竞赛响应列表：多个竞赛时应全部转换")
    void getCompetitionResponseList_multipleCompetitions_shouldConvertAll() {
        // 准备
        int limit = 10;
        List<CompetitionBriefVO> voList = new ArrayList<>();
        voList.add(CompetitionBriefVO.builder().id(1L).name("竞赛1").build());
        voList.add(CompetitionBriefVO.builder().id(2L).name("竞赛2").build());
        voList.add(CompetitionBriefVO.builder().id(3L).name("竞赛3").build());

        when(competitionDomainService.getCompetitionList(limit)).thenReturn(voList);
        when(competitionConverter.convertToResponseDTOList(voList)).thenReturn(new ArrayList<>());

        // 执行
        competitionService.getCompetitionResponseList(limit);

        // 验证
        verify(competitionConverter).convertToResponseDTOList(voList);
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
        CompetitionDetailDTO expectedDTO = createTestCompetitionDetailDTO();

        when(competitionDomainService.getCompetitionById(TEST_ID)).thenReturn(Optional.of(competitionVO));
        when(competitionConverter.convertToDetailDTO(competitionVO)).thenReturn(expectedDTO);

        // 执行
        CompetitionDetailDTO result = competitionService.getCompetitionDetail(TEST_ID);

        // 验证
        assertNotNull(result);
        assertEquals(TEST_ID, result.getId());
        assertEquals(TEST_NAME, result.getName());
        verify(competitionDomainService).getCompetitionById(TEST_ID);
        verify(competitionConverter).convertToDetailDTO(competitionVO);
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

        when(
                competitionDomainService.createCompetition(
                        anyString(),
                        anyString(),
                        anyLong(),
                        anyString(),
                        anyString(),
                        anyString(),
                        anyString(),
                        anyString()))
                                .thenReturn(TEST_ID);
        when(competitionDomainService.getCompetitionById(TEST_ID)).thenReturn(Optional.of(createdVO));
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
                TEST_DETAIL,
                request.getLevel(),
                request.getMonth(),
                request.getOrganizer());
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

        when(
                competitionDomainService.createCompetition(
                        anyString(),
                        anyString(),
                        anyLong(),
                        anyString(),
                        anyString(),
                        anyString(),
                        anyString(),
                        anyString()))
                                .thenReturn(TEST_ID);
        when(competitionDomainService.getCompetitionById(TEST_ID)).thenReturn(Optional.empty());

        // 执行 & 验证
        GlobalException exception = assertThrows(
                GlobalException.class,
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
                request.getLevel(),
                request.getMonth(),
                request.getOrganizer());
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
        GlobalException exception = assertThrows(
                GlobalException.class,
                () -> competitionService.updateCompetition(TEST_ID, request));
        assertEquals("更新竞赛失败", exception.getMessage());
    }

    // ==================== deleteCompetition ====================

    /**
     * 删除竞赛：应成功删除竞赛
     */
    @Test
    @DisplayName("删除竞赛：应成功删除竞赛")
    void deleteCompetition_shouldDeleteCompetition() {
        // 准备
        when(competitionDomainService.existsById(TEST_ID)).thenReturn(true);

        // 执行
        competitionService.deleteCompetition(TEST_ID);

        // 验证
        verify(competitionDomainService).existsById(TEST_ID);
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

    // ==================== updateLogo ====================

    /**
     * 更新Logo：应成功更新竞赛Logo
     */
    @Test
    @DisplayName("更新Logo：应成功更新竞赛Logo")
    void updateLogo_shouldUpdateSuccessfully() {
        // 准备
        Long fileId = 200L;
        FileVO fileVO = FileVO.builder().id(fileId).type(FileType.NORMAL_IMG).build();

        when(competitionDomainService.existsById(TEST_ID)).thenReturn(true);
        when(fileDomainService.getFileById(fileId)).thenReturn(fileVO);

        // 执行
        competitionService.updateLogo(TEST_ID, fileId);

        // 验证
        verify(competitionDomainService).updateLogo(TEST_ID, fileId);
    }

    /**
     * 更新Logo：竞赛不存在应抛出DataNotFound
     */
    @Test
    @DisplayName("更新Logo：竞赛不存在应抛出DataNotFound")
    void updateLogo_competitionNotFound_shouldThrowDataNotFound() {
        // 准备
        Long fileId = 200L;

        when(competitionDomainService.existsById(TEST_ID)).thenReturn(false);

        // 执行 & 验证
        DataNotFound exception = assertThrows(
                DataNotFound.class,
                () -> competitionService.updateLogo(TEST_ID, fileId));
        assertEquals("竞赛不存在", exception.getMessage());
    }

    /**
     * 更新Logo：文件不存在应抛出DataNotFound
     */
    @Test
    @DisplayName("更新Logo：文件不存在应抛出DataNotFound")
    void updateLogo_fileNotFound_shouldThrowDataNotFound() {
        // 准备
        Long fileId = 200L;

        when(competitionDomainService.existsById(TEST_ID)).thenReturn(true);
        when(fileDomainService.getFileById(fileId)).thenReturn(null);

        // 执行 & 验证
        DataNotFound exception = assertThrows(
                DataNotFound.class,
                () -> competitionService.updateLogo(TEST_ID, fileId));
        assertEquals("文件不存在", exception.getMessage());
    }

    /**
     * 更新Logo：文件类型不匹配应抛出BadRequest
     */
    @Test
    @DisplayName("更新Logo：文件类型不匹配应抛出BadRequest")
    void updateLogo_fileTypeMismatch_shouldThrowBadRequest() {
        // 准备
        Long fileId = 200L;
        FileVO fileVO = FileVO.builder().id(fileId).type(FileType.AVATAR).build();

        when(competitionDomainService.existsById(TEST_ID)).thenReturn(true);
        when(fileDomainService.getFileById(fileId)).thenReturn(fileVO);

        // 执行 & 验证
        BadRequest exception = assertThrows(
                BadRequest.class,
                () -> competitionService.updateLogo(TEST_ID, fileId));
        assertEquals("文件类型不匹配，期望 NORMAL_IMG", exception.getMessage());
    }

    // ==================== updateCover ====================

    /**
     * 更新封面：应成功更新竞赛封面
     */
    @Test
    @DisplayName("更新封面：应成功更新竞赛封面")
    void updateCover_shouldUpdateSuccessfully() {
        // 准备
        Long fileId = 300L;
        FileVO fileVO = FileVO.builder().id(fileId).type(FileType.NORMAL_IMG).build();

        when(competitionDomainService.existsById(TEST_ID)).thenReturn(true);
        when(fileDomainService.getFileById(fileId)).thenReturn(fileVO);

        // 执行
        competitionService.updateCover(TEST_ID, fileId);

        // 验证
        verify(competitionDomainService).updateCover(TEST_ID, fileId);
    }

    /**
     * 更新封面：竞赛不存在应抛出DataNotFound
     */
    @Test
    @DisplayName("更新封面：竞赛不存在应抛出DataNotFound")
    void updateCover_competitionNotFound_shouldThrowDataNotFound() {
        // 准备
        Long fileId = 300L;

        when(competitionDomainService.existsById(TEST_ID)).thenReturn(false);

        // 执行 & 验证
        DataNotFound exception = assertThrows(
                DataNotFound.class,
                () -> competitionService.updateCover(TEST_ID, fileId));
        assertEquals("竞赛不存在", exception.getMessage());
    }

    /**
     * 更新封面：文件不存在应抛出DataNotFound
     */
    @Test
    @DisplayName("更新封面：文件不存在应抛出DataNotFound")
    void updateCover_fileNotFound_shouldThrowDataNotFound() {
        // 准备
        Long fileId = 300L;

        when(competitionDomainService.existsById(TEST_ID)).thenReturn(true);
        when(fileDomainService.getFileById(fileId)).thenReturn(null);

        // 执行 & 验证
        DataNotFound exception = assertThrows(
                DataNotFound.class,
                () -> competitionService.updateCover(TEST_ID, fileId));
        assertEquals("文件不存在", exception.getMessage());
    }

    /**
     * 更新封面：文件类型不匹配应抛出BadRequest
     */
    @Test
    @DisplayName("更新封面：文件类型不匹配应抛出BadRequest")
    void updateCover_fileTypeMismatch_shouldThrowBadRequest() {
        // 准备
        Long fileId = 300L;
        FileVO fileVO = FileVO.builder().id(fileId).type(FileType.AVATAR).build();

        when(competitionDomainService.existsById(TEST_ID)).thenReturn(true);
        when(fileDomainService.getFileById(fileId)).thenReturn(fileVO);

        // 执行 & 验证
        BadRequest exception = assertThrows(
                BadRequest.class,
                () -> competitionService.updateCover(TEST_ID, fileId));
        assertEquals("文件类型不匹配，期望 NORMAL_IMG", exception.getMessage());
    }
}
