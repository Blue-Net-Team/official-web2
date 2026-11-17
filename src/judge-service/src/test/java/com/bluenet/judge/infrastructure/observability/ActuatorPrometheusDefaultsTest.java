package com.bluenet.judge.infrastructure.observability;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "management.health.db.enabled=false",
        "management.health.rabbit.enabled=false",
        "spring.rabbitmq.listener.simple.auto-startup=false"
})
@AutoConfigureMockMvc
class ActuatorPrometheusDefaultsTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void defaultConfigurationDoesNotExposePrometheus() throws Exception {
        mockMvc.perform(get("/actuator/prometheus")).andExpect(status().isNotFound());
    }
}
