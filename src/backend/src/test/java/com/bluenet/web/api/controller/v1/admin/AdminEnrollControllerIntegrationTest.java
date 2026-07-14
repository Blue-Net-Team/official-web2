package com.bluenet.web.api.controller.v1.admin;

import com.bluenet.web.BaseIntegrationTest;
import com.bluenet.web.api.dto.enrollment.ApproveEnrollmentRequestDTO;
import com.bluenet.web.api.dto.enrollment.EnrollmentApprovalResultDTO;
import com.bluenet.web.api.dto.enrollment.EnrollmentBriefDTO;
import com.bluenet.web.api.dto.enrollment.EnrollmentDetailDTO;
import com.bluenet.web.api.dto.enrollment.EnrollmentStatisticsDTO;
import com.bluenet.web.api.dto.enrollment.RejectEnrollmentRequestDTO;
import com.bluenet.web.api.converter.enroll.EnrollResponseConverter;
import com.bluenet.web.application.result.enroll.EnrollResult;
import com.bluenet.web.application.service.EnrollAppService;
import com.bluenet.web.domain.exception.DataNotFound;
import com.bluenet.web.domain.model.enumerate.Direction;
import com.bluenet.web.domain.model.enumerate.EnrollStatus;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestSecurityConfig.class)
@DisplayName("AdminEnrollController 集成测试")
class AdminEnrollControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private EnrollAppService enrollAppService;

    @MockitoBean
    private EnrollResponseConverter enrollResponseConverter;

    @AfterEach
    void tearDown() {
        UserCTX.clear();
    }

    private static final long SUPER_ADMIN_USER_ID = 9999L;

    private EnrollResult.Brief stubBriefResult() {
        return new EnrollResult.Brief(
                1L,
                "张三",
                "20210001001",
                "zhangsan@example.com",
                "计算机学院",
                "计算机科学与技术",
                Gender.MALE,
                Direction.COMPUTER_VISION,
                EnrollStatus.PENDING,
                100L);
    }

    private EnrollmentBriefDTO stubBriefDTO() {
        return EnrollmentBriefDTO.builder()
                .id(1L)
                .username("张三")
                .studentId("20210001001")
                .status(EnrollStatus.PENDING)
                .build();
    }

    @Test
    @DisplayName("getEnrollmentList: 超级管理员应返回分页报名列表")
    @WithSecurityPrincipal(userId = SUPER_ADMIN_USER_ID, roleType = "SUPER_ADMIN", roleId = 1L, permissions = {
            "enrollment:list" })
    void getEnrollmentList_asSuperAdmin_shouldReturnPagedEnrollments() throws Exception {
        Page<EnrollResult.Brief> resultPage = new PageImpl<>(List.of(stubBriefResult()));
        Page<EnrollmentBriefDTO> dtoPage = new PageImpl<>(List.of(stubBriefDTO()));
        when(enrollAppService.getEnrollmentList(any())).thenReturn(resultPage);
        when(enrollResponseConverter.toBriefDTOPage(any())).thenReturn(dtoPage);

        MvcResult mvcResult = mockMvc.perform(
                get("/api/v1/admin/enrollments")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content[0].id").value(1))
                .andReturn();
        assertThat(mvcResult.getResponse().getStatus()).isEqualTo(200);
    }

    @Test
    @DisplayName("getEnrollmentList: 普通成员访问应返回 403")
    @WithSecurityPrincipal(roleType = "MEMBER", roleId = 3L, permissions = {})
    void getEnrollmentList_asMember_shouldReturn403() throws Exception {
        mockMvc.perform(get("/api/v1/admin/enrollments"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("getEnrollmentDetail: 超级管理员应返回报名详情")
    @WithSecurityPrincipal(userId = SUPER_ADMIN_USER_ID, roleType = "SUPER_ADMIN", roleId = 1L, permissions = {
            "enrollment:detail" })
    void getEnrollmentDetail_asSuperAdmin_shouldReturnDetail() throws Exception {
        EnrollResult.Detail result = new EnrollResult.Detail(
                1L,
                "张三",
                "20210001001",
                "zhangsan@example.com",
                1L,
                "计算机学院",
                "计算机科学与技术",
                Gender.MALE,
                Direction.COMPUTER_VISION,
                100L,
                EnrollStatus.PENDING,
                "自我介绍",
                "ABC123",
                "推荐人");
        EnrollmentDetailDTO dto = EnrollmentDetailDTO.builder()
                .id(1L)
                .username("张三")
                .studentId("20210001001")
                .status(EnrollStatus.PENDING)
                .build();
        when(enrollAppService.getEnrollmentDetail(1L)).thenReturn(result);
        when(enrollResponseConverter.toDetailDTO(any(EnrollResult.Detail.class))).thenReturn(dto);

        MvcResult mvcResult = mockMvc.perform(get("/api/v1/admin/enrollments/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1))
                .andReturn();
        assertThat(mvcResult.getResponse().getStatus()).isEqualTo(200);
    }

    @Test
    @DisplayName("getEnrollmentDetail: 报名不存在应返回 404")
    @WithSecurityPrincipal(userId = SUPER_ADMIN_USER_ID, roleType = "SUPER_ADMIN", roleId = 1L, permissions = {
            "enrollment:detail" })
    void getEnrollmentDetail_notFound_shouldReturn404() throws Exception {
        when(enrollAppService.getEnrollmentDetail(999L)).thenThrow(new DataNotFound("报名记录不存在"));

        MvcResult mvcResult = mockMvc.perform(get("/api/v1/admin/enrollments/{id}", 999L))
                .andExpect(status().isNotFound())
                .andReturn();
        assertThat(mvcResult.getResponse().getStatus()).isEqualTo(404);
    }

    @Test
    @DisplayName("approveEnrollment: 超级管理员应成功通过报名")
    @WithSecurityPrincipal(userId = SUPER_ADMIN_USER_ID, roleType = "SUPER_ADMIN", roleId = 1L, permissions = {
            "enrollment:approve" })
    void approveEnrollment_asSuperAdmin_shouldReturnApprovedResult() throws Exception {
        EnrollResult.Approval result = new EnrollResult.Approval(1L, EnrollStatus.APPROVED, 100L);
        EnrollmentApprovalResultDTO dto = EnrollmentApprovalResultDTO.builder()
                .id(1L)
                .status(EnrollStatus.APPROVED)
                .createdUserId(100L)
                .build();
        when(enrollAppService.approveEnrollment(any(), any())).thenReturn(result);
        when(enrollResponseConverter.toApprovalDTO(any(EnrollResult.Approval.class))).thenReturn(dto);

        ApproveEnrollmentRequestDTO request = ApproveEnrollmentRequestDTO.builder()
                .assessmentGradeYear(2024)
                .build();

        MvcResult mvcResult = mockMvc.perform(
                put("/api/v1/admin/enrollments/{id}/approve", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("APPROVED"))
                .andReturn();
        assertThat(mvcResult.getResponse().getStatus()).isEqualTo(200);
    }

    @Test
    @DisplayName("approveEnrollment: 年份超出范围应返回 400")
    @WithSecurityPrincipal(userId = SUPER_ADMIN_USER_ID, roleType = "SUPER_ADMIN", roleId = 1L, permissions = {
            "enrollment:approve" })
    void approveEnrollment_withInvalidYear_shouldReturn400() throws Exception {
        ApproveEnrollmentRequestDTO request = ApproveEnrollmentRequestDTO.builder()
                .assessmentGradeYear(1999)
                .build();

        MvcResult mvcResult = mockMvc.perform(
                put("/api/v1/admin/enrollments/{id}/approve", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andReturn();
        assertThat(mvcResult.getResponse().getStatus()).isEqualTo(400);
    }

    @Test
    @DisplayName("rejectEnrollment: 超级管理员应成功拒绝报名")
    @WithSecurityPrincipal(userId = SUPER_ADMIN_USER_ID, roleType = "SUPER_ADMIN", roleId = 1L, permissions = {
            "enrollment:reject" })
    void rejectEnrollment_asSuperAdmin_shouldReturnRejectedResult() throws Exception {
        EnrollResult.Approval result = new EnrollResult.Approval(1L, EnrollStatus.REJECTED, null);
        EnrollmentApprovalResultDTO dto = EnrollmentApprovalResultDTO.builder()
                .id(1L)
                .status(EnrollStatus.REJECTED)
                .build();
        when(enrollAppService.rejectEnrollment(any(), any())).thenReturn(result);
        when(enrollResponseConverter.toApprovalDTO(any(EnrollResult.Approval.class))).thenReturn(dto);

        RejectEnrollmentRequestDTO request = RejectEnrollmentRequestDTO.builder()
                .reason("面试未通过")
                .build();

        MvcResult mvcResult = mockMvc.perform(
                put("/api/v1/admin/enrollments/{id}/reject", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("REJECTED"))
                .andReturn();
        assertThat(mvcResult.getResponse().getStatus()).isEqualTo(200);
    }

    @Test
    @DisplayName("getStatistics: 超级管理员应返回报名统计")
    @WithSecurityPrincipal(userId = SUPER_ADMIN_USER_ID, roleType = "SUPER_ADMIN", roleId = 1L, permissions = {
            "enrollment:statistics" })
    void getStatistics_asSuperAdmin_shouldReturnStatistics() throws Exception {
        EnrollResult.Statistics result = new EnrollResult.Statistics(
                10L,
                Map.of("PENDING", 5L, "APPROVED", 5L),
                Map.of(Direction.COMPUTER_VISION, 10L));
        EnrollmentStatisticsDTO dto = EnrollmentStatisticsDTO.builder()
                .total(10L)
                .byStatus(Map.of("PENDING", 5L, "APPROVED", 5L))
                .byDirection(Map.of(Direction.COMPUTER_VISION, 10L))
                .build();
        when(enrollAppService.getStatistics()).thenReturn(result);
        when(enrollResponseConverter.toStatisticsDTO(any(EnrollResult.Statistics.class))).thenReturn(dto);

        MvcResult mvcResult = mockMvc.perform(get("/api/v1/admin/enrollments/statistics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").value(10))
                .andReturn();
        assertThat(mvcResult.getResponse().getStatus()).isEqualTo(200);
    }
}
