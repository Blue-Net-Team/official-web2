package com.bluenet.web.api.controller.v1.softwareresource;

import com.bluenet.web.BaseIntegrationTest;
import com.bluenet.web.api.dto.PageDTO;
import com.bluenet.web.api.dto.softwareresource.SoftwareResourceDTO;
import com.bluenet.web.api.converter.softwareresource.SoftwareResourceRequestConverter;
import com.bluenet.web.api.converter.softwareresource.SoftwareResourceResponseConverter;
import com.bluenet.web.application.result.softwareresource.SoftwareResourceResult;
import com.bluenet.web.application.service.SoftwareResourceAppService;
import com.bluenet.web.domain.model.enumerate.SoftwareResourceDirection;
import com.bluenet.web.domain.model.enumerate.SoftwareResourceStatus;
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
import org.springframework.data.domain.Pageable;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestSecurityConfig.class)
@DisplayName("SoftwareResourceController 集成测试")
class SoftwareResourceControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SoftwareResourceAppService softwareResourceAppService;

    @MockitoBean
    private SoftwareResourceRequestConverter requestConverter;

    @MockitoBean
    private SoftwareResourceResponseConverter responseConverter;

    @AfterEach
    void tearDown() {
        // 公开接口，不涉及 UserCTX。
    }

    @Test
    @DisplayName("listSoftwareResources: 默认参数应返回分页资源列表")
    void listSoftwareResources_defaultParams_shouldReturnPagedList() throws Exception {
        SoftwareResourceResult result = new SoftwareResourceResult(
                1L,
                "OpenCV",
                SoftwareResourceDirection.COMPUTER_VISION,
                "视觉库",
                "计算机视觉库",
                "https://opencv.org",
                10,
                SoftwareResourceStatus.ACTIVE);
        SoftwareResourceDTO dto = SoftwareResourceDTO.builder()
                .id(1L)
                .name("OpenCV")
                .direction(SoftwareResourceDirection.COMPUTER_VISION)
                .build();
        PageDTO<SoftwareResourceDTO> pageDTO = new PageDTO<>(
                List.of(dto), 1L, 1, 0, 20, 1, true, true, false);
        when(requestConverter.toDirection(any())).thenReturn(null);
        when(requestConverter.toPageable(any())).thenReturn(Pageable.ofSize(20));
        when(
                softwareResourceAppService.listActiveResources(
                        nullable(SoftwareResourceDirection.class),
                        nullable(String.class),
                        any(Pageable.class)))
                                .thenReturn(new PageImpl<>(List.of(result)));
        when(responseConverter.toPageDTO(any())).thenReturn(pageDTO);

        mockMvc.perform(get("/api/v1/software-resources"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.content[0].id").value(1))
                .andExpect(jsonPath("$.data.content[0].name").value("OpenCV"));
    }

    @Test
    @DisplayName("listSoftwareResources: 带方向与关键词参数应返回过滤结果")
    void listSoftwareResources_withFilter_shouldReturnFiltered() throws Exception {
        SoftwareResourceResult result = new SoftwareResourceResult(
                2L,
                "SolidWorks",
                SoftwareResourceDirection.STRUCTURAL_DESIGN,
                "CAD",
                "结构设计软件",
                "https://solidworks.com",
                20,
                SoftwareResourceStatus.ACTIVE);
        SoftwareResourceDTO dto = SoftwareResourceDTO.builder()
                .id(2L)
                .name("SolidWorks")
                .direction(SoftwareResourceDirection.STRUCTURAL_DESIGN)
                .build();
        PageDTO<SoftwareResourceDTO> pageDTO = new PageDTO<>(
                List.of(dto), 1L, 1, 0, 20, 1, true, true, false);
        when(requestConverter.toDirection(any())).thenReturn(SoftwareResourceDirection.STRUCTURAL_DESIGN);
        when(requestConverter.toPageable(any())).thenReturn(Pageable.ofSize(20));
        when(
                softwareResourceAppService
                        .listActiveResources(SoftwareResourceDirection.STRUCTURAL_DESIGN, "CAD", Pageable.ofSize(20)))
                                .thenReturn(new PageImpl<>(List.of(result)));
        when(responseConverter.toPageDTO(any())).thenReturn(pageDTO);

        mockMvc.perform(
                get("/api/v1/software-resources")
                        .param("direction", "STRUCTURAL_DESIGN")
                        .param("keyword", "CAD")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].direction").value("STRUCTURAL_DESIGN"));
    }

    @Test
    @DisplayName("listSoftwareResources: page 为负数时应返回 400")
    void listSoftwareResources_negativePage_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(
                get("/api/v1/software-resources")
                        .param("page", "-1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400));
    }
}
