package com.bluenet.web.application.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDate;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.bluenet.web.api.dto.achievement.AchievementDTO;
import com.bluenet.web.api.dto.achievement.CreateAchievementRequestDTO;
import com.bluenet.web.api.dto.achievement.UpdateAchievementRequestDTO;
import com.bluenet.web.application.converter.AchievementConverter;
import com.bluenet.web.domain.exception.BadRequest;
import com.bluenet.web.domain.exception.DataNotFound;
import com.bluenet.web.domain.model.entity.Achievement;
import com.bluenet.web.domain.model.enumerate.AchievementType;
import com.bluenet.web.domain.model.enumerate.AwardLevel;
import com.bluenet.web.domain.model.vo.AchievementVO;
import com.bluenet.web.domain.model.vo.FileVO;
import com.bluenet.web.domain.service.AchievementDomainService;
import com.bluenet.web.domain.service.FileDomainService;
import com.bluenet.web.domain.repository.AchievementRepository;

@DisplayName("AchievementServiceImplTest - 成就服务测试")
@ExtendWith(MockitoExtension.class)
class AchievementServiceImplTest {

    @Mock
    private AchievementDomainService achievementDomainService;

    @Mock
    private FileDomainService fileDomainService;

    @Mock
    private AchievementConverter achievementConverter;

    @Mock
    private AchievementRepository achievementRepository;

    @InjectMocks
    private AchievementServiceImpl achievementService;

    private static final Long TEST_ID = 1L;
    private static final String TEST_TITLE = "蓝桥杯全国一等奖";
    private static final String TEST_RELATE_TO = "蓝桥杯";
    private static final LocalDate TEST_ACHIEVE_AT = LocalDate.of(2024, 4, 15);
    private static final Long TEST_FILE_ID = 123L;

    private CreateAchievementRequestDTO createCompetitionRequest() {
        return CreateAchievementRequestDTO.builder()
                .title(TEST_TITLE)
                .type(AchievementType.COMPETITION)
                .relateTo(TEST_RELATE_TO)
                .achieveAt(TEST_ACHIEVE_AT)
                .awardLevel(AwardLevel.NATIONAL)
                .awardName("一等奖")
                .fileId(TEST_FILE_ID)
                .build();
    }

    private CreateAchievementRequestDTO createPaperRequest() {
        return CreateAchievementRequestDTO.builder()
                .title("基于深度学习的图像识别研究")
                .type(AchievementType.PAPER)
                .relateTo("计算机学报")
                .achieveAt(LocalDate.of(2024, 3, 20))
                .fileId(124L)
                .build();
    }

