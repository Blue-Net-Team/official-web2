package com.bluenet.web.infrastructure.repository.impl;

import com.bluenet.web.BaseIntegrationTest;
import com.bluenet.web.domain.model.entity.Role;
import com.bluenet.web.domain.model.enumerate.RoleType;
import com.bluenet.web.domain.repository.RoleRepository;
import com.bluenet.web.testsupport.fixture.RoleFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * RoleRepositoryImpl 集成测试。
 */
@DisplayName("RoleRepositoryImpl 集成测试")
class RoleRepositoryImplIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private RoleRepository roleRepository;

    @Test
    @DisplayName("findById: 应返回 Flyway 初始化的超级管理员角色")
    void findById_shouldReturnSuperAdminRole() {
        Optional<Role> found = roleRepository.findById(RoleFixture.defaultRoleId(RoleType.SUPER_ADMIN));

        assertTrue(found.isPresent());
        assertEquals("SUPER_ADMIN", found.get().getName());
    }

    @Test
    @DisplayName("findById: 不存在的角色应返回空")
    void findById_notExist_shouldReturnEmpty() {
        Optional<Role> found = roleRepository.findById(-1L);

        assertTrue(found.isEmpty());
    }

    @Test
    @DisplayName("findByName: 应返回对应角色")
    void findByName_shouldReturnRole() {
        Optional<Role> found = roleRepository.findByName("MEMBER");

        assertTrue(found.isPresent());
        assertEquals(RoleFixture.defaultRoleId(RoleType.MEMBER), found.get().getId());
    }

    @Test
    @DisplayName("findByName: 不存在的角色名应返回空")
    void findByName_notExist_shouldReturnEmpty() {
        Optional<Role> found = roleRepository.findByName("NOT_EXIST_ROLE");

        assertTrue(found.isEmpty());
    }
}
