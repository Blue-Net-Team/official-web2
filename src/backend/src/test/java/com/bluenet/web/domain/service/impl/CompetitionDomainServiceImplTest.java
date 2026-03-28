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

import com.bluenet.web.domain.model.vo.CompetitionBriefVO;
import com.bluenet.web.domain.model.vo.CompetitionVO;
import com.bluenet.web.domain.repository.CompetitionRepository;

/**
 * CompetitionDomainServiceImpl单元测试
 */
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
    private static final String TEST_LOGO_URL = "http://example.com/logo.png";
    private static final String TEST_SUMMARY = "全国软件和信息技术专业人才大赛";
    private static final String TEST_DETAIL = "蓝桥杯全国软件和信息技术专业人才大赛是由工业和信息化部人才交流中心举办的全国性IT学科赛事。";

    private CompetitionBriefVO createTestCompetitionBriefVO() {
        return CompetitionBriefVO.builder()
                .id(TEST_ID)
                .name(TEST_NAME)
                .shortName(TEST_SHORT_NAME)
                .logoUrl(TEST_LOGO_URL)
                .summary(TEST_SUMMARY)
                .build();
    }

    private CompetitionVO createTestCompetitionVO() {
        return CompetitionVO.builder()
                .id(TEST_ID)
                .name(TEST_NAME)
                .shortName(TEST_SHORT_NAME)
                .logoUrl(TEST_LOGO_URL)
                .summary(TEST_SUMMARY)
                .detail(TEST_DETAIL)
                .sortOrder(0)
                .enabled(true)
                .build();
    }

    // ==================== getCompetitionList ====================

    /**
     * 获取竞赛列表：应返回所有启用的竞赛
     */
    @Test
    @DisplayName("获取竞赛列表：应返回所有启用的竞赛")
    void getCompetitionList_shouldReturnEnabledCompetitions() {
        // 准备
        int limit = 10;
        List<CompetitionBriefVO> expectedList = new ArrayList<>();
        expectedList.add(createTestCompetitionBriefVO());

        when(competitionRepository.findEnabledCompetitionsWithLimit(limit)).thenReturn(expectedList);

        // 执行
        List<CompetitionBriefVO> result = competitionDomainService.getCompetitionList(limit);

        // 验证
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(TEST_ID, result.get(0).getId());
        assertEquals(TEST_NAME, result.get(0).getName());
        verify(competitionRepository).findEnabledCompetitionsWithLimit(limit);
    }

    /**
     * 获取竞赛列表：无竞赛时应返回空列表
     */
    @Test
    @DisplayName("获取竞赛列表：无竞赛时应返回空列表")
    void getCompetitionList_noCompetitions_shouldReturnEmptyList() {
        // 准备
        int limit = 10;
        List<CompetitionBriefVO> expectedList = new ArrayList<>();

        when(competitionRepository.findEnabledCompetitionsWithLimit(limit)).thenReturn(expectedList);

        // 执行
        List<CompetitionBriefVO> result = competitionDomainService.getCompetitionList(limit);

        // 验证
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(competitionRepository).findEnabledCompetitionsWithLimit(limit);
    }

    /**
     * 获取竞赛列表：限制为1时应返回1条记录
     */
    @Test
    @DisplayName("获取竞赛列表：限制为1时应返回1条记录")
    void getCompetitionList_withLimitOne_shouldReturnOneRecord() {
        // 准备
        int limit = 1;
        List<CompetitionBriefVO> expectedList = new ArrayList<>();
        expectedList.add(createTestCompetitionBriefVO());

        when(competitionRepository.findEnabledCompetitionsWithLimit(limit)).thenReturn(expectedList);

        // 执行
        List<CompetitionBriefVO> result = competitionDomainService.getCompetitionList(limit);

        // 验证
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(competitionRepository).findEnabledCompetitionsWithLimit(limit);
    }

    // ==================== getCompetitionById ====================

    /**
     * 获取竞赛详情：应返回竞赛详情
     */
    @Test
    @DisplayName("获取竞赛详情：应返回竞赛详情")
    void getCompetitionById_shouldReturnCompetition() {
        // 准备
        CompetitionVO expectedVO = createTestCompetitionVO();

        when(competitionRepository.findCompetitionById(TEST_ID)).thenReturn(Optional.of(expectedVO));

        // 执行
        Optional<CompetitionVO> result = competitionDomainService.getCompetitionById(TEST_ID);

        // 验证
        assertTrue(result.isPresent());
        assertEquals(TEST_ID, result.get().getId());
        assertEquals(TEST_NAME, result.get().getName());
        assertEquals(TEST_DETAIL, result.get().getDetail());
        verify(competitionRepository).findCompetitionById(TEST_ID);
    }

    /**
     * 获取竞赛详情：竞赛不存在时应返回空Optional
     */
    @Test
    @DisplayName("获取竞赛详情：竞赛不存在时应返回空Optional")
    void getCompetitionById_notFound_shouldReturnEmpty() {
        // 准备
        when(competitionRepository.findCompetitionById(TEST_ID)).thenReturn(Optional.empty());

        // 执行
        Optional<CompetitionVO> result = competitionDomainService.getCompetitionById(TEST_ID);

        // 验证
        assertTrue(result.isEmpty());
        verify(competitionRepository).findCompetitionById(TEST_ID);
    }

    // ==================== createCompetition ====================

    /**
     * 创建竞赛：应成功创建并返回ID
     */
    @Test
    @DisplayName("创建竞赛：应成功创建并返回ID")
    void createCompetition_shouldCreateAndReturnId() {
        // 准备
        Long logoFileId = 100L;
        Long expectedId = 1L;

        when(competitionRepository.save(any())).thenReturn(expectedId);

        // 执行
        Long result = competitionDomainService.createCompetition(
                TEST_NAME,
                TEST_SHORT_NAME,
                logoFileId,
                TEST_SUMMARY,
                TEST_DETAIL);

        // 验证
        assertEquals(expectedId, result);
        verify(competitionRepository).save(
                argThat(
                        competition -> competition.getName().equals(TEST_NAME)
                                && competition.getShortName().equals(TEST_SHORT_NAME)
                                && competition.getLogoFileId().equals(logoFileId)
                                && competition.getSummary().equals(TEST_SUMMARY)
                                && competition.getDetail().equals(TEST_DETAIL)
                                && competition.getSortOrder().equals(0) && competition.getEnabled().equals(true)));
    }

    /**
     * 创建竞赛：logoFileId为null时应成功创建
     */
    @Test
    @DisplayName("创建竞赛：logoFileId为null时应成功创建")
    void createCompetition_withNullLogoFileId_shouldCreateSuccessfully() {
        // 准备
        Long expectedId = 1L;

        when(competitionRepository.save(any())).thenReturn(expectedId);

        // 执行
        Long result = competitionDomainService.createCompetition(
                TEST_NAME,
                TEST_SHORT_NAME,
                null,
                TEST_SUMMARY,
                TEST_DETAIL);

        // 验证
        assertEquals(expectedId, result);
        verify(competitionRepository).save(argThat(competition -> competition.getLogoFileId() == null));
    }

    // ==================== updateCompetition ====================

    /**
     * 更新竞赛：应成功更新竞赛信息
     */
    @Test
    @DisplayName("更新竞赛：应成功更新竞赛信息")
    void updateCompetition_shouldUpdateSuccessfully() {
        // 准备
        Long logoFileId = 100L;
        Boolean enabled = true;

        // 执行
        competitionDomainService.updateCompetition(
                TEST_ID,
                TEST_NAME,
                TEST_SHORT_NAME,
                logoFileId,
                TEST_SUMMARY,
                TEST_DETAIL,
                enabled);

        // 验证
        verify(competitionRepository).update(
                argThat(
                        competition -> competition.getId().equals(TEST_ID)
                                && competition.getName().equals(TEST_NAME)
                                && competition.getShortName().equals(TEST_SHORT_NAME)
                                && competition.getLogoFileId().equals(logoFileId)
                                && competition.getSummary().equals(TEST_SUMMARY)
                                && competition.getDetail().equals(TEST_DETAIL)
                                && competition.getEnabled().equals(enabled)));
    }

    /**
     * 更新竞赛：enabled为false时应成功更新
     */
    @Test
    @DisplayName("更新竞赛：enabled为false时应成功更新")
    void updateCompetition_disableCompetition_shouldUpdateSuccessfully() {
        // 准备
        Boolean enabled = false;

        // 执行
        competitionDomainService.updateCompetition(
                TEST_ID,
                TEST_NAME,
                TEST_SHORT_NAME,
                null,
                TEST_SUMMARY,
                TEST_DETAIL,
                enabled);

        // 验证
        verify(competitionRepository).update(argThat(competition -> competition.getEnabled().equals(false)));
    }

    // ==================== deleteCompetition ====================

    /**
     * 删除竞赛：应成功删除竞赛
     */
    @Test
    @DisplayName("删除竞赛：应成功删除竞赛")
    void deleteCompetition_shouldDeleteSuccessfully() {
        // 执行
        competitionDomainService.deleteCompetition(TEST_ID);

        // 验证
        verify(competitionRepository).deleteById(TEST_ID);
    }

    // ==================== updateSortOrder ====================

    /**
     * 更新排序：应成功更新排序权重
     */
    @Test
    @DisplayName("更新排序：应成功更新排序权重")
    void updateSortOrder_shouldUpdateSuccessfully() {
        // 准备
        Integer newSortOrder = 100;

        // 执行
        competitionDomainService.updateSortOrder(TEST_ID, newSortOrder);

        // 验证
        verify(competitionRepository).update(
                argThat(
                        competition -> competition.getId().equals(TEST_ID)
                                && competition.getSortOrder().equals(newSortOrder)));
    }

    /**
     * 更新排序：排序权重为0时应成功更新
     */
    @Test
    @DisplayName("更新排序：排序权重为0时应成功更新")
    void updateSortOrder_withZeroSortOrder_shouldUpdateSuccessfully() {
        // 准备
        Integer newSortOrder = 0;

        // 执行
        competitionDomainService.updateSortOrder(TEST_ID, newSortOrder);

        // 验证
        verify(competitionRepository).update(argThat(competition -> competition.getSortOrder().equals(0)));
    }

    // ==================== existsById ====================

    /**
     * 检查存在：竞赛存在时应返回true
     */
    @Test
    @DisplayName("检查存在：竞赛存在时应返回true")
    void existsById_existingCompetition_shouldReturnTrue() {
        // 准备
        when(competitionRepository.existsById(TEST_ID)).thenReturn(true);

        // 执行
        boolean result = competitionDomainService.existsById(TEST_ID);

        // 验证
        assertTrue(result);
        verify(competitionRepository).existsById(TEST_ID);
    }

    /**
     * 检查存在：竞赛不存在时应返回false
     */
    @Test
    @DisplayName("检查存在：竞赛不存在时应返回false")
    void existsById_nonExistingCompetition_shouldReturnFalse() {
        // 准备
        when(competitionRepository.existsById(TEST_ID)).thenReturn(false);

        // 执行
        boolean result = competitionDomainService.existsById(TEST_ID);

        // 验证
        assertFalse(result);
        verify(competitionRepository).existsById(TEST_ID);
    }

    // ==================== updateLogo ====================

    /**
     * 更新Logo：应成功更新竞赛Logo
     */
    @Test
    @DisplayName("更新Logo：应成功更新竞赛Logo")
    void updateLogo_shouldUpdateSuccessfully() {
        // 准备
        Long newLogoFileId = 200L;

        // 执行
        competitionDomainService.updateLogo(TEST_ID, newLogoFileId);

        // 验证
        verify(competitionRepository).update(
                argThat(
                        competition -> competition.getId().equals(TEST_ID)
                                && competition.getLogoFileId().equals(newLogoFileId)));
    }

    /**
     * 更新Logo：Logo文件ID为null时应成功更新
     */
    @Test
    @DisplayName("更新Logo：Logo文件ID为null时应成功更新")
    void updateLogo_withNullLogoFileId_shouldUpdateSuccessfully() {
        // 执行
        competitionDomainService.updateLogo(TEST_ID, null);

        // 验证
        verify(competitionRepository).update(
                argThat(
                        competition -> competition.getId().equals(TEST_ID)
                                && competition.getLogoFileId() == null));
    }

    /**
     * 更新Logo：更新不同竞赛的Logo应调用正确的ID
     */
    @Test
    @DisplayName("更新Logo：更新不同竞赛的Logo应调用正确的ID")
    void updateLogo_differentCompetition_shouldUpdateCorrectId() {
        // 准备
        Long differentCompetitionId = 999L;
        Long logoFileId = 300L;

        // 执行
        competitionDomainService.updateLogo(differentCompetitionId, logoFileId);

        // 验证
        verify(competitionRepository).update(
                argThat(
                        competition -> competition.getId().equals(differentCompetitionId)
                                && competition.getLogoFileId().equals(logoFileId)));
    }
}
