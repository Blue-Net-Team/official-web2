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

import com.bluenet.web.application.CompetitionResult;
import com.bluenet.web.application.command.competition.CompetitionCommands;
import com.bluenet.web.domain.exception.DataNotFound;
import com.bluenet.web.domain.model.entity.Competition;
import com.bluenet.web.domain.model.enumerate.AwardLevel;
import com.bluenet.web.domain.model.vo.CompetitionVO;
import com.bluenet.web.domain.repository.CompetitionRepository;
import com.bluenet.web.domain.service.FileDomainService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

@DisplayName("CompetitionAppServiceImpl 单元测试")
@ExtendWith(MockitoExtension.class)
class CompetitionAppServiceImplTest {

    @Mock
    private CompetitionRepository competitionRepository;

    @Mock
    private FileDomainService fileDomainService;

    @InjectMocks
    private CompetitionAppServiceImpl competitionAppService;

    private static final Long TEST_ID = 1L;
    private static final String TEST_NAME = "蓝桥杯";
    private static final String TEST_SHORT_NAME = "蓝桥杯";
    private static final String TEST_SUMMARY = "全国软件和信息技术专业人才大赛";

    private CompetitionVO createTestCompetitionBriefVO() {
        return CompetitionVO.builder()
                .id(TEST_ID)
                .name(TEST_NAME)
                .shortName(TEST_SHORT_NAME)
                .summary(TEST_SUMMARY)
                .build();
    }

    private CompetitionCommands.CreateCompetitionCommand createTestCreateCommand() {
        return new CompetitionCommands.CreateCompetitionCommand(
                TEST_NAME, TEST_SHORT_NAME, null, null, TEST_SUMMARY,
                AwardLevel.NATIONAL, "4月", "工信部");
    }

    private CompetitionCommands.UpdateCompetitionCommand createTestUpdateCommand() {
        return new CompetitionCommands.UpdateCompetitionCommand(
                TEST_ID, TEST_NAME, TEST_SHORT_NAME, null, null, TEST_SUMMARY,
                AwardLevel.NATIONAL, "4月", "工信部");
    }

    // ==================== getCompetitionResponseList ====================

    @Test
    @DisplayName("获取竞赛响应列表：应返回VO列表")
    void getCompetitionResponseList_shouldReturnVOList() {
        int limit = 10;
        List<CompetitionVO> voList = new ArrayList<>();
        voList.add(createTestCompetitionBriefVO());

        when(competitionRepository.findCompetitionsWithLimit(limit)).thenReturn(voList);

        List<CompetitionVO> result = competitionAppService.getCompetitionResponseList(limit);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(TEST_ID, result.get(0).getId());
        verify(competitionRepository).findCompetitionsWithLimit(limit);
    }

    @Test
    @DisplayName("获取竞赛响应列表：limit小于1时应使用1")
    void getCompetitionResponseList_limitLessThanOne_shouldUseOne() {
        int limit = 0;
        List<CompetitionVO> voList = new ArrayList<>();

        when(competitionRepository.findCompetitionsWithLimit(1)).thenReturn(voList);

        List<CompetitionVO> result = competitionAppService.getCompetitionResponseList(limit);

        assertNotNull(result);
        verify(competitionRepository).findCompetitionsWithLimit(1);
    }

