package com.bluenet.web.infrastructure.repository.mapper;

import com.bluenet.web.BaseIntegrationTest;
import com.bluenet.web.domain.model.enumerate.SoftwareResourceDirection;
import com.bluenet.web.domain.model.enumerate.SoftwareResourceStatus;
import com.bluenet.web.domain.repository.SoftwareResourceRepository;
import com.bluenet.web.infrastructure.repository.dataobject.SoftwareResourceDO;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("SoftwareResourceMapper 集成测试")
class SoftwareResourceMapperIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private SoftwareResourceMapper softwareResourceMapper;

    @Test
    @DisplayName("insert: 应能保存并回读软件资源")
    void insert_shouldPersistAndReadBack() {
        SoftwareResourceDO resource = SoftwareResourceDO.builder()
                .name("VS Code")
                .direction(SoftwareResourceDirection.GENERAL)
                .category("IDE")
                .description("编辑器")
                .externalUrl("https://code.visualstudio.com/")
                .sortOrder(10)
                .status(SoftwareResourceStatus.ACTIVE)
                .build();

        softwareResourceMapper.insert(resource);

        assertThat(resource.getId()).isNotNull();
        SoftwareResourceDO read = softwareResourceMapper.selectById(resource.getId());
        assertThat(read).isNotNull();
        assertThat(read.getName()).isEqualTo("VS Code");
        assertThat(read.getDirection()).isEqualTo(SoftwareResourceDirection.GENERAL);
        assertThat(read.getStatus()).isEqualTo(SoftwareResourceStatus.ACTIVE);
    }

    @Test
    @DisplayName("selectActiveByDirection: 应返回指定方向及通用方向且已启用的资源")
    void selectActiveByDirection_shouldReturnActiveResourcesForDirectionAndGeneral() {
        insertResource("CV Tool", SoftwareResourceDirection.COMPUTER_VISION, 1, SoftwareResourceStatus.ACTIVE);
        insertResource("CV Disabled", SoftwareResourceDirection.COMPUTER_VISION, 2, SoftwareResourceStatus.DISABLED);
        insertResource("General Tool", SoftwareResourceDirection.GENERAL, 3, SoftwareResourceStatus.ACTIVE);

        Page<SoftwareResourceDO> page = new Page<>(1, 10);
        List<SoftwareResourceDirection> directions = List.of(
                SoftwareResourceDirection.COMPUTER_VISION,
                SoftwareResourceDirection.GENERAL);
        var result = softwareResourceMapper.selectActiveByDirection(
                page,
                directions,
                SoftwareResourceStatus.ACTIVE,
                null);

        List<SoftwareResourceDO> records = result.getRecords();
        assertThat(records).hasSize(2);
        assertThat(records).extracting(SoftwareResourceDO::getName)
                .containsExactlyInAnyOrder("CV Tool", "General Tool");
    }

    @Test
    @DisplayName("selectActiveByDirection: 关键字应匹配名称、分类或描述")
    void selectActiveByDirection_withKeyword_shouldMatchNameCategoryOrDescription() {
        insertResource("CV Tool", SoftwareResourceDirection.COMPUTER_VISION, 1, SoftwareResourceStatus.ACTIVE);
        insertResource("CV Disabled", SoftwareResourceDirection.COMPUTER_VISION, 2, SoftwareResourceStatus.DISABLED);
        insertResource("General Tool", SoftwareResourceDirection.GENERAL, 3, SoftwareResourceStatus.ACTIVE);

        Page<SoftwareResourceDO> page = new Page<>(1, 10);
        var result = softwareResourceMapper.selectActiveByDirection(
                page,
                null,
                SoftwareResourceStatus.ACTIVE,
                "tool");

        List<SoftwareResourceDO> records = result.getRecords();
        assertThat(records).hasSize(2);
        assertThat(records).extracting(SoftwareResourceDO::getName)
                .containsExactlyInAnyOrder("CV Tool", "General Tool");
    }

    @Test
    @DisplayName("selectActiveByDirection: direction 为 null 时应返回所有已启用资源")
    void selectActiveByDirection_nullDirection_shouldReturnAllActive() {
        insertResource("CV Tool", SoftwareResourceDirection.COMPUTER_VISION, 1, SoftwareResourceStatus.ACTIVE);
        insertResource("General Tool", SoftwareResourceDirection.GENERAL, 2, SoftwareResourceStatus.ACTIVE);
        insertResource("Disabled Tool", SoftwareResourceDirection.EMBEDDED, 3, SoftwareResourceStatus.DISABLED);

        Page<SoftwareResourceDO> page = new Page<>(1, 10);
        var result = softwareResourceMapper.selectActiveByDirection(
                page,
                null,
                SoftwareResourceStatus.ACTIVE,
                null);

        List<SoftwareResourceDO> records = result.getRecords();
        assertThat(records).hasSize(2);
        assertThat(records).extracting(SoftwareResourceDO::getName)
                .containsExactlyInAnyOrder("CV Tool", "General Tool");
    }

    @Test
    @DisplayName("selectAllForAdmin: 应返回所有资源并按 sort_order 排序")
    void selectAllForAdmin_shouldReturnAllOrdered() {
        insertResource("B", SoftwareResourceDirection.GENERAL, 2, SoftwareResourceStatus.ACTIVE);
        insertResource("A", SoftwareResourceDirection.GENERAL, 1, SoftwareResourceStatus.DISABLED);

        Page<SoftwareResourceDO> page = new Page<>(1, 10);
        var result = softwareResourceMapper.selectAllForAdmin(page);

        assertThat(result.getRecords()).hasSize(2);
        assertThat(result.getRecords().get(0).getName()).isEqualTo("A");
        assertThat(result.getRecords().get(1).getName()).isEqualTo("B");
    }

    @Test
    @DisplayName("batchUpdateSortOrder: 应以单条 SQL 批量更新多条记录的排序号")
    void batchUpdateSortOrder_shouldUpdateMultipleRowsInSingleStatement() {
        SoftwareResourceDO first = buildResource(
                "Git",
                SoftwareResourceDirection.GENERAL,
                1,
                SoftwareResourceStatus.ACTIVE);
        SoftwareResourceDO second = buildResource(
                "PyCharm",
                SoftwareResourceDirection.GENERAL,
                2,
                SoftwareResourceStatus.ACTIVE);
        SoftwareResourceDO third = buildResource(
                "SolidWorks",
                SoftwareResourceDirection.STRUCTURAL_DESIGN,
                3,
                SoftwareResourceStatus.ACTIVE);
        softwareResourceMapper.insert(first);
        softwareResourceMapper.insert(second);
        softwareResourceMapper.insert(third);

        List<SoftwareResourceRepository.SortItem> sortItems = List.of(
                new SoftwareResourceRepository.SortItem(first.getId(), 30),
                new SoftwareResourceRepository.SortItem(second.getId(), 10),
                new SoftwareResourceRepository.SortItem(third.getId(), 20));

        softwareResourceMapper.batchUpdateSortOrder(sortItems);

        assertThat(softwareResourceMapper.selectById(first.getId()).getSortOrder()).isEqualTo(30);
        assertThat(softwareResourceMapper.selectById(second.getId()).getSortOrder()).isEqualTo(10);
        assertThat(softwareResourceMapper.selectById(third.getId()).getSortOrder()).isEqualTo(20);
    }

    @Test
    @DisplayName("selectMaxSortOrder: 无记录时返回 null，有记录时返回最大排序号")
    void selectMaxSortOrder_shouldReturnMax() {
        assertThat(softwareResourceMapper.selectMaxSortOrder()).isNull();

        insertResource("A", SoftwareResourceDirection.GENERAL, 3, SoftwareResourceStatus.ACTIVE);
        insertResource("B", SoftwareResourceDirection.GENERAL, 8, SoftwareResourceStatus.ACTIVE);
        insertResource("C", SoftwareResourceDirection.GENERAL, 5, SoftwareResourceStatus.DISABLED);

        assertThat(softwareResourceMapper.selectMaxSortOrder()).isEqualTo(8);
    }

    private SoftwareResourceDO buildResource(String name, SoftwareResourceDirection direction, int sortOrder,
            SoftwareResourceStatus status) {
        return SoftwareResourceDO.builder()
                .name(name)
                .direction(direction)
                .category("tool")
                .description("desc")
                .externalUrl("https://example.com/" + name)
                .sortOrder(sortOrder)
                .status(status)
                .build();
    }

    private void insertResource(String name, SoftwareResourceDirection direction, int sortOrder,
            SoftwareResourceStatus status) {
        softwareResourceMapper.insert(buildResource(name, direction, sortOrder, status));
    }
}
