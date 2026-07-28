package com.bluenet.web.application.service.impl;

import com.bluenet.web.BaseIntegrationTest;
import com.bluenet.web.application.command.achievement.AchievementCommands;
import com.bluenet.web.application.result.achievement.AchievementResult;
import com.bluenet.web.application.result.achievement.AchievementStatistics;
import com.bluenet.web.application.service.AchievementAppService;
import com.bluenet.web.domain.exception.BadRequest;
import com.bluenet.web.domain.exception.DataNotFound;
import com.bluenet.web.domain.model.entity.Achievement;
import com.bluenet.web.domain.model.entity.File;
import com.bluenet.web.domain.model.entity.User;
import com.bluenet.web.domain.model.enumerate.AchievementType;
import com.bluenet.web.domain.model.enumerate.AwardLevel;
import com.bluenet.web.domain.model.enumerate.FileType;
import com.bluenet.web.domain.repository.AchievementRepository;
import com.bluenet.web.domain.repository.FileRepository;
import com.bluenet.web.domain.repository.UserRepository;
import com.bluenet.web.infrastructure.security.principal.WithSecurityPrincipal;
import com.bluenet.web.infrastructure.security.util.UserCTX;
import com.bluenet.web.testsupport.fixture.FileFixture;
import com.bluenet.web.testsupport.fixture.UserFixture;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * AchievementAppServiceImpl 集成测试。
 *
 * <p>
 * 验证成就应用服务的创建、更新、删除、分页查询及统计逻辑，同时覆盖文件类型校验、 竞赛成就奖项级别校验等关键业务规则分支。
 * </p>
 */
