package com.bluenet.web.application.converter;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import com.bluenet.web.api.dto.achievement.AchievementDTO;
import com.bluenet.web.api.dto.achievement.AchievementStatsDTO;
import com.bluenet.web.domain.model.entity.Achievement;
import com.bluenet.web.domain.model.enumerate.AchievementType;
import com.bluenet.web.domain.model.enumerate.AwardLevel;
import com.bluenet.web.domain.model.vo.AchievementStatsVO;
import com.bluenet.web.domain.model.vo.AchievementVO;

@DisplayName("AchievementConverter 单元测试")
class AchievementConverterTest {

    private final AchievementConverter converter = new AchievementConverter();

    private static final Long TEST_ID = 1L;
    private static final String TEST_TITLE = "蓝桥杯全国一等奖";
    private static final String TEST_RELATE_TO = "蓝桥杯";
    private static final LocalDate TEST_ACHIEVE_AT = LocalDate.of(2024, 4, 15);
    private static final Long TEST_FILE_ID = 123L;
    private static final String TEST_FILE_URL = "http://example.com/image.jpg";
    private static final String TEST_COMPETITION_NAME = "蓝桥杯全国软件和信息技术专业人才大赛";
    private static final String TEST_COMPETITION_SHORT_NAME = "蓝桥杯";
    private static final Long TEST_COMPETITION_LOGO_FILE_ID = 456L;

    private AchievementVO createTestAchievementVO() {
        return AchievementVO.builder()
                .id(TEST_ID)
                .title(TEST_TITLE)
                .relateTo(TEST_RELATE_TO)
                .type(AchievementType.COMPETITION)
                .achieveAt(TEST_ACHIEVE_AT)
                .awardLevel(AwardLevel.NATIONAL)
                .awardName("一等奖")
                .competitionName(TEST_COMPETITION_NAME)
                .competitionShortName(TEST_COMPETITION_SHORT_NAME)
                .competitionLogoFileId(TEST_COMPETITION_LOGO_FILE_ID)
                .fileId(TEST_FILE_ID)
                .fileUrl(TEST_FILE_URL)
                .build();
    }

    private Achievement createTestAchievement() {
        Achievement achievement = new Achievement();
        achievement.setId(TEST_ID);
        achievement.setTitle(TEST_TITLE);
        achievement.setType(AchievementType.COMPETITION);
        achievement.setRelateTo(TEST_RELATE_TO);
        achievement.setAchieveAt(TEST_ACHIEVE_AT);
        achievement.setAwardLevel(AwardLevel.NATIONAL);
        achievement.setAwardName("一等奖");
        achievement.setFileId(TEST_FILE_ID);
        return achievement;
    }

