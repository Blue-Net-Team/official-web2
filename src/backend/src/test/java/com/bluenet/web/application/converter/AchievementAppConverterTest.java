package com.bluenet.web.application.converter;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.bluenet.web.api.dto.achievement.AchievementDTO;
import com.bluenet.web.api.dto.achievement.AchievementStatsDTO;
import com.bluenet.web.application.AchievementResult;
import com.bluenet.web.domain.model.enumerate.AchievementType;
import com.bluenet.web.domain.model.enumerate.AwardLevel;
import com.bluenet.web.domain.model.vo.AchievementStatsVO;

@DisplayName("AchievementAppConverter 单元测试")
class AchievementAppConverterTest {

    private final AchievementAppConverter converter = new AchievementAppConverter();

    private static final Long TEST_ID = 1L;
    private static final String TEST_TITLE = "蓝桥杯全国一等奖";
    private static final String TEST_RELATE_TO = "蓝桥杯";
    private static final LocalDate TEST_ACHIEVE_AT = LocalDate.of(2024, 4, 15);
    private static final Long TEST_FILE_ID = 123L;
    private static final String TEST_FILE_URL = "http://example.com/image.jpg";
    private static final String TEST_COMPETITION_NAME = "蓝桥杯全国软件和信息技术专业人才大赛";
    private static final String TEST_COMPETITION_SHORT_NAME = "蓝桥杯";
    private static final Long TEST_COMPETITION_LOGO_FILE_ID = 456L;

    private AchievementResult createTestAchievementResult() {
        return new AchievementResult(
                TEST_ID,
                TEST_TITLE,
                AchievementType.COMPETITION,
                TEST_RELATE_TO,
                TEST_ACHIEVE_AT,
                AwardLevel.NATIONAL,
                "国家级",
                "一等奖",
                TEST_COMPETITION_NAME,
                TEST_COMPETITION_SHORT_NAME,
                TEST_COMPETITION_LOGO_FILE_ID,
                TEST_FILE_ID,
                TEST_FILE_URL);
    }

    private AchievementResult createTestPaperAchievementResult() {
        return new AchievementResult(
                2L,
                "基于深度学习的图像识别研究",
                AchievementType.PAPER,
                "计算机学报",
                LocalDate.of(2024, 3, 20),
                null,
                null,
                null,
                null,
                null,
                null,
                124L,
                null);
    }

    private AchievementStatsVO createTestAchievementStatsVO() {
        return AchievementStatsVO.builder()
                .totalAchievements(50L)
                .nationalCount(10L)
                .provincialCount(20L)
                .schoolCount(20L)
                .build();
    }

    @Test
    @DisplayName("转换为DTO：应正确转换所有字段")
    void toDTO_shouldConvertAllFields() {
        AchievementResult result = createTestAchievementResult();

        AchievementDTO dto = converter.toDTO(result);

        assertNotNull(dto);
        assertEquals(TEST_ID, dto.getId());
        assertEquals(TEST_TITLE, dto.getTitle());
        assertEquals(TEST_RELATE_TO, dto.getRelateTo());
        assertEquals(AchievementType.COMPETITION, dto.getType());
        assertEquals(TEST_ACHIEVE_AT, dto.getAchieveAt());
        assertEquals(AwardLevel.NATIONAL, dto.getAwardLevel());
        assertEquals("国家级", dto.getAwardLevelName());
        assertEquals("一等奖", dto.getAwardName());
        assertEquals(TEST_COMPETITION_NAME, dto.getCompetitionName());
        assertEquals(TEST_COMPETITION_SHORT_NAME, dto.getCompetitionShortName());
        assertEquals(TEST_COMPETITION_LOGO_FILE_ID, dto.getCompetitionLogoFileId());
        assertEquals(TEST_FILE_ID, dto.getFileId());
        assertEquals(TEST_FILE_URL, dto.getFileUrl());
    }

