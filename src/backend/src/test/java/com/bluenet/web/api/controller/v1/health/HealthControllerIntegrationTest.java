package com.bluenet.web.api.controller.v1.health;

import com.bluenet.web.BaseIntegrationTest;
import com.bluenet.web.testconfig.TestSecurityConfig;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthEndpoint;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestSecurityConfig.class)
@DisplayName("HealthController 集成测试")
class HealthControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private HealthEndpoint healthEndpoint;

    @AfterEach
    void tearDown() {
        // HealthController 不涉及 UserCTX，无需清理。
    }

    @Test
    @DisplayName("health: 所有组件正常时应返回 200 与 UP 状态")
    void health_allComponentsUp_shouldReturnOk() throws Exception {
        when(healthEndpoint.health()).thenReturn(Health.up().build());

        mockMvc.perform(get("/api/v1/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.status").value("UP"));
    }

    @Test
    @DisplayName("health: 组件异常时应返回 503 与 DOWN 状态")
    void health_componentDown_shouldReturnServiceUnavailable() throws Exception {
        when(healthEndpoint.health()).thenReturn(Health.down().build());

        mockMvc.perform(get("/api/v1/health"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.code").value(503))
                .andExpect(jsonPath("$.data.status").value("DOWN"));
    }
}
