package com.bluenet.web.api.controller.v1.enrollment;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.bluenet.web.BaseIntegrationTest;
import com.bluenet.web.domain.model.entity.College;
import com.bluenet.web.domain.model.entity.Enroll;
import com.bluenet.web.domain.model.entity.Role;
import com.bluenet.web.domain.model.entity.User;
import com.bluenet.web.domain.model.enumerate.Direction;
import com.bluenet.web.domain.model.enumerate.EnrollStatus;
import com.bluenet.web.infrastructure.repository.mapper.CollegeMapper;
import com.bluenet.web.infrastructure.repository.mapper.EnrollMapper;
import com.bluenet.web.infrastructure.repository.mapper.RoleMapper;
import com.bluenet.web.infrastructure.repository.mapper.UserMapper;
import com.bluenet.web.infrastructure.security.WithUserVO;
import com.bluenet.web.infrastructure.security.util.UserCTX;
import com.bluenet.web.testcontainers.TestcontainersConfiguration;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * AdminEnrollController 集成测试
 * <p>
 * 测试管理员报名管理接口的完整流程
 * </p>
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Testcontainers
@Import(TestcontainersConfiguration.class)
class AdminEnrollControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EnrollMapper enrollMapper;

    @Autowired
    private CollegeMapper collegeMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private RoleMapper roleMapper;

    @Autowired
    private ObjectMapper objectMapper;

    private Long testCollegeId;
    private Long memberRoleId;

    private static final String TEST_STUDENT_ID = "20210001001";
    private static final String TEST_USERNAME = "张三";

    @BeforeEach
    void setUp() {
        // 创建测试学院
        College college = College.builder()
                .name("计算机学院")
                .build();
        collegeMapper.insert(college);
        testCollegeId = college.getId();

        // 获取 MEMBER 角色（由 Flyway 迁移初始化）
        Role memberRole = roleMapper.selectByName("MEMBER");
        if (memberRole != null) {
            memberRoleId = memberRole.getId();
        }
    }

    @AfterEach
    void tearDown() {
        UserCTX.clear();
    }

    /**
     * 创建测试报名记录
     */
    private Enroll createTestEnroll(EnrollStatus status) {
        String uniqueSuffix = String.valueOf(System.nanoTime() % 100000);
        Enroll enroll = Enroll.builder()
                .username(TEST_USERNAME)
                .studentId(TEST_STUDENT_ID.substring(0, 8) + uniqueSuffix)
                .collegeId(testCollegeId)
                .major("计算机科学与技术")
                .direction(Direction.COMPUTER_VISION)
                .status(status)
                .build();
        enrollMapper.insert(enroll);
        return enroll;
    }

    // ==================== 分页查询报名列表测试 ====================

    @Nested
    @DisplayName("GET /api/v1/admin/enrollments - 分页查询报名列表")
    class GetEnrollmentListTests {

        @Test
        @DisplayName("正常查询：应返回分页结果")
        @WithUserVO(userId = 1L, studentId = "admin001", username = "管理员", roleName = "SUPER_ADMIN", permissions = {
                "enrollment:list", "enrollment:detail", "enrollment:approve", "enrollment:reject",
                "enrollment:statistics" })
        void getEnrollmentList_normalQuery_shouldReturnPagedResult() throws Exception {
            createTestEnroll(EnrollStatus.PENDING);
            createTestEnroll(EnrollStatus.APPROVED);

            mockMvc.perform(
                    get("/api/v1/admin/enrollments")
                            .param("page", "0")
                            .param("size", "10")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.content").isArray());
        }

        @Test
        @DisplayName("按状态筛选：应返回对应状态的报名")
        @WithUserVO(userId = 1L, studentId = "admin001", username = "管理员", roleName = "SUPER_ADMIN", permissions = {
                "enrollment:list" })
        void getEnrollmentList_filterByStatus_shouldReturnFiltered() throws Exception {
            createTestEnroll(EnrollStatus.PENDING);
            createTestEnroll(EnrollStatus.APPROVED);

            mockMvc.perform(
                    get("/api/v1/admin/enrollments")
                            .param("status", "pending")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.content").isArray());
        }

        @Test
        @DisplayName("按方向筛选：应返回对应方向的报名")
        @WithUserVO(userId = 1L, studentId = "admin001", username = "管理员", roleName = "SUPER_ADMIN", permissions = {
                "enrollment:list" })
        void getEnrollmentList_filterByDirection_shouldReturnFiltered() throws Exception {
            createTestEnroll(EnrollStatus.PENDING);

            mockMvc.perform(
                    get("/api/v1/admin/enrollments")
                            .param("direction", "computer_vision")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));
        }

        @Test
        @DisplayName("关键词搜索：应返回匹配的报名")
        @WithUserVO(userId = 1L, studentId = "admin001", username = "管理员", roleName = "SUPER_ADMIN", permissions = {
                "enrollment:list" })
        void getEnrollmentList_searchByKeyword_shouldReturnMatching() throws Exception {
            createTestEnroll(EnrollStatus.PENDING);

            mockMvc.perform(
                    get("/api/v1/admin/enrollments")
                            .param("keyword", TEST_USERNAME)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.content").isArray());
        }

        @Test
        @DisplayName("未登录访问：应返回401")
        void getEnrollmentList_noAuth_shouldReturn401() throws Exception {
            mockMvc.perform(
                    get("/api/v1/admin/enrollments")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("普通用户访问：应返回403")
        @WithUserVO(userId = 2L, studentId = "member001", username = "普通用户", roleName = "MEMBER")
        void getEnrollmentList_memberRole_shouldReturn403() throws Exception {
            mockMvc.perform(
                    get("/api/v1/admin/enrollments")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isForbidden());
        }
    }

    // ==================== 获取报名详情测试 ====================

    @Nested
    @DisplayName("GET /api/v1/admin/enrollments/{id} - 获取报名详情")
    class GetEnrollmentDetailTests {

        @Test
        @DisplayName("正常查询：应返回报名详情")
        @WithUserVO(userId = 1L, studentId = "admin001", username = "管理员", roleName = "SUPER_ADMIN", permissions = {
                "enrollment:detail" })
        void getEnrollmentDetail_existingId_shouldReturnDetail() throws Exception {
            Enroll enroll = createTestEnroll(EnrollStatus.PENDING);

            mockMvc.perform(
                    get("/api/v1/admin/enrollments/" + enroll.getId())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.id").value(enroll.getId()))
                    .andExpect(jsonPath("$.data.username").value(TEST_USERNAME));
        }

        @Test
        @DisplayName("报名不存在：应返回404")
        @WithUserVO(userId = 1L, studentId = "admin001", username = "管理员", roleName = "SUPER_ADMIN", permissions = {
                "enrollment:detail" })
        void getEnrollmentDetail_nonExistingId_shouldReturn404() throws Exception {
            mockMvc.perform(
                    get("/api/v1/admin/enrollments/999999")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.code").value(404));
        }

        @Test
        @DisplayName("未登录访问：应返回401")
        void getEnrollmentDetail_noAuth_shouldReturn401() throws Exception {
            mockMvc.perform(
                    get("/api/v1/admin/enrollments/1")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isUnauthorized());
        }
    }

    // ==================== 通过报名测试 ====================

    @Nested
    @DisplayName("PUT /api/v1/admin/enrollments/{id}/approve - 通过报名")
    class ApproveEnrollmentTests {

        @Test
        @DisplayName("正常审核通过：应更新状态并创建用户")
        @WithUserVO(userId = 1L, studentId = "admin001", username = "管理员", roleName = "SUPER_ADMIN", permissions = {
                "enrollment:approve" })
        void approveEnrollment_normalCase_shouldApproveAndCreateUser() throws Exception {
            Enroll enroll = createTestEnroll(EnrollStatus.PENDING);

            mockMvc.perform(
                    put("/api/v1/admin/enrollments/" + enroll.getId() + "/approve")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.status").value("APPROVED"));

            // 验证用户已创建
            User createdUser = userMapper.selectByStudentId(enroll.getStudentId());
            assert createdUser != null : "用户应该被创建";
            assert createdUser.getUsername().equals(enroll.getUsername()) : "用户名应该匹配";
        }

        @Test
        @DisplayName("报名不存在：应返回404")
        @WithUserVO(userId = 1L, studentId = "admin001", username = "管理员", roleName = "SUPER_ADMIN", permissions = {
                "enrollment:approve" })
        void approveEnrollment_nonExistingId_shouldReturn404() throws Exception {
            mockMvc.perform(
                    put("/api/v1/admin/enrollments/999999/approve")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.code").value(404));
        }

        @Test
        @DisplayName("非PENDING状态：应返回409")
        @WithUserVO(userId = 1L, studentId = "admin001", username = "管理员", roleName = "SUPER_ADMIN", permissions = {
                "enrollment:approve" })
        void approveEnrollment_notPendingStatus_shouldReturn409() throws Exception {
            Enroll enroll = createTestEnroll(EnrollStatus.APPROVED);

            mockMvc.perform(
                    put("/api/v1/admin/enrollments/" + enroll.getId() + "/approve")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.code").value(409));
        }

        @Test
        @DisplayName("用户已存在：应跳过用户创建")
        @WithUserVO(userId = 1L, studentId = "admin001", username = "管理员", roleName = "SUPER_ADMIN", permissions = {
                "enrollment:approve" })
        void approveEnrollment_userAlreadyExists_shouldSkipUserCreation() throws Exception {
            Enroll enroll = createTestEnroll(EnrollStatus.PENDING);

            // 创建已存在的用户
            User existingUser = User.builder()
                    .studentId(enroll.getStudentId())
                    .username("已存在用户")
                    .roleId(memberRoleId)
                    .disable(false)
                    .build();
            userMapper.insert(existingUser);

            mockMvc.perform(
                    put("/api/v1/admin/enrollments/" + enroll.getId() + "/approve")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.status").value("APPROVED"));
        }

        @Test
        @DisplayName("未登录访问：应返回401")
        void approveEnrollment_noAuth_shouldReturn401() throws Exception {
            mockMvc.perform(
                    put("/api/v1/admin/enrollments/1/approve")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("普通用户访问：应返回403")
        @WithUserVO(userId = 2L, studentId = "member001", username = "普通用户", roleName = "MEMBER")
        void approveEnrollment_memberRole_shouldReturn403() throws Exception {
            Enroll enroll = createTestEnroll(EnrollStatus.PENDING);

            mockMvc.perform(
                    put("/api/v1/admin/enrollments/" + enroll.getId() + "/approve")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isForbidden());
        }
    }

    // ==================== 拒绝报名测试 ====================

    @Nested
    @DisplayName("PUT /api/v1/admin/enrollments/{id}/reject - 拒绝报名")
    class RejectEnrollmentTests {

        @Test
        @DisplayName("正常拒绝：应更新状态为REJECTED")
        @WithUserVO(userId = 1L, studentId = "admin001", username = "管理员", roleName = "SUPER_ADMIN", permissions = {
                "enrollment:reject" })
        void rejectEnrollment_normalCase_shouldReject() throws Exception {
            Enroll enroll = createTestEnroll(EnrollStatus.PENDING);

            String requestBody = "{\"reason\":\"不符合条件\"}";

            mockMvc.perform(
                    put("/api/v1/admin/enrollments/" + enroll.getId() + "/reject")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.status").value("REJECTED"));
        }

        @Test
        @DisplayName("无拒绝原因：应正常处理")
        @WithUserVO(userId = 1L, studentId = "admin001", username = "管理员", roleName = "SUPER_ADMIN", permissions = {
                "enrollment:reject" })
        void rejectEnrollment_noReason_shouldReject() throws Exception {
            Enroll enroll = createTestEnroll(EnrollStatus.PENDING);

            mockMvc.perform(
                    put("/api/v1/admin/enrollments/" + enroll.getId() + "/reject")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.status").value("REJECTED"));
        }

        @Test
        @DisplayName("报名不存在：应返回404")
        @WithUserVO(userId = 1L, studentId = "admin001", username = "管理员", roleName = "SUPER_ADMIN", permissions = {
                "enrollment:reject" })
        void rejectEnrollment_nonExistingId_shouldReturn404() throws Exception {
            mockMvc.perform(
                    put("/api/v1/admin/enrollments/999999/reject")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.code").value(404));
        }

        @Test
        @DisplayName("非PENDING状态：应返回409")
        @WithUserVO(userId = 1L, studentId = "admin001", username = "管理员", roleName = "SUPER_ADMIN", permissions = {
                "enrollment:reject" })
        void rejectEnrollment_notPendingStatus_shouldReturn409() throws Exception {
            Enroll enroll = createTestEnroll(EnrollStatus.REJECTED);

            mockMvc.perform(
                    put("/api/v1/admin/enrollments/" + enroll.getId() + "/reject")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.code").value(409));
        }

        @Test
        @DisplayName("未登录访问：应返回401")
        void rejectEnrollment_noAuth_shouldReturn401() throws Exception {
            mockMvc.perform(
                    put("/api/v1/admin/enrollments/1/reject")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("普通用户访问：应返回403")
        @WithUserVO(userId = 2L, studentId = "member001", username = "普通用户", roleName = "MEMBER")
        void rejectEnrollment_memberRole_shouldReturn403() throws Exception {
            Enroll enroll = createTestEnroll(EnrollStatus.PENDING);

            mockMvc.perform(
                    put("/api/v1/admin/enrollments/" + enroll.getId() + "/reject")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isForbidden());
        }
    }

    // ==================== 报名统计测试 ====================

    @Nested
    @DisplayName("GET /api/v1/admin/enrollments/statistics - 报名统计")
    class GetStatisticsTests {

        @Test
        @DisplayName("正常查询：应返回统计数据")
        @WithUserVO(userId = 1L, studentId = "admin001", username = "管理员", roleName = "SUPER_ADMIN", permissions = {
                "enrollment:statistics" })
        void getStatistics_normalCase_shouldReturnStatistics() throws Exception {
            createTestEnroll(EnrollStatus.PENDING);
            createTestEnroll(EnrollStatus.PENDING);
            createTestEnroll(EnrollStatus.APPROVED);
            createTestEnroll(EnrollStatus.REJECTED);

            mockMvc.perform(
                    get("/api/v1/admin/enrollments/statistics")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.byStatus").exists())
                    .andExpect(jsonPath("$.data.byDirection").exists());
        }

        @Test
        @DisplayName("无数据：应返回零统计")
        @WithUserVO(userId = 1L, studentId = "admin001", username = "管理员", roleName = "SUPER_ADMIN", permissions = {
                "enrollment:statistics" })
        void getStatistics_noData_shouldReturnZeroStatistics() throws Exception {
            mockMvc.perform(
                    get("/api/v1/admin/enrollments/statistics")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data").exists());
        }

        @Test
        @DisplayName("未登录访问：应返回401")
        void getStatistics_noAuth_shouldReturn401() throws Exception {
            mockMvc.perform(
                    get("/api/v1/admin/enrollments/statistics")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("普通用户访问：应返回403")
        @WithUserVO(userId = 2L, studentId = "member001", username = "普通用户", roleName = "MEMBER")
        void getStatistics_memberRole_shouldReturn403() throws Exception {
            mockMvc.perform(
                    get("/api/v1/admin/enrollments/statistics")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isForbidden());
        }
    }
}