    @Test
    @DisplayName("获取竞赛响应列表：空列表时应返回空列表")
    void getCompetitionResponseList_emptyList_shouldReturnEmptyList() {
        int limit = 10;
        List<CompetitionVO> voList = new ArrayList<>();

        when(competitionRepository.findCompetitionsWithLimit(limit)).thenReturn(voList);

        List<CompetitionVO> result = competitionAppService.getCompetitionResponseList(limit);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // ==================== createCompetition ====================

    @Test
    @DisplayName("创建竞赛：应成功创建并返回结果")
    void createCompetition_shouldCreateAndReturnResult() {
        CompetitionCommands.CreateCompetitionCommand command = createTestCreateCommand();

        when(competitionRepository.findMaxSortOrder()).thenReturn(0);

        CompetitionResult result = competitionAppService.createCompetition(command);

        assertNotNull(result);
        assertEquals(TEST_NAME, result.name());
        assertEquals(TEST_SHORT_NAME, result.shortName());
        verify(competitionRepository).save(any(Competition.class));
    }

    // ==================== updateCompetition ====================

    @Test
    @DisplayName("更新竞赛：应成功更新并返回结果")
    void updateCompetition_shouldUpdateAndReturnResult() {
        CompetitionCommands.UpdateCompetitionCommand command = createTestUpdateCommand();

        Competition existing = Competition.reconstruct(
                TEST_ID,
                "旧名称",
                "旧简称",
                null,
                null,
                "旧简介",
                AwardLevel.PROVINCIAL,
                "3月",
                "旧主办方",
                1);

        when(competitionRepository.findById(TEST_ID)).thenReturn(Optional.of(existing));

        CompetitionResult result = competitionAppService.updateCompetition(command);

        assertNotNull(result);
        assertEquals(TEST_NAME, result.name());
        verify(competitionRepository).update(any(Competition.class));
    }

    @Test
    @DisplayName("更新竞赛：竞赛不存在应抛出异常")
    void updateCompetition_notFound_shouldThrowException() {
        CompetitionCommands.UpdateCompetitionCommand command = createTestUpdateCommand();

        when(competitionRepository.findById(TEST_ID)).thenReturn(Optional.empty());

        DataNotFound exception = assertThrows(
                DataNotFound.class,
                () -> competitionAppService.updateCompetition(command));
        assertEquals("竞赛不存在", exception.getMessage());
    }

    // ==================== deleteCompetition ====================

    @Test
    @DisplayName("删除竞赛：应成功删除竞赛")
    void deleteCompetition_shouldDeleteCompetition() {
        Competition existing = Competition.reconstruct(
                TEST_ID,
                TEST_NAME,
                TEST_SHORT_NAME,
                null,
                null,
                TEST_SUMMARY,
                AwardLevel.NATIONAL,
                "4月",
                "工信部",
                1);

        when(competitionRepository.findById(TEST_ID)).thenReturn(Optional.of(existing));

        competitionAppService.deleteCompetition(TEST_ID);

        verify(competitionRepository).deleteById(TEST_ID);
    }

    @Test
    @DisplayName("删除竞赛：竞赛不存在应抛出异常")
    void deleteCompetition_notFound_shouldThrowException() {
        when(competitionRepository.findById(TEST_ID)).thenReturn(Optional.empty());

        DataNotFound exception = assertThrows(
                DataNotFound.class,
                () -> competitionAppService.deleteCompetition(TEST_ID));
        assertEquals("竞赛不存在", exception.getMessage());
    }

    // ==================== getCompetitionPage ====================

    @Test
    @DisplayName("分页查询：默认参数时应返回分页数据")
    void getCompetitionPage_defaultParams_shouldReturnPagedData() {
        List<CompetitionVO> voList = new ArrayList<>();
        voList.add(createTestCompetitionBriefVO());
        Page<CompetitionVO> voPage = new PageImpl<>(voList, PageRequest.of(0, 10), 1);

        when(competitionRepository.findCompetitionsPage(PageRequest.of(0, 10))).thenReturn(voPage);

        Page<CompetitionVO> result = competitionAppService.getCompetitionPage(null, null);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(1, result.getContent().size());
        verify(competitionRepository).findCompetitionsPage(PageRequest.of(0, 10));
    }

    // ==================== moveCompetition ====================

    @Test
    @DisplayName("移动竞赛：应成功交换排序号")
    void moveCompetition_shouldSwapSortOrders() {
        Competition competition = Competition.reconstruct(
                1L,
                "竞赛A",
                "A",
                null,
                null,
                "简介",
                AwardLevel.NATIONAL,
                "4月",
                "工信部",
                10);
        Competition adjacent = Competition.reconstruct(
                2L,
                "竞赛B",
                "B",
                null,
                null,
                "简介",
                AwardLevel.NATIONAL,
                "4月",
                "工信部",
                5);

        when(competitionRepository.findById(1L)).thenReturn(Optional.of(competition));
        when(competitionRepository.findAdjacent(10, "DOWN")).thenReturn(Optional.of(adjacent));

        competitionAppService.moveCompetition(new CompetitionCommands.MoveCompetitionCommand(1L, "DOWN"));

        assertEquals(5, competition.getSortOrder());
        assertEquals(10, adjacent.getSortOrder());
        verify(competitionRepository, times(2)).update(any(Competition.class));
    }

    @Test
    @DisplayName("移动竞赛：方向无效应抛出异常")
    void moveCompetition_invalidDirection_shouldThrowException() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> competitionAppService
                        .moveCompetition(new CompetitionCommands.MoveCompetitionCommand(1L, "LEFT")));
        assertEquals("移动方向必须是 UP 或 DOWN", exception.getMessage());
    }
}
