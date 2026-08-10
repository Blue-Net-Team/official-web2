package com.bluenet.web.api.controller.v1.enrollform;

import com.bluenet.web.BaseIntegrationTest;
import com.bluenet.web.application.result.enrollform.EnrollFormResult;
import com.bluenet.web.application.service.EnrollFormAppService;
import com.bluenet.web.testconfig.TestSecurityConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestSecurityConfig.class)
@DisplayName("EnrollFormController 集成测试")
class EnrollFormControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private EnrollFormAppService enrollFormAppService;

    @Test
    @DisplayName("getCurrentEnrollForm: 匿名用户应能获取当前报名表")
    void getCurrentEnrollForm_anonymous_shouldReturnForm() throws Exception {
        EnrollFormResult result = new EnrollFormResult(100L, LocalDateTime.of(2026, 8, 8, 12, 0));
        when(enrollFormAppService.getCurrentEnrollForm()).thenReturn(Optional.of(result));

        mockMvc.perform(get("/api/v1/enroll-form"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.fileId").value(100))
                .andExpect(jsonPath("$.data.createdAt").exists());
    }

    @Test
    @DisplayName("getCurrentEnrollForm: 无报名表时应返回成功且 data 为 null")
    void getCurrentEnrollForm_noForm_shouldReturnNullData() throws Exception {
        when(enrollFormAppService.getCurrentEnrollForm()).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/enroll-form"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(org.hamcrest.Matchers.nullValue()));
    }
}
