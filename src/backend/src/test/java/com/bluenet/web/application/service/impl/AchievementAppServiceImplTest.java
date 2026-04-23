package com.bluenet.web.application.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.bluenet.web.application.AchievementResult;
import com.bluenet.web.application.command.achievement.AchievementCommands;
import com.bluenet.web.application.converter.AchievementAppConverter;
import com.bluenet.web.domain.exception.DataNotFound;
import com.bluenet.web.domain.model.entity.Achievement;
import com.bluenet.web.domain.model.enumerate.AchievementType;
import com.bluenet.web.domain.model.enumerate.AwardLevel;
import com.bluenet.web.domain.model.enumerate.FileType;
import com.bluenet.web.domain.model.vo.FileVO;
import com.bluenet.web.domain.repository.AchievementRepository;
import com.bluenet.web.domain.service.FileDomainService;

@DisplayName("AchievementAppServiceImplTest - 成就应用服务测试")
@ExtendWith(MockitoExtension.class)
class AchievementAppServiceImplTest {

    @Mock
    private FileDomainService fileDomainService;

    @Mock
    private AchievementAppConverter achievementAppConverter;

    @Mock
    private AchievementRepository achievementRepository;

    @InjectMocks
    private AchievementAppServiceImpl achievementAppService;

    private static final Long TEST_ID = 1L;
    private static final String TEST_TITLE = "蓝桥杯全国一等奖";
    private static final String TEST_RELATE_TO = "蓝桥杯";
    private static final LocalDate TEST_ACHIEVE_AT = LocalDate.of(2024, 4, 15);
    private static final Long TEST_FILE_ID = 123L;

    private AchievementCommands.CreateAchievementCommand createCompetitionCommand() {
        return new AchievementCommands.CreateAchievementCommand(
                TEST_TITLE,
                AchievementType.COMPETITION,
                TEST_RELATE_TO,
                TEST_ACHIEVE_AT,
                AwardLevel.NATIONAL,
                "一等奖",
                TEST_FILE_ID);
    }

    private AchievementCommands.CreateAchievementCommand createPaperCommand() {
        return new AchievementCommands.CreateAchievementCommand(
                "基于深度学习的图像识别研究",
                AchievementType.PAPER,
                "计算机学报",
                LocalDate.of(2024, 3, 20),
                null,
                null,
                124L);
    }

    private FileVO createTestFileVO(Long fileId) {
        return FileVO.builder()
                .id(fileId)
                .name("test-image.jpg")
                .type(FileType.NORMAL_IMG)
                .build();
    }

    @Test
    @DisplayName("TC-001: 创建竞赛获奖成就（正常场景）")
    void createCompetitionAchievement_Success() {
        AchievementCommands.CreateAchievementCommand command = createCompetitionCommand();
        FileVO fileVO = createTestFileVO(TEST_FILE_ID);

        when(fileDomainService.getFileById(TEST_FILE_ID)).thenReturn(fileVO);

        AchievementResult result = achievementAppService.createAchievement(command);

        assertNotNull(result);
        assertEquals(TEST_TITLE, result.title());
        assertEquals(AchievementType.COMPETITION, result.type());
        assertEquals(TEST_RELATE_TO, result.relateTo());
        assertEquals(TEST_ACHIEVE_AT, result.achieveAt());
        assertEquals(AwardLevel.NATIONAL, result.awardLevel());
        assertEquals("一等奖", result.awardName());
        assertEquals(TEST_FILE_ID, result.fileId());

        verify(fileDomainService).getFileById(TEST_FILE_ID);
        verify(achievementRepository).save(any(Achievement.class));
    }

    @Test
    @DisplayName("TC-002: 创建论文成就（无奖项信息）")
    void createPaperAchievement_Success() {
        AchievementCommands.CreateAchievementCommand command = createPaperCommand();
        FileVO fileVO = createTestFileVO(124L);

        when(fileDomainService.getFileById(124L)).thenReturn(fileVO);

        AchievementResult result = achievementAppService.createAchievement(command);

        assertNotNull(result);
        assertEquals("基于深度学习的图像识别研究", result.title());
        assertEquals(AchievementType.PAPER, result.type());
        assertEquals("计算机学报", result.relateTo());
        assertEquals(LocalDate.of(2024, 3, 20), result.achieveAt());
        assertNull(result.awardLevel());
        assertNull(result.awardName());
        assertEquals(124L, result.fileId());
    }

