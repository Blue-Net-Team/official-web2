package com.bluenet.web.domain.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.bluenet.web.domain.model.vo.CompetitionBriefVO;
import com.bluenet.web.domain.repository.CompetitionRepository;

@DisplayName("CompetitionDomainServiceImpl 单元测试")
@ExtendWith(MockitoExtension.class)
class CompetitionDomainServiceImplTest {

    @Mock
    private CompetitionRepository competitionRepository;

    @InjectMocks
    private CompetitionDomainServiceImpl competitionDomainService;

    private static final Long TEST_ID = 1L;
    private static final String TEST_NAME = "蓝桥杯";
    private static final String TEST_SHORT_NAME = "蓝桥杯";
    private static final String TEST_SUMMARY = "全国软件和信息技术专业人才大赛";

    private CompetitionBriefVO createTestCompetitionBriefVO() {
        return CompetitionBriefVO.builder()
                .id(TEST_ID)
                .name(TEST_NAME)
                .shortName(TEST_SHORT_NAME)
                .summary(TEST_SUMMARY)
                .build();
    }

    @Test
    @DisplayName("获取竞赛列表：应返回所有启用的竞赛")
    void getCompetitionList_shouldReturnEnabledCompetitions() {
        int limit = 10;
        List<CompetitionBriefVO> expectedList = new ArrayList<>();
        expectedList.add(createTestCompetitionBriefVO());

        when(competitionRepository.findEnabledCompetitionsWithLimit(limit)).thenReturn(expectedList);

        List<CompetitionBriefVO> result = competitionDomainService.getCompetitionList(limit);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(TEST_ID, result.get(0).getId());
        assertEquals(TEST_NAME, result.get(0).getName());
        verify(competitionRepository).findEnabledCompetitionsWithLimit(limit);
    }

    @Test
    @DisplayName("获取竞赛列表：无竞赛时应返回空列表")
    void getCompetitionList_noCompetitions_shouldReturnEmptyList() {
        int limit = 10;
        List<CompetitionBriefVO> expectedList = new ArrayList<>();

        when(competitionRepository.findEnabledCompetitionsWithLimit(limit)).thenReturn(expectedList);

        List<CompetitionBriefVO> result = competitionDomainService.getCompetitionList(limit);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(competitionRepository).findEnabledCompetitionsWithLimit(limit);
    }

    @Test
    @DisplayName("获取竞赛列表：限制为1时应返回1条记录")
    void getCompetitionList_withLimitOne_shouldReturnOneRecord() {
        int limit = 1;
        List<CompetitionBriefVO> expectedList = new ArrayList<>();
        expectedList.add(createTestCompetitionBriefVO());

        when(competitionRepository.findEnabledCompetitionsWithLimit(limit)).thenReturn(expectedList);

        List<CompetitionBriefVO> result = competitionDomainService.getCompetitionList(limit);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(competitionRepository).findEnabledCompetitionsWithLimit(limit);
    }

    @Test
    @DisplayName("创建竞赛：应成功创建并返回ID")
    void createCompetition_shouldCreateAndReturnId() {
        Long logoFileId = 100L;
        Long coverFileId = 200L;
        Long expectedId = 1L;
        String level = "国家级";
        String month = "4月";
        String organizer = "工信部";

        when(competitionRepository.save(any())).thenReturn(expectedId);

        Long result = competitionDomainService.createCompetition(
                TEST_NAME,
                TEST_SHORT_NAME,
                logoFileId,
                coverFileId,
                TEST_SUMMARY,
                level,
                month,
                organizer);

        assertEquals(expectedId, result);
        verify(competitionRepository).save(
                argThat(
                        competition -> competition.getName().equals(TEST_NAME)
                                && competition.getShortName().equals(TEST_SHORT_NAME)
                                && competition.getLogoFileId().equals(logoFileId)
                                && competition.getCoverFileId().equals(coverFileId)
                                && competition.getSummary().equals(TEST_SUMMARY)
                                && competition.getLevel().equals(level)
                                && competition.getMonth().equals(month)
                                && competition.getOrganizer().equals(organizer)
                                && competition.getSortOrder().equals(0)));
    }

