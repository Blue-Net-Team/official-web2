package com.bluenet.web.api.controller.v1.admin;

import com.bluenet.web.BaseIntegrationTest;
import com.bluenet.web.api.dto.PageDTO;
import com.bluenet.web.api.dto.adminuser.AdminUserBatchOperateRequestDTO;
import com.bluenet.web.api.dto.adminuser.AdminUserBatchUpdateRoleRequestDTO;
import com.bluenet.web.api.dto.adminuser.AdminUserCreateRequestDTO;
import com.bluenet.web.api.dto.adminuser.AdminUserCreateResponseDTO;
import com.bluenet.web.api.dto.adminuser.AdminUserDetailResponseDTO;
import com.bluenet.web.api.dto.adminuser.AdminUserListItemResponseDTO;
import com.bluenet.web.api.dto.adminuser.AdminUserResetPasswordRequestDTO;
import com.bluenet.web.api.dto.adminuser.AdminUserUpdateRequestDTO;
import com.bluenet.web.api.converter.adminuser.AdminUserResponseConverter;
import com.bluenet.web.application.result.adminuser.AdminUserResult;
import com.bluenet.web.application.service.AdminUserAppService;
import com.bluenet.web.domain.model.enumerate.Direction;
import com.bluenet.web.domain.model.enumerate.Gender;
import com.bluenet.web.infrastructure.security.principal.WithSecurityPrincipal;
import com.bluenet.web.infrastructure.security.util.UserCTX;
import com.bluenet.web.testconfig.TestSecurityConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
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

    @MockitoBean
    private AdminUserAppService adminUserAppService;

    @MockitoBean
    private AdminUserResponseConverter responseConverter;

    @AfterEach
    void tearDown() {
        UserCTX.clear();
    }

    private static final long SUPER_ADMIN_USER_ID = 9999L;

    @Test
    @DisplayName("getUserList: 超级管理员应返回分页用户列表")
    @WithSecurityPrincipal(userId = SUPER_ADMIN_USER_ID, roleType = "SUPER_ADMIN", roleId = 1L, permissions = {
            "user:manage:list" })
    void getUserList_asSuperAdmin_shouldReturnPagedUsers() throws Exception {
        AdminUserListItemResponseDTO item = AdminUserListItemResponseDTO.builder()
                .id(1L)
                .studentId("2024003001001")
                .username("测试用户")
                .build();
        PageDTO<AdminUserListItemResponseDTO> pageDTO = new PageDTO<>(
                List.of(item),
                1,
                1,
                0,
                20,
                1,
                true,
                true,
                false);
        when(responseConverter.toPageDTO(any())).thenReturn(pageDTO);

        mockMvc.perform(
                get("/api/v1/admin/users")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content[0].id").value(1));
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
    @WithSecurityPrincipal(userId = SUPER_ADMIN_USER_ID, roleType = "SUPER_ADMIN", roleId = 1L, permissions = {
            "user:manage:detail" })
    void getUserDetail_asSuperAdmin_shouldReturnDetail() throws Exception {
        AdminUserResult.Detail detail = new AdminUserResult.Detail(
                1L,
                "2024003001002",
                "测试用户",
                "昵称",
                "test@example.com",
                3L,
                "MEMBER",
                Direction.COMPUTER_VISION,
                "计算机学院",
                "计算机科学与技术",
                Gender.MALE,
                "后端开发",
                false,
                100L,
                "github",
                "简介",
                2024,
                0L,
                0L,
                0L,
                0L);
        AdminUserDetailResponseDTO detailDTO = AdminUserDetailResponseDTO.builder()
                .id(1L)
                .studentId("2024003001002")
                .username("测试用户")
                .build();
        when(adminUserAppService.getUserDetail(1L)).thenReturn(detail);
        when(responseConverter.toDetailDTO(any(AdminUserResult.Detail.class))).thenReturn(detailDTO);

        mockMvc.perform(get("/api/v1/admin/users/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.studentId").value("2024003001002"));
    }

    @Test
    @DisplayName("updateUser: 超级管理员应成功更新用户信息")
    @WithSecurityPrincipal(userId = SUPER_ADMIN_USER_ID, roleType = "SUPER_ADMIN", roleId = 1L, permissions = {
            "user:manage:update" })
    void updateUser_asSuperAdmin_shouldReturnOk() throws Exception {
        doNothing().when(adminUserAppService).updateUser(any());

        AdminUserUpdateRequestDTO request = AdminUserUpdateRequestDTO.builder()
                .roleId(3L)
                .direction(Direction.STRUCTURAL_DESIGN)
                .disable(true)
                .job("算法工程师")
                .studentId("2024003001003")
                .email("updated@example.com")
                .username("更新用户")
                .nickname("更新昵称")
                .collegeId(1L)
                .major("新专业")
                .gender(Gender.FEMALE)
                .assessmentGradeYear(2025)
                .build();

        mockMvc.perform(
                put("/api/v1/admin/users/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("resetPassword: 超级管理员应成功重置用户密码")
    @WithSecurityPrincipal(userId = SUPER_ADMIN_USER_ID, roleType = "SUPER_ADMIN", roleId = 1L, permissions = {
            "user:manage:reset-password" })
    void resetPassword_asSuperAdmin_shouldReturnOk() throws Exception {
        doNothing().when(adminUserAppService).resetPassword(any());

        AdminUserResetPasswordRequestDTO request = AdminUserResetPasswordRequestDTO.builder()
                .newPassword("newPassword123")
                .confirmPassword("newPassword123")
                .build();

        mockMvc.perform(
                put("/api/v1/admin/users/{id}/password", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("deleteUser: 超级管理员应成功删除普通用户")
    @WithSecurityPrincipal(userId = SUPER_ADMIN_USER_ID, roleType = "SUPER_ADMIN", roleId = 1L, permissions = {
            "user:manage:delete" })
    void deleteUser_asSuperAdmin_shouldReturnOk() throws Exception {
        doNothing().when(adminUserAppService).deleteUser(1L);

        mockMvc.perform(delete("/api/v1/admin/users/{id}", 1L))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("batchDelete: 超级管理员应成功批量删除用户")
    @WithSecurityPrincipal(userId = SUPER_ADMIN_USER_ID, roleType = "SUPER_ADMIN", roleId = 1L, permissions = {
            "user:manage:batch-delete" })
    void batchDelete_asSuperAdmin_shouldReturnOk() throws Exception {
        doNothing().when(adminUserAppService).batchDelete(any());

        AdminUserBatchOperateRequestDTO request = AdminUserBatchOperateRequestDTO.builder()
                .userIds(List.of(1L, 2L))
                .build();

        mockMvc.perform(
                post("/api/v1/admin/users/batch-delete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("batchDisable: 超级管理员应成功批量禁用用户")
    @WithSecurityPrincipal(userId = SUPER_ADMIN_USER_ID, roleType = "SUPER_ADMIN", roleId = 1L, permissions = {
            "user:manage:batch-disable" })
    void batchDisable_asSuperAdmin_shouldReturnOk() throws Exception {
        doNothing().when(adminUserAppService).batchDisable(any(), any());

        AdminUserBatchOperateRequestDTO request = AdminUserBatchOperateRequestDTO.builder()
                .userIds(List.of(1L))
                .build();

        mockMvc.perform(
                post("/api/v1/admin/users/batch-disable")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("batchEnable: 超级管理员应成功批量启用用户")
    @WithSecurityPrincipal(userId = SUPER_ADMIN_USER_ID, roleType = "SUPER_ADMIN", roleId = 1L, permissions = {
            "user:manage:batch-enable" })
    void batchEnable_asSuperAdmin_shouldReturnOk() throws Exception {
        doNothing().when(adminUserAppService).batchDisable(any(), any());

        AdminUserBatchOperateRequestDTO request = AdminUserBatchOperateRequestDTO.builder()
                .userIds(List.of(1L))
                .build();

        mockMvc.perform(
                post("/api/v1/admin/users/batch-enable")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("batchUpdateRole: 超级管理员应成功批量更新用户角色")
    @WithSecurityPrincipal(userId = SUPER_ADMIN_USER_ID, roleType = "SUPER_ADMIN", roleId = 1L, permissions = {
            "user:manage:batch-update-role" })
    void batchUpdateRole_asSuperAdmin_shouldReturnOk() throws Exception {
        doNothing().when(adminUserAppService).batchUpdateRole(any());

        AdminUserBatchUpdateRoleRequestDTO request = AdminUserBatchUpdateRoleRequestDTO.builder()
                .userIds(List.of(1L))
                .roleId(2L)
                .build();

        mockMvc.perform(
                post("/api/v1/admin/users/batch-role")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("createUser: 超级管理员应成功创建用户")
    @WithSecurityPrincipal(userId = SUPER_ADMIN_USER_ID, roleType = "SUPER_ADMIN", roleId = 1L, permissions = {
            "user:manage:create" })
    void createUser_asSuperAdmin_shouldReturnCreatedUser() throws Exception {
        AdminUserResult.Created created = new AdminUserResult.Created(
                1L,
                "2024003001011",
                "新建用户",
                3L);
        AdminUserCreateResponseDTO responseDTO = AdminUserCreateResponseDTO.builder()
                .id(1L)
                .studentId("2024003001011")
                .username("新建用户")
                .roleId(3L)
                .build();
        when(adminUserAppService.createUser(any())).thenReturn(created);
        when(responseConverter.toCreateResponseDTO(any(AdminUserResult.Created.class))).thenReturn(responseDTO);

        AdminUserCreateRequestDTO request = AdminUserCreateRequestDTO.builder()
                .studentId("2024003001011")
                .email("2024003001011@example.com")
                .username("新建用户")
                .password("newPassword123")
                .nickname("新建昵称")
                .roleId(3L)
                .collegeId(1L)
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
