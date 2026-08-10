package com.bluenet.web.api.controller.v1.venue;

import com.bluenet.web.BaseIntegrationTest;
import com.bluenet.web.api.dto.venue.VenueDTO;
import com.bluenet.web.api.converter.venue.VenueResponseConverter;
import com.bluenet.web.application.result.venue.VenueResult;
import com.bluenet.web.application.service.VenueAppService;
import com.bluenet.web.domain.exception.DataNotFound;
import com.bluenet.web.testconfig.TestSecurityConfig;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
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
@DisplayName("VenueController 集成测试")
class VenueControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private VenueAppService venueAppService;

    @MockitoBean
    private VenueResponseConverter venueResponseConverter;

    @AfterEach
    void tearDown() {
        // 公开接口，不涉及 UserCTX。
    }

    private VenueResult createResult(Long id, String name) {
        return new VenueResult(id, name, "副标题", "描述", "http://example.com/image", 1L, 10);
    }

    @Test
    @DisplayName("getVenueList: 应返回场地列表")
    void getVenueList_shouldReturnList() throws Exception {
        VenueResult result1 = createResult(1L, "实验室A");
        VenueResult result2 = createResult(2L, "实验室B");
        VenueDTO dto1 = VenueDTO.builder().id(1L).name("实验室A").build();
        VenueDTO dto2 = VenueDTO.builder().id(2L).name("实验室B").build();
        when(venueAppService.getAllVenues()).thenReturn(List.of(result1, result2));
        when(venueResponseConverter.toDTOList(any())).thenReturn(List.of(dto1, dto2));

        mockMvc.perform(get("/api/v1/venues"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data[0].id").value(1))
                .andExpect(jsonPath("$.data[0].name").value("实验室A"))
                .andExpect(jsonPath("$.data[1].name").value("实验室B"));
    }

    @Test
    @DisplayName("getVenueById: 存在时应返回场地详情")
    void getVenueById_existing_shouldReturnDetail() throws Exception {
        VenueResult result = createResult(1L, "实验室A");
        VenueDTO dto = VenueDTO.builder().id(1L).name("实验室A").description("描述").build();
        when(venueAppService.getVenueDetail(1L)).thenReturn(result);
        when(venueResponseConverter.toDTO(result)).thenReturn(dto);

        mockMvc.perform(get("/api/v1/venues/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.name").value("实验室A"));
    }

    @Test
    @DisplayName("getVenueById: 不存在时应返回 404")
    void getVenueById_nonExistent_shouldReturnNotFound() throws Exception {
        when(venueAppService.getVenueDetail(9999L)).thenThrow(new DataNotFound("场地不存在"));

        mockMvc.perform(get("/api/v1/venues/{id}", 9999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(404));
    }
}
