package com.bluenet.web.domain.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDate;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.bluenet.web.domain.exception.BadRequest;
import com.bluenet.web.domain.model.entity.Achievement;
import com.bluenet.web.domain.model.enumerate.AchievementType;
import com.bluenet.web.domain.model.enumerate.AwardLevel;
import com.bluenet.web.domain.repository.AchievementRepository;

@DisplayName("AchievementDomainServiceImpl 单元测试")
@ExtendWith(MockitoExtension.class)
class AchievementDomainServiceImplTest {

    @Mock
    private AchievementRepository achievementRepository;

    @InjectMocks
    private AchievementDomainServiceImpl achievementDomainService;

    private static final Long TEST_ID = 1L;
    private static final String TEST_TITLE = "蓝桥杯全国一等奖";
    private static final String TEST_RELATE_TO = "蓝桥杯";
    private static final LocalDate TEST_ACHIEVE_AT = LocalDate.of(2024, 4, 15);
    private static final Long TEST_FILE_ID = 123L;

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

    @Test
    @DisplayName("创建竞赛成就：正常场景")
    void createCompetitionAchievement_Success() {
        Achievement result = achievementDomainService.createAchievement(
                TEST_TITLE,
                AchievementType.COMPETITION,
                TEST_RELATE_TO,
                TEST_ACHIEVE_AT,
                AwardLevel.NATIONAL,
                "一等奖",
                TEST_FILE_ID);

        assertNotNull(result);
        assertEquals(TEST_TITLE, result.getTitle());
        assertEquals(AchievementType.COMPETITION, result.getType());
        assertEquals(TEST_RELATE_TO, result.getRelateTo());
        assertEquals(TEST_ACHIEVE_AT, result.getAchieveAt());
        assertEquals(AwardLevel.NATIONAL, result.getAwardLevel());
        assertEquals("一等奖", result.getAwardName());
        assertEquals(TEST_FILE_ID, result.getFileId());
    }

    @Test
    @DisplayName("创建论文成就：无奖项信息")
    void createPaperAchievement_Success() {
        Achievement result = achievementDomainService.createAchievement(
                "基于深度学习的图像识别研究",
                AchievementType.PAPER,
                "计算机学报",
                LocalDate.of(2024, 3, 20),
                null,
                null,
                124L);

        assertNotNull(result);
        assertEquals("基于深度学习的图像识别研究", result.getTitle());
        assertEquals(AchievementType.PAPER, result.getType());
        assertEquals("计算机学报", result.getRelateTo());
        assertEquals(LocalDate.of(2024, 3, 20), result.getAchieveAt());
        assertNull(result.getAwardLevel());
        assertNull(result.getAwardName());
        assertEquals(124L, result.getFileId());
    }

    @Test
    @DisplayName("创建竞赛成就：缺少奖项级别")
    void createCompetitionAchievement_MissingAwardLevel_ThrowsException() {
        BadRequest exception = assertThrows(
                BadRequest.class,
                () -> achievementDomainService.createAchievement(
                        TEST_TITLE,
                        AchievementType.COMPETITION,
                        TEST_RELATE_TO,
                        TEST_ACHIEVE_AT,
                        null,
                        "一等奖",
                        TEST_FILE_ID));

        assertEquals("竞赛成就必须指定奖项级别", exception.getMessage());
    }

    @Test
    @DisplayName("更新成就：正常场景")
    void updateAchievement_Success() {
        Achievement existing = createTestAchievement();

        Achievement result = achievementDomainService.updateAchievement(
                existing,
                "更新后的标题",
                AchievementType.COMPETITION,
                "更新后的关联项",
                LocalDate.of(2024, 5, 1),
                AwardLevel.PROVINCIAL,
                "二等奖",
                456L);

        assertNotNull(result);
        assertEquals("更新后的标题", result.getTitle());
        assertEquals(AchievementType.COMPETITION, result.getType());
        assertEquals("更新后的关联项", result.getRelateTo());
        assertEquals(LocalDate.of(2024, 5, 1), result.getAchieveAt());
        assertEquals(AwardLevel.PROVINCIAL, result.getAwardLevel());
        assertEquals("二等奖", result.getAwardName());
        assertEquals(456L, result.getFileId());
    }

    @Test
    @DisplayName("更新竞赛成就：缺少奖项级别")
    void updateCompetitionAchievement_MissingAwardLevel_ThrowsException() {
        Achievement existing = createTestAchievement();

        BadRequest exception = assertThrows(
                BadRequest.class,
                () -> achievementDomainService.updateAchievement(
                        existing,
                        "更新后的标题",
                        AchievementType.COMPETITION,
                        "更新后的关联项",
                        LocalDate.of(2024, 5, 1),
                        null,
                        "一等奖",
                        456L));

        assertEquals("竞赛成就必须指定奖项级别", exception.getMessage());
    }
}