    private CreateAchievementRequestDTO createPatentRequest() {
        return CreateAchievementRequestDTO.builder()
                .title("一种新型数据处理装置")
                .type(AchievementType.PATENT)
                .achieveAt(LocalDate.of(2024, 2, 10))
                .fileId(125L)
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

    private AchievementVO createTestAchievementVO() {
        return AchievementVO.builder()
                .id(TEST_ID)
                .title(TEST_TITLE)
                .relateTo(TEST_RELATE_TO)
                .type(AchievementType.COMPETITION)
                .achieveAt(TEST_ACHIEVE_AT)
                .awardLevel(AwardLevel.NATIONAL)
                .awardName("一等奖")
                .fileId(TEST_FILE_ID)
                .build();
    }

    private FileVO createTestFileVO() {
        return FileVO.builder()
                .id(TEST_FILE_ID)
                .name("test-image.jpg")
                .type(com.bluenet.web.domain.model.enumerate.FileType.NORMAL_IMG)
                .build();
    }

    @Test
    @DisplayName("TC-001: 创建竞赛获奖成就（正常场景）")
    void createCompetitionAchievement_Success() {
        CreateAchievementRequestDTO request = createCompetitionRequest();
        Achievement achievement = createTestAchievement();
        AchievementVO achievementVO = createTestAchievementVO();
        FileVO fileVO = createTestFileVO();

        when(fileDomainService.getFileById(TEST_FILE_ID)).thenReturn(fileVO);
        when(achievementDomainService.createAchievement(any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(achievement);
        when(achievementRepository.save(achievement)).thenReturn(achievementVO);
        when(achievementConverter.toDTO(achievementVO)).thenReturn(
                AchievementDTO.builder()
                        .id(TEST_ID)
                        .title(TEST_TITLE)
                        .type(AchievementType.COMPETITION)
                        .relateTo(TEST_RELATE_TO)
                        .achieveAt(TEST_ACHIEVE_AT)
                        .awardLevel(AwardLevel.NATIONAL)
                        .awardName("一等奖")
                        .fileId(TEST_FILE_ID)
                        .build());

        AchievementDTO result = achievementService.createAchievement(request);

        assertNotNull(result);
        assertEquals(TEST_ID, result.getId());
        assertEquals(TEST_TITLE, result.getTitle());
        assertEquals(AchievementType.COMPETITION, result.getType());
        assertEquals(TEST_RELATE_TO, result.getRelateTo());
        assertEquals(TEST_ACHIEVE_AT, result.getAchieveAt());
        assertEquals(AwardLevel.NATIONAL, result.getAwardLevel());
        assertEquals("一等奖", result.getAwardName());
        assertEquals(TEST_FILE_ID, result.getFileId());

        verify(fileDomainService).getFileById(TEST_FILE_ID);
        verify(achievementDomainService).createAchievement(
                eq(TEST_TITLE),
                eq(AchievementType.COMPETITION),
                eq(TEST_RELATE_TO),
                eq(TEST_ACHIEVE_AT),
                eq(AwardLevel.NATIONAL),
                eq("一等奖"),
                eq(TEST_FILE_ID));
    }

    @Test
    @DisplayName("TC-002: 创建论文成就（无奖项信息）")
    void createPaperAchievement_Success() {
        CreateAchievementRequestDTO request = createPaperRequest();
        Achievement achievement = new Achievement();
        achievement.setId(2L);
        achievement.setTitle("基于深度学习的图像识别研究");
        achievement.setType(AchievementType.PAPER);
        achievement.setRelateTo("计算机学报");
        achievement.setAchieveAt(LocalDate.of(2024, 3, 20));
        achievement.setFileId(124L);

        AchievementVO achievementVO = AchievementVO.builder()
                .id(2L)
                .title("基于深度学习的图像识别研究")
                .type(AchievementType.PAPER)
                .relateTo("计算机学报")
                .achieveAt(LocalDate.of(2024, 3, 20))
                .fileId(124L)
                .build();

        FileVO fileVO = FileVO.builder()
                .id(124L)
                .name("paper-image.jpg")
                .type(com.bluenet.web.domain.model.enumerate.FileType.NORMAL_IMG)
                .build();

        when(fileDomainService.getFileById(124L)).thenReturn(fileVO);
        when(achievementDomainService.createAchievement(any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(achievement);
        when(achievementRepository.save(achievement)).thenReturn(achievementVO);
        when(achievementConverter.toDTO(achievementVO)).thenReturn(
                AchievementDTO.builder()
                        .id(2L)
                        .title("基于深度学习的图像识别研究")
                        .type(AchievementType.PAPER)
                        .relateTo("计算机学报")
                        .achieveAt(LocalDate.of(2024, 3, 20))
                        .fileId(124L)
                        .build());

        AchievementDTO result = achievementService.createAchievement(request);

        assertNotNull(result);
        assertEquals(AchievementType.PAPER, result.getType());
        assertNull(result.getAwardLevel());
        assertNull(result.getAwardName());
    }

    @Test
    @DisplayName("TC-006: 业务规则违反 - 竞赛成就缺少奖项级别")
    void createCompetitionAchievement_MissingAwardLevel_ThrowsException() {
        CreateAchievementRequestDTO request = CreateAchievementRequestDTO.builder()
                .title(TEST_TITLE)
                .type(AchievementType.COMPETITION)
                .achieveAt(TEST_ACHIEVE_AT)
                .awardLevel(null)
                .fileId(TEST_FILE_ID)
                .build();

        FileVO fileVO = createTestFileVO();
        when(fileDomainService.getFileById(TEST_FILE_ID)).thenReturn(fileVO);
        when(achievementDomainService.createAchievement(any(), any(), any(), any(), any(), any(), any()))
                .thenThrow(new BadRequest("竞赛成就必须指定奖项级别"));

        BadRequest exception = assertThrows(
                BadRequest.class,
                () -> achievementService.createAchievement(request));
        assertEquals("竞赛成就必须指定奖项级别", exception.getMessage());
    }

    @Test
    @DisplayName("TC-008: 资源不存在 - 文件ID无效")
    void createAchievement_InvalidFileId_ThrowsException() {
        CreateAchievementRequestDTO request = createCompetitionRequest();
        request.setFileId(999999L);

        when(fileDomainService.getFileById(999999L)).thenReturn(null);

        DataNotFound exception = assertThrows(
                DataNotFound.class,
                () -> achievementService.createAchievement(request));
        assertEquals("文件不存在", exception.getMessage());
    }

    @Test
    @DisplayName("TC-009: 更新成就 - 成功")
    void updateAchievement_Success() {
        Long achievementId = 1L;
        UpdateAchievementRequestDTO request = UpdateAchievementRequestDTO.builder()
                .title("更新后的标题")
                .type(AchievementType.COMPETITION)
                .relateTo("更新后的关联项")
                .achieveAt(LocalDate.of(2024, 5, 1))
                .awardLevel(AwardLevel.PROVINCIAL)
                .awardName("二等奖")
                .fileId(456L)
                .build();

        AchievementVO existingVO = AchievementVO.builder()
                .id(achievementId)
                .title("原始标题")
                .type(AchievementType.COMPETITION)
                .relateTo("原始关联项")
                .achieveAt(LocalDate.of(2024, 1, 1))
                .awardLevel(AwardLevel.NATIONAL)
                .awardName("一等奖")
                .fileId(123L)
                .build();

        FileVO fileVO = FileVO.builder()
                .id(456L)
                .name("updated-image.jpg")
                .type(com.bluenet.web.domain.model.enumerate.FileType.NORMAL_IMG)
                .build();

        Achievement updatedEntity = new Achievement();
        updatedEntity.setId(achievementId);
        updatedEntity.setTitle("更新后的标题");
        updatedEntity.setType(AchievementType.COMPETITION);
        updatedEntity.setRelateTo("更新后的关联项");
        updatedEntity.setAchieveAt(LocalDate.of(2024, 5, 1));
        updatedEntity.setAwardLevel(AwardLevel.PROVINCIAL);
        updatedEntity.setAwardName("二等奖");
        updatedEntity.setFileId(456L);

        AchievementVO savedVO = AchievementVO.builder()
                .id(achievementId)
                .title("更新后的标题")
                .type(AchievementType.COMPETITION)
                .relateTo("更新后的关联项")
                .achieveAt(LocalDate.of(2024, 5, 1))
                .awardLevel(AwardLevel.PROVINCIAL)
                .awardName("二等奖")
                .fileId(456L)
                .build();

        when(achievementRepository.findById(achievementId)).thenReturn(existingVO);
        when(fileDomainService.getFileById(456L)).thenReturn(fileVO);
        when(achievementDomainService.updateAchievement(any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(updatedEntity);
        when(achievementRepository.update(updatedEntity)).thenReturn(savedVO);
        when(achievementConverter.toDTO(savedVO)).thenReturn(
                AchievementDTO.builder()
                        .id(achievementId)
                        .title("更新后的标题")
                        .type(AchievementType.COMPETITION)
                        .relateTo("更新后的关联项")
                        .achieveAt(LocalDate.of(2024, 5, 1))
                        .awardLevel(AwardLevel.PROVINCIAL)
                        .awardName("二等奖")
                        .fileId(456L)
                        .build());

        AchievementDTO result = achievementService.updateAchievement(achievementId, request);

        assertNotNull(result);
        assertEquals(achievementId, result.getId());
        assertEquals("更新后的标题", result.getTitle());
        assertEquals(AchievementType.COMPETITION, result.getType());
        assertEquals("更新后的关联项", result.getRelateTo());
        assertEquals(LocalDate.of(2024, 5, 1), result.getAchieveAt());
        assertEquals(AwardLevel.PROVINCIAL, result.getAwardLevel());
        assertEquals("二等奖", result.getAwardName());
        assertEquals(456L, result.getFileId());
    }

    @Test
    @DisplayName("TC-010: 删除成就 - 成功")
    void deleteAchievement_Success() {
        Long achievementId = 1L;
        AchievementVO existingVO = AchievementVO.builder()
                .id(achievementId)
                .title("待删除成就")
                .type(AchievementType.PAPER)
                .relateTo("期刊")
                .achieveAt(LocalDate.of(2024, 1, 1))
                .fileId(123L)
                .build();

        when(achievementRepository.findById(achievementId)).thenReturn(existingVO);

        achievementService.deleteAchievement(achievementId);

        verify(achievementRepository).delete(achievementId);
    }
}
