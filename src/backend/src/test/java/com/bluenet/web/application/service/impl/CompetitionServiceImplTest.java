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

import com.bluenet.web.api.dto.PageDTO;
import com.bluenet.web.api.dto.competition.*;
import com.bluenet.web.application.converter.CompetitionConverter;
import com.bluenet.web.domain.exception.GlobalException;
import com.bluenet.web.domain.model.enumerate.AwardLevel;
import com.bluenet.web.domain.model.vo.CompetitionVO;
import com.bluenet.web.domain.service.CompetitionDomainService;
import com.bluenet.web.domain.service.FileDomainService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

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

    private CompetitionVO createTestCompetitionBriefVO() {
        return CompetitionVO.builder()
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
                .logoFileId(null)
                .coverFileId(null)
                .summary(TEST_SUMMARY)
                .level("national")
                .month("4月")
                .organizer("工信部")
                .build();
    }

    private CompetitionRequestDTO createTestRequestWithFileIds() {
        return CompetitionRequestDTO.builder()
                .name(TEST_NAME)
                .shortName(TEST_SHORT_NAME)
                .logoFileId(TEST_LOGO_FILE_ID)
                .coverFileId(TEST_COVER_FILE_ID)
                .summary(TEST_SUMMARY)
                .level("national")
                .month("4月")
                .organizer("工信部")
                .build();
    }

    // ==================== getCompetitionResponseList ====================

    @Test
    @DisplayName("获取竞赛响应列表：应返回响应DTO列表")
    void getCompetitionResponseList_shouldReturnResponseDTOs() {
        int limit = 10;
        List<CompetitionVO> voList = new ArrayList<>();
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
        List<CompetitionVO> voList = new ArrayList<>();
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
        List<CompetitionVO> voList = new ArrayList<>();
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
        List<CompetitionVO> voList = new ArrayList<>();
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
        List<CompetitionVO> voList = new ArrayList<>();
        voList.add(CompetitionVO.builder().id(1L).name("竞赛1").build());
        voList.add(CompetitionVO.builder().id(2L).name("竞赛2").build());
        voList.add(CompetitionVO.builder().id(3L).name("竞赛3").build());

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
                        any(),
                        anyString(),
                        any(),
                        anyString(),
                        anyString()))
                                .thenReturn(TEST_ID);
        when(competitionConverter.convertToResponseDTO(any(CompetitionVO.class))).thenReturn(expectedDTO);

        CompetitionResponseDTO result = competitionService.createCompetition(request);

        assertNotNull(result);
        assertEquals(TEST_ID, result.getId());
        assertEquals(TEST_NAME, result.getName());
        verify(competitionDomainService).createCompetition(
                TEST_NAME,
                TEST_SHORT_NAME,
                null,
                null,
                TEST_SUMMARY,
                AwardLevel.NATIONAL,
                request.getMonth(),
                request.getOrganizer());
        verify(competitionConverter).convertToResponseDTO(any(CompetitionVO.class));
    }

    // ==================== updateCompetition ====================

    @Test
    @DisplayName("更新竞赛：应成功更新并返回DTO")
    void updateCompetition_shouldUpdateAndReturnDTO() {
        CompetitionRequestDTO request = createTestRequest();
        CompetitionVO updatedVO = createTestCompetitionBriefVO();
        CompetitionResponseDTO expectedDTO = CompetitionResponseDTO.builder()
                .id(TEST_ID)
                .name(TEST_NAME)
                .summary(TEST_SUMMARY)
                .build();

        when(competitionDomainService.existsById(TEST_ID)).thenReturn(true);
        when(competitionDomainService.getCompetitionList(50)).thenReturn(Collections.singletonList(updatedVO));
        when(competitionConverter.convertToResponseDTO(any(CompetitionVO.class))).thenReturn(expectedDTO);

        CompetitionResponseDTO result = competitionService.updateCompetition(TEST_ID, request);

        assertNotNull(result);
        assertEquals(TEST_ID, result.getId());
        assertEquals(TEST_NAME, result.getName());
        verify(competitionDomainService).updateCompetition(
                eq(TEST_ID),
                eq(TEST_NAME),
                eq(TEST_SHORT_NAME),
                eq(null),
                eq(null),
                eq(TEST_SUMMARY),
                eq(AwardLevel.NATIONAL),
                eq(request.getMonth()),
                eq(request.getOrganizer()));
        verify(competitionConverter).convertToResponseDTO(any(CompetitionVO.class));
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

    // ==================== getCompetitionPage ====================

    @Test
    @DisplayName("分页查询：默认参数时应返回分页数据")
    void getCompetitionPage_defaultParams_shouldReturnPagedData() {
        List<CompetitionVO> voList = new ArrayList<>();
        voList.add(createTestCompetitionBriefVO());
        Page<CompetitionVO> voPage = new PageImpl<>(voList, PageRequest.of(0, 10), 1);

        List<CompetitionResponseDTO> dtoList = new ArrayList<>();
        dtoList.add(
                CompetitionResponseDTO.builder().id(TEST_ID).name(TEST_NAME).summary(TEST_SUMMARY).build());
        Page<CompetitionResponseDTO> dtoPage = new PageImpl<>(dtoList, PageRequest.of(0, 10), 1);

        when(competitionDomainService.getCompetitionPage(PageRequest.of(0, 10))).thenReturn(voPage);
        when(competitionConverter.convertToDTOPage(voPage)).thenReturn(dtoPage);

        PageDTO<CompetitionResponseDTO> result = competitionService.getCompetitionPage(null, null);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(1, result.getContent().size());
        verify(competitionDomainService).getCompetitionPage(PageRequest.of(0, 10));
    }

    @Test
    @DisplayName("分页查询：自定义参数应正确透传")
    void getCompetitionPage_customParams_shouldPassCorrectly() {
        Page<CompetitionVO> voPage = new PageImpl<>(new ArrayList<>(), PageRequest.of(1, 5), 10);
        Page<CompetitionResponseDTO> dtoPage = new PageImpl<>(new ArrayList<>(), PageRequest.of(1, 5), 10);

        when(competitionDomainService.getCompetitionPage(PageRequest.of(1, 5))).thenReturn(voPage);
        when(competitionConverter.convertToDTOPage(voPage)).thenReturn(dtoPage);

        PageDTO<CompetitionResponseDTO> result = competitionService.getCompetitionPage(1, 5);

        assertNotNull(result);
        assertEquals(10, result.getTotalElements());
        verify(competitionDomainService).getCompetitionPage(PageRequest.of(1, 5));
    }

    @Test
    @DisplayName("分页查询：size超过50应clamp为50")
    void getCompetitionPage_sizeOverMax_shouldClampTo50() {
        Page<CompetitionVO> voPage = new PageImpl<>(new ArrayList<>(), PageRequest.of(0, 50), 0);
        Page<CompetitionResponseDTO> dtoPage = new PageImpl<>(new ArrayList<>(), PageRequest.of(0, 50), 0);

        when(competitionDomainService.getCompetitionPage(PageRequest.of(0, 50))).thenReturn(voPage);
        when(competitionConverter.convertToDTOPage(voPage)).thenReturn(dtoPage);

        competitionService.getCompetitionPage(0, 100);

        verify(competitionDomainService).getCompetitionPage(PageRequest.of(0, 50));
    }

    @Test
    @DisplayName("分页查询：空数据应返回空PageDTO")
    void getCompetitionPage_emptyData_shouldReturnEmptyPageDTO() {
        Page<CompetitionVO> voPage = new PageImpl<>(new ArrayList<>(), PageRequest.of(0, 10), 0);
        Page<CompetitionResponseDTO> dtoPage = new PageImpl<>(new ArrayList<>(), PageRequest.of(0, 10), 0);

        when(competitionDomainService.getCompetitionPage(PageRequest.of(0, 10))).thenReturn(voPage);
        when(competitionConverter.convertToDTOPage(voPage)).thenReturn(dtoPage);

        PageDTO<CompetitionResponseDTO> result = competitionService.getCompetitionPage(0, 10);

        assertNotNull(result);
        assertTrue(result.getContent().isEmpty());
        assertEquals(0, result.getTotalElements());
    }

}
