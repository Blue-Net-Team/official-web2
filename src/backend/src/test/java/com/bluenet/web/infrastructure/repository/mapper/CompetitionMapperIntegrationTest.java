package com.bluenet.web.infrastructure.repository.mapper;

import com.bluenet.web.BaseIntegrationTest;
import com.bluenet.web.domain.repository.CompetitionRepository;
import com.bluenet.web.infrastructure.repository.dataobject.CompetitionDO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("CompetitionMapper 集成测试")
class CompetitionMapperIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private CompetitionMapper competitionMapper;

    @Test
    @DisplayName("batchUpdateSortOrder: 应以单条 SQL 批量更新多条记录的排序号")
    void batchUpdateSortOrder_shouldUpdateMultipleRowsInSingleStatement() {
        CompetitionDO first = buildCompetition("赛事 A", 1);
        CompetitionDO second = buildCompetition("赛事 B", 2);
        CompetitionDO third = buildCompetition("赛事 C", 3);
        competitionMapper.insert(first);
        competitionMapper.insert(second);
        competitionMapper.insert(third);

        List<CompetitionRepository.SortItem> sortItems = List.of(
                new CompetitionRepository.SortItem(first.getId(), 30),
                new CompetitionRepository.SortItem(second.getId(), 10),
                new CompetitionRepository.SortItem(third.getId(), 20));

        competitionMapper.batchUpdateSortOrder(sortItems);

        assertThat(competitionMapper.selectById(first.getId()).getSortOrder()).isEqualTo(30);
        assertThat(competitionMapper.selectById(second.getId()).getSortOrder()).isEqualTo(10);
        assertThat(competitionMapper.selectById(third.getId()).getSortOrder()).isEqualTo(20);
    }

    private CompetitionDO buildCompetition(String name, int sortOrder) {
        return CompetitionDO.builder()
                .name(name)
                .sortOrder(sortOrder)
                .build();
    }
}
