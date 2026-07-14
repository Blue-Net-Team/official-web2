package com.bluenet.web.api.controller.v1.admin;

import com.bluenet.web.BaseIntegrationTest;
import com.bluenet.web.api.dto.competition.BatchSortRequestDTO;
import com.bluenet.web.api.dto.competition.CompetitionRequestDTO;
import com.bluenet.web.api.dto.competition.MoveCompetitionRequestDTO;
import com.bluenet.web.application.result.competition.CompetitionResult;
import com.bluenet.web.application.service.CompetitionAppService;
import com.bluenet.web.domain.exception.DataNotFound;
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
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestSecurityConfig.class)
@DisplayName("AdminCompetitionController 集成测试")
class AdminCompetitionControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CompetitionAppService competitionAppService;

    @AfterEach
    void tearDown() {
        UserCTX.clear();
    }

    private static final long SUPER_ADMIN_USER_ID = 9999L;

    private CompetitionResult stubResult() {
        return new CompetitionResult(
                1L,
                "蓝桥杯",
                "蓝桥杯",
                "national",
                "4月",
                "工业和信息化部",
                "全国软件大赛",
                100L,
                200L,
                10);
    }

    @Test
    @DisplayName("createCompetition: 超级管理员应成功创建竞赛")
    @WithSecurityPrincipal(userId = SUPER_ADMIN_USER_ID, roleType = "SUPER_ADMIN", roleId = 1L, permissions = {
            "competition:create" })
    void createCompetition_asSuperAdmin_shouldReturnCreatedCompetition() throws Exception {
        when(competitionAppService.createCompetition(any())).thenReturn(stubResult());

        CompetitionRequestDTO request = CompetitionRequestDTO.builder()
                .name("蓝桥杯")
                .shortName("蓝桥杯")
                .level("national")
                .month("4月")
                .organizer("工业和信息化部")
                .summary("全国软件大赛")
                .logoFileId(100L)
                .coverFileId(200L)
                .build();

        MvcResult mvcResult = mockMvc.perform(
                post("/api/v1/admin/competitions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.name").value("蓝桥杯"))
                .andReturn();
        assertThat(mvcResult.getResponse().getStatus()).isEqualTo(200);
    }

    @Test
    @DisplayName("createCompetition: 普通成员访问应返回 403")
    @WithSecurityPrincipal(roleType = "MEMBER", roleId = 3L, permissions = {})
    void createCompetition_asMember_shouldReturn403() throws Exception {
        CompetitionRequestDTO request = CompetitionRequestDTO.builder()
                .name("测试竞赛")
                .build();

        mockMvc.perform(
                post("/api/v1/admin/competitions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("updateCompetition: 超级管理员应成功更新竞赛")
    @WithSecurityPrincipal(userId = SUPER_ADMIN_USER_ID, roleType = "SUPER_ADMIN", roleId = 1L, permissions = {
            "competition:update" })
    void updateCompetition_asSuperAdmin_shouldReturnUpdatedCompetition() throws Exception {
        when(competitionAppService.updateCompetition(any())).thenReturn(stubResult());

        CompetitionRequestDTO request = CompetitionRequestDTO.builder()
                .name("蓝桥杯")
                .shortName("蓝桥杯")
                .level("national")
                .month("4月")
                .organizer("工业和信息化部")
                .summary("全国软件大赛")
                .logoFileId(100L)
                .coverFileId(200L)
                .build();

        MvcResult mvcResult = mockMvc.perform(
                put("/api/v1/admin/competitions/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1))
                .andReturn();
        assertThat(mvcResult.getResponse().getStatus()).isEqualTo(200);
    }

    @Test
    @DisplayName("updateCompetition: 竞赛不存在应返回 404")
    @WithSecurityPrincipal(userId = SUPER_ADMIN_USER_ID, roleType = "SUPER_ADMIN", roleId = 1L, permissions = {
            "competition:update" })
    void updateCompetition_notFound_shouldReturn404() throws Exception {
        when(competitionAppService.updateCompetition(any())).thenThrow(new DataNotFound("竞赛不存在"));

        CompetitionRequestDTO request = CompetitionRequestDTO.builder()
                .name("蓝桥杯")
                .build();

        MvcResult mvcResult = mockMvc.perform(
                put("/api/v1/admin/competitions/{id}", 999L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andReturn();
        assertThat(mvcResult.getResponse().getStatus()).isEqualTo(404);
    }

    @Test
    @DisplayName("deleteCompetition: 超级管理员应成功删除竞赛")
    @WithSecurityPrincipal(userId = SUPER_ADMIN_USER_ID, roleType = "SUPER_ADMIN", roleId = 1L, permissions = {
            "competition:delete" })
    void deleteCompetition_asSuperAdmin_shouldReturnOk() throws Exception {
        doNothing().when(competitionAppService).deleteCompetition(1L);

        MvcResult mvcResult = mockMvc.perform(delete("/api/v1/admin/competitions/{id}", 1L))
                .andExpect(status().isOk())
                .andReturn();
        assertThat(mvcResult.getResponse().getStatus()).isEqualTo(200);
    }

    @Test
    @DisplayName("batchUpdateSortOrder: 超级管理员应成功批量调整竞赛排序")
    @WithSecurityPrincipal(userId = SUPER_ADMIN_USER_ID, roleType = "SUPER_ADMIN", roleId = 1L, permissions = {
            "competition:sort" })
    void batchUpdateSortOrder_asSuperAdmin_shouldReturnOk() throws Exception {
        doNothing().when(competitionAppService).batchUpdateSortOrder(any());

        BatchSortRequestDTO request = BatchSortRequestDTO.builder()
                .items(
                        List.of(
                                BatchSortRequestDTO.SortItemDTO.builder().id(1L).sortOrder(1).build(),
                                BatchSortRequestDTO.SortItemDTO.builder().id(2L).sortOrder(2).build()))
                .build();

        MvcResult mvcResult = mockMvc.perform(
                put("/api/v1/admin/competitions/sort")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn();
        assertThat(mvcResult.getResponse().getStatus()).isEqualTo(200);
    }

    @Test
    @DisplayName("batchUpdateSortOrder: 排序列表为空应返回 400")
    @WithSecurityPrincipal(userId = SUPER_ADMIN_USER_ID, roleType = "SUPER_ADMIN", roleId = 1L, permissions = {
            "competition:sort" })
    void batchUpdateSortOrder_withEmptyItems_shouldReturn400() throws Exception {
        BatchSortRequestDTO request = BatchSortRequestDTO.builder()
                .items(List.of())
                .build();

        MvcResult mvcResult = mockMvc.perform(
                put("/api/v1/admin/competitions/sort")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andReturn();
        assertThat(mvcResult.getResponse().getStatus()).isEqualTo(400);
    }

    @Test
    @DisplayName("moveCompetition: 超级管理员应成功移动竞赛排序")
    @WithSecurityPrincipal(userId = SUPER_ADMIN_USER_ID, roleType = "SUPER_ADMIN", roleId = 1L, permissions = {
            "competition:move" })
    void moveCompetition_asSuperAdmin_shouldReturnOk() throws Exception {
        doNothing().when(competitionAppService).moveCompetition(any());

        MoveCompetitionRequestDTO request = MoveCompetitionRequestDTO.builder()
                .direction("UP")
                .build();

        MvcResult mvcResult = mockMvc.perform(
                put("/api/v1/admin/competitions/{id}/move", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn();
        assertThat(mvcResult.getResponse().getStatus()).isEqualTo(200);
    }
}