    @Test
    @DisplayName("转换为DTO：论文成就（无奖项信息）")
    void toDTO_paperAchievement_shouldHandleNullAwardFields() {
        AchievementResult result = createTestPaperAchievementResult();

        AchievementDTO dto = converter.toDTO(result);

        assertNotNull(dto);
        assertEquals(2L, dto.getId());
        assertEquals("基于深度学习的图像识别研究", dto.getTitle());
        assertEquals(AchievementType.PAPER, dto.getType());
        assertEquals(LocalDate.of(2024, 3, 20), dto.getAchieveAt());
        assertEquals("计算机学报", dto.getRelateTo());
        assertNull(dto.getAwardLevel());
        assertNull(dto.getAwardLevelName());
        assertNull(dto.getAwardName());
        assertEquals(124L, dto.getFileId());
    }

    @Test
    @DisplayName("转换为DTO：null结果应返回null")
    void toDTO_nullResult_shouldReturnNull() {
        AchievementDTO dto = converter.toDTO(null);
        assertNull(dto);
    }

    @Test
    @DisplayName("转换为DTO列表：应正确转换列表")
    void toDTOList_shouldConvertList() {
        List<AchievementResult> resultList = new ArrayList<>();
        resultList.add(createTestAchievementResult());
        resultList.add(createTestPaperAchievementResult());

        List<AchievementDTO> dtoList = converter.toDTOList(resultList);

        assertNotNull(dtoList);
        assertEquals(2, dtoList.size());

        AchievementDTO first = dtoList.get(0);
        assertEquals(TEST_ID, first.getId());
        assertEquals(AchievementType.COMPETITION, first.getType());

        AchievementDTO second = dtoList.get(1);
        assertEquals(2L, second.getId());
        assertEquals(AchievementType.PAPER, second.getType());
    }

    @Test
    @DisplayName("转换为DTO列表：空列表应返回空列表")
    void toDTOList_emptyList_shouldReturnEmptyList() {
        List<AchievementResult> resultList = new ArrayList<>();

        List<AchievementDTO> dtoList = converter.toDTOList(resultList);

        assertNotNull(dtoList);
        assertTrue(dtoList.isEmpty());
    }

    @Test
    @DisplayName("转换为DTO列表：null列表应返回空列表")
    void toDTOList_nullList_shouldReturnEmptyList() {
        List<AchievementDTO> dtoList = converter.toDTOList(null);

        assertNotNull(dtoList);
        assertTrue(dtoList.isEmpty());
    }

    @Test
    @DisplayName("转换为统计DTO：应正确转换统计信息")
    void toStatsDTO_shouldConvertStats() {
        AchievementStatsVO vo = createTestAchievementStatsVO();

        AchievementStatsDTO dto = converter.toStatsDTO(vo);

        assertNotNull(dto);
        assertEquals(50, dto.getTotalAchievements());
        assertEquals(10, dto.getNationalCount());
        assertEquals(20, dto.getProvincialCount());
        assertEquals(20, dto.getSchoolCount());
    }

    @Test
    @DisplayName("转换为统计DTO：零值统计信息")
    void toStatsDTO_zeroValues_shouldConvertCorrectly() {
        AchievementStatsVO vo = AchievementStatsVO.builder()
                .totalAchievements(0L)
                .nationalCount(0L)
                .provincialCount(0L)
                .schoolCount(0L)
                .build();

        AchievementStatsDTO dto = converter.toStatsDTO(vo);

        assertNotNull(dto);
        assertEquals(0, dto.getTotalAchievements());
        assertEquals(0, dto.getNationalCount());
        assertEquals(0, dto.getProvincialCount());
        assertEquals(0, dto.getSchoolCount());
    }

    @Test
    @DisplayName("转换为统计DTO：null应返回null")
    void toStatsDTO_null_shouldReturnNull() {
        AchievementStatsDTO dto = converter.toStatsDTO(null);
        assertNull(dto);
    }
}
