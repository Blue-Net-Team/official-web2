package com.bluenet.web.api.controller.v1.competition;

import com.bluenet.web.api.dto.competition.CompetitionDetailDTO;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * CompetitionController 单元测试
 * <p>
 * 测试竞赛列表页面相关的控制器接口
 * </p>
 */
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
    private static final String TEST_LEVEL = "国家级";
    private static final String TEST_MONTH = "4月";
    private static final String TEST_ORGANIZER = "工业和信息化部人才交流中心";
    private static final String TEST_SUMMARY = "全国软件和信息技术专业人才大赛";
    private static final String TEST_DETAIL = "蓝桥杯全国软件和信息技术专业人才大赛详情";
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

    private CompetitionDetailDTO createTestCompetitionDetailDTO() {
        return CompetitionDetailDTO.builder()
                .id(TEST_ID)
                .name(TEST_NAME)
                .shortName(TEST_NAME)
                .level(TEST_LEVEL)
                .month(TEST_MONTH)
                .organizer(TEST_ORGANIZER)
                .summary(TEST_SUMMARY)
                .detail(TEST_DETAIL)
                .coverFileId(TEST_COVER_FILE_ID)
                .build();
    }

    // ==================== GET /api/v1/competitions ====================

    /**
     * 获取竞赛列表：应返回包含 level、month、organizer 字段的响应
     */
    @Test
    @DisplayName("获取竞赛列表：应返回包含 level、month、organizer 字段的响应")
    void getCompetitionList_shouldReturnCompetitionsWithAllFields() throws Exception {
        // 准备
        List<CompetitionResponseDTO> competitions = new ArrayList<>();
        competitions.add(createTestCompetitionResponseDTO());

        when(competitionService.getCompetitionResponseList(anyInt())).thenReturn(competitions);

        // 执行 & 验证
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

    /**
     * 获取竞赛列表：无参数时应使用默认 limit=10
     */
    @Test
    @DisplayName("获取竞赛列表：无参数时应使用默认 limit=10")
    void getCompetitionList_withoutLimitParam_shouldUseDefaultLimit() throws Exception {
        // 准备
        List<CompetitionResponseDTO> competitions = new ArrayList<>();
        when(competitionService.getCompetitionResponseList(anyInt())).thenReturn(competitions);

        // 执行 & 验证
        mockMvc.perform(
                get("/api/v1/competitions")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(competitionService).getCompetitionResponseList(10);
    }

    /**
     * 获取竞赛列表：空列表时应返回空数组
     */
    @Test
    @DisplayName("获取竞赛列表：空列表时应返回空数组")
    void getCompetitionList_emptyList_shouldReturnEmptyArray() throws Exception {
        // 准备
        when(competitionService.getCompetitionResponseList(anyInt())).thenReturn(new ArrayList<>());

        // 执行 & 验证
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

    /**
     * 获取竞赛列表：多个竞赛时应返回全部
     */
    @Test
    @DisplayName("获取竞赛列表：多个竞赛时应返回全部")
    void getCompetitionList_multipleCompetitions_shouldReturnAll() throws Exception {
        // 准备
        List<CompetitionResponseDTO> competitions = Arrays.asList(
                CompetitionResponseDTO.builder().id(1L).name("蓝桥杯").level("国家级").build(),
                CompetitionResponseDTO.builder().id(2L).name("ACM").level("国际级").build(),
                CompetitionResponseDTO.builder().id(3L).name("数学建模").level("省级").build());

        when(competitionService.getCompetitionResponseList(anyInt())).thenReturn(competitions);

        // 执行 & 验证
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

    // ==================== GET /api/v1/competitions/{id} ====================

    /**
     * 获取竞赛详情：应返回完整详情
     */
    @Test
    @DisplayName("获取竞赛详情：应返回完整详情")
    void getCompetitionDetail_shouldReturnCompleteDetail() throws Exception {
        // 准备
        CompetitionDetailDTO detail = createTestCompetitionDetailDTO();
        when(competitionService.getCompetitionDetail(eq(TEST_ID))).thenReturn(detail);

        // 执行 & 验证
        mockMvc.perform(
                get("/api/v1/competitions/{id}", TEST_ID)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(TEST_ID))
                .andExpect(jsonPath("$.data.name").value(TEST_NAME))
                .andExpect(jsonPath("$.data.level").value(TEST_LEVEL))
                .andExpect(jsonPath("$.data.month").value(TEST_MONTH))
                .andExpect(jsonPath("$.data.organizer").value(TEST_ORGANIZER))
                .andExpect(jsonPath("$.data.summary").value(TEST_SUMMARY))
                .andExpect(jsonPath("$.data.detail").value(TEST_DETAIL))
                .andExpect(jsonPath("$.data.coverFileId").value(TEST_COVER_FILE_ID));

        verify(competitionService).getCompetitionDetail(TEST_ID);
    }

    /**
     * 获取竞赛详情：竞赛不存在时应返回 404
     */
    @Test
    @DisplayName("获取竞赛详情：竞赛不存在时应返回 404")
    void getCompetitionDetail_notFound_shouldReturn404() throws Exception {
        // 准备
        when(competitionService.getCompetitionDetail(eq(99999L)))
                .thenThrow(new IllegalArgumentException("竞赛不存在"));

        // 执行 & 验证
        mockMvc.perform(
                get("/api/v1/competitions/{id}", 99999L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.msg").value("竞赛不存在"));

        verify(competitionService).getCompetitionDetail(99999L);
    }

    /**
     * 获取竞赛详情：有封面时应正确返回coverFileId
     */
    @Test
    @DisplayName("获取竞赛详情：有封面时应正确返回coverFileId")
    void getCompetitionDetail_withCover_shouldReturnCoverFileId() throws Exception {
        // 准备
        CompetitionDetailDTO detail = CompetitionDetailDTO.builder()
                .id(TEST_ID)
                .name(TEST_NAME)
                .coverFileId(200L)
                .build();

        when(competitionService.getCompetitionDetail(eq(TEST_ID))).thenReturn(detail);

        // 执行 & 验证
        mockMvc.perform(
                get("/api/v1/competitions/{id}", TEST_ID)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.coverFileId").value(200));

        verify(competitionService).getCompetitionDetail(TEST_ID);
    }

    /**
     * 获取竞赛详情：无封面时coverFileId应为null
     */
    @Test
    @DisplayName("获取竞赛详情：无封面时coverFileId应为null")
    void getCompetitionDetail_noCover_shouldReturnNullCoverFileId() throws Exception {
        // 准备
        CompetitionDetailDTO detail = CompetitionDetailDTO.builder()
                .id(TEST_ID)
                .name(TEST_NAME)
                .coverFileId(null)
                .build();

        when(competitionService.getCompetitionDetail(eq(TEST_ID))).thenReturn(detail);

        // 执行 & 验证
        mockMvc.perform(
                get("/api/v1/competitions/{id}", TEST_ID)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.coverFileId").isEmpty());

        verify(competitionService).getCompetitionDetail(TEST_ID);
    }
}
