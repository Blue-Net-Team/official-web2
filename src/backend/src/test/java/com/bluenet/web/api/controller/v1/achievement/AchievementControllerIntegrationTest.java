package com.bluenet.web.api.controller.v1.achievement;

import com.bluenet.web.BaseIntegrationTest;
import com.bluenet.web.api.dto.PageDTO;
import com.bluenet.web.api.dto.achievement.AchievementDTO;
import com.bluenet.web.api.dto.achievement.AchievementStatsDTO;
import com.bluenet.web.api.converter.achievement.AchievementResponseConverter;
import com.bluenet.web.application.result.achievement.AchievementResult;
import com.bluenet.web.application.result.achievement.AchievementStatistics;
import com.bluenet.web.application.service.AchievementAppService;
import com.bluenet.web.domain.model.enumerate.AchievementType;
import com.bluenet.web.domain.model.enumerate.AwardLevel;
import com.bluenet.web.testconfig.TestSecurityConfig;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestSecurityConfig.class)
@DisplayName("AchievementController 集成测试")
class AchievementControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AchievementAppService achievementAppService;

    @MockitoBean
    private AchievementResponseConverter achievementResponseConverter;

    @AfterEach
    void tearDown() {
        // 公开接口，不涉及 UserCTX。
    }

    @Test
    @DisplayName("getAchievements: 默认参数应返回分页成就列表")
    void getAchievements_defaultParams_shouldReturnPagedList() throws Exception {
        AchievementResult result = new AchievementResult(
                1L,
                "全国一等奖",
                AchievementType.COMPETITION,
                "蓝桥杯",
                LocalDate.of(2024, 5, 1),
                AwardLevel.NATIONAL,
                "国家级",
                "一等奖",
                "蓝桥杯",
                "蓝桥杯",
                10L,
                20L,
                "http://example.com/file",
                List.of(),
                List.of());
        AchievementDTO dto = AchievementDTO.builder()
                .id(1L)
                .title("全国一等奖")
                .type(AchievementType.COMPETITION)
                .build();
        when(achievementAppService.getAchievements(0, 12, null, null, null))
                .thenReturn(new PageImpl<>(List.of(result)));
        when(achievementResponseConverter.toDTOPage(any())).thenReturn(
                new PageDTO<>(List.of(dto), 1L, 1, 0, 12, 1, true, true, false));

        mockMvc.perform(get("/api/v1/achievements"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.content[0].id").value(1))
                .andExpect(jsonPath("$.data.content[0].title").value("全国一等奖"));
    }

    @Test
    @DisplayName("getAchievements: 带筛选参数应返回过滤结果")
    void getAchievements_withFilter_shouldReturnFilteredList() throws Exception {
        AchievementResult result = new AchievementResult(
                2L,
                "省级二等奖",
                AchievementType.COMPETITION,
                "ACM",
                LocalDate.of(2024, 6, 1),
                AwardLevel.PROVINCIAL,
                "省级",
                "二等奖",
                "ACM",
                "ACM",
                null,
                null,
                null,
                List.of(),
                List.of());
        AchievementDTO dto = AchievementDTO.builder()
                .id(2L)
                .title("省级二等奖")
                .awardLevel(AwardLevel.PROVINCIAL)
                .build();
        when(achievementAppService.getAchievements(0, 12, AchievementType.COMPETITION, AwardLevel.PROVINCIAL, 2024))
                .thenReturn(new PageImpl<>(List.of(result)));
        when(achievementResponseConverter.toDTOPage(any())).thenReturn(
                new PageDTO<>(List.of(dto), 1L, 1, 0, 12, 1, true, true, false));

        mockMvc.perform(
                get("/api/v1/achievements")
                        .param("type", "COMPETITION")
                        .param("awardLevel", "PROVINCIAL")
                        .param("year", "2024"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].awardLevel").value("PROVINCIAL"));
    }

    @Test
    @DisplayName("getAchievementStats: 应返回成就统计数据")
    void getAchievementStats_shouldReturnStats() throws Exception {
        AchievementStatistics statistics = AchievementStatistics.builder()
                .totalAchievements(10L)
                .nationalCount(3L)
                .provincialCount(4L)
                .schoolCount(3L)
                .build();
        AchievementStatsDTO dto = AchievementStatsDTO.builder()
                .totalAchievements(10L)
                .nationalCount(3L)
                .provincialCount(4L)
                .schoolCount(3L)
                .build();
        when(achievementAppService.getAchievementStats()).thenReturn(statistics);
        when(achievementResponseConverter.toStatsDTO(statistics)).thenReturn(dto);

        mockMvc.perform(get("/api/v1/achievements/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.totalAchievements").value(10))
                .andExpect(jsonPath("$.data.nationalCount").value(3));
    }
}
