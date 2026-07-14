package com.bluenet.web.api.controller.v1.admin;

import com.bluenet.web.BaseIntegrationTest;
import com.bluenet.web.api.dto.achievement.AchievementDTO;
import com.bluenet.web.api.dto.achievement.CreateAchievementRequestDTO;
import com.bluenet.web.api.dto.achievement.UpdateAchievementRequestDTO;
import com.bluenet.web.api.converter.achievement.AchievementResponseConverter;
import com.bluenet.web.application.result.achievement.AchievementResult;
import com.bluenet.web.application.service.AchievementAppService;
import com.bluenet.web.domain.model.enumerate.AchievementType;
import com.bluenet.web.domain.model.enumerate.AwardLevel;
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

import java.time.LocalDate;

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
@DisplayName("AdminAchievementController 集成测试")
class AdminAchievementControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AchievementAppService achievementAppService;

    @MockitoBean
    private AchievementResponseConverter achievementResponseConverter;

    @AfterEach
    void tearDown() {
        UserCTX.clear();
    }

    private static final long SUPER_ADMIN_USER_ID = 9999L;

    private AchievementResult stubResult() {
        return new AchievementResult(
                1L,
                "蓝桥杯全国一等奖",
                AchievementType.COMPETITION,
                "蓝桥杯",
                LocalDate.of(2024, 4, 15),
                AwardLevel.NATIONAL,
                "国家级",
                "一等奖",
                "蓝桥杯",
                "蓝桥杯",
                100L,
                200L,
                "https://example.com/image.jpg");
    }

    private AchievementDTO stubDTO() {
        return AchievementDTO.builder()
                .id(1L)
                .title("蓝桥杯全国一等奖")
                .type(AchievementType.COMPETITION)
                .awardLevel(AwardLevel.NATIONAL)
                .build();
    }

    @Test
    @DisplayName("createAchievement: 超级管理员应成功创建成就")
    @WithSecurityPrincipal(userId = SUPER_ADMIN_USER_ID, roleType = "SUPER_ADMIN", roleId = 1L, permissions = {
            "achievement:create" })
    void createAchievement_asSuperAdmin_shouldReturnCreatedAchievement() throws Exception {
        AchievementResult result = stubResult();
        AchievementDTO dto = stubDTO();
        when(achievementAppService.createAchievement(any())).thenReturn(result);
        when(achievementResponseConverter.toDTO(any(AchievementResult.class))).thenReturn(dto);

        CreateAchievementRequestDTO request = CreateAchievementRequestDTO.builder()
                .title("蓝桥杯全国一等奖")
                .type(AchievementType.COMPETITION)
                .relateTo("蓝桥杯")
                .achieveAt(LocalDate.of(2024, 4, 15))
                .awardLevel(AwardLevel.NATIONAL)
                .awardName("一等奖")
                .fileId(200L)
                .build();

        MvcResult mvcResult = mockMvc.perform(
                post("/api/v1/admin/achievements")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.title").value("蓝桥杯全国一等奖"))
                .andReturn();
        assertThat(mvcResult.getResponse().getStatus()).isEqualTo(200);
    }

    @Test
    @DisplayName("createAchievement: 参数校验失败应返回 400")
    @WithSecurityPrincipal(userId = SUPER_ADMIN_USER_ID, roleType = "SUPER_ADMIN", roleId = 1L, permissions = {
            "achievement:create" })
    void createAchievement_withInvalidRequest_shouldReturn400() throws Exception {
        CreateAchievementRequestDTO request = CreateAchievementRequestDTO.builder()
                .title("")
                .build();

        MvcResult mvcResult = mockMvc.perform(
                post("/api/v1/admin/achievements")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andReturn();
        assertThat(mvcResult.getResponse().getStatus()).isEqualTo(400);
    }

    @Test
    @DisplayName("createAchievement: 普通成员访问应返回 403")
    @WithSecurityPrincipal(roleType = "MEMBER", roleId = 3L, permissions = {})
    void createAchievement_asMember_shouldReturn403() throws Exception {
        CreateAchievementRequestDTO request = CreateAchievementRequestDTO.builder()
                .title("测试成就")
                .type(AchievementType.COMPETITION)
                .achieveAt(LocalDate.of(2024, 4, 15))
                .fileId(1L)
                .build();

        mockMvc.perform(
                post("/api/v1/admin/achievements")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("updateAchievement: 超级管理员应成功更新成就")
    @WithSecurityPrincipal(userId = SUPER_ADMIN_USER_ID, roleType = "SUPER_ADMIN", roleId = 1L, permissions = {
            "achievement:update" })
    void updateAchievement_asSuperAdmin_shouldReturnUpdatedAchievement() throws Exception {
        AchievementResult result = stubResult();
        AchievementDTO dto = stubDTO();
        when(achievementAppService.updateAchievement(any())).thenReturn(result);
        when(achievementResponseConverter.toDTO(any(AchievementResult.class))).thenReturn(dto);

        UpdateAchievementRequestDTO request = UpdateAchievementRequestDTO.builder()
                .title("蓝桥杯全国一等奖")
                .type(AchievementType.COMPETITION)
                .relateTo("蓝桥杯")
                .achieveAt(LocalDate.of(2024, 4, 15))
                .awardLevel(AwardLevel.NATIONAL)
                .awardName("一等奖")
                .fileId(200L)
                .build();

        MvcResult mvcResult = mockMvc.perform(
                put("/api/v1/admin/achievements/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1))
                .andReturn();
        assertThat(mvcResult.getResponse().getStatus()).isEqualTo(200);
    }

    @Test
    @DisplayName("updateAchievement: 成就不存在应返回 404")
    @WithSecurityPrincipal(userId = SUPER_ADMIN_USER_ID, roleType = "SUPER_ADMIN", roleId = 1L, permissions = {
            "achievement:update" })
    void updateAchievement_notFound_shouldReturn404() throws Exception {
        when(achievementAppService.updateAchievement(any())).thenThrow(new DataNotFound("成就不存在"));

        UpdateAchievementRequestDTO request = UpdateAchievementRequestDTO.builder()
                .title("蓝桥杯全国一等奖")
                .type(AchievementType.COMPETITION)
                .achieveAt(LocalDate.of(2024, 4, 15))
                .fileId(200L)
                .build();

        MvcResult mvcResult = mockMvc.perform(
                put("/api/v1/admin/achievements/{id}", 999L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andReturn();
        assertThat(mvcResult.getResponse().getStatus()).isEqualTo(404);
    }

    @Test
    @DisplayName("deleteAchievement: 超级管理员应成功删除成就")
    @WithSecurityPrincipal(userId = SUPER_ADMIN_USER_ID, roleType = "SUPER_ADMIN", roleId = 1L, permissions = {
            "achievement:delete" })
    void deleteAchievement_asSuperAdmin_shouldReturnOk() throws Exception {
        doNothing().when(achievementAppService).deleteAchievement(1L);

        MvcResult mvcResult = mockMvc.perform(delete("/api/v1/admin/achievements/{id}", 1L))
                .andExpect(status().isOk())
                .andReturn();
        assertThat(mvcResult.getResponse().getStatus()).isEqualTo(200);
    }
}
