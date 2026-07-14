package com.bluenet.web.infrastructure.repository.impl;

import com.bluenet.web.BaseIntegrationTest;
import com.bluenet.web.domain.model.entity.Achievement;
import com.bluenet.web.domain.model.entity.File;
import com.bluenet.web.domain.model.enumerate.AchievementType;
import com.bluenet.web.domain.model.enumerate.AwardLevel;
import com.bluenet.web.domain.model.enumerate.FileType;
import com.bluenet.web.domain.repository.AchievementRepository;
import com.bluenet.web.domain.repository.FileRepository;
import com.bluenet.web.domain.model.readmodel.AchievementReadModel;
import com.bluenet.web.application.result.achievement.AchievementStatistics;
import com.bluenet.web.infrastructure.repository.dataobject.AchievementDO;
import com.bluenet.web.infrastructure.repository.mapper.AchievementMapper;
import com.bluenet.web.testsupport.fixture.FileFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDate;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * AchievementRepositoryImpl 集成测试。
 */
@DisplayName("AchievementRepositoryImpl 集成测试")
class AchievementRepositoryImplIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private AchievementRepository achievementRepository;

    @Autowired
    private AchievementMapper achievementMapper;

    @Autowired
    private FileRepository fileRepository;

    private final AtomicLong counter = new AtomicLong(1);

    private File createFile() {
        String name = "achievement-" + counter.getAndIncrement() + ".png";
        return FileFixture.save(fileRepository, name, FileType.NORMAL_IMG);
    }

    private Achievement createAchievement(String title, AchievementType type, AwardLevel level) {
        File file = createFile();
        Achievement achievement = Achievement.create(
                title,
                type,
                title + "关联项",
                LocalDate.of(2024, 6, 1),
                level,
                level.getDescription() + "奖",
                file.getId());
        achievementRepository.save(achievement);
        return achievement;
    }

    @Test
    @DisplayName("save: 新成果应插入并回写ID")
    void save_newAchievement_shouldInsertAndReturnId() {
        Achievement achievement = createAchievement("测试论文", AchievementType.PAPER, AwardLevel.NATIONAL);

        assertThat(achievement.getId()).isNotNull();
        AchievementDO dataObject = achievementMapper.selectById(achievement.getId());
        assertThat(dataObject).isNotNull();
        assertThat(dataObject.getTitle()).isEqualTo("测试论文");
        assertThat(dataObject.getType()).isEqualTo(AchievementType.PAPER);
    }

    @Test
    @DisplayName("save: 已有成果应更新字段")
    void save_existingAchievement_shouldUpdateFields() {
        Achievement achievement = createAchievement("旧标题", AchievementType.PATENT, AwardLevel.PROVINCIAL);
        Achievement updated = Achievement.reconstruct(
                achievement.getId(),
                "新标题",
                AchievementType.PAPER,
                "新关联项",
                LocalDate.of(2025, 1, 1),
                AwardLevel.NATIONAL,
                "新奖项",
                achievement.getFileId());

        achievementRepository.save(updated);

        AchievementDO dataObject = achievementMapper.selectById(achievement.getId());
        assertThat(dataObject.getTitle()).isEqualTo("新标题");
        assertThat(dataObject.getType()).isEqualTo(AchievementType.PAPER);
    }

    @Test
    @DisplayName("findById: 存在返回实体，不存在返回空")
    void findById_shouldReturnOptional() {
        Achievement achievement = createAchievement("查询成果", AchievementType.COMPETITION, AwardLevel.SCHOOL);

        Optional<Achievement> found = achievementRepository.findById(achievement.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getTitle()).isEqualTo("查询成果");

        assertThat(achievementRepository.findById(-1L)).isEmpty();
    }

    @Test
    @DisplayName("deleteById: 应删除成果")
    void deleteById_shouldRemoveAchievement() {
        Achievement achievement = createAchievement("待删除成果", AchievementType.PAPER, AwardLevel.NATIONAL);
        Long achievementId = achievement.getId();

        achievementRepository.deleteById(achievementId);

        assertThat(achievementMapper.selectById(achievementId)).isNull();
    }

    @Test
    @DisplayName("findAchievementsWithFilter: 应按类型和级别分页查询")
    void findAchievementsWithFilter_shouldFilterAndPaginate() {
        Achievement achievement = createAchievement("过滤成果", AchievementType.COMPETITION, AwardLevel.NATIONAL);
        createAchievement("其他成果", AchievementType.PAPER, AwardLevel.PROVINCIAL);

        Page<AchievementReadModel> page = achievementRepository.findAchievementsWithFilter(
                AchievementType.COMPETITION,
                AwardLevel.NATIONAL,
                2024,
                PageRequest.of(0, 10));

        assertThat(page.getTotalElements()).isEqualTo(1);
        assertThat(page.getContent()).hasSize(1);
        assertThat(page.getContent().get(0).getId()).isEqualTo(achievement.getId());
    }

    @Test
    @DisplayName("findAchievementStats: 应统计竞赛类各级别成果数量")
    void findAchievementStats_shouldReturnStatistics() {
        createAchievement("国家级竞赛", AchievementType.COMPETITION, AwardLevel.NATIONAL);
        createAchievement("省级竞赛1", AchievementType.COMPETITION, AwardLevel.PROVINCIAL);
        createAchievement("省级竞赛2", AchievementType.COMPETITION, AwardLevel.PROVINCIAL);

        AchievementStatistics statistics = achievementRepository.findAchievementStats();

        assertThat(statistics.getTotalAchievements()).isGreaterThanOrEqualTo(3);
        assertThat(statistics.getNationalCount()).isGreaterThanOrEqualTo(1);
        assertThat(statistics.getProvincialCount()).isGreaterThanOrEqualTo(2);
    }
}
