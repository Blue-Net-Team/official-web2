package com.bluenet.web.api.controller.v1.admin;

import com.bluenet.web.BaseIntegrationTest;
import com.bluenet.web.api.converter.bugreport.BugReportRequestConverter;
import com.bluenet.web.api.converter.bugreport.BugReportResponseConverter;
import com.bluenet.web.api.dto.bugreport.BugReportBriefDTO;
import com.bluenet.web.api.dto.bugreport.BugReportDetailDTO;
import com.bluenet.web.application.query.bugreport.GetBugReportListQuery;
import com.bluenet.web.application.result.bugreport.BugReportResult;
import com.bluenet.web.application.service.BugReportAdminAppService;
import com.bluenet.web.domain.exception.DataNotFound;
import com.bluenet.web.domain.model.enumerate.BugReportStatus;
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
import org.springframework.data.domain.PageImpl;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestSecurityConfig.class)
@DisplayName("AdminBugReportController 集成测试")
class AdminBugReportControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private BugReportAdminAppService bugReportAdminAppService;

    @MockitoBean
    private BugReportResponseConverter responseConverter;

    @Autowired
    private BugReportRequestConverter requestConverter;

    @AfterEach
    void tearDown() {
        UserCTX.clear();
    }

    private static final long SUPER_ADMIN_USER_ID = 9999L;

    private BugReportResult.Brief briefResult() {
        return new BugReportResult.Brief(
                1L,
                "按钮无响应",
                "点击提交按钮无响应",
                "/home",
                "user@example.com",
                BugReportStatus.PENDING,
                0,
                null,
                null);
    }

    private BugReportBriefDTO briefDTO() {
        return BugReportBriefDTO.builder()
                .id(1L)
                .title("按钮无响应")
                .description("点击提交按钮无响应")
                .pageUrl("/home")
                .reporterEmail("user@example.com")
                .status(BugReportStatus.PENDING)
                .imageCount(0)
                .build();
    }

    @Test
    @DisplayName("getBugReportList: 普通成员访问应返回 403")
    @WithSecurityPrincipal(roleType = "MEMBER", roleId = 3L, permissions = {})
    void getBugReportList_asMember_shouldReturn403() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/v1/admin/bug-reports"))
                .andExpect(status().isForbidden())
                .andReturn();
        assertThat(result.getResponse().getStatus()).isEqualTo(403);
    }

    @Test
    @DisplayName("getBugReportList: 超级管理员应返回分页 Bug 报告列表")
    @WithSecurityPrincipal(userId = SUPER_ADMIN_USER_ID, roleType = "SUPER_ADMIN", roleId = 1L, permissions = {
            "bug-report:list" })
    void getBugReportList_asSuperAdmin_shouldReturnPagedReports() throws Exception {
        when(bugReportAdminAppService.getBugReportList(any(GetBugReportListQuery.class)))
                .thenReturn(new PageImpl<>(List.of(briefResult())));
        when(responseConverter.toBriefDTOPage(any())).thenReturn(new PageImpl<>(List.of(briefDTO())));

        MvcResult result = mockMvc.perform(
                get("/api/v1/admin/bug-reports")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content[0].id").value(1))
                .andReturn();
        assertThat(result.getResponse().getStatus()).isEqualTo(200);
    }

    @Test
    @DisplayName("getBugReportDetail: 超级管理员应返回 Bug 报告详情")
    @WithSecurityPrincipal(userId = SUPER_ADMIN_USER_ID, roleType = "SUPER_ADMIN", roleId = 1L, permissions = {
            "bug-report:detail" })
    void getBugReportDetail_asSuperAdmin_shouldReturnDetail() throws Exception {
        BugReportResult.Detail detailResult = new BugReportResult.Detail(
                1L,
                "按钮无响应",
                "点击提交按钮无响应",
                "/home",
                "{}",
                "user@example.com",
                BugReportStatus.PENDING,
                null,
                null,
                List.of());
        BugReportDetailDTO dto = BugReportDetailDTO.builder()
                .id(1L)
                .title("按钮无响应")
                .description("点击提交按钮无响应")
                .pageUrl("/home")
                .environmentJson("{}")
                .reporterEmail("user@example.com")
                .status(BugReportStatus.PENDING)
                .fileIds(List.of())
                .build();
        when(bugReportAdminAppService.getBugReportDetail(1L)).thenReturn(detailResult);
        when(responseConverter.toDetailDTO(any(BugReportResult.Detail.class))).thenReturn(dto);

        MvcResult result = mockMvc.perform(get("/api/v1/admin/bug-reports/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1))
                .andReturn();
        assertThat(result.getResponse().getStatus()).isEqualTo(200);
    }

    @Test
    @DisplayName("getBugReportDetail: 报告不存在时应返回 404")
    @WithSecurityPrincipal(userId = SUPER_ADMIN_USER_ID, roleType = "SUPER_ADMIN", roleId = 1L, permissions = {
            "bug-report:detail" })
    void getBugReportDetail_whenNotFound_shouldReturn404() throws Exception {
        when(bugReportAdminAppService.getBugReportDetail(1L)).thenThrow(new DataNotFound("报告不存在"));

        MvcResult result = mockMvc.perform(get("/api/v1/admin/bug-reports/{id}", 1L))
                .andExpect(status().isNotFound())
                .andReturn();
        assertThat(result.getResponse().getStatus()).isEqualTo(404);
    }
}
