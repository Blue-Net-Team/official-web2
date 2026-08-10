package com.bluenet.web.infrastructure.repository.impl;

import com.bluenet.web.BaseIntegrationTest;
import com.bluenet.web.domain.model.entity.SoftwareResource;
import com.bluenet.web.domain.model.enumerate.SoftwareResourceDirection;
import com.bluenet.web.domain.model.enumerate.SoftwareResourceStatus;
import com.bluenet.web.domain.repository.SoftwareResourceRepository;
import com.bluenet.web.infrastructure.repository.dataobject.SoftwareResourceDO;
import com.bluenet.web.infrastructure.repository.mapper.SoftwareResourceMapper;
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
 * SoftwareResourceRepositoryImpl 集成测试。
 */
@DisplayName("SoftwareResourceRepositoryImpl 集成测试")
class SoftwareResourceRepositoryImplIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private SoftwareResourceRepository softwareResourceRepository;

    @Autowired
    private SoftwareResourceMapper softwareResourceMapper;

    private final AtomicLong counter = new AtomicLong(1);

    private SoftwareResource createResource(String name, SoftwareResourceDirection direction, Integer sortOrder,
            SoftwareResourceStatus status) {
        SoftwareResource resource = SoftwareResource.create(
                name,
                direction,
                "工具",
                name + "描述",
                "http://example.com/" + name,
                sortOrder);
        if (status == SoftwareResourceStatus.DISABLED) {
            resource.update(name, direction, "工具", name + "描述", "http://example.com/" + name, sortOrder, status);
        }
        softwareResourceRepository.save(resource);
        return resource;
    }

    @Test
    @DisplayName("save: 新软件资源应插入并回写ID")
    void save_newResource_shouldInsertAndReturnId() {
        SoftwareResource resource = createResource(
                "VS Code",
                SoftwareResourceDirection.COMPUTER_VISION,
                10,
                SoftwareResourceStatus.ACTIVE);

        assertThat(resource.getId()).isNotNull();
        SoftwareResourceDO dataObject = softwareResourceMapper.selectById(resource.getId());
        assertThat(dataObject).isNotNull();
        assertThat(dataObject.getName()).isEqualTo("VS Code");
        assertThat(dataObject.getDirection()).isEqualTo(SoftwareResourceDirection.COMPUTER_VISION);
    }

    @Test
    @DisplayName("save: 已有软件资源应更新字段")
    void save_existingResource_shouldUpdateFields() {
        SoftwareResource resource = createResource(
                "旧软件",
                SoftwareResourceDirection.EMBEDDED,
                5,
                SoftwareResourceStatus.ACTIVE);
        resource.update(
                "新软件",
                SoftwareResourceDirection.STRUCTURAL_DESIGN,
                "设计工具",
                "新描述",
                "http://example.com/new",
                20,
                SoftwareResourceStatus.DISABLED);

        softwareResourceRepository.save(resource);

        SoftwareResourceDO updated = softwareResourceMapper.selectById(resource.getId());
        assertThat(updated.getName()).isEqualTo("新软件");
        assertThat(updated.getDirection()).isEqualTo(SoftwareResourceDirection.STRUCTURAL_DESIGN);
        assertThat(updated.getStatus()).isEqualTo(SoftwareResourceStatus.DISABLED);
    }

    @Test
    @DisplayName("findById: 存在返回实体，不存在返回空")
    void findById_shouldReturnOptional() {
        SoftwareResource resource = createResource(
                "查询软件",
                SoftwareResourceDirection.GENERAL,
                1,
                SoftwareResourceStatus.ACTIVE);

        Optional<SoftwareResource> found = softwareResourceRepository.findById(resource.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("查询软件");

        assertThat(softwareResourceRepository.findById(-1L)).isEmpty();
    }

    @Test
    @DisplayName("findActiveByDirection: 应返回指定方向已启用资源")
    void findActiveByDirection_shouldFilterActiveByDirection() {
        createResource("CV工具", SoftwareResourceDirection.COMPUTER_VISION, 10, SoftwareResourceStatus.ACTIVE);
        createResource("通用工具", SoftwareResourceDirection.GENERAL, 20, SoftwareResourceStatus.ACTIVE);
        createResource("禁用工具", SoftwareResourceDirection.COMPUTER_VISION, 30, SoftwareResourceStatus.DISABLED);
        createResource("嵌入工具", SoftwareResourceDirection.EMBEDDED, 40, SoftwareResourceStatus.ACTIVE);

        Page<SoftwareResource> page = softwareResourceRepository.findActiveByDirection(
                SoftwareResourceDirection.COMPUTER_VISION,
                null,
                PageRequest.of(0, 10));

        assertThat(page.getContent())
                .extracting(SoftwareResource::getName)
                .contains("CV工具", "通用工具")
                .doesNotContain("禁用工具", "嵌入工具");
    }

    @Test
    @DisplayName("findActiveByDirection: 应支持关键字搜索")
    void findActiveByDirection_shouldSupportKeyword() {
        createResource("关键字匹配", SoftwareResourceDirection.COMPUTER_VISION, 10, SoftwareResourceStatus.ACTIVE);
        createResource("不匹配名称", SoftwareResourceDirection.COMPUTER_VISION, 20, SoftwareResourceStatus.ACTIVE);

        Page<SoftwareResource> page = softwareResourceRepository.findActiveByDirection(
                SoftwareResourceDirection.COMPUTER_VISION,
                "关键字",
                PageRequest.of(0, 10));

        assertThat(page.getContent()).hasSize(1);
        assertThat(page.getContent().get(0).getName()).isEqualTo("关键字匹配");
    }

    @Test
    @DisplayName("findAllForAdmin: 应分页返回所有资源")
    void findAllForAdmin_shouldPaginate() {
        createResource("管理资源1", SoftwareResourceDirection.COMPUTER_VISION, 1, SoftwareResourceStatus.ACTIVE);
        createResource("管理资源2", SoftwareResourceDirection.EMBEDDED, 2, SoftwareResourceStatus.DISABLED);

        Page<SoftwareResource> page = softwareResourceRepository.findAllForAdmin(PageRequest.of(0, 10));

        assertThat(page.getTotalElements()).isGreaterThanOrEqualTo(2);
        assertThat(page.getContent()).hasSizeGreaterThanOrEqualTo(2);
    }

    @Test
    @DisplayName("existsById: 应正确判断资源是否存在")
    void existsById_shouldWork() {
        SoftwareResource resource = createResource(
                "存在资源",
                SoftwareResourceDirection.GENERAL,
                1,
                SoftwareResourceStatus.ACTIVE);

        assertThat(softwareResourceRepository.existsById(resource.getId())).isTrue();
        assertThat(softwareResourceRepository.existsById(-1L)).isFalse();
    }

    @Test
    @DisplayName("findMaxSortOrder: 应返回当前最大排序号")
    void findMaxSortOrder_shouldReturnMax() {
        createResource("排序100", SoftwareResourceDirection.COMPUTER_VISION, 100, SoftwareResourceStatus.ACTIVE);
        createResource("排序200", SoftwareResourceDirection.EMBEDDED, 200, SoftwareResourceStatus.ACTIVE);

        Integer maxSortOrder = softwareResourceRepository.findMaxSortOrder();

        assertThat(maxSortOrder).isGreaterThanOrEqualTo(200);
    }

    @Test
    @DisplayName("batchUpdateSortOrder: 应批量更新排序号")
    void batchUpdateSortOrder_shouldUpdate() {
        SoftwareResource resource1 = createResource(
                "批量1",
                SoftwareResourceDirection.COMPUTER_VISION,
                1,
                SoftwareResourceStatus.ACTIVE);
        SoftwareResource resource2 = createResource(
                "批量2",
                SoftwareResourceDirection.EMBEDDED,
                2,
                SoftwareResourceStatus.ACTIVE);

        softwareResourceRepository.batchUpdateSortOrder(
                List.of(
                        new SoftwareResourceRepository.SortItem(resource1.getId(), 100),
                        new SoftwareResourceRepository.SortItem(resource2.getId(), 200)));

        SoftwareResourceDO updated1 = softwareResourceMapper.selectById(resource1.getId());
        SoftwareResourceDO updated2 = softwareResourceMapper.selectById(resource2.getId());
        assertThat(updated1.getSortOrder()).isEqualTo(100);
        assertThat(updated2.getSortOrder()).isEqualTo(200);
    }

    @Test
    @DisplayName("deleteById: 应删除软件资源")
    void deleteById_shouldRemoveResource() {
        SoftwareResource resource = createResource(
                "待删除资源",
                SoftwareResourceDirection.GENERAL,
                1,
                SoftwareResourceStatus.ACTIVE);
        Long resourceId = resource.getId();

        softwareResourceRepository.deleteById(resourceId);

        assertThat(softwareResourceMapper.selectById(resourceId)).isNull();
    }
}
