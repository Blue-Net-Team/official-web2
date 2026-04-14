package com.bluenet.web.api.controller.v1.competition;

import com.bluenet.web.api.dto.competition.CompetitionResponseDTO;
import com.bluenet.web.application.service.CompetitionService;
import com.bluenet.web.testcontainers.TestcontainersConfiguration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("CompetitionController 单元测试")
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Testcontainers
@Import(TestcontainersConfiguration.class)
class CompetitionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CompetitionService competitionService;

    private static final Long TEST_ID = 1L;
    private static final String TEST_NAME = "蓝桥杯";
    private static final String TEST_LEVEL = "national";
    private static final String TEST_MONTH = "4月";
    private static final String TEST_ORGANIZER = "工业和信息化部人才交流中心";
    private static final String TEST_SUMMARY = "全国软件和信息技术专业人才大赛";
    private static final Long TEST_COVER_FILE_ID = 100L;

    private CompetitionResponseDTO createTestCompetitionResponseDTO() {
        return CompetitionResponseDTO.builder()
                .id(TEST_ID)
                .name(TEST_NAME)
                .level(TEST_LEVEL)
                .month(TEST_MONTH)
                .organizer(TEST_ORGANIZER)
                .summary(TEST_SUMMARY)
                .coverFileId(TEST_COVER_FILE_ID)
                .build();
    }

    @Test
    @DisplayName("获取竞赛列表：应返回包含 level、month、organizer 字段的响应")
    void getCompetitionList_shouldReturnCompetitionsWithAllFields() throws Exception {
        List<CompetitionResponseDTO> competitions = new ArrayList<>();
        competitions.add(createTestCompetitionResponseDTO());

        when(competitionService.getCompetitionResponseList(anyInt())).thenReturn(competitions);

        mockMvc.perform(
                get("/api/v1/competitions")
                        .param("limit", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.msg").value("Success"))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].id").value(TEST_ID))
                .andExpect(jsonPath("$.data[0].name").value(TEST_NAME))
                .andExpect(jsonPath("$.data[0].level").value(TEST_LEVEL))
                .andExpect(jsonPath("$.data[0].month").value(TEST_MONTH))
                .andExpect(jsonPath("$.data[0].organizer").value(TEST_ORGANIZER))
                .andExpect(jsonPath("$.data[0].summary").value(TEST_SUMMARY))
                .andExpect(jsonPath("$.data[0].coverFileId").value(TEST_COVER_FILE_ID));

        verify(competitionService).getCompetitionResponseList(10);
    }

    @Test
    @DisplayName("获取竞赛列表：无参数时应使用默认 limit=10")
    void getCompetitionList_withoutLimitParam_shouldUseDefaultLimit() throws Exception {
        List<CompetitionResponseDTO> competitions = new ArrayList<>();
        when(competitionService.getCompetitionResponseList(anyInt())).thenReturn(competitions);

        mockMvc.perform(
                get("/api/v1/competitions")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(competitionService).getCompetitionResponseList(10);
    }

    @Test
    @DisplayName("获取竞赛列表：空列表时应返回空数组")
    void getCompetitionList_emptyList_shouldReturnEmptyArray() throws Exception {
        when(competitionService.getCompetitionResponseList(anyInt())).thenReturn(new ArrayList<>());

        mockMvc.perform(
                get("/api/v1/competitions")
                        .param("limit", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data").isEmpty());

        verify(competitionService).getCompetitionResponseList(10);
    }

    @Test
    @DisplayName("获取竞赛列表：多个竞赛时应返回全部")
    void getCompetitionList_multipleCompetitions_shouldReturnAll() throws Exception {
        List<CompetitionResponseDTO> competitions = Arrays.asList(
                CompetitionResponseDTO.builder().id(1L).name("蓝桥杯").level("national").build(),
                CompetitionResponseDTO.builder().id(2L).name("ACM").level("national").build(),
                CompetitionResponseDTO.builder().id(3L).name("数学建模").level("provincial").build());

        when(competitionService.getCompetitionResponseList(anyInt())).thenReturn(competitions);

        mockMvc.perform(
                get("/api/v1/competitions")
                        .param("limit", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(3))
                .andExpect(jsonPath("$.data[0].name").value("蓝桥杯"))
                .andExpect(jsonPath("$.data[1].name").value("ACM"))
                .andExpect(jsonPath("$.data[2].name").value("数学建模"));

        verify(competitionService).getCompetitionResponseList(10);
    }
}
