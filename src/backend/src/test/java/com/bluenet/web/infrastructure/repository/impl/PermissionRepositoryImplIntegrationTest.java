package com.bluenet.web.infrastructure.repository.impl;

import com.bluenet.web.BaseIntegrationTest;
import com.bluenet.web.domain.model.entity.Permission;
import com.bluenet.web.domain.repository.PermissionRepository;
import com.bluenet.web.infrastructure.repository.dataobject.PermissionDO;
import com.bluenet.web.infrastructure.repository.mapper.PermissionMapper;
import com.bluenet.web.testsupport.fixture.PermissionFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * PermissionRepositoryImpl 集成测试。
 * <p>
 * 验证权限仓储的基础 CRUD、分页与批量查询行为。
 * </p>
 */
@DisplayName("PermissionRepositoryImpl 集成测试")
class PermissionRepositoryImplIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private PermissionRepository permissionRepository;

    @Autowired
    private PermissionMapper permissionMapper;

    @Test
    @DisplayName("save: 新权限应插入并回写ID")
    void save_newPermission_shouldInsertAndAssignId() {
        Permission permission = PermissionFixture.create("用户管理", "user:manage");

        permissionRepository.save(permission);

        assertNotNull(permission.getId());
        PermissionDO dataObject = permissionMapper.selectById(permission.getId());
        assertNotNull(dataObject);
        assertEquals("用户管理", dataObject.getName());
        assertEquals("user:manage", dataObject.getValue());
        assertEquals("PROTECTED", dataObject.getAccessLevel());
    }

    @Test
    @DisplayName("save: 已有权限应更新字段")
    void save_existingPermission_shouldUpdateFields() {
        Permission permission = PermissionFixture.save(permissionRepository, "角色管理", "role:manage");
        permission.setName("角色管理-已更新");
        permission.setAccessLevel("AUTHENTICATED");

        permissionRepository.save(permission);

        PermissionDO updated = permissionMapper.selectById(permission.getId());
        assertEquals("角色管理-已更新", updated.getName());
        assertEquals("AUTHENTICATED", updated.getAccessLevel());
        assertEquals("role:manage", updated.getValue());
    }

    @Test
    @DisplayName("findById: 存在返回实体，不存在返回空")
    void findById_shouldReturnOptional() {
        Permission permission = PermissionFixture.save(permissionRepository, "文件管理", "file:manage");

        Optional<Permission> found = permissionRepository.findById(permission.getId());
        assertTrue(found.isPresent());
        assertEquals(permission.getValue(), found.get().getValue());

        Optional<Permission> notFound = permissionRepository.findById(-1L);
        assertTrue(notFound.isEmpty());
    }

    @Test
    @DisplayName("findAll: 应返回全部权限（包含已保存的权限）")
    void findAll_shouldReturnAllPermissions() {
        Permission first = PermissionFixture.save(permissionRepository, "第一个权限", "repo-test:first");
        Permission second = PermissionFixture.save(permissionRepository, "第二个权限", "repo-test:second");

        List<Permission> all = permissionRepository.findAll();

        List<String> values = all.stream().map(Permission::getValue).toList();
        assertTrue(values.contains(first.getValue()));
        assertTrue(values.contains(second.getValue()));
    }

    @Test
    @DisplayName("findAll: 应按关键字过滤")
    void findAll_shouldFilterByKeyword() {
        PermissionFixture.save(permissionRepository, "用户查询", "repo-keyword:query");
        PermissionFixture.save(permissionRepository, "订单管理", "repo-keyword:order");
        PermissionFixture.save(permissionRepository, "用户删除", "repo-keyword:delete");

        Pageable pageable = PageRequest.of(0, 10);
        Page<Permission> page = permissionRepository.findAll("repo-keyword", null, pageable);

        assertEquals(3, page.getTotalElements());
        List<String> values = page.getContent().stream().map(Permission::getValue).toList();
        assertTrue(values.contains("repo-keyword:query"));
        assertTrue(values.contains("repo-keyword:delete"));
        assertTrue(values.contains("repo-keyword:order"));
    }

    @Test
    @DisplayName("findAll: 应按格式过滤")
    void findAll_shouldFilterByFormat() {
        PermissionFixture.save(permissionRepository, "单层级操作", "repo-single:action");
        PermissionFixture.save(permissionRepository, "多层级操作", "repo-double:sub:action");
        PermissionFixture.save(permissionRepository, "横杠格式", "repo-hyphen-action:action");

        Pageable pageable = PageRequest.of(0, 10);

        Page<Permission> singleColonPage = permissionRepository.findAll("repo-single", "resource:action", pageable);
        assertEquals(1, singleColonPage.getTotalElements());
        assertEquals("repo-single:action", singleColonPage.getContent().get(0).getValue());

        Page<Permission> doubleColonPage = permissionRepository
                .findAll("repo-double", "resource:subresource:action", pageable);
        assertEquals(1, doubleColonPage.getTotalElements());
        assertEquals("repo-double:sub:action", doubleColonPage.getContent().get(0).getValue());

        Page<Permission> hyphenPage = permissionRepository.findAll("repo-hyphen", "resource-action:action", pageable);
        assertEquals(1, hyphenPage.getTotalElements());
        assertEquals("repo-hyphen-action:action", hyphenPage.getContent().get(0).getValue());
    }

    @Test
    @DisplayName("findAll: 应支持分页")
    void findAll_shouldPaginate() {
        PermissionFixture.save(permissionRepository, "权限一", "repo-page:one");
        PermissionFixture.save(permissionRepository, "权限二", "repo-page:two");
        PermissionFixture.save(permissionRepository, "权限三", "repo-page:three");

        Pageable pageable = PageRequest.of(0, 2);
        Page<Permission> firstPage = permissionRepository.findAll("repo-page", null, pageable);

        assertEquals(3, firstPage.getTotalElements());
        assertEquals(2, firstPage.getNumberOfElements());
        assertTrue(firstPage.hasNext());

        Pageable nextPageable = PageRequest.of(1, 2);
        Page<Permission> secondPage = permissionRepository.findAll("repo-page", null, nextPageable);

        assertEquals(1, secondPage.getNumberOfElements());
        assertFalse(secondPage.hasNext());
    }

    @Test
    @DisplayName("findAll: 关键字和格式组合过滤应生效")
    void findAll_withKeywordAndFormat_shouldFilterCombined() {
        PermissionFixture.save(permissionRepository, "用户创建", "repo-combo:create");
        PermissionFixture.save(permissionRepository, "用户详情", "repo-combo:detail");
        PermissionFixture.save(permissionRepository, "用户子资源删除", "repo-combo:sub:delete");

        Pageable pageable = PageRequest.of(0, 10);
        Page<Permission> page = permissionRepository.findAll("repo-combo", "resource:action", pageable);

        assertEquals(2, page.getTotalElements());
        List<String> values = page.getContent().stream().map(Permission::getValue).toList();
        assertTrue(values.contains("repo-combo:create"));
        assertTrue(values.contains("repo-combo:detail"));
    }

    @Test
    @DisplayName("findAllByIds: 应批量查询权限")
    void findAllByIds_shouldReturnMatchingPermissions() {
        Permission first = PermissionFixture.save(permissionRepository, "批量一", "batch:one");
        Permission second = PermissionFixture.save(permissionRepository, "批量二", "batch:two");
        Permission third = PermissionFixture.save(permissionRepository, "批量三", "batch:three");

        List<Permission> found = permissionRepository.findAllByIds(List.of(first.getId(), third.getId()));

        assertEquals(2, found.size());
        List<Long> ids = found.stream().map(Permission::getId).toList();
        assertTrue(ids.contains(first.getId()));
        assertTrue(ids.contains(third.getId()));
        assertFalse(ids.contains(second.getId()));
    }

    @Test
    @DisplayName("findAllByIds: 空列表应返回空列表")
    void findAllByIds_emptyList_shouldReturnEmptyList() {
        List<Permission> found = permissionRepository.findAllByIds(Collections.emptyList());

        assertTrue(found.isEmpty());
    }

    @Test
    @DisplayName("deleteById: 应删除指定权限")
    void deleteById_shouldRemovePermission() {
        Permission permission = PermissionFixture.save(permissionRepository, "待删除", "to:delete");
        Long id = permission.getId();

        permissionRepository.deleteById(id);

        assertNull(permissionMapper.selectById(id));
        assertTrue(permissionRepository.findById(id).isEmpty());
    }

    @Test
    @DisplayName("existsById: 应正确判断权限是否存在")
    void existsById_shouldReturnCorrectly() {
        Permission permission = PermissionFixture.save(permissionRepository, "存在检查", "exist:check");

        assertTrue(permissionRepository.existsById(permission.getId()));
        assertFalse(permissionRepository.existsById(-1L));
    }
}
