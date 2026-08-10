package com.bluenet.web.api.controller.v1.equipment;

import com.bluenet.web.BaseIntegrationTest;
import com.bluenet.web.api.dto.equipment.EquipmentDTO;
import com.bluenet.web.api.converter.equipment.EquipmentResponseConverter;
import com.bluenet.web.application.result.equipment.EquipmentResult;
import com.bluenet.web.application.service.EquipmentAppService;
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
@DisplayName("EquipmentController 集成测试")
class EquipmentControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private EquipmentAppService equipmentAppService;

    @MockitoBean
    private EquipmentResponseConverter equipmentResponseConverter;

    @AfterEach
    void tearDown() {
        // 公开接口，不涉及 UserCTX。
    }

    private EquipmentResult createResult(Long id, String name) {
        return new EquipmentResult(id, name, "品牌", "描述", "http://example.com/image", 1L, 10);
    }

    @Test
    @DisplayName("getEquipmentList: 应返回设备列表")
    void getEquipmentList_shouldReturnList() throws Exception {
        EquipmentResult result1 = createResult(1L, "服务器");
        EquipmentResult result2 = createResult(2L, "相机");
        EquipmentDTO dto1 = EquipmentDTO.builder().id(1L).name("服务器").build();
        EquipmentDTO dto2 = EquipmentDTO.builder().id(2L).name("相机").build();
        when(equipmentAppService.getAllEquipments()).thenReturn(List.of(result1, result2));
        when(equipmentResponseConverter.toDTOList(any())).thenReturn(List.of(dto1, dto2));

        mockMvc.perform(get("/api/v1/equipments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data[0].id").value(1))
                .andExpect(jsonPath("$.data[0].name").value("服务器"))
                .andExpect(jsonPath("$.data[1].name").value("相机"));
    }

    @Test
    @DisplayName("getEquipmentById: 存在时应返回设备详情")
    void getEquipmentById_existing_shouldReturnDetail() throws Exception {
        EquipmentResult result = createResult(1L, "服务器");
        EquipmentDTO dto = EquipmentDTO.builder().id(1L).name("服务器").brand("品牌").build();
        when(equipmentAppService.getEquipmentDetail(1L)).thenReturn(result);
        when(equipmentResponseConverter.toDTO(result)).thenReturn(dto);

        mockMvc.perform(get("/api/v1/equipments/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.name").value("服务器"));
    }

    @Test
    @DisplayName("getEquipmentById: 不存在时应返回 404")
    void getEquipmentById_nonExistent_shouldReturnNotFound() throws Exception {
        when(equipmentAppService.getEquipmentDetail(9999L)).thenThrow(new DataNotFound("设备不存在"));

        mockMvc.perform(get("/api/v1/equipments/{id}", 9999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(404));
    }
}
