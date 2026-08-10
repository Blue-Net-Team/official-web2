package com.bluenet.web.api.controller.v1.college;

import com.bluenet.web.BaseIntegrationTest;
import com.bluenet.web.api.dto.college.CollegeDTO;
import com.bluenet.web.api.converter.college.CollegeResponseConverter;
import com.bluenet.web.application.result.college.CollegeResult;
import com.bluenet.web.application.service.CollegeAppService;
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
@DisplayName("CollegeController 集成测试")
class CollegeControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CollegeAppService collegeAppService;

    @MockitoBean
    private CollegeResponseConverter collegeResponseConverter;

    @AfterEach
    void tearDown() {
        // 公开接口，不涉及 UserCTX。
    }

    @Test
    @DisplayName("getAllColleges: 应返回学院列表")
    void getAllColleges_shouldReturnCollegeList() throws Exception {
        CollegeResult result1 = new CollegeResult(1L, "计算机学院");
        CollegeResult result2 = new CollegeResult(2L, "软件学院");
        CollegeDTO dto1 = CollegeDTO.builder().id(1L).name("计算机学院").build();
        CollegeDTO dto2 = CollegeDTO.builder().id(2L).name("软件学院").build();
        when(collegeAppService.getAllColleges()).thenReturn(List.of(result1, result2));
        when(collegeResponseConverter.toDTOList(any())).thenReturn(List.of(dto1, dto2));

        mockMvc.perform(get("/api/v1/colleges"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].id").value(1))
                .andExpect(jsonPath("$.data[0].name").value("计算机学院"))
                .andExpect(jsonPath("$.data[1].name").value("软件学院"));
    }

    @Test
    @DisplayName("getAllColleges: 空列表时应返回空数组")
    void getAllColleges_empty_shouldReturnEmptyList() throws Exception {
        when(collegeAppService.getAllColleges()).thenReturn(List.of());
        when(collegeResponseConverter.toDTOList(any())).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/colleges"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data").isEmpty());
    }
}
