package com.bluenet.judge.infrastructure.observability;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "management.server.port=0",
                "management.endpoints.web.exposure.include=health,info,metrics,prometheus",
                "management.prometheus.metrics.export.enabled=true",
                "management.health.db.enabled=false",
                "management.health.rabbit.enabled=false",
                "spring.rabbitmq.listener.simple.auto-startup=false"
        })
class ActuatorPrometheusIntegrationTest {

    @LocalServerPort
    private int businessPort;

    @Value("${local.management.port}")
    private int managementPort;

    private final TestRestTemplate http = new TestRestTemplate();

    @Test
    void prometheusIsAvailableOnlyOnManagementPort() {
        String managementBase = "http://localhost:" + managementPort;
        String businessBase = "http://localhost:" + businessPort;
        http.getForEntity(businessBase + "/api/v1/judge/health", String.class);
        http.getForEntity(managementBase + "/actuator/health", String.class);

        ResponseEntity<String> metrics = http.getForEntity(
                managementBase + "/actuator/prometheus", String.class);
        ResponseEntity<String> business = http.getForEntity(
                businessBase + "/actuator/prometheus", String.class);

        assertThat(metrics.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(metrics.getHeaders().getContentType().toString())
                .contains("text/plain");
        assertThat(metrics.getBody())
                .contains("jvm_memory_used_bytes")
                .contains("http_server_requests_seconds");
        assertThat(business.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }
}
