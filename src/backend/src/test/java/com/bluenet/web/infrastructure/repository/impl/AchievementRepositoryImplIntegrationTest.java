package com.bluenet.web.infrastructure.repository.impl;

import com.bluenet.web.BaseIntegrationTest;
import com.bluenet.web.domain.model.entity.Achievement;
import com.bluenet.web.domain.model.entity.File;
import com.bluenet.web.domain.model.entity.User;
import com.bluenet.web.domain.model.enumerate.AchievementType;
import com.bluenet.web.domain.model.enumerate.AwardLevel;
import com.bluenet.web.domain.model.enumerate.FileType;
import com.bluenet.web.domain.repository.AchievementRepository;
import com.bluenet.web.domain.repository.FileRepository;
import com.bluenet.web.domain.repository.UserRepository;
import com.bluenet.web.domain.model.readmodel.AchievementMemberReadModel;
import com.bluenet.web.domain.model.readmodel.AchievementReadModel;
import com.bluenet.web.application.result.achievement.AchievementStatistics;
import com.bluenet.web.infrastructure.repository.dataobject.AchievementDO;
import com.bluenet.web.infrastructure.repository.dataobject.AchievementExternalMemberDO;
import com.bluenet.web.infrastructure.repository.dataobject.UserAchievementDO;
import com.bluenet.web.infrastructure.repository.mapper.AchievementExternalMemberMapper;
import com.bluenet.web.infrastructure.repository.mapper.AchievementMapper;
import com.bluenet.web.infrastructure.repository.mapper.UserAchievementMapper;
import com.bluenet.web.testsupport.fixture.FileFixture;
import com.bluenet.web.testsupport.fixture.UserFixture;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
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

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserAchievementMapper userAchievementMapper;

    @Autowired
    private AchievementExternalMemberMapper externalMemberMapper;

    private final AtomicLong counter = new AtomicLong(1);
    private final AtomicLong studentIdCounter = new AtomicLong(2026006000L);

    private User createUser() {
        return UserFixture.member(String.valueOf(studentIdCounter.getAndIncrement()))
                .save(userRepository, passwordEncoder);
    }

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

    @Test
    @DisplayName("save: 应写入成员关联和外部协作者")
    void save_withMembers_shouldPersistAssociations() {
        User user1 = createUser();
        User user2 = createUser();
        Achievement achievement = createAchievement("团队成果", AchievementType.COMPETITION, AwardLevel.NATIONAL);
        achievement.assignMembers(List.of(user1.getId(), user2.getId()), List.of("外部A", "外部B"));

        achievementRepository.save(achievement);

        List<UserAchievementDO> associations = userAchievementMapper.selectList(
                new QueryWrapper<UserAchievementDO>().eq("achievement_id", achievement.getId()));
        assertThat(associations)
                .extracting(UserAchievementDO::getUserId)
                .containsExactlyInAnyOrder(user1.getId(), user2.getId());
        List<AchievementExternalMemberDO> externals = externalMemberMapper.selectList(
                new QueryWrapper<AchievementExternalMemberDO>().eq("achievement_id", achievement.getId()));
        assertThat(externals)
                .extracting(AchievementExternalMemberDO::getName)
                .containsExactlyInAnyOrder("外部A", "外部B");
    }

    @Test
    @DisplayName("save: 重新保存应全量替换关联数据")
    void save_resave_shouldReplaceAssociations() {
        User user1 = createUser();
        User user2 = createUser();
        Achievement achievement = createAchievement("替换成果", AchievementType.COMPETITION, AwardLevel.NATIONAL);
        achievement.assignMembers(List.of(user1.getId()), List.of("外部A"));
        achievementRepository.save(achievement);

        achievement.assignMembers(List.of(user2.getId()), List.of("外部B"));
        achievementRepository.save(achievement);

        List<UserAchievementDO> associations = userAchievementMapper.selectList(
                new QueryWrapper<UserAchievementDO>().eq("achievement_id", achievement.getId()));
        assertThat(associations)
                .extracting(UserAchievementDO::getUserId)
                .containsExactly(user2.getId());
        List<AchievementExternalMemberDO> externals = externalMemberMapper.selectList(
                new QueryWrapper<AchievementExternalMemberDO>().eq("achievement_id", achievement.getId()));
        assertThat(externals)
                .extracting(AchievementExternalMemberDO::getName)
                .containsExactly("外部B");
    }

    @Test
    @DisplayName("deleteById: 应级联删除成员关联和外部协作者")
    void deleteById_shouldCascadeAssociations() {
        User user = createUser();
        Achievement achievement = createAchievement("级联删除成果", AchievementType.COMPETITION, AwardLevel.NATIONAL);
        achievement.assignMembers(List.of(user.getId()), List.of("外部A"));
        achievementRepository.save(achievement);

        achievementRepository.deleteById(achievement.getId());

        assertThat(achievementMapper.selectById(achievement.getId())).isNull();
        assertThat(
                userAchievementMapper.selectCount(
                        new QueryWrapper<UserAchievementDO>().eq("achievement_id", achievement.getId()))).isZero();
        assertThat(
                externalMemberMapper.selectCount(
                        new QueryWrapper<AchievementExternalMemberDO>().eq("achievement_id", achievement.getId())))
                                .isZero();
    }

    @Test
    @DisplayName("findById: 应回读成员关联和外部协作者")
    void findById_shouldLoadAssociations() {
        User user = createUser();
        Achievement achievement = createAchievement("回读成果", AchievementType.COMPETITION, AwardLevel.NATIONAL);
        achievement.assignMembers(List.of(user.getId()), List.of("外部A"));
        achievementRepository.save(achievement);

        Optional<Achievement> found = achievementRepository.findById(achievement.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getMemberIds()).containsExactly(user.getId());
        assertThat(found.get().getExternalMembers()).containsExactly("外部A");
    }

    @Test
    @DisplayName("findMembersByAchievementIds: 应返回成员简要信息")
    void findMembersByAchievementIds_shouldReturnMemberBriefs() {
        User user = createUser();
        Achievement achievement = createAchievement("成员查询成果", AchievementType.COMPETITION, AwardLevel.NATIONAL);
        achievement.assignMembers(List.of(user.getId()), null);
        achievementRepository.save(achievement);

        Map<Long, List<AchievementMemberReadModel>> result = achievementRepository
                .findMembersByAchievementIds(List.of(achievement.getId()));

        assertThat(result).containsKey(achievement.getId());
        List<AchievementMemberReadModel> members = result.get(achievement.getId());
        assertThat(members).hasSize(1);
        assertThat(members.get(0).getUserId()).isEqualTo(user.getId());
        assertThat(members.get(0).getUsername()).isEqualTo(user.getUsername());
    }

    @Test
    @DisplayName("findByUserId: 应按用户查询成就并按获奖日期倒序")
    void findByUserId_shouldReturnOrderedAchievements() {
        User user = createUser();
        File file = createFile();
        Achievement older = Achievement.create(
                "较早成果",
                AchievementType.COMPETITION,
                "竞赛",
                LocalDate.of(2023, 6, 1),
                AwardLevel.NATIONAL,
                "一等奖",
                file.getId());
        older.assignMembers(List.of(user.getId()), null);
        achievementRepository.save(older);
        Achievement newer = Achievement.create(
                "较新成果",
                AchievementType.COMPETITION,
                "竞赛",
                LocalDate.of(2024, 6, 1),
                AwardLevel.NATIONAL,
                "一等奖",
                file.getId());
        newer.assignMembers(List.of(user.getId()), null);
        achievementRepository.save(newer);

        List<AchievementReadModel> results = achievementRepository.findByUserId(user.getId());

        assertThat(results)
                .extracting(AchievementReadModel::getTitle)
                .containsExactly("较新成果", "较早成果");
        assertThat(results.get(0).getMembers())
                .extracting(AchievementMemberReadModel::getUserId)
                .contains(user.getId());
    }

    @Test
    @DisplayName("findByUserId: 无关联成就应返回空列表")
    void findByUserId_noAssociation_shouldReturnEmpty() {
        User user = createUser();

        assertThat(achievementRepository.findByUserId(user.getId())).isEmpty();
    }
}
