package com.bluenet.web.application.service.impl;

import com.bluenet.web.BaseIntegrationTest;
import com.bluenet.web.application.command.softwareresource.SoftwareResourceCommands;
import com.bluenet.web.application.result.softwareresource.SoftwareResourceResult;
import com.bluenet.web.application.service.SoftwareResourceAppService;
import com.bluenet.web.domain.exception.DataNotFound;
import com.bluenet.web.domain.model.entity.SoftwareResource;
import com.bluenet.web.domain.model.enumerate.SoftwareResourceDirection;
import com.bluenet.web.domain.model.enumerate.SoftwareResourceStatus;
import com.bluenet.web.domain.repository.SoftwareResourceRepository;
import com.bluenet.web.infrastructure.security.principal.WithSecurityPrincipal;
import com.bluenet.web.infrastructure.security.util.UserCTX;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * SoftwareResourceAppServiceImpl 集成测试。
 *
 * <p>
 * 验证软件资源应用服务的查询、创建、更新、删除及批量排序逻辑。
 * </p>
 */
@DisplayName("SoftwareResourceAppServiceImpl 集成测试")
class SoftwareResourceAppServiceImplIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private SoftwareResourceAppService softwareResourceAppService;

    @Autowired
    private SoftwareResourceRepository softwareResourceRepository;

    @AfterEach
    void cleanupSecurityContext() {
        UserCTX.clear();
    }

    @Test
    @WithSecurityPrincipal(userId = 1L, roleType = "SUPER_ADMIN")
    @DisplayName("createSoftwareResource: 应创建资源且 sortOrder 默认从 1 开始并递增")
    void createSoftwareResource_shouldCreateWithDefaultSortOrder() {
        SoftwareResourceCommands.CreateSoftwareResourceCommand firstCommand = new SoftwareResourceCommands.CreateSoftwareResourceCommand(
                "PyCharm",
                SoftwareResourceDirection.COMPUTER_VISION,
                "IDE",
                "Python IDE",
                "https://example.com/pycharm",
                null);

        SoftwareResourceResult firstResult = softwareResourceAppService.createSoftwareResource(firstCommand);

        assertThat(firstResult).isNotNull();
        assertThat(firstResult.id()).isNotNull();
        assertThat(firstResult.sortOrder()).isEqualTo(1);

        SoftwareResourceCommands.CreateSoftwareResourceCommand secondCommand = new SoftwareResourceCommands.CreateSoftwareResourceCommand(
                "VS Code",
                SoftwareResourceDirection.COMPUTER_VISION,
                "IDE",
                "Code editor",
                "https://example.com/vscode",
                null);

        SoftwareResourceResult secondResult = softwareResourceAppService.createSoftwareResource(secondCommand);

        assertThat(secondResult.sortOrder()).isEqualTo(2);
    }

    @Test
    @WithSecurityPrincipal(userId = 1L, roleType = "MEMBER")
    @DisplayName("listActiveResources: 应返回指定方向已启用资源并排除其他方向与禁用资源")
    void listActiveResources_shouldReturnActiveResourcesForDirection() {
        saveSoftwareResource(
                "CV工具",
                SoftwareResourceDirection.COMPUTER_VISION,
                "工具",
                null,
                SoftwareResourceStatus.ACTIVE);
        saveSoftwareResource(
                "结构工具",
                SoftwareResourceDirection.STRUCTURAL_DESIGN,
                "工具",
                null,
                SoftwareResourceStatus.ACTIVE);
        saveSoftwareResource(
                "禁用CV工具",
                SoftwareResourceDirection.COMPUTER_VISION,
                "工具",
                null,
                SoftwareResourceStatus.DISABLED);
        saveSoftwareResource(
                "通用工具",
                SoftwareResourceDirection.GENERAL,
                "工具",
                null,
                SoftwareResourceStatus.ACTIVE);
        Pageable pageable = PageRequest.of(0, 10);

        Page<SoftwareResourceResult> result = softwareResourceAppService.listActiveResources(
                SoftwareResourceDirection.COMPUTER_VISION,
                null,
                pageable);

        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent())
                .extracting(SoftwareResourceResult::name)
                .containsExactlyInAnyOrder("CV工具", "通用工具");
    }

    @Test
    @WithSecurityPrincipal(userId = 1L, roleType = "MEMBER")
    @DisplayName("listActiveResources: 关键字应匹配名称、分类与描述")
    void listActiveResources_keywordMatchesNameCategoryOrDescription() {
        saveSoftwareResource(
                "名称匹配",
                SoftwareResourceDirection.COMPUTER_VISION,
                "工具",
                "描述",
                SoftwareResourceStatus.ACTIVE);
        saveSoftwareResource(
                "其他",
                SoftwareResourceDirection.COMPUTER_VISION,
                "分类匹配",
                "描述",
                SoftwareResourceStatus.ACTIVE);
        saveSoftwareResource(
                "其他2",
                SoftwareResourceDirection.COMPUTER_VISION,
                "工具",
                "描述匹配",
                SoftwareResourceStatus.ACTIVE);
        saveSoftwareResource(
                "不匹配",
                SoftwareResourceDirection.COMPUTER_VISION,
                "工具",
                "描述",
                SoftwareResourceStatus.ACTIVE);
        Pageable pageable = PageRequest.of(0, 10);

        Page<SoftwareResourceResult> nameResult = softwareResourceAppService.listActiveResources(
                SoftwareResourceDirection.COMPUTER_VISION,
                "名称匹配",
                pageable);
        Page<SoftwareResourceResult> categoryResult = softwareResourceAppService.listActiveResources(
                SoftwareResourceDirection.COMPUTER_VISION,
                "分类匹配",
                pageable);
        Page<SoftwareResourceResult> descriptionResult = softwareResourceAppService.listActiveResources(
                SoftwareResourceDirection.COMPUTER_VISION,
                "描述匹配",
                pageable);

        assertThat(nameResult.getContent()).hasSize(1);
        assertThat(nameResult.getContent().get(0).name()).isEqualTo("名称匹配");
        assertThat(categoryResult.getContent()).hasSize(1);
        assertThat(categoryResult.getContent().get(0).name()).isEqualTo("其他");
        assertThat(descriptionResult.getContent()).hasSize(1);
        assertThat(descriptionResult.getContent().get(0).name()).isEqualTo("其他2");
    }

    @Test
    @WithSecurityPrincipal(userId = 1L, roleType = "SUPER_ADMIN")
    @DisplayName("listAllForAdmin: 应返回所有资源，包含已禁用")
    void listAllForAdmin_shouldReturnAllResourcesIncludingDisabled() {
        saveSoftwareResource(
                "启用资源",
                SoftwareResourceDirection.COMPUTER_VISION,
                "工具",
                null,
                SoftwareResourceStatus.ACTIVE);
        saveSoftwareResource(
                "禁用资源",
                SoftwareResourceDirection.COMPUTER_VISION,
                "工具",
                null,
                SoftwareResourceStatus.DISABLED);
        Pageable pageable = PageRequest.of(0, 10);

        Page<SoftwareResourceResult> result = softwareResourceAppService.listAllForAdmin(pageable);

        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent())
                .extracting(SoftwareResourceResult::name)
                .containsExactlyInAnyOrder("启用资源", "禁用资源");
    }

    @Test
    @WithSecurityPrincipal(userId = 1L, roleType = "SUPER_ADMIN")
    @DisplayName("updateSoftwareResource: 应更新并持久化变更")
    void updateSoftwareResource_shouldUpdateAndPersist() {
        SoftwareResource resource = saveSoftwareResource(
                "旧名称",
                SoftwareResourceDirection.COMPUTER_VISION,
                "旧分类",
                "旧描述",
                SoftwareResourceStatus.ACTIVE);
        SoftwareResourceCommands.UpdateSoftwareResourceCommand command = new SoftwareResourceCommands.UpdateSoftwareResourceCommand(
                resource.getId(),
                "新名称",
                SoftwareResourceDirection.STRUCTURAL_DESIGN,
                "新分类",
                "新描述",
                "https://example.com/new",
                10,
                SoftwareResourceStatus.DISABLED);

        SoftwareResourceResult result = softwareResourceAppService.updateSoftwareResource(command);

        assertThat(result.name()).isEqualTo("新名称");
        assertThat(result.direction()).isEqualTo(SoftwareResourceDirection.STRUCTURAL_DESIGN);
        assertThat(result.category()).isEqualTo("新分类");
        assertThat(result.description()).isEqualTo("新描述");
        assertThat(result.externalUrl()).isEqualTo("https://example.com/new");
        assertThat(result.sortOrder()).isEqualTo(10);
        assertThat(result.status()).isEqualTo(SoftwareResourceStatus.DISABLED);
        assertThat(softwareResourceRepository.findById(resource.getId()))
                .isPresent()
                .hasValueSatisfying(updated -> {
                    assertThat(updated.getName()).isEqualTo("新名称");
                    assertThat(updated.getDirection()).isEqualTo(SoftwareResourceDirection.STRUCTURAL_DESIGN);
                    assertThat(updated.getCategory()).isEqualTo("新分类");
                    assertThat(updated.getDescription()).isEqualTo("新描述");
                    assertThat(updated.getExternalUrl()).isEqualTo("https://example.com/new");
                    assertThat(updated.getSortOrder()).isEqualTo(10);
                    assertThat(updated.getStatus()).isEqualTo(SoftwareResourceStatus.DISABLED);
                });
    }

    @Test
    @WithSecurityPrincipal(userId = 1L, roleType = "SUPER_ADMIN")
    @DisplayName("updateSoftwareResource: 不存在的 id 应抛 DataNotFound")
    void updateSoftwareResource_notFound_shouldThrowDataNotFound() {
        SoftwareResourceCommands.UpdateSoftwareResourceCommand command = new SoftwareResourceCommands.UpdateSoftwareResourceCommand(
                99999L,
                "任意名称",
                SoftwareResourceDirection.COMPUTER_VISION,
                "分类",
                "描述",
                "https://example.com/any",
                1,
                SoftwareResourceStatus.ACTIVE);

        assertThatThrownBy(() -> softwareResourceAppService.updateSoftwareResource(command))
                .isInstanceOf(DataNotFound.class)
                .hasMessageContaining("软件资源不存在");
    }

    @Test
    @WithSecurityPrincipal(userId = 1L, roleType = "SUPER_ADMIN")
    @DisplayName("deleteSoftwareResource: 应删除资源")
    void deleteSoftwareResource_shouldDelete() {
        SoftwareResource resource = saveSoftwareResource(
                "待删除",
                SoftwareResourceDirection.COMPUTER_VISION,
                "工具",
                null,
                SoftwareResourceStatus.ACTIVE);

        softwareResourceAppService.deleteSoftwareResource(resource.getId());

        assertThat(softwareResourceRepository.findById(resource.getId())).isEmpty();
    }

    @Test
    @WithSecurityPrincipal(userId = 1L, roleType = "SUPER_ADMIN")
    @DisplayName("deleteSoftwareResource: 不存在的 id 应抛 DataNotFound")
    void deleteSoftwareResource_notFound_shouldThrowDataNotFound() {
        assertThatThrownBy(() -> softwareResourceAppService.deleteSoftwareResource(99999L))
                .isInstanceOf(DataNotFound.class)
                .hasMessageContaining("软件资源不存在");
    }

    @Test
    @WithSecurityPrincipal(userId = 1L, roleType = "SUPER_ADMIN")
    @DisplayName("batchUpdateSortOrder: 应批量更新多个资源的排序号")
    void batchUpdateSortOrder_shouldUpdateSortOrders() {
        SoftwareResource first = saveSoftwareResource(
                "资源一",
                SoftwareResourceDirection.COMPUTER_VISION,
                "工具",
                null,
                SoftwareResourceStatus.ACTIVE);
        SoftwareResource second = saveSoftwareResource(
                "资源二",
                SoftwareResourceDirection.COMPUTER_VISION,
                "工具",
                null,
                SoftwareResourceStatus.ACTIVE);
        SoftwareResource third = saveSoftwareResource(
                "资源三",
                SoftwareResourceDirection.COMPUTER_VISION,
                "工具",
                null,
                SoftwareResourceStatus.ACTIVE);
        SoftwareResourceCommands.BatchUpdateSortOrderCommand command = new SoftwareResourceCommands.BatchUpdateSortOrderCommand(
                List.of(
                        new SoftwareResourceCommands.SortItemCommand(first.getId(), 30),
                        new SoftwareResourceCommands.SortItemCommand(second.getId(), 20),
                        new SoftwareResourceCommands.SortItemCommand(third.getId(), 10)));

        softwareResourceAppService.batchUpdateSortOrder(command);

        assertThat(softwareResourceRepository.findById(first.getId()))
                .isPresent()
                .hasValueSatisfying(updated -> assertThat(updated.getSortOrder()).isEqualTo(30));
        assertThat(softwareResourceRepository.findById(second.getId()))
                .isPresent()
                .hasValueSatisfying(updated -> assertThat(updated.getSortOrder()).isEqualTo(20));
        assertThat(softwareResourceRepository.findById(third.getId()))
                .isPresent()
                .hasValueSatisfying(updated -> assertThat(updated.getSortOrder()).isEqualTo(10));
    }

    @Test
    @WithSecurityPrincipal(userId = 1L, roleType = "SUPER_ADMIN")
    @DisplayName("batchUpdateSortOrder: 包含不存在的 id 应抛 IllegalArgumentException")
    void batchUpdateSortOrder_notFound_shouldThrowIllegalArgument() {
        SoftwareResource resource = saveSoftwareResource(
                "资源",
                SoftwareResourceDirection.COMPUTER_VISION,
                "工具",
                null,
                SoftwareResourceStatus.ACTIVE);
        SoftwareResourceCommands.BatchUpdateSortOrderCommand command = new SoftwareResourceCommands.BatchUpdateSortOrderCommand(
                List.of(
                        new SoftwareResourceCommands.SortItemCommand(resource.getId(), 5),
                        new SoftwareResourceCommands.SortItemCommand(99999L, 5)));

        assertThatThrownBy(() -> softwareResourceAppService.batchUpdateSortOrder(command))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("软件资源不存在");
    }

    private SoftwareResource saveSoftwareResource(String name, SoftwareResourceDirection direction, String category,
            String description, SoftwareResourceStatus status) {
        SoftwareResource resource = SoftwareResource.create(
                name,
                direction,
                category,
                description,
                "https://example.com/" + name,
                null);
        resource.changeStatus(status);
        softwareResourceRepository.save(resource);
        return resource;
    }
}