@DisplayName("AchievementAppServiceImpl 集成测试")
class AchievementAppServiceImplIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private AchievementAppService achievementAppService;

    @Autowired
    private AchievementRepository achievementRepository;

    @Autowired
    private FileRepository fileRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @AfterEach
    void cleanupSecurityContext() {
        UserCTX.clear();
    }

    private File createNormalImageFile(String name) {
        return FileFixture.save(fileRepository, name, FileType.NORMAL_IMG);
    }

    private Achievement createAchievementDirectly(String title, AchievementType type, AwardLevel awardLevel,
            LocalDate achieveAt, Long fileId) {
        Achievement achievement = Achievement.create(title, type, "关联对象", achieveAt, awardLevel, "奖项", fileId);
        achievementRepository.save(achievement);
        return achievement;
    }

    @Test
    @WithSecurityPrincipal(userId = 1L, roleType = "SUPER_ADMIN")
    @DisplayName("createAchievement: 竞赛成就应创建成功")
    void createAchievement_competition_shouldCreate() {
        File file = createNormalImageFile("competition-img");
        AchievementCommands.CreateAchievementCommand command = new AchievementCommands.CreateAchievementCommand(
                "全国大学生竞赛一等奖",
                AchievementType.COMPETITION,
                "全国大学生程序设计竞赛",
                LocalDate.of(2024, 8, 15),
                AwardLevel.NATIONAL,
                "一等奖",
                file.getId(),
                null,
                null);

        AchievementResult result = achievementAppService.createAchievement(command);

        assertThat(result).isNotNull();
        assertThat(result.id()).isNotNull();
        assertThat(result.title()).isEqualTo("全国大学生竞赛一等奖");
        assertThat(result.type()).isEqualTo(AchievementType.COMPETITION);
        assertThat(result.awardLevel()).isEqualTo(AwardLevel.NATIONAL);
        assertThat(result.fileId()).isEqualTo(file.getId());
        assertThat(achievementRepository.findById(result.id())).isPresent();
    }

    @Test
    @WithSecurityPrincipal(userId = 1L, roleType = "SUPER_ADMIN")
    @DisplayName("createAchievement: 非竞赛成就可以不指定奖项级别")
    void createAchievement_paperWithoutAwardLevel_shouldCreate() {
        File file = createNormalImageFile("paper-img");
        AchievementCommands.CreateAchievementCommand command = new AchievementCommands.CreateAchievementCommand(
                "论文发表",
                AchievementType.PAPER,
                "核心期刊",
                LocalDate.of(2024, 6, 1),
                null,
                null,
                file.getId(),
                null,
                null);

        AchievementResult result = achievementAppService.createAchievement(command);

        assertThat(result).isNotNull();
        assertThat(result.type()).isEqualTo(AchievementType.PAPER);
        assertThat(result.awardLevel()).isNull();
    }

    @Test
    @WithSecurityPrincipal(userId = 1L, roleType = "SUPER_ADMIN")
    @DisplayName("createAchievement: 文件不存在应抛 DataNotFound")
    void createAchievement_fileNotFound_shouldThrowDataNotFound() {
        AchievementCommands.CreateAchievementCommand command = new AchievementCommands.CreateAchievementCommand(
                "不存在文件的成就",
                AchievementType.PATENT,
                "专利",
                LocalDate.of(2024, 5, 1),
                null,
                null,
                99999L,
                null,
                null);

        assertThatThrownBy(() -> achievementAppService.createAchievement(command))
                .isInstanceOf(DataNotFound.class)
                .hasMessageContaining("文件不存在");
    }

    @Test
    @WithSecurityPrincipal(userId = 1L, roleType = "SUPER_ADMIN")
    @DisplayName("createAchievement: 文件类型不是 NORMAL_IMG 应抛 BadRequest")
    void createAchievement_fileTypeMismatch_shouldThrowBadRequest() {
        File workFile = FileFixture.save(fileRepository, "work-file", FileType.WORK);
        AchievementCommands.CreateAchievementCommand command = new AchievementCommands.CreateAchievementCommand(
                "文件类型不匹配",
                AchievementType.PAPER,
                "论文",
                LocalDate.of(2024, 5, 1),
                null,
                null,
                workFile.getId(),
                null,
                null);

        assertThatThrownBy(() -> achievementAppService.createAchievement(command))
                .isInstanceOf(BadRequest.class)
                .hasMessageContaining("文件类型不匹配");
    }

    @Test
    @WithSecurityPrincipal(userId = 1L, roleType = "SUPER_ADMIN")
    @DisplayName("createAchievement: 竞赛成就未指定奖项级别应抛 IllegalArgumentException")
    void createAchievement_competitionWithoutAwardLevel_shouldThrowIllegalArgument() {
        File file = createNormalImageFile("competition-no-level-img");
        AchievementCommands.CreateAchievementCommand command = new AchievementCommands.CreateAchievementCommand(
                "竞赛成就无级别",
                AchievementType.COMPETITION,
                "竞赛",
                LocalDate.of(2024, 5, 1),
                null,
                null,
                file.getId(),
                null,
                null);

        assertThatThrownBy(() -> achievementAppService.createAchievement(command))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("竞赛成就必须指定奖项级别");
    }

    @Test
    @WithSecurityPrincipal(userId = 1L, roleType = "SUPER_ADMIN")
    @DisplayName("updateAchievement: 应更新成就信息")
    void updateAchievement_shouldUpdate() {
        File originalFile = createNormalImageFile("original-img");
        AchievementResult created = achievementAppService.createAchievement(
                new AchievementCommands.CreateAchievementCommand(
                        "旧标题",
                        AchievementType.COMPETITION,
                        "旧竞赛",
                        LocalDate.of(2023, 7, 1),
                        AwardLevel.PROVINCIAL,
                        "旧奖项",
                        originalFile.getId(),
                        null,
                        null));
        File newFile = createNormalImageFile("new-img");
        AchievementCommands.UpdateAchievementCommand command = new AchievementCommands.UpdateAchievementCommand(
                created.id(),
                "新标题",
                AchievementType.COMPETITION,
                "新竞赛",
                LocalDate.of(2024, 9, 1),
                AwardLevel.NATIONAL,
                "新奖项",
                newFile.getId(),
                null,
                null);

        AchievementResult result = achievementAppService.updateAchievement(command);

        assertThat(result.title()).isEqualTo("新标题");
        assertThat(result.relateTo()).isEqualTo("新竞赛");
        assertThat(result.achieveAt()).isEqualTo(LocalDate.of(2024, 9, 1));
        assertThat(result.awardLevel()).isEqualTo(AwardLevel.NATIONAL);
        assertThat(result.awardName()).isEqualTo("新奖项");
        assertThat(result.fileId()).isEqualTo(newFile.getId());
    }

    @Test
    @WithSecurityPrincipal(userId = 1L, roleType = "SUPER_ADMIN")
    @DisplayName("updateAchievement: 成就不存在应抛 DataNotFound")
    void updateAchievement_notFound_shouldThrowDataNotFound() {
        File file = createNormalImageFile("update-notfound-img");
        AchievementCommands.UpdateAchievementCommand command = new AchievementCommands.UpdateAchievementCommand(
                99999L,
                "标题",
                AchievementType.PAPER,
                "期刊",
                LocalDate.of(2024, 5, 1),
                null,
                null,
                file.getId(),
                null,
                null);

        assertThatThrownBy(() -> achievementAppService.updateAchievement(command))
                .isInstanceOf(DataNotFound.class)
                .hasMessageContaining("成就不存在");
    }

    @Test
    @WithSecurityPrincipal(userId = 1L, roleType = "SUPER_ADMIN")
    @DisplayName("deleteAchievement: 应删除成就")
    void deleteAchievement_shouldDelete() {
        File file = createNormalImageFile("delete-img");
        AchievementResult created = achievementAppService.createAchievement(
                new AchievementCommands.CreateAchievementCommand(
                        "待删除成就",
                        AchievementType.PATENT,
                        "专利",
                        LocalDate.of(2024, 4, 1),
                        null,
                        null,
                        file.getId(),
                        null,
                        null));

        achievementAppService.deleteAchievement(created.id());

        assertThat(achievementRepository.findById(created.id())).isEmpty();
    }

    @Test
    @WithSecurityPrincipal(userId = 1L, roleType = "SUPER_ADMIN")
    @DisplayName("deleteAchievement: 成就不存在应抛 DataNotFound")
    void deleteAchievement_notFound_shouldThrowDataNotFound() {
        assertThatThrownBy(() -> achievementAppService.deleteAchievement(99999L))
                .isInstanceOf(DataNotFound.class)
                .hasMessageContaining("成就不存在");
    }

    @Test
    @WithSecurityPrincipal(userId = 1L, roleType = "MEMBER")
    @DisplayName("getAchievements: 应支持分页查询并使用默认值")
    void getAchievements_withDefaults_shouldReturnPage() {
        File file = createNormalImageFile("list-img");
        createAchievementDirectly(
                "成就一",
                AchievementType.COMPETITION,
                AwardLevel.NATIONAL,
                LocalDate.of(2024, 3, 1),
                file.getId());
        createAchievementDirectly(
                "成就二",
                AchievementType.PAPER,
                null,
                LocalDate.of(2024, 2, 1),
                file.getId());
        createAchievementDirectly(
                "成就三",
                AchievementType.COMPETITION,
                AwardLevel.SCHOOL,
                LocalDate.of(2024, 1, 1),
                file.getId());

        Page<AchievementResult> result = achievementAppService.getAchievements(null, null, null, null, null);

        assertThat(result).isNotNull();
        assertThat(result.getNumber()).isZero();
        assertThat(result.getSize()).isEqualTo(12);
        assertThat(result.getTotalElements()).isEqualTo(3L);
        assertThat(result.getContent()).hasSize(3);
        assertThat(result.getContent().get(0).title()).isEqualTo("成就一");
    }

    @Test
    @WithSecurityPrincipal(userId = 1L, roleType = "MEMBER")
    @DisplayName("getAchievements: 应按类型和年份过滤")
    void getAchievements_withTypeAndYearFilter_shouldReturnFilteredPage() {
        File file = createNormalImageFile("filter-img");
        createAchievementDirectly(
                "2024 国家级竞赛",
                AchievementType.COMPETITION,
                AwardLevel.NATIONAL,
                LocalDate.of(2024, 5, 10),
                file.getId());
        createAchievementDirectly(
                "2023 国家级竞赛",
                AchievementType.COMPETITION,
                AwardLevel.NATIONAL,
                LocalDate.of(2023, 5, 10),
                file.getId());
        createAchievementDirectly(
                "2024 论文",
                AchievementType.PAPER,
                null,
                LocalDate.of(2024, 5, 10),
                file.getId());

        Page<AchievementResult> result = achievementAppService.getAchievements(
                0,
                10,
                AchievementType.COMPETITION,
                AwardLevel.NATIONAL,
                2024);

        assertThat(result.getTotalElements()).isEqualTo(1L);
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).title()).isEqualTo("2024 国家级竞赛");
    }

    @Test
    @WithSecurityPrincipal(userId = 1L, roleType = "MEMBER")
    @DisplayName("getAchievementStats: 应统计竞赛成果数量")
    void getAchievementStats_shouldReturnStatistics() {
        File file = createNormalImageFile("stats-img");
        createAchievementDirectly(
                "国家级",
                AchievementType.COMPETITION,
                AwardLevel.NATIONAL,
                LocalDate.of(2024, 5, 1),
                file.getId());
        createAchievementDirectly(
                "省级1",
                AchievementType.COMPETITION,
                AwardLevel.PROVINCIAL,
                LocalDate.of(2024, 5, 2),
                file.getId());
        createAchievementDirectly(
                "省级2",
                AchievementType.COMPETITION,
                AwardLevel.PROVINCIAL,
                LocalDate.of(2024, 5, 3),
                file.getId());
        createAchievementDirectly(
                "校级",
                AchievementType.COMPETITION,
                AwardLevel.SCHOOL,
                LocalDate.of(2024, 5, 4),
                file.getId());
        createAchievementDirectly(
                "论文非竞赛",
                AchievementType.PAPER,
                null,
                LocalDate.of(2024, 5, 5),
                file.getId());

        AchievementStatistics statistics = achievementAppService.getAchievementStats();

        assertThat(statistics.getTotalAchievements()).isEqualTo(4L);
        assertThat(statistics.getNationalCount()).isEqualTo(1L);
        assertThat(statistics.getProvincialCount()).isEqualTo(2L);
        assertThat(statistics.getSchoolCount()).isEqualTo(1L);
    }

    @Test
    @WithSecurityPrincipal(userId = 1L, roleType = "SUPER_ADMIN")
    @DisplayName("createAchievement: 应写入系统内成员关联和外部协作者")
    void createAchievement_withMembers_shouldPersistAssociations() {
        User member1 = UserFixture.member("2026005001").save(userRepository, passwordEncoder);
        User member2 = UserFixture.member("2026005002").save(userRepository, passwordEncoder);
        File file = createNormalImageFile("member-img");
        AchievementCommands.CreateAchievementCommand command = new AchievementCommands.CreateAchievementCommand(
                "团队竞赛一等奖",
                AchievementType.COMPETITION,
                "全国大学生程序设计竞赛",
                LocalDate.of(2024, 8, 15),
                AwardLevel.NATIONAL,
                "一等奖",
                file.getId(),
                List.of(member1.getId(), member2.getId()),
                List.of(" 张三-外校 ", "李四-他队", "张三-外校"));

        AchievementResult result = achievementAppService.createAchievement(command);

        assertThat(result.members())
                .extracting("userId")
                .containsExactlyInAnyOrder(member1.getId(), member2.getId());
        // 外部协作者 trim 并去重
        assertThat(result.externalMembers()).containsExactly("张三-外校", "李四-他队");

        Achievement persisted = achievementRepository.findById(result.id()).orElseThrow();
        assertThat(persisted.getMemberIds())
                .containsExactlyInAnyOrder(member1.getId(), member2.getId());
        assertThat(persisted.getExternalMembers()).containsExactly("张三-外校", "李四-他队");
    }

    @Test
    @WithSecurityPrincipal(userId = 1L, roleType = "SUPER_ADMIN")
    @DisplayName("createAchievement: 关联不存在的用户应抛 BadRequest")
    void createAchievement_invalidMember_shouldThrowBadRequest() {
        File file = createNormalImageFile("invalid-member-img");
        AchievementCommands.CreateAchievementCommand command = new AchievementCommands.CreateAchievementCommand(
                "无效成员成就",
                AchievementType.PAPER,
                "期刊",
                LocalDate.of(2024, 6, 1),
                null,
                null,
                file.getId(),
                List.of(99999L),
                null);

        assertThatThrownBy(() -> achievementAppService.createAchievement(command))
                .isInstanceOf(BadRequest.class)
                .hasMessageContaining("存在无效的成员用户");
    }

    @Test
    @WithSecurityPrincipal(userId = 1L, roleType = "SUPER_ADMIN")
    @DisplayName("updateAchievement: 应全量替换成员关联和外部协作者")
    void updateAchievement_shouldReplaceAssociations() {
        User member1 = UserFixture.member("2026005003").save(userRepository, passwordEncoder);
        User member2 = UserFixture.member("2026005004").save(userRepository, passwordEncoder);
        File file = createNormalImageFile("replace-img");
        AchievementResult created = achievementAppService.createAchievement(
                new AchievementCommands.CreateAchievementCommand(
                        "旧成就",
                        AchievementType.COMPETITION,
                        "竞赛",
                        LocalDate.of(2023, 7, 1),
                        AwardLevel.PROVINCIAL,
                        "二等奖",
                        file.getId(),
                        List.of(member1.getId()),
                        List.of("王五-外校")));

        AchievementResult updated = achievementAppService.updateAchievement(
                new AchievementCommands.UpdateAchievementCommand(
                        created.id(),
                        "新成就",
                        AchievementType.COMPETITION,
                        "竞赛",
                        LocalDate.of(2024, 9, 1),
                        AwardLevel.NATIONAL,
                        "一等奖",
                        file.getId(),
                        List.of(member2.getId()),
                        List.of("赵六-外校")));

        assertThat(updated.members())
                .extracting("userId")
                .containsExactly(member2.getId());
        assertThat(updated.externalMembers()).containsExactly("赵六-外校");

        Achievement persisted = achievementRepository.findById(created.id()).orElseThrow();
        assertThat(persisted.getMemberIds()).containsExactly(member2.getId());
        assertThat(persisted.getExternalMembers()).containsExactly("赵六-外校");
    }

    @Test
    @WithSecurityPrincipal(userId = 1L, roleType = "SUPER_ADMIN")
    @DisplayName("deleteAchievement: 应级联清理成员关联和外部协作者")
    void deleteAchievement_shouldCascadeAssociations() {
        User member = UserFixture.member("2026005005").save(userRepository, passwordEncoder);
        File file = createNormalImageFile("cascade-img");
        AchievementResult created = achievementAppService.createAchievement(
                new AchievementCommands.CreateAchievementCommand(
                        "待删除成就",
                        AchievementType.COMPETITION,
                        "竞赛",
                        LocalDate.of(2024, 4, 1),
                        AwardLevel.SCHOOL,
                        "三等奖",
                        file.getId(),
                        List.of(member.getId()),
                        List.of("外部-协作")));

        achievementAppService.deleteAchievement(created.id());

        assertThat(achievementRepository.findById(created.id())).isEmpty();
        assertThat(achievementRepository.findMembersByAchievementIds(List.of(created.id()))).isEmpty();
        assertThat(achievementRepository.findExternalMembersByAchievementIds(List.of(created.id()))).isEmpty();
        assertThat(achievementRepository.findByUserId(member.getId())).isEmpty();
    }

    @Test
    @WithSecurityPrincipal(userId = 1L, roleType = "MEMBER")
    @DisplayName("getMemberAchievements: 应按获奖日期倒序返回成员成就")
    void getMemberAchievements_shouldReturnOrderedList() {
        User member = UserFixture.member("2026005006").save(userRepository, passwordEncoder);
        User other = UserFixture.member("2026005007").save(userRepository, passwordEncoder);
        File file = createNormalImageFile("member-achievements-img");
        achievementAppService.createAchievement(
                new AchievementCommands.CreateAchievementCommand(
                        "较早成就",
                        AchievementType.COMPETITION,
                        "竞赛A",
                        LocalDate.of(2023, 5, 1),
                        AwardLevel.NATIONAL,
                        "一等奖",
                        file.getId(),
                        List.of(member.getId()),
                        null));
        achievementAppService.createAchievement(
                new AchievementCommands.CreateAchievementCommand(
                        "较晚成就",
                        AchievementType.PAPER,
                        "期刊B",
                        LocalDate.of(2024, 5, 1),
                        null,
                        null,
                        file.getId(),
                        List.of(member.getId()),
                        null));
        achievementAppService.createAchievement(
                new AchievementCommands.CreateAchievementCommand(
                        "他人成就",
                        AchievementType.PAPER,
                        "期刊C",
                        LocalDate.of(2025, 5, 1),
                        null,
                        null,
                        file.getId(),
                        List.of(other.getId()),
                        null));

        List<AchievementResult> results = achievementAppService.getMemberAchievements(member.getId());

        assertThat(results)
                .extracting(AchievementResult::title)
                .containsExactly("较晚成就", "较早成就");
        assertThat(results.get(0).members())
                .extracting("userId")
                .contains(member.getId());
    }

    @Test
    @WithSecurityPrincipal(userId = 1L, roleType = "MEMBER")
    @DisplayName("getMemberAchievements: 用户不存在应抛 DataNotFound")
    void getMemberAchievements_userNotFound_shouldThrowDataNotFound() {
        assertThatThrownBy(() -> achievementAppService.getMemberAchievements(99999L))
                .isInstanceOf(DataNotFound.class)
                .hasMessageContaining("成员不存在");
    }
}
