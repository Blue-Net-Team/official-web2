package com.bluenet.web.api.controller.v1.competition;

import com.bluenet.web.BaseIntegrationTest;
import com.bluenet.web.api.dto.PageDTO;
import com.bluenet.web.api.dto.competition.CompetitionResponseDTO;
import com.bluenet.web.api.converter.competition.CompetitionResponseConverter;
import com.bluenet.web.application.service.CompetitionAppService;
import com.bluenet.web.domain.model.readmodel.CompetitionReadModel;
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

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestSecurityConfig.class)
@DisplayName("CompetitionController 集成测试")
class CompetitionControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CompetitionAppService competitionAppService;

    @MockitoBean
    private CompetitionResponseConverter responseConverter;

    @AfterEach
    void tearDown() {
        // 公开接口，不涉及 UserCTX。
    }

    @Test
    @DisplayName("getCompetitionList: 默认参数应返回竞赛列表")
    void getCompetitionList_defaultParams_shouldReturnList() throws Exception {
        CompetitionReadModel readModel = CompetitionReadModel.builder()
                .id(1L)
                .name("蓝桥杯")
                .shortName("蓝桥杯简称")
                .level("national")
                .month("10")
                .organizer("主办方")
                .summary("简介")
                .sortOrder(100)
                .build();
        CompetitionResponseDTO dto = CompetitionResponseDTO.builder()
                .id(1L)
                .name("蓝桥杯")
                .level("national")
                .build();
        when(competitionAppService.getCompetitionResponseList(10)).thenReturn(List.of(readModel));
        when(responseConverter.toDTOList(any())).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/v1/competitions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data[0].id").value(1))
                .andExpect(jsonPath("$.data[0].name").value("蓝桥杯"));
    }

    @Test
    @DisplayName("getCompetitionList: 自定义 limit 应被传递")
    void getCompetitionList_withLimit_shouldPassLimit() throws Exception {
        when(competitionAppService.getCompetitionResponseList(5)).thenReturn(List.of());
        when(responseConverter.toDTOList(any())).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/competitions").param("limit", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    @DisplayName("getCompetitionPage: 分页参数应返回分页结果")
    void getCompetitionPage_withParams_shouldReturnPaged() throws Exception {
        CompetitionReadModel readModel = CompetitionReadModel.builder()
                .id(1L)
                .name("ACM")
                .shortName("ACM简称")
                .level("provincial")
                .month("11")
                .organizer("ACM主办方")
                .summary("ACM简介")
                .sortOrder(50)
                .build();
        CompetitionResponseDTO dto = CompetitionResponseDTO.builder()
                .id(1L)
                .name("ACM")
                .build();
        PageDTO<CompetitionResponseDTO> pageDTO = new PageDTO<>(
                List.of(dto), 1L, 1, 0, 10, 1, true, true, false);
        when(competitionAppService.getCompetitionPage(0, 10)).thenReturn(new PageImpl<>(List.of(readModel)));
        when(responseConverter.toPageDTO(any())).thenReturn(pageDTO);

        mockMvc.perform(
                get("/api/v1/competitions/page")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.content[0].id").value(1))
                .andExpect(jsonPath("$.data.content[0].name").value("ACM"));
    }
}
