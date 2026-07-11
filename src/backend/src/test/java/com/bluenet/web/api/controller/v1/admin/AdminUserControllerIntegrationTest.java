package com.bluenet.web.api.controller.v1.admin;

import com.bluenet.web.BaseIntegrationTest;
import com.bluenet.web.api.dto.adminuser.AdminUserBatchOperateRequestDTO;
import com.bluenet.web.api.dto.adminuser.AdminUserBatchUpdateRoleRequestDTO;
import com.bluenet.web.api.dto.adminuser.AdminUserCreateRequestDTO;
import com.bluenet.web.api.dto.adminuser.AdminUserResetPasswordRequestDTO;
import com.bluenet.web.api.dto.adminuser.AdminUserUpdateRequestDTO;
import com.bluenet.web.domain.model.entity.College;
import com.bluenet.web.domain.model.entity.User;
import com.bluenet.web.domain.model.enumerate.Direction;
import com.bluenet.web.domain.model.enumerate.Gender;
import com.bluenet.web.domain.model.enumerate.RoleType;
import com.bluenet.web.domain.repository.CollegeRepository;
import com.bluenet.web.domain.repository.UserRepository;
import com.bluenet.web.infrastructure.repository.mapper.RoleMapper;
import com.bluenet.web.infrastructure.security.principal.WithSecurityPrincipal;
import com.bluenet.web.infrastructure.security.util.UserCTX;
import com.bluenet.web.testconfig.TestSecurityConfig;
import com.bluenet.web.testsupport.fixture.CollegeFixture;
import com.bluenet.web.testsupport.fixture.RoleFixture;
import com.bluenet.web.testsupport.fixture.UserFixture;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestSecurityConfig.class)
@DisplayName("AdminUserController 集成测试")
class AdminUserControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CollegeRepository collegeRepository;

    @Autowired
    private RoleMapper roleMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private Long memberRoleId;
    private Long candidateRoleId;
    private Long collegeId;

    @BeforeEach
    void prepare() {
        memberRoleId = RoleFixture.roleId(roleMapper, RoleType.MEMBER);
        candidateRoleId = RoleFixture.roleId(roleMapper, RoleType.CANDIDATE);
        College college = CollegeFixture.saveDefaultCollege(collegeRepository);
        collegeId = college.getId();
    }

    @AfterEach
    void tearDown() {
        UserCTX.clear();
    }

    private User createMemberUser(String studentId) {
        User user = UserFixture.builder()
                .withStudentId(studentId)
                .withRoleId(memberRoleId)
                .withCollegeId(collegeId)
                .build();
        return UserFixture.save(userRepository, passwordEncoder, user);
    }

    @Test
    @DisplayName("getUserList: 超级管理员应返回分页用户列表")
    @WithSecurityPrincipal(userId = 9999L, roleType = "SUPER_ADMIN", roleId = 1L, permissions = { "user:manage:list" })
    void getUserList_asSuperAdmin_shouldReturnPagedUsers() throws Exception {
        User user = createMemberUser("2024003001001");

        mockMvc.perform(
                get("/api/v1/admin/users")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.content").isArray());
    }

    @Test
    @DisplayName("getUserList: 普通成员访问应返回 403")
    @WithSecurityPrincipal(roleType = "MEMBER", roleId = 3L, permissions = {})
    void getUserList_asMember_shouldReturn403() throws Exception {
        mockMvc.perform(get("/api/v1/admin/users"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("getUserDetail: 超级管理员应返回用户详情")
    @WithSecurityPrincipal(userId = 9999L, roleType = "SUPER_ADMIN", roleId = 1L, permissions = {
            "user:manage:detail" })
    void getUserDetail_asSuperAdmin_shouldReturnDetail() throws Exception {
        User user = createMemberUser("2024003001002");

        mockMvc.perform(get("/api/v1/admin/users/{id}", user.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.id").value(user.getId()))
                .andExpect(jsonPath("$.data.studentId").value(user.getStudentId()));
    }

    @Test
    @DisplayName("updateUser: 超级管理员应成功更新用户信息")
    @WithSecurityPrincipal(userId = 9999L, roleType = "SUPER_ADMIN", roleId = 1L, permissions = {
            "user:manage:update" })
    void updateUser_asSuperAdmin_shouldReturnOk() throws Exception {
        User user = createMemberUser("2024003001003");
        AdminUserUpdateRequestDTO request = AdminUserUpdateRequestDTO.builder()
                .roleId(candidateRoleId)
                .direction(Direction.STRUCTURAL_DESIGN)
                .disable(true)
                .job("算法工程师")
                .studentId(user.getStudentId())
                .email("updated@example.com")
                .username("更新用户")
                .nickname("更新昵称")
                .collegeId(collegeId)
                .major("新专业")
                .gender(Gender.FEMALE)
                .assessmentGradeYear(2025)
                .build();

        mockMvc.perform(
                put("/api/v1/admin/users/{id}", user.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("resetPassword: 超级管理员应成功重置用户密码")
    @WithSecurityPrincipal(userId = 9999L, roleType = "SUPER_ADMIN", roleId = 1L, permissions = {
            "user:manage:reset-password" })
    void resetPassword_asSuperAdmin_shouldReturnOk() throws Exception {
        User user = createMemberUser("2024003001004");
        AdminUserResetPasswordRequestDTO request = AdminUserResetPasswordRequestDTO.builder()
                .newPassword("newPassword123")
                .confirmPassword("newPassword123")
                .build();

        mockMvc.perform(
                put("/api/v1/admin/users/{id}/password", user.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("deleteUser: 超级管理员应成功删除普通用户")
    @WithSecurityPrincipal(userId = 9999L, roleType = "SUPER_ADMIN", roleId = 1L, permissions = {
            "user:manage:delete" })
    void deleteUser_asSuperAdmin_shouldReturnOk() throws Exception {
        User user = createMemberUser("2024003001005");

        mockMvc.perform(delete("/api/v1/admin/users/{id}", user.getId()))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("batchDelete: 超级管理员应成功批量删除用户")
    @WithSecurityPrincipal(userId = 9999L, roleType = "SUPER_ADMIN", roleId = 1L, permissions = {
            "user:manage:batch-delete" })
    void batchDelete_asSuperAdmin_shouldReturnOk() throws Exception {
        User user1 = createMemberUser("2024003001006");
        User user2 = createMemberUser("2024003001007");
        AdminUserBatchOperateRequestDTO request = AdminUserBatchOperateRequestDTO.builder()
                .userIds(List.of(user1.getId(), user2.getId()))
                .build();

        mockMvc.perform(
                post("/api/v1/admin/users/batch-delete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("batchDisable: 超级管理员应成功批量禁用用户")
    @WithSecurityPrincipal(userId = 9999L, roleType = "SUPER_ADMIN", roleId = 1L, permissions = {
            "user:manage:batch-disable" })
    void batchDisable_asSuperAdmin_shouldReturnOk() throws Exception {
        User user = createMemberUser("2024003001008");
        AdminUserBatchOperateRequestDTO request = AdminUserBatchOperateRequestDTO.builder()
                .userIds(List.of(user.getId()))
                .build();

        mockMvc.perform(
                post("/api/v1/admin/users/batch-disable")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("batchEnable: 超级管理员应成功批量启用用户")
    @WithSecurityPrincipal(userId = 9999L, roleType = "SUPER_ADMIN", roleId = 1L, permissions = {
            "user:manage:batch-enable" })
    void batchEnable_asSuperAdmin_shouldReturnOk() throws Exception {
        User user = UserFixture.builder()
                .withStudentId("2024003001009")
                .withRoleId(memberRoleId)
                .withCollegeId(collegeId)
                .disabled()
                .build();
        UserFixture.save(userRepository, passwordEncoder, user);
        AdminUserBatchOperateRequestDTO request = AdminUserBatchOperateRequestDTO.builder()
                .userIds(List.of(user.getId()))
                .build();

        mockMvc.perform(
                post("/api/v1/admin/users/batch-enable")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("batchUpdateRole: 超级管理员应成功批量更新用户角色")
    @WithSecurityPrincipal(userId = 9999L, roleType = "SUPER_ADMIN", roleId = 1L, permissions = {
            "user:manage:batch-update-role" })
    void batchUpdateRole_asSuperAdmin_shouldReturnOk() throws Exception {
        User user = createMemberUser("2024003001010");
        AdminUserBatchUpdateRoleRequestDTO request = AdminUserBatchUpdateRoleRequestDTO.builder()
                .userIds(List.of(user.getId()))
                .roleId(candidateRoleId)
                .build();

        mockMvc.perform(
                post("/api/v1/admin/users/batch-role")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("createUser: 超级管理员应成功创建用户")
    @WithSecurityPrincipal(userId = 9999L, roleType = "SUPER_ADMIN", roleId = 1L, permissions = {
            "user:manage:create" })
    void createUser_asSuperAdmin_shouldReturnCreatedUser() throws Exception {
        AdminUserCreateRequestDTO request = AdminUserCreateRequestDTO.builder()
                .studentId("2024003001011")
                .email("2024003001011@example.com")
                .username("新建用户")
                .password("newPassword123")
                .nickname("新建昵称")
                .roleId(memberRoleId)
                .collegeId(collegeId)
                .major("计算机科学与技术")
                .direction(Direction.COMPUTER_VISION)
                .gender(Gender.MALE)
                .job("后端开发")
                .assessmentGradeYear(2024)
                .build();

        mockMvc.perform(
                post("/api/v1/admin/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.id").exists())
                .andExpect(jsonPath("$.data.studentId").value("2024003001011"));
    }
}
