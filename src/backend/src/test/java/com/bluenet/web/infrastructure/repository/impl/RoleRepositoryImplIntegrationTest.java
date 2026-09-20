package com.bluenet.web.infrastructure.repository.impl;

import com.bluenet.web.DBIntegrationTest;
import com.bluenet.web.domain.model.entity.Role;
import com.bluenet.web.domain.model.enumerate.RoleType;
import com.bluenet.web.domain.repository.RoleRepository;
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
class RoleRepositoryImplIntegrationTest extends DBIntegrationTest {

    @Autowired
    private RoleRepository roleRepository;

    @Test
    @DisplayName("findById: 应返回 Flyway 初始化的超级管理员角色")
    void findById_shouldReturnSuperAdminRole() {
        // 角色 ID 由 SERIAL 生成，不可硬编码；先按名称解析出真实 ID。
        Long superAdminId = roleRepository.findByName(RoleType.SUPER_ADMIN.getName()).orElseThrow().getId();

        Optional<Role> found = roleRepository.findById(superAdminId);

        assertTrue(found.isPresent());
        assertEquals(RoleType.SUPER_ADMIN.getName(), found.get().getName());
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
        Optional<Role> found = roleRepository.findByName(RoleType.MEMBER.getName());

        assertTrue(found.isPresent());
        assertEquals(RoleType.MEMBER.getName(), found.get().getName());
        // 按名称得到的 ID 应能被按 ID 查询取回同一角色。
        Optional<Role> foundById = roleRepository.findById(found.get().getId());
        assertTrue(foundById.isPresent());
        assertEquals(found.get().getId(), foundById.get().getId());
    }

    @Test
    @DisplayName("findByName: 不存在的角色名应返回空")
    void findByName_notExist_shouldReturnEmpty() {
        Optional<Role> found = roleRepository.findByName("NOT_EXIST_ROLE");

        assertTrue(found.isEmpty());
    }
}