    private AchievementVO createTestPaperAchievementVO() {
        return AchievementVO.builder()
                .id(2L)
                .title("基于深度学习的图像识别研究")
                .type(AchievementType.PAPER)
                .achieveAt(LocalDate.of(2024, 3, 20))
                .relateTo("计算机学报")
                .fileId(124L)
                .build();
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
    void convertToDTO_shouldConvertAllFields() {
        AchievementVO vo = createTestAchievementVO();

        AchievementDTO dto = converter.convertToDTO(vo);

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
    void convertToDTO_paperAchievement_shouldHandleNullAwardFields() {
        AchievementVO vo = createTestPaperAchievementVO();

        AchievementDTO dto = converter.convertToDTO(vo);

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
    @DisplayName("转换为DTO：null字段应保持null")
    void convertToDTO_withNullFields_shouldKeepNull() {
        AchievementVO vo = AchievementVO.builder()
                .id(TEST_ID)
                .title(TEST_TITLE)
                .type(AchievementType.PATENT)
                .achieveAt(TEST_ACHIEVE_AT)
                .fileId(TEST_FILE_ID)
                .build();

        AchievementDTO dto = converter.convertToDTO(vo);

        assertNotNull(dto);
        assertEquals(TEST_ID, dto.getId());
        assertEquals(TEST_TITLE, dto.getTitle());
        assertEquals(AchievementType.PATENT, dto.getType());
        assertEquals(TEST_ACHIEVE_AT, dto.getAchieveAt());
        assertNull(dto.getRelateTo());
        assertNull(dto.getAwardLevel());
        assertNull(dto.getAwardLevelName());
        assertNull(dto.getAwardName());
        assertEquals(TEST_FILE_ID, dto.getFileId());
    }

    @Test
    @DisplayName("转换为DTO列表：应正确转换列表")
    void convertToDTOList_shouldConvertList() {
        List<AchievementVO> voList = new ArrayList<>();
        voList.add(createTestAchievementVO());
        voList.add(createTestPaperAchievementVO());

        List<AchievementDTO> dtoList = converter.convertToDTOList(voList);

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
    void convertToDTOList_emptyList_shouldReturnEmptyList() {
        List<AchievementVO> voList = new ArrayList<>();

        List<AchievementDTO> dtoList = converter.convertToDTOList(voList);

        assertNotNull(dtoList);
        assertTrue(dtoList.isEmpty());
    }

    @Test
    @DisplayName("转换为DTO列表：单元素列表应正确转换")
    void convertToDTOList_singleElement_shouldConvertCorrectly() {
        List<AchievementVO> voList = new ArrayList<>();
        voList.add(createTestAchievementVO());

        List<AchievementDTO> dtoList = converter.convertToDTOList(voList);

        assertNotNull(dtoList);
        assertEquals(1, dtoList.size());
        assertEquals(TEST_ID, dtoList.get(0).getId());
        assertEquals(TEST_TITLE, dtoList.get(0).getTitle());
    }

    @Test
    @DisplayName("转换为DTO分页：应正确转换分页")
    void convertToDTOPage_shouldConvertPage() {
        List<AchievementVO> voList = new ArrayList<>();
        voList.add(createTestAchievementVO());
        voList.add(createTestPaperAchievementVO());

        Page<AchievementVO> voPage = new PageImpl<>(voList, PageRequest.of(0, 10), 2);

        Page<AchievementDTO> dtoPage = converter.convertToDTOPage(voPage);

        assertNotNull(dtoPage);
        assertEquals(2, dtoPage.getTotalElements());
        assertEquals(1, dtoPage.getTotalPages());
        assertEquals(2, dtoPage.getContent().size());

        AchievementDTO first = dtoPage.getContent().get(0);
        assertEquals(TEST_ID, first.getId());
        assertEquals(AchievementType.COMPETITION, first.getType());
    }

    @Test
    @DisplayName("转换为DTO分页：空分页应返回空分页")
    void convertToDTOPage_emptyPage_shouldReturnEmptyPage() {
        List<AchievementVO> voList = new ArrayList<>();
        Page<AchievementVO> voPage = new PageImpl<>(voList, PageRequest.of(0, 10), 0);

        Page<AchievementDTO> dtoPage = converter.convertToDTOPage(voPage);

        assertNotNull(dtoPage);
        assertEquals(0, dtoPage.getTotalElements());
        assertTrue(dtoPage.getContent().isEmpty());
    }

    @Test
    @DisplayName("转换为统计DTO：应正确转换统计信息")
    void convertToStatsDTO_shouldConvertStats() {
        AchievementStatsVO vo = createTestAchievementStatsVO();

        AchievementStatsDTO dto = converter.convertToStatsDTO(vo);

        assertNotNull(dto);
        assertEquals(50, dto.getTotalAchievements());
        assertEquals(10, dto.getNationalCount());
        assertEquals(20, dto.getProvincialCount());
        assertEquals(20, dto.getSchoolCount());
    }

    @Test
    @DisplayName("转换为统计DTO：零值统计信息")
    void convertToStatsDTO_zeroValues_shouldConvertCorrectly() {
        AchievementStatsVO vo = AchievementStatsVO.builder()
                .totalAchievements(0L)
                .nationalCount(0L)
                .provincialCount(0L)
                .schoolCount(0L)
                .build();

        AchievementStatsDTO dto = converter.convertToStatsDTO(vo);

        assertNotNull(dto);
        assertEquals(0, dto.getTotalAchievements());
        assertEquals(0, dto.getNationalCount());
        assertEquals(0, dto.getProvincialCount());
        assertEquals(0, dto.getSchoolCount());
    }

    @Test
    @DisplayName("实体转VO：应正确转换实体")
    void toVO_shouldConvertEntity() {
        Achievement entity = createTestAchievement();

        AchievementVO vo = converter.toVO(entity);

        assertNotNull(vo);
        assertEquals(TEST_ID, vo.getId());
        assertEquals(TEST_TITLE, vo.getTitle());
        assertEquals(AchievementType.COMPETITION, vo.getType());
        assertEquals(TEST_RELATE_TO, vo.getRelateTo());
        assertEquals(TEST_ACHIEVE_AT, vo.getAchieveAt());
        assertEquals(AwardLevel.NATIONAL, vo.getAwardLevel());
        assertEquals("一等奖", vo.getAwardName());
        assertEquals(TEST_FILE_ID, vo.getFileId());
    }

    @Test
    @DisplayName("实体转VO：null实体应返回null")
    void toVO_nullEntity_shouldReturnNull() {
        AchievementVO vo = converter.toVO(null);

        assertNull(vo);
    }

    @Test
    @DisplayName("toDTO方法：应调用convertToDTO")
    void toDTO_shouldCallConvertToDTO() {
        AchievementVO vo = createTestAchievementVO();

        AchievementDTO dto = converter.toDTO(vo);

        assertNotNull(dto);
        assertEquals(TEST_ID, dto.getId());
        assertEquals(TEST_TITLE, dto.getTitle());
        assertEquals(AchievementType.COMPETITION, dto.getType());
    }
}
