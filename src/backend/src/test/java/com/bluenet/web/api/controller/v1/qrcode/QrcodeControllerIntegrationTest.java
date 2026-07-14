package com.bluenet.web.api.controller.v1.qrcode;

import com.bluenet.web.BaseIntegrationTest;
import com.bluenet.web.api.dto.qrcode.ConsultationQrcodeDTO;
import com.bluenet.web.api.converter.qrcode.QrcodeResponseConverter;
import com.bluenet.web.application.result.qrcode.QrcodeResult;
import com.bluenet.web.application.service.QrcodeAppService;
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
@DisplayName("QrcodeController 集成测试")
class QrcodeControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private QrcodeAppService qrcodeAppService;

    @MockitoBean
    private QrcodeResponseConverter qrcodeResponseConverter;

    @AfterEach
    void tearDown() {
        // 公开接口，不涉及 UserCTX。
    }

    @Test
    @DisplayName("getConsultationQrcodes: 应返回咨询群二维码列表")
    void getConsultationQrcodes_shouldReturnList() throws Exception {
        QrcodeResult result1 = new QrcodeResult(1L, 100L);
        QrcodeResult result2 = new QrcodeResult(2L, 200L);
        ConsultationQrcodeDTO dto1 = ConsultationQrcodeDTO.builder().id(1L).fileId(100L).build();
        ConsultationQrcodeDTO dto2 = ConsultationQrcodeDTO.builder().id(2L).fileId(200L).build();
        when(qrcodeAppService.getConsultationQrcodes()).thenReturn(List.of(result1, result2));
        when(qrcodeResponseConverter.toConsultationDTOList(any())).thenReturn(List.of(dto1, dto2));

        mockMvc.perform(get("/api/v1/qrcodes/consultation"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data[0].id").value(1))
                .andExpect(jsonPath("$.data[0].fileId").value(100))
                .andExpect(jsonPath("$.data[1].id").value(2));
    }

    @Test
    @DisplayName("getConsultationQrcodes: 空列表时应返回空数组")
    void getConsultationQrcodes_empty_shouldReturnEmptyList() throws Exception {
        when(qrcodeAppService.getConsultationQrcodes()).thenReturn(List.of());
        when(qrcodeResponseConverter.toConsultationDTOList(any())).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/qrcodes/consultation"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data").isEmpty());
    }
}
