package com.bluenet.web.infrastructure.repository.impl;

import com.bluenet.web.BaseIntegrationTest;
import com.bluenet.web.domain.model.entity.Competition;
import com.bluenet.web.domain.model.enumerate.AwardLevel;
import com.bluenet.web.domain.repository.CompetitionRepository;
import com.bluenet.web.domain.model.readmodel.CompetitionReadModel;
import com.bluenet.web.infrastructure.repository.dataobject.CompetitionDO;
import com.bluenet.web.infrastructure.repository.mapper.CompetitionMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * CompetitionRepositoryImpl 集成测试。
 */
@DisplayName("CompetitionRepositoryImpl 集成测试")
class CompetitionRepositoryImplIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private CompetitionRepository competitionRepository;

    @Autowired
    private CompetitionMapper competitionMapper;

    private final AtomicLong counter = new AtomicLong(1);

    private Competition createCompetition(String name, Integer sortOrder) {
        Competition competition = Competition.create(
                name,
                name + "简称",
                null,
                null,
                name + "简介",
                AwardLevel.NATIONAL,
                "10",
                name + "主办方",
                sortOrder);
        competitionRepository.save(competition);
        return competition;
    }

    @Test
    @DisplayName("save: 新竞赛应插入并回写ID")
    void save_newCompetition_shouldInsertAndReturnId() {
        Competition competition = createCompetition("蓝桥杯", 10);

        assertThat(competition.getId()).isNotNull();
        CompetitionDO dataObject = competitionMapper.selectById(competition.getId());
        assertThat(dataObject).isNotNull();
        assertThat(dataObject.getName()).isEqualTo("蓝桥杯");
        assertThat(dataObject.getSortOrder()).isEqualTo(10);
    }

    @Test
    @DisplayName("save: 已有竞赛应更新字段")
    void save_existingCompetition_shouldUpdateFields() {
        Competition competition = createCompetition("待更新竞赛", 5);
        competition.update("更新后竞赛", "新简称", null, null, "新简介", AwardLevel.PROVINCIAL, "11", "新主办方");
        competition.updateSortOrder(20);

        competitionRepository.save(competition);

        CompetitionDO updated = competitionMapper.selectById(competition.getId());
        assertThat(updated.getName()).isEqualTo("更新后竞赛");
        assertThat(updated.getLevel()).isEqualTo(AwardLevel.PROVINCIAL);
        assertThat(updated.getSortOrder()).isEqualTo(20);
    }

    @Test
    @DisplayName("findById: 存在返回实体，不存在返回空")
    void findById_shouldReturnOptional() {
        Competition competition = createCompetition("查询竞赛", 1);

        Optional<Competition> found = competitionRepository.findById(competition.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("查询竞赛");

        assertThat(competitionRepository.findById(-1L)).isEmpty();
    }

    @Test
    @DisplayName("findCompetitionsWithLimit: 应按排序返回指定数量")
    void findCompetitionsWithLimit_shouldReturnLimited() {
        createCompetition("竞赛1", 1);
        createCompetition("竞赛2", 3);
        createCompetition("竞赛3", 2);

        List<CompetitionReadModel> competitions = competitionRepository.findCompetitionsWithLimit(2);

        assertThat(competitions).hasSize(2);
    }

    @Test
    @DisplayName("findCompetitionsPage: 应分页返回竞赛")
    void findCompetitionsPage_shouldPaginate() {
        createCompetition("分页竞赛1", 1);
        createCompetition("分页竞赛2", 2);

        Page<CompetitionReadModel> page = competitionRepository.findCompetitionsPage(PageRequest.of(0, 10));

        assertThat(page.getTotalElements()).isGreaterThanOrEqualTo(2);
        assertThat(page.getContent()).hasSizeGreaterThanOrEqualTo(2);
    }

    @Test
    @DisplayName("existsById: 应正确判断竞赛是否存在")
    void existsById_shouldWork() {
        Competition competition = createCompetition("存在竞赛", 1);

        assertThat(competitionRepository.existsById(competition.getId())).isTrue();
        assertThat(competitionRepository.existsById(-1L)).isFalse();
    }

    @Test
    @DisplayName("findMaxSortOrder: 应返回当前最大排序号")
    void findMaxSortOrder_shouldReturnMax() {
        createCompetition("排序100", 100);
        createCompetition("排序200", 200);

        Integer maxSortOrder = competitionRepository.findMaxSortOrder();

        assertThat(maxSortOrder).isGreaterThanOrEqualTo(200);
    }

    @Test
    @DisplayName("batchUpdateSortOrder: 应批量更新排序号")
    void batchUpdateSortOrder_shouldUpdate() {
        Competition competition1 = createCompetition("批量1", 1);
        Competition competition2 = createCompetition("批量2", 2);

        competitionRepository.batchUpdateSortOrder(
                List.of(
                        new CompetitionRepository.SortItem(competition1.getId(), 100),
                        new CompetitionRepository.SortItem(competition2.getId(), 200)));

        CompetitionDO updated1 = competitionMapper.selectById(competition1.getId());
        CompetitionDO updated2 = competitionMapper.selectById(competition2.getId());
        assertThat(updated1.getSortOrder()).isEqualTo(100);
        assertThat(updated2.getSortOrder()).isEqualTo(200);
    }

    @Test
    @DisplayName("findAdjacent: 应查询相邻排序竞赛")
    void findAdjacent_shouldReturnAdjacent() {
        createCompetition("相邻上", 10);
        createCompetition("相邻中", 20);
        createCompetition("相邻下", 30);

        Optional<Competition> up = competitionRepository.findAdjacent(20, "UP");
        Optional<Competition> down = competitionRepository.findAdjacent(20, "DOWN");

        assertThat(up).isPresent();
        assertThat(up.get().getSortOrder()).isEqualTo(10);
        assertThat(down).isPresent();
        assertThat(down.get().getSortOrder()).isEqualTo(30);
    }

    @Test
    @DisplayName("deleteById: 应删除竞赛")
    void deleteById_shouldRemoveCompetition() {
        Competition competition = createCompetition("待删除竞赛", 1);
        Long competitionId = competition.getId();

        competitionRepository.deleteById(competitionId);

        assertThat(competitionMapper.selectById(competitionId)).isNull();
    }
}