    @Test
    @DisplayName("TC-006: 业务规则违反 - 竞赛成就缺少奖项级别")
    void createCompetitionAchievement_MissingAwardLevel_ThrowsException() {
        AchievementCommands.CreateAchievementCommand command = new AchievementCommands.CreateAchievementCommand(
                TEST_TITLE,
                AchievementType.COMPETITION,
                TEST_RELATE_TO,
                TEST_ACHIEVE_AT,
                null,
                "一等奖",
                TEST_FILE_ID);

        FileVO fileVO = createTestFileVO(TEST_FILE_ID);
        when(fileDomainService.getFileById(TEST_FILE_ID)).thenReturn(fileVO);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> achievementAppService.createAchievement(command));
        assertEquals("竞赛成就必须指定奖项级别", exception.getMessage());
    }

    @Test
    @DisplayName("TC-008: 资源不存在 - 文件ID无效")
    void createAchievement_InvalidFileId_ThrowsException() {
        AchievementCommands.CreateAchievementCommand command = createCompetitionCommand();
        AchievementCommands.CreateAchievementCommand invalidFileCommand = new AchievementCommands.CreateAchievementCommand(
                command.title(), command.type(), command.relateTo(), command.achieveAt(),
                command.awardLevel(), command.awardName(), 999999L);

        when(fileDomainService.getFileById(999999L)).thenReturn(null);

        DataNotFound exception = assertThrows(
                DataNotFound.class,
                new org.junit.jupiter.api.function.Executable() {
                    @Override
                    public void execute() throws Throwable {
                        achievementAppService.createAchievement(invalidFileCommand);
                    }
                });
        assertEquals("文件不存在", exception.getMessage());
    }

    @Test
    @DisplayName("TC-009: 更新成就 - 成功")
    void updateAchievement_Success() {
        Long achievementId = 1L;
        AchievementCommands.UpdateAchievementCommand command = new AchievementCommands.UpdateAchievementCommand(
                achievementId,
                "更新后的标题",
                AchievementType.COMPETITION,
                "更新后的关联项",
                LocalDate.of(2024, 5, 1),
                AwardLevel.PROVINCIAL,
                "二等奖",
                456L);

        Achievement existing = Achievement.reconstruct(
                achievementId,
                "原始标题",
                AchievementType.COMPETITION,
                "原始关联项",
                LocalDate.of(2024, 1, 1),
                AwardLevel.NATIONAL,
                "一等奖",
                123L);

        FileVO fileVO = FileVO.builder()
                .id(456L)
                .name("updated-image.jpg")
                .type(FileType.NORMAL_IMG)
                .build();

        when(achievementRepository.findById(achievementId)).thenReturn(Optional.of(existing));
        when(fileDomainService.getFileById(456L)).thenReturn(fileVO);

        AchievementResult result = achievementAppService.updateAchievement(command);

        assertNotNull(result);
        assertEquals(achievementId, result.id());
        assertEquals("更新后的标题", result.title());
        assertEquals(AchievementType.COMPETITION, result.type());
        assertEquals("更新后的关联项", result.relateTo());
        assertEquals(LocalDate.of(2024, 5, 1), result.achieveAt());
        assertEquals(AwardLevel.PROVINCIAL, result.awardLevel());
        assertEquals("二等奖", result.awardName());
        assertEquals(456L, result.fileId());

        verify(achievementRepository).update(any(Achievement.class));
    }

    @Test
    @DisplayName("TC-010: 删除成就 - 成功")
    void deleteAchievement_Success() {
        Long achievementId = 1L;
        Achievement existing = Achievement.reconstruct(
                achievementId,
                "待删除成就",
                AchievementType.PAPER,
                "期刊",
                LocalDate.of(2024, 1, 1),
                null,
                null,
                123L);

        when(achievementRepository.findById(achievementId)).thenReturn(Optional.of(existing));

        achievementAppService.deleteAchievement(achievementId);

        verify(achievementRepository).deleteById(achievementId);
    }

    @Test
    @DisplayName("删除成就：成就不存在应抛出异常")
    void deleteAchievement_notFound_shouldThrowException() {
        Long achievementId = 999L;
        when(achievementRepository.findById(achievementId)).thenReturn(Optional.empty());

        DataNotFound exception = assertThrows(
                DataNotFound.class,
                () -> achievementAppService.deleteAchievement(achievementId));
        assertEquals("成就不存在", exception.getMessage());
    }
}
