package com.bluenet.web.application.service.impl;

import com.bluenet.web.BaseIntegrationTest;
import com.bluenet.web.application.command.competition.CompetitionCommands;
import com.bluenet.web.application.result.competition.CompetitionResult;
import com.bluenet.web.application.service.CompetitionAppService;
import com.bluenet.web.domain.exception.BadRequest;
import com.bluenet.web.domain.exception.DataNotFound;
import com.bluenet.web.domain.model.entity.File;
import com.bluenet.web.domain.model.enumerate.AwardLevel;
import com.bluenet.web.domain.model.enumerate.FileType;
import com.bluenet.web.domain.model.readmodel.CompetitionReadModel;
import com.bluenet.web.domain.repository.CompetitionRepository;
import com.bluenet.web.domain.repository.FileRepository;
import com.bluenet.web.infrastructure.security.principal.WithSecurityPrincipal;
import com.bluenet.web.infrastructure.security.util.UserCTX;
import com.bluenet.web.testsupport.fixture.FileFixture;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * CompetitionAppServiceImpl 集成测试。
 *
 * <p>
 * 验证竞赛应用服务的创建、查询、更新、删除及排序调整逻辑，同时覆盖文件校验、分页参数钳位等分支。
 * </p>
 */
@DisplayName("CompetitionAppServiceImpl 集成测试")
class CompetitionAppServiceImplIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private CompetitionAppService competitionAppService;

    @Autowired
    private CompetitionRepository competitionRepository;

    @Autowired
    private FileRepository fileRepository;

    @AfterEach
    void cleanupSecurityContext() {
        UserCTX.clear();
    }

    private File createNormalImage(String name) {
        return FileFixture.save(fileRepository, name, FileType.NORMAL_IMG);
    }

    private CompetitionCommands.CreateCompetitionCommand createCommand(Long logoFileId, Long coverFileId) {
        return createCommand("蓝网杯", logoFileId, coverFileId);
    }

    private CompetitionCommands.CreateCompetitionCommand createCommand(String name, Long logoFileId, Long coverFileId) {
        return new CompetitionCommands.CreateCompetitionCommand(
                name,
                name,
                logoFileId,
                coverFileId,
                "竞赛摘要",
                AwardLevel.PROVINCIAL,
                "10月",
                "蓝网团队");
    }

    private CompetitionCommands.CreateCompetitionCommand createCommand(File logo, File cover) {
        return createCommand(logo.getId(), cover.getId());
    }

    @Test
    @WithSecurityPrincipal(userId = 1L, roleType = "SUPER_ADMIN")
    @DisplayName("createCompetition: 使用 NORMAL_IMG 文件创建竞赛，sortOrder 默认为 max+1")
    void createCompetition_withValidFiles_shouldCreateWithNextSortOrder() {
        File logo = createNormalImage("competition-logo");
        File cover = createNormalImage("competition-cover");

        CompetitionResult first = competitionAppService
                .createCompetition(createCommand("竞赛A", logo.getId(), cover.getId()));

        assertThat(first).isNotNull();
        assertThat(first.id()).isNotNull();
        assertThat(first.sortOrder()).isEqualTo(1);

        File logo2 = createNormalImage("competition-logo-2");
        File cover2 = createNormalImage("competition-cover-2");

        CompetitionResult second = competitionAppService
                .createCompetition(createCommand("竞赛B", logo2.getId(), cover2.getId()));

        assertThat(second.sortOrder()).isEqualTo(2);
        assertThat(competitionRepository.findById(second.id()))
                .isPresent()
                .hasValueSatisfying(competition -> assertThat(competition.getSortOrder()).isEqualTo(2));
    }

    @Test
    @WithSecurityPrincipal(userId = 1L, roleType = "SUPER_ADMIN")
    @DisplayName("createCompetition: Logo 文件不存在应抛 DataNotFound")
    void createCompetition_logoNotFound_shouldThrowDataNotFound() {
        File cover = createNormalImage("cover-without-logo");
        CompetitionCommands.CreateCompetitionCommand command = new CompetitionCommands.CreateCompetitionCommand(
                "蓝网杯",
                "蓝网杯",
                99999L,
                cover.getId(),
                "竞赛摘要",
                AwardLevel.PROVINCIAL,
                "10月",
                "蓝网团队");

        assertThatThrownBy(() -> competitionAppService.createCompetition(command))
                .isInstanceOf(DataNotFound.class)
                .hasMessageContaining("Logo文件不存在");
    }

    @Test
    @WithSecurityPrincipal(userId = 1L, roleType = "SUPER_ADMIN")
    @DisplayName("createCompetition: 封面文件类型不匹配应抛 BadRequest")
    void createCompetition_coverTypeMismatch_shouldThrowBadRequest() {
        File logo = createNormalImage("logo-for-cover-test");
        File cover = FileFixture.save(fileRepository, "work-cover", FileType.WORK);
        CompetitionCommands.CreateCompetitionCommand command = createCommand(logo.getId(), cover.getId());

        assertThatThrownBy(() -> competitionAppService.createCompetition(command))
                .isInstanceOf(BadRequest.class)
                .hasMessageContaining("封面文件类型不匹配");
    }

    @Test
    @WithSecurityPrincipal(userId = 1L, roleType = "MEMBER")
    @DisplayName("getCompetitionResponseList: limit 应被钳制在 [1,50]")
    void getCompetitionResponseList_shouldClampLimitBetweenOneAndFifty() {
        File logo = createNormalImage("list-logo");
        File cover = createNormalImage("list-cover");
        competitionAppService.createCompetition(createCommand("列表竞赛A", logo.getId(), cover.getId()));
        competitionAppService.createCompetition(
                createCommand(
                        "列表竞赛B",
                        createNormalImage("list-logo-2").getId(),
                        createNormalImage("list-cover-2").getId()));
        competitionAppService.createCompetition(
                createCommand(
                        "列表竞赛C",
                        createNormalImage("list-logo-3").getId(),
                        createNormalImage("list-cover-3").getId()));

        List<CompetitionReadModel> lowerBound = competitionAppService.getCompetitionResponseList(0);
        List<CompetitionReadModel> limited = competitionAppService.getCompetitionResponseList(2);
        List<CompetitionReadModel> upperBound = competitionAppService.getCompetitionResponseList(100);

        assertThat(lowerBound).hasSize(1);
        assertThat(limited).hasSize(2);
        assertThat(upperBound).hasSize(3);
    }

    @Test
    @WithSecurityPrincipal(userId = 1L, roleType = "MEMBER")
    @DisplayName("getCompetitionPage: 应返回分页结果")
    void getCompetitionPage_shouldReturnPagedResults() {
        for (int i = 0; i < 3; i++) {
            competitionAppService.createCompetition(
                    createCommand(
                            "分页竞赛" + i,
                            createNormalImage("page-logo-" + i).getId(),
                            createNormalImage("page-cover-" + i).getId()));
        }

        Page<CompetitionReadModel> firstPage = competitionAppService.getCompetitionPage(0, 2);

        assertThat(firstPage.getNumber()).isZero();
        assertThat(firstPage.getSize()).isEqualTo(2);
        assertThat(firstPage.getTotalElements()).isEqualTo(3L);
        assertThat(firstPage.getContent()).hasSize(2);

        Page<CompetitionReadModel> secondPage = competitionAppService.getCompetitionPage(1, 2);

        assertThat(secondPage.getContent()).hasSize(1);
    }

    @Test
    @WithSecurityPrincipal(userId = 1L, roleType = "SUPER_ADMIN")
    @DisplayName("updateCompetition: 应更新并持久化竞赛信息")
    void updateCompetition_shouldUpdateAndPersist() {
        CompetitionResult created = competitionAppService.createCompetition(
                createCommand(
                        createNormalImage("update-old-logo"),
                        createNormalImage("update-old-cover")));
        File newLogo = createNormalImage("update-new-logo");
        File newCover = createNormalImage("update-new-cover");
        CompetitionCommands.UpdateCompetitionCommand command = new CompetitionCommands.UpdateCompetitionCommand(
                created.id(),
                "蓝网杯新名称",
                "新简称",
                newLogo.getId(),
                newCover.getId(),
                "新摘要",
                AwardLevel.NATIONAL,
                "11月",
                "新主办方");

        CompetitionResult result = competitionAppService.updateCompetition(command);

        assertThat(result.name()).isEqualTo("蓝网杯新名称");
        assertThat(result.shortName()).isEqualTo("新简称");
        assertThat(result.summary()).isEqualTo("新摘要");
        assertThat(result.level()).isEqualTo(AwardLevel.NATIONAL.getValue());
        assertThat(result.month()).isEqualTo("11月");
        assertThat(result.organizer()).isEqualTo("新主办方");
        assertThat(result.logoFileId()).isEqualTo(newLogo.getId());
        assertThat(result.coverFileId()).isEqualTo(newCover.getId());
        assertThat(competitionRepository.findById(created.id()))
                .isPresent()
                .hasValueSatisfying(competition -> assertThat(competition.getName()).isEqualTo("蓝网杯新名称"));
    }

    @Test
    @WithSecurityPrincipal(userId = 1L, roleType = "SUPER_ADMIN")
    @DisplayName("updateCompetition: 不存在的 id 应抛 DataNotFound")
    void updateCompetition_notFound_shouldThrowDataNotFound() {
        File logo = createNormalImage("update-notfound-logo");
        File cover = createNormalImage("update-notfound-cover");
        CompetitionCommands.UpdateCompetitionCommand command = new CompetitionCommands.UpdateCompetitionCommand(
                99999L,
                "名称",
                "简称",
                logo.getId(),
                cover.getId(),
                "摘要",
                AwardLevel.SCHOOL,
                "12月",
                "主办方");

        assertThatThrownBy(() -> competitionAppService.updateCompetition(command))
                .isInstanceOf(DataNotFound.class)
                .hasMessageContaining("竞赛不存在");
    }

    @Test
    @WithSecurityPrincipal(userId = 1L, roleType = "SUPER_ADMIN")
    @DisplayName("deleteCompetition: 应删除竞赛")
    void deleteCompetition_shouldDelete() {
        CompetitionResult created = competitionAppService.createCompetition(
                createCommand(
                        createNormalImage("delete-logo"),
                        createNormalImage("delete-cover")));

        competitionAppService.deleteCompetition(created.id());

        assertThat(competitionRepository.findById(created.id())).isEmpty();
    }

    @Test
    @WithSecurityPrincipal(userId = 1L, roleType = "SUPER_ADMIN")
    @DisplayName("deleteCompetition: 不存在的 id 应抛 DataNotFound")
    void deleteCompetition_notFound_shouldThrowDataNotFound() {
        assertThatThrownBy(() -> competitionAppService.deleteCompetition(99999L))
                .isInstanceOf(DataNotFound.class)
                .hasMessageContaining("竞赛不存在");
    }

    @Test
    @WithSecurityPrincipal(userId = 1L, roleType = "SUPER_ADMIN")
    @DisplayName("updateSortOrder: 应更新排序号")
    void updateSortOrder_shouldUpdateSortOrder() {
        CompetitionResult created = competitionAppService.createCompetition(
                createCommand(
                        createNormalImage("sort-logo"),
                        createNormalImage("sort-cover")));

        competitionAppService.updateSortOrder(
                new CompetitionCommands.UpdateSortOrderCommand(created.id(), 999));

        assertThat(competitionRepository.findById(created.id()))
                .isPresent()
                .hasValueSatisfying(competition -> assertThat(competition.getSortOrder()).isEqualTo(999));
    }

    @Test
    @WithSecurityPrincipal(userId = 1L, roleType = "SUPER_ADMIN")
    @DisplayName("batchUpdateSortOrder: 应批量更新多个竞赛的排序号")
    void batchUpdateSortOrder_shouldUpdateMultiple() {
        CompetitionResult first = competitionAppService.createCompetition(
                createCommand(
                        "批量竞赛A",
                        createNormalImage("batch-logo-1").getId(),
                        createNormalImage("batch-cover-1").getId()));
        CompetitionResult second = competitionAppService.createCompetition(
                createCommand(
                        "批量竞赛B",
                        createNormalImage("batch-logo-2").getId(),
                        createNormalImage("batch-cover-2").getId()));

        competitionAppService.batchUpdateSortOrder(
                new CompetitionCommands.BatchUpdateSortOrderCommand(List.of(
                        new CompetitionCommands.SortItemCommand(first.id(), 100),
                        new CompetitionCommands.SortItemCommand(second.id(), 200))));

        assertThat(competitionRepository.findById(first.id()))
                .isPresent()
                .hasValueSatisfying(competition -> assertThat(competition.getSortOrder()).isEqualTo(100));
        assertThat(competitionRepository.findById(second.id()))
                .isPresent()
                .hasValueSatisfying(competition -> assertThat(competition.getSortOrder()).isEqualTo(200));
    }

    @Test
    @WithSecurityPrincipal(userId = 1L, roleType = "SUPER_ADMIN")
    @DisplayName("batchUpdateSortOrder: 包含不存在的 id 应抛 IllegalArgumentException")
    void batchUpdateSortOrder_notFound_shouldThrowIllegalArgument() {
        CompetitionResult existing = competitionAppService.createCompetition(
                createCommand(
                        createNormalImage("batch-existing-logo"),
                        createNormalImage("batch-existing-cover")));

        CompetitionCommands.BatchUpdateSortOrderCommand command = new CompetitionCommands.BatchUpdateSortOrderCommand(
                List.of(
                        new CompetitionCommands.SortItemCommand(existing.id(), 50),
                        new CompetitionCommands.SortItemCommand(99999L, 100)));

        assertThatThrownBy(() -> competitionAppService.batchUpdateSortOrder(command))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("竞赛不存在");
    }

    @Test
    @WithSecurityPrincipal(userId = 1L, roleType = "SUPER_ADMIN")
    @DisplayName("moveCompetition: UP 应与前一个竞赛交换排序号")
    void moveCompetition_up_shouldSwapWithPrevious() {
        CompetitionResult first = competitionAppService.createCompetition(
                createCommand(
                        "移动竞赛A",
                        createNormalImage("move-up-logo-1").getId(),
                        createNormalImage("move-up-cover-1").getId()));
        CompetitionResult second = competitionAppService.createCompetition(
                createCommand(
                        "移动竞赛B",
                        createNormalImage("move-up-logo-2").getId(),
                        createNormalImage("move-up-cover-2").getId()));
        CompetitionResult third = competitionAppService.createCompetition(
                createCommand(
                        "移动竞赛C",
                        createNormalImage("move-up-logo-3").getId(),
                        createNormalImage("move-up-cover-3").getId()));

        competitionAppService.moveCompetition(
                new CompetitionCommands.MoveCompetitionCommand(second.id(), "UP"));

        assertThat(competitionRepository.findById(second.id()))
                .isPresent()
                .hasValueSatisfying(competition -> assertThat(competition.getSortOrder()).isEqualTo(1));
        assertThat(competitionRepository.findById(first.id()))
                .isPresent()
                .hasValueSatisfying(competition -> assertThat(competition.getSortOrder()).isEqualTo(2));
        assertThat(competitionRepository.findById(third.id()))
                .isPresent()
                .hasValueSatisfying(competition -> assertThat(competition.getSortOrder()).isEqualTo(3));
    }

    @Test
    @WithSecurityPrincipal(userId = 1L, roleType = "SUPER_ADMIN")
    @DisplayName("moveCompetition: DOWN 应与后一个竞赛交换排序号")
    void moveCompetition_down_shouldSwapWithNext() {
        CompetitionResult first = competitionAppService.createCompetition(
                createCommand(
                        "下移竞赛A",
                        createNormalImage("move-down-logo-1").getId(),
                        createNormalImage("move-down-cover-1").getId()));
        CompetitionResult second = competitionAppService.createCompetition(
                createCommand(
                        "下移竞赛B",
                        createNormalImage("move-down-logo-2").getId(),
                        createNormalImage("move-down-cover-2").getId()));
        CompetitionResult third = competitionAppService.createCompetition(
                createCommand(
                        "下移竞赛C",
                        createNormalImage("move-down-logo-3").getId(),
                        createNormalImage("move-down-cover-3").getId()));

        competitionAppService.moveCompetition(
                new CompetitionCommands.MoveCompetitionCommand(second.id(), "DOWN"));

        assertThat(competitionRepository.findById(second.id()))
                .isPresent()
                .hasValueSatisfying(competition -> assertThat(competition.getSortOrder()).isEqualTo(3));
        assertThat(competitionRepository.findById(third.id()))
                .isPresent()
                .hasValueSatisfying(competition -> assertThat(competition.getSortOrder()).isEqualTo(2));
        assertThat(competitionRepository.findById(first.id()))
                .isPresent()
                .hasValueSatisfying(competition -> assertThat(competition.getSortOrder()).isEqualTo(1));
    }

    @Test
    @WithSecurityPrincipal(userId = 1L, roleType = "SUPER_ADMIN")
    @DisplayName("moveCompetition: 非法方向应抛 IllegalArgumentException")
    void moveCompetition_invalidDirection_shouldThrowIllegalArgument() {
        CompetitionResult created = competitionAppService.createCompetition(
                createCommand(
                        createNormalImage("move-invalid-logo"),
                        createNormalImage("move-invalid-cover")));

        assertThatThrownBy(
                () -> competitionAppService.moveCompetition(
                        new CompetitionCommands.MoveCompetitionCommand(created.id(), "LEFT")))
                                .isInstanceOf(IllegalArgumentException.class)
                                .hasMessageContaining("移动方向必须是 UP 或 DOWN");
    }

    @Test
    @WithSecurityPrincipal(userId = 1L, roleType = "SUPER_ADMIN")
    @DisplayName("moveCompetition: 已是第一个时 UP 应抛 IllegalArgumentException")
    void moveCompetition_upAtFirst_shouldThrowIllegalArgument() {
        CompetitionResult first = competitionAppService.createCompetition(
                createCommand(
                        "首竞赛A",
                        createNormalImage("move-first-logo-1").getId(),
                        createNormalImage("move-first-cover-1").getId()));
        competitionAppService.createCompetition(
                createCommand(
                        "首竞赛B",
                        createNormalImage("move-first-logo-2").getId(),
                        createNormalImage("move-first-cover-2").getId()));

        assertThatThrownBy(
                () -> competitionAppService.moveCompetition(
                        new CompetitionCommands.MoveCompetitionCommand(first.id(), "UP")))
                                .isInstanceOf(IllegalArgumentException.class)
                                .hasMessageContaining("已是第一个");
    }

    @Test
    @WithSecurityPrincipal(userId = 1L, roleType = "SUPER_ADMIN")
    @DisplayName("createCompetition: 竞赛名称重复应抛 BadRequest")
    void createCompetition_duplicateName_shouldThrowBadRequest() {
        File logo = createNormalImage("dup-logo-1");
        File cover = createNormalImage("dup-cover-1");
        competitionAppService.createCompetition(createCommand(logo, cover));

        File logo2 = createNormalImage("dup-logo-2");
        File cover2 = createNormalImage("dup-cover-2");
        CompetitionCommands.CreateCompetitionCommand duplicateCommand = createCommand(logo2, cover2);

        assertThatThrownBy(() -> competitionAppService.createCompetition(duplicateCommand))
                .isInstanceOf(BadRequest.class)
                .hasMessageContaining("竞赛名称已存在");
    }

    @Test
    @WithSecurityPrincipal(userId = 1L, roleType = "SUPER_ADMIN")
    @DisplayName("updateCompetition: 竞赛名称重复应抛 BadRequest")
    void updateCompetition_duplicateName_shouldThrowBadRequest() {
        CompetitionResult first = competitionAppService.createCompetition(
                createCommand(
                        createNormalImage("update-dup-logo-1"),
                        createNormalImage("update-dup-cover-1")));
        CompetitionResult second = competitionAppService.createCompetition(
                new CompetitionCommands.CreateCompetitionCommand(
                        "另一个竞赛",
                        "另一个竞赛",
                        createNormalImage("update-dup-logo-2").getId(),
                        createNormalImage("update-dup-cover-2").getId(),
                        "摘要",
                        AwardLevel.PROVINCIAL,
                        "10月",
                        "主办方"));

        CompetitionCommands.UpdateCompetitionCommand command = new CompetitionCommands.UpdateCompetitionCommand(
                second.id(),
                "蓝网杯",
                "新简称",
                null,
                null,
                "新摘要",
                AwardLevel.NATIONAL,
                "11月",
                "新主办方");

        assertThatThrownBy(() -> competitionAppService.updateCompetition(command))
                .isInstanceOf(BadRequest.class)
                .hasMessageContaining("竞赛名称已存在");
    }

    @Test
    @WithSecurityPrincipal(userId = 1L, roleType = "SUPER_ADMIN")
    @DisplayName("updateCompetition: 名称未修改时不应抛重复异常")
    void updateCompetition_sameName_shouldNotThrow() {
        CompetitionResult created = competitionAppService.createCompetition(
                createCommand(
                        createNormalImage("same-name-logo"),
                        createNormalImage("same-name-cover")));

        CompetitionCommands.UpdateCompetitionCommand command = new CompetitionCommands.UpdateCompetitionCommand(
                created.id(),
                "蓝网杯",
                "更新后的简称",
                null,
                null,
                "更新后的摘要",
                AwardLevel.NATIONAL,
                "12月",
                "更新后的主办方");

        CompetitionResult result = competitionAppService.updateCompetition(command);

        assertThat(result.name()).isEqualTo("蓝网杯");
        assertThat(result.shortName()).isEqualTo("更新后的简称");
    }

    @Test
    @WithSecurityPrincipal(userId = 1L, roleType = "SUPER_ADMIN")
    @DisplayName("moveCompetition: 已是最后一个时 DOWN 应抛 IllegalArgumentException")
    void moveCompetition_downAtLast_shouldThrowIllegalArgument() {
        competitionAppService.createCompetition(
                createCommand(
                        "末竞赛A",
                        createNormalImage("move-last-logo-1").getId(),
                        createNormalImage("move-last-cover-1").getId()));
        CompetitionResult last = competitionAppService.createCompetition(
                createCommand(
                        "末竞赛B",
                        createNormalImage("move-last-logo-2").getId(),
                        createNormalImage("move-last-cover-2").getId()));

        assertThatThrownBy(
                () -> competitionAppService.moveCompetition(
                        new CompetitionCommands.MoveCompetitionCommand(last.id(), "DOWN")))
                                .isInstanceOf(IllegalArgumentException.class)
                                .hasMessageContaining("已是最后一个");
    }
}