    @Test
    @DisplayName("创建竞赛：logoFileId为null时应成功创建")
    void createCompetition_withNullLogoFileId_shouldCreateSuccessfully() {
        Long expectedId = 1L;
        String level = "国家级";
        String month = "4月";
        String organizer = "工信部";

        when(competitionRepository.save(any())).thenReturn(expectedId);

        Long result = competitionDomainService.createCompetition(
                TEST_NAME,
                TEST_SHORT_NAME,
                null,
                null,
                TEST_SUMMARY,
                level,
                month,
                organizer);

        assertEquals(expectedId, result);
        verify(competitionRepository).save(argThat(competition -> competition.getLogoFileId() == null));
    }

    @Test
    @DisplayName("更新竞赛：应成功更新竞赛信息")
    void updateCompetition_shouldUpdateSuccessfully() {
        Long logoFileId = 100L;
        Long coverFileId = 200L;
        String level = "国家级";
        String month = "4月";
        String organizer = "工信部";

        competitionDomainService.updateCompetition(
                TEST_ID,
                TEST_NAME,
                TEST_SHORT_NAME,
                logoFileId,
                coverFileId,
                TEST_SUMMARY,
                level,
                month,
                organizer);

        verify(competitionRepository).update(
                argThat(
                        competition -> competition.getId().equals(TEST_ID)
                                && competition.getName().equals(TEST_NAME)
                                && competition.getShortName().equals(TEST_SHORT_NAME)
                                && competition.getLogoFileId().equals(logoFileId)
                                && competition.getCoverFileId().equals(coverFileId)
                                && competition.getSummary().equals(TEST_SUMMARY)
                                && competition.getLevel().equals(level)
                                && competition.getMonth().equals(month)
                                && competition.getOrganizer().equals(organizer)));
    }

    @Test
    @DisplayName("删除竞赛：应成功删除竞赛")
    void deleteCompetition_shouldDeleteSuccessfully() {
        competitionDomainService.deleteCompetition(TEST_ID);

        verify(competitionRepository).deleteById(TEST_ID);
    }

    @Test
    @DisplayName("更新排序：应成功更新排序权重")
    void updateSortOrder_shouldUpdateSuccessfully() {
        Integer newSortOrder = 100;

        competitionDomainService.updateSortOrder(TEST_ID, newSortOrder);

        verify(competitionRepository).update(
                argThat(
                        competition -> competition.getId().equals(TEST_ID)
                                && competition.getSortOrder().equals(newSortOrder)));
    }

    @Test
    @DisplayName("更新排序：排序权重为0时应成功更新")
    void updateSortOrder_withZeroSortOrder_shouldUpdateSuccessfully() {
        Integer newSortOrder = 0;

        competitionDomainService.updateSortOrder(TEST_ID, newSortOrder);

        verify(competitionRepository).update(argThat(competition -> competition.getSortOrder().equals(0)));
    }

    @Test
    @DisplayName("检查存在：竞赛存在时应返回true")
    void existsById_existingCompetition_shouldReturnTrue() {
        when(competitionRepository.existsById(TEST_ID)).thenReturn(true);

        boolean result = competitionDomainService.existsById(TEST_ID);

        assertTrue(result);
        verify(competitionRepository).existsById(TEST_ID);
    }

    @Test
    @DisplayName("检查存在：竞赛不存在时应返回false")
    void existsById_nonExistingCompetition_shouldReturnFalse() {
        when(competitionRepository.existsById(TEST_ID)).thenReturn(false);

        boolean result = competitionDomainService.existsById(TEST_ID);

        assertFalse(result);
        verify(competitionRepository).existsById(TEST_ID);
    }

}
