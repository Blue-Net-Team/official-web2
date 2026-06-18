package com.bluenet.web.infrastructure.repository.mapper;

import com.bluenet.web.BaseIntegrationTest;
import com.bluenet.web.domain.model.enumerate.Direction;
import com.bluenet.web.domain.model.enumerate.SoftwareResourceStatus;
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
                .direction(Direction.GENERAL)
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
        assertThat(read.getDirection()).isEqualTo(Direction.GENERAL);
        assertThat(read.getStatus()).isEqualTo(SoftwareResourceStatus.ACTIVE);
    }

    @Test
    @DisplayName("selectActiveByDirection: 应只返回指定方向且已启用的资源")
    void selectActiveByDirection_shouldReturnActiveResourcesForDirection() {
        insertResource("CV Tool", Direction.COMPUTER_VISION, 1, SoftwareResourceStatus.ACTIVE);
        insertResource("CV Disabled", Direction.COMPUTER_VISION, 2, SoftwareResourceStatus.DISABLED);
        insertResource("General Tool", Direction.GENERAL, 3, SoftwareResourceStatus.ACTIVE);

        Page<SoftwareResourceDO> page = new Page<>(1, 10);
        var result = softwareResourceMapper.selectActiveByDirection(page, Direction.COMPUTER_VISION);

        List<SoftwareResourceDO> records = result.getRecords();
        assertThat(records).hasSize(1);
        assertThat(records.get(0).getName()).isEqualTo("CV Tool");
    }

    @Test
    @DisplayName("selectActiveByDirection: direction 为 null 时应返回所有已启用资源")
    void selectActiveByDirection_nullDirection_shouldReturnAllActive() {
        insertResource("CV Tool", Direction.COMPUTER_VISION, 1, SoftwareResourceStatus.ACTIVE);
        insertResource("General Tool", Direction.GENERAL, 2, SoftwareResourceStatus.ACTIVE);
        insertResource("Disabled Tool", Direction.EMBEDDED, 3, SoftwareResourceStatus.DISABLED);

        Page<SoftwareResourceDO> page = new Page<>(1, 10);
        var result = softwareResourceMapper.selectActiveByDirection(page, null);

        List<SoftwareResourceDO> records = result.getRecords();
        assertThat(records).hasSize(2);
        assertThat(records).extracting(SoftwareResourceDO::getName)
                .containsExactlyInAnyOrder("CV Tool", "General Tool");
    }

    @Test
    @DisplayName("selectAllForAdmin: 应返回所有资源并按 sort_order 排序")
    void selectAllForAdmin_shouldReturnAllOrdered() {
        insertResource("B", Direction.GENERAL, 2, SoftwareResourceStatus.ACTIVE);
        insertResource("A", Direction.GENERAL, 1, SoftwareResourceStatus.DISABLED);

        Page<SoftwareResourceDO> page = new Page<>(1, 10);
        var result = softwareResourceMapper.selectAllForAdmin(page);

        assertThat(result.getRecords()).hasSize(2);
        assertThat(result.getRecords().get(0).getName()).isEqualTo("A");
        assertThat(result.getRecords().get(1).getName()).isEqualTo("B");
    }

    private void insertResource(String name, Direction direction, int sortOrder,
            SoftwareResourceStatus status) {
        SoftwareResourceDO resource = SoftwareResourceDO.builder()
                .name(name)
                .direction(direction)
                .category("tool")
                .description("desc")
                .externalUrl("https://example.com/" + name)
                .sortOrder(sortOrder)
                .status(status)
                .build();
        softwareResourceMapper.insert(resource);
    }
}
