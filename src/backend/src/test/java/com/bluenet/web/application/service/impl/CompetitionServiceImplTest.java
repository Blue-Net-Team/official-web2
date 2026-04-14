package com.bluenet.web.application.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
import com.bluenet.web.domain.model.vo.FileVO;
import com.bluenet.web.domain.service.CompetitionDomainService;
import com.bluenet.web.domain.service.FileDomainService;

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
    private static final Long TEST_LOGO_FILE_ID = 100L;
    private static final Long TEST_COVER_FILE_ID = 200L;
    private static final String TEST_SUMMARY = "全国软件和信息技术专业人才大赛";
    private static final Long TEST_FILE_ID = 100L;

    private CompetitionBriefVO createTestCompetitionBriefVO() {
        return CompetitionBriefVO.builder()
                .id(TEST_ID)
                .name(TEST_NAME)
                .shortName(TEST_SHORT_NAME)
                .logoFileId(TEST_LOGO_FILE_ID)
                .coverFileId(TEST_COVER_FILE_ID)
                .summary(TEST_SUMMARY)
                .build();
    }

    private CompetitionRequestDTO createTestRequest() {
        return CompetitionRequestDTO.builder()
                .name(TEST_NAME)
                .shortName(TEST_SHORT_NAME)
                .logoFileId(TEST_FILE_ID)
                .summary(TEST_SUMMARY)
                .level("国家级")
                .month("4月")
                .organizer("工信部")
                .build();
    }

    // ==================== getCompetitionResponseList ====================

    @Test
    @DisplayName("获取竞赛响应列表：应返回响应DTO列表")
    void getCompetitionResponseList_shouldReturnResponseDTOs() {
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

        List<CompetitionResponseDTO> result = competitionService.getCompetitionResponseList(limit);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(TEST_ID, result.get(0).getId());
        verify(competitionDomainService).getCompetitionList(limit);
        verify(competitionConverter).convertToResponseDTOList(voList);
    }

    @Test
    @DisplayName("获取竞赛响应列表：limit小于1时应使用1")
    void getCompetitionResponseList_limitLessThanOne_shouldUseOne() {
        int limit = 0;
        List<CompetitionBriefVO> voList = new ArrayList<>();
        List<CompetitionResponseDTO> expectedDTOs = new ArrayList<>();

        when(competitionDomainService.getCompetitionList(1)).thenReturn(voList);
        when(competitionConverter.convertToResponseDTOList(voList)).thenReturn(expectedDTOs);

        List<CompetitionResponseDTO> result = competitionService.getCompetitionResponseList(limit);

        assertNotNull(result);
        verify(competitionDomainService).getCompetitionList(1);
    }

    @Test
    @DisplayName("获取竞赛响应列表：limit大于50时应使用50")
    void getCompetitionResponseList_limitGreaterThan50_shouldUse50() {
        int limit = 100;
        List<CompetitionBriefVO> voList = new ArrayList<>();
        List<CompetitionResponseDTO> expectedDTOs = new ArrayList<>();

        when(competitionDomainService.getCompetitionList(50)).thenReturn(voList);
        when(competitionConverter.convertToResponseDTOList(voList)).thenReturn(expectedDTOs);

        List<CompetitionResponseDTO> result = competitionService.getCompetitionResponseList(limit);

        assertNotNull(result);
        verify(competitionDomainService).getCompetitionList(50);
    }

    @Test
    @DisplayName("获取竞赛响应列表：空列表时应返回空列表")
    void getCompetitionResponseList_emptyList_shouldReturnEmptyList() {
        int limit = 10;
        List<CompetitionBriefVO> voList = new ArrayList<>();
        List<CompetitionResponseDTO> expectedDTOs = new ArrayList<>();

        when(competitionDomainService.getCompetitionList(limit)).thenReturn(voList);
        when(competitionConverter.convertToResponseDTOList(voList)).thenReturn(expectedDTOs);

        List<CompetitionResponseDTO> result = competitionService.getCompetitionResponseList(limit);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("获取竞赛响应列表：多个竞赛时应全部转换")
    void getCompetitionResponseList_multipleCompetitions_shouldConvertAll() {
        int limit = 10;
        List<CompetitionBriefVO> voList = new ArrayList<>();
        voList.add(CompetitionBriefVO.builder().id(1L).name("竞赛1").build());
        voList.add(CompetitionBriefVO.builder().id(2L).name("竞赛2").build());
        voList.add(CompetitionBriefVO.builder().id(3L).name("竞赛3").build());

        when(competitionDomainService.getCompetitionList(limit)).thenReturn(voList);
        when(competitionConverter.convertToResponseDTOList(voList)).thenReturn(new ArrayList<>());

        competitionService.getCompetitionResponseList(limit);

        verify(competitionConverter).convertToResponseDTOList(voList);
    }

    // ==================== createCompetition ====================

    @Test
    @DisplayName("创建竞赛：应成功创建并返回DTO")
    void createCompetition_shouldCreateAndReturnDTO() {
        CompetitionRequestDTO request = createTestRequest();
        CompetitionResponseDTO expectedDTO = CompetitionResponseDTO.builder()
                .id(TEST_ID)
                .name(TEST_NAME)
                .summary(TEST_SUMMARY)
                .build();

        when(
                competitionDomainService.createCompetition(
                        anyString(),
                        any(),
                        any(),
                        anyString(),
                        anyString(),
                        anyString(),
                        anyString()))
                                .thenReturn(TEST_ID);
        when(competitionConverter.convertToResponseDTO(any(CompetitionBriefVO.class))).thenReturn(expectedDTO);

        CompetitionResponseDTO result = competitionService.createCompetition(request);

        assertNotNull(result);
        assertEquals(TEST_ID, result.getId());
        assertEquals(TEST_NAME, result.getName());
        verify(competitionDomainService).createCompetition(
                TEST_NAME,
                TEST_SHORT_NAME,
                TEST_FILE_ID,
                TEST_SUMMARY,
                request.getLevel(),
                request.getMonth(),
                request.getOrganizer());
        verify(competitionConverter).convertToResponseDTO(any(CompetitionBriefVO.class));
    }

    // ==================== updateCompetition ====================

    @Test
    @DisplayName("更新竞赛：应成功更新并返回DTO")
    void updateCompetition_shouldUpdateAndReturnDTO() {
        CompetitionRequestDTO request = createTestRequest();
        CompetitionBriefVO updatedVO = createTestCompetitionBriefVO();
        CompetitionResponseDTO expectedDTO = CompetitionResponseDTO.builder()
                .id(TEST_ID)
                .name(TEST_NAME)
                .summary(TEST_SUMMARY)
                .build();

        when(competitionDomainService.existsById(TEST_ID)).thenReturn(true);
        when(competitionDomainService.getCompetitionList(50)).thenReturn(Collections.singletonList(updatedVO));
        when(competitionConverter.convertToResponseDTO(any(CompetitionBriefVO.class))).thenReturn(expectedDTO);

        CompetitionResponseDTO result = competitionService.updateCompetition(TEST_ID, request);

        assertNotNull(result);
        assertEquals(TEST_ID, result.getId());
        assertEquals(TEST_NAME, result.getName());
        verify(competitionDomainService).updateCompetition(
                eq(TEST_ID),
                eq(TEST_NAME),
                eq(TEST_SHORT_NAME),
                eq(TEST_FILE_ID),
                eq(TEST_SUMMARY),
                eq(request.getLevel()),
                eq(request.getMonth()),
                eq(request.getOrganizer()));
        verify(competitionConverter).convertToResponseDTO(any(CompetitionBriefVO.class));
    }

    @Test
    @DisplayName("更新竞赛：竞赛不存在应抛出异常")
    void updateCompetition_notFound_shouldThrowException() {
        CompetitionRequestDTO request = createTestRequest();

        when(competitionDomainService.existsById(TEST_ID)).thenReturn(false);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> competitionService.updateCompetition(TEST_ID, request));
        assertEquals("竞赛不存在", exception.getMessage());
    }

    @Test
    @DisplayName("更新竞赛：更新后查询为空应抛出异常")
    void updateCompetition_updateFailed_shouldThrowException() {
        CompetitionRequestDTO request = createTestRequest();

        when(competitionDomainService.existsById(TEST_ID)).thenReturn(true);
        when(competitionDomainService.getCompetitionList(50)).thenReturn(Collections.emptyList());

        GlobalException exception = assertThrows(
                GlobalException.class,
                () -> competitionService.updateCompetition(TEST_ID, request));
        assertEquals("更新竞赛失败", exception.getMessage());
    }

    // ==================== deleteCompetition ====================

    @Test
    @DisplayName("删除竞赛：应成功删除竞赛")
    void deleteCompetition_shouldDeleteCompetition() {
        when(competitionDomainService.existsById(TEST_ID)).thenReturn(true);

        competitionService.deleteCompetition(TEST_ID);

        verify(competitionDomainService).existsById(TEST_ID);
        verify(competitionDomainService).deleteCompetition(TEST_ID);
    }

    @Test
    @DisplayName("删除竞赛：竞赛不存在应抛出异常")
    void deleteCompetition_notFound_shouldThrowException() {
        when(competitionDomainService.existsById(TEST_ID)).thenReturn(false);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> competitionService.deleteCompetition(TEST_ID));
        assertEquals("竞赛不存在", exception.getMessage());
    }

    // ==================== updateSortOrder ====================

    @Test
    @DisplayName("更新排序：应成功更新排序权重")
    void updateSortOrder_shouldUpdateSuccessfully() {
        UpdateSortOrderRequestDTO request = UpdateSortOrderRequestDTO.builder().sortOrder(100).build();

        when(competitionDomainService.existsById(TEST_ID)).thenReturn(true);

        competitionService.updateSortOrder(TEST_ID, request);

        verify(competitionDomainService).updateSortOrder(TEST_ID, 100);
    }

    @Test
    @DisplayName("更新排序：竞赛不存在应抛出异常")
    void updateSortOrder_notFound_shouldThrowException() {
        UpdateSortOrderRequestDTO request = UpdateSortOrderRequestDTO.builder().sortOrder(100).build();

        when(competitionDomainService.existsById(TEST_ID)).thenReturn(false);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> competitionService.updateSortOrder(TEST_ID, request));
        assertEquals("竞赛不存在", exception.getMessage());
    }

    // ==================== updateLogo ====================

    @Test
    @DisplayName("更新Logo：应成功更新竞赛Logo")
    void updateLogo_shouldUpdateSuccessfully() {
        Long fileId = 200L;
        FileVO fileVO = FileVO.builder().id(fileId).type(FileType.NORMAL_IMG).build();

        when(competitionDomainService.existsById(TEST_ID)).thenReturn(true);
        when(fileDomainService.getFileById(fileId)).thenReturn(fileVO);

        competitionService.updateLogo(TEST_ID, fileId);

        verify(competitionDomainService).updateLogo(TEST_ID, fileId);
    }

    @Test
    @DisplayName("更新Logo：竞赛不存在应抛出DataNotFound")
    void updateLogo_competitionNotFound_shouldThrowDataNotFound() {
        Long fileId = 200L;

        when(competitionDomainService.existsById(TEST_ID)).thenReturn(false);

        DataNotFound exception = assertThrows(
                DataNotFound.class,
                () -> competitionService.updateLogo(TEST_ID, fileId));
        assertEquals("竞赛不存在", exception.getMessage());
    }

    @Test
    @DisplayName("更新Logo：文件不存在应抛出DataNotFound")
    void updateLogo_fileNotFound_shouldThrowDataNotFound() {
        Long fileId = 200L;

        when(competitionDomainService.existsById(TEST_ID)).thenReturn(true);
        when(fileDomainService.getFileById(fileId)).thenReturn(null);

        DataNotFound exception = assertThrows(
                DataNotFound.class,
                () -> competitionService.updateLogo(TEST_ID, fileId));
        assertEquals("文件不存在", exception.getMessage());
    }

    @Test
    @DisplayName("更新Logo：文件类型不匹配应抛出BadRequest")
    void updateLogo_fileTypeMismatch_shouldThrowBadRequest() {
        Long fileId = 200L;
        FileVO fileVO = FileVO.builder().id(fileId).type(FileType.AVATAR).build();

        when(competitionDomainService.existsById(TEST_ID)).thenReturn(true);
        when(fileDomainService.getFileById(fileId)).thenReturn(fileVO);

        BadRequest exception = assertThrows(
                BadRequest.class,
                () -> competitionService.updateLogo(TEST_ID, fileId));
        assertEquals("文件类型不匹配，期望 NORMAL_IMG", exception.getMessage());
    }

    // ==================== updateCover ====================

    @Test
    @DisplayName("更新封面：应成功更新竞赛封面")
    void updateCover_shouldUpdateSuccessfully() {
        Long fileId = 300L;
        FileVO fileVO = FileVO.builder().id(fileId).type(FileType.NORMAL_IMG).build();

        when(competitionDomainService.existsById(TEST_ID)).thenReturn(true);
        when(fileDomainService.getFileById(fileId)).thenReturn(fileVO);

        competitionService.updateCover(TEST_ID, fileId);

        verify(competitionDomainService).updateCover(TEST_ID, fileId);
    }

    @Test
    @DisplayName("更新封面：竞赛不存在应抛出DataNotFound")
    void updateCover_competitionNotFound_shouldThrowDataNotFound() {
        Long fileId = 300L;

        when(competitionDomainService.existsById(TEST_ID)).thenReturn(false);

        DataNotFound exception = assertThrows(
                DataNotFound.class,
                () -> competitionService.updateCover(TEST_ID, fileId));
        assertEquals("竞赛不存在", exception.getMessage());
    }

    @Test
    @DisplayName("更新封面：文件不存在应抛出DataNotFound")
    void updateCover_fileNotFound_shouldThrowDataNotFound() {
        Long fileId = 300L;

        when(competitionDomainService.existsById(TEST_ID)).thenReturn(true);
        when(fileDomainService.getFileById(fileId)).thenReturn(null);

        DataNotFound exception = assertThrows(
                DataNotFound.class,
                () -> competitionService.updateCover(TEST_ID, fileId));
        assertEquals("文件不存在", exception.getMessage());
    }

    @Test
    @DisplayName("更新封面：文件类型不匹配应抛出BadRequest")
    void updateCover_fileTypeMismatch_shouldThrowBadRequest() {
        Long fileId = 300L;
        FileVO fileVO = FileVO.builder().id(fileId).type(FileType.AVATAR).build();

        when(competitionDomainService.existsById(TEST_ID)).thenReturn(true);
        when(fileDomainService.getFileById(fileId)).thenReturn(fileVO);

        BadRequest exception = assertThrows(
                BadRequest.class,
                () -> competitionService.updateCover(TEST_ID, fileId));
        assertEquals("文件类型不匹配，期望 NORMAL_IMG", exception.getMessage());
    }
}
