package com.bluenet.web.infrastructure.observability;

import com.bluenet.web.APIIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class ActuatorPrometheusDefaultsTest extends APIIntegrationTest {

    @Value("${management.endpoints.web.exposure.include}")
    private String exposedEndpoints;

    @Test
    void defaultConfigurationDoesNotExposePrometheus() {
        assertThat(exposedEndpoints).doesNotContain("prometheus");
    }
}
