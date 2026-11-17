package com.bluenet.web.infrastructure.observability;

import com.bluenet.web.APIIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.TestPropertySource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
        "management.server.port=0",
        "management.endpoints.web.exposure.include=health,prometheus",
        "management.prometheus.metrics.export.enabled=true"
})
@TestPropertySource(properties = {
        "management.server.port=0",
        "management.endpoints.web.exposure.include=health,prometheus",
        "management.prometheus.metrics.export.enabled=true"
})
class ActuatorPrometheusIntegrationTest extends APIIntegrationTest {

    @LocalServerPort
    private int businessPort;

    @Value("${local.management.port}")
    private int managementPort;

    private final TestRestTemplate http = new TestRestTemplate();

    @Test
    void prometheusIsAvailableOnlyOnManagementPort() {
        String managementBase = "http://localhost:" + managementPort;
        String businessBase = "http://localhost:" + businessPort;
        http.getForEntity(businessBase + "/api/v1/health", String.class);
        http.getForEntity(managementBase + "/actuator/health", String.class);

        ResponseEntity<String> metrics = http.getForEntity(
                managementBase + "/actuator/prometheus",
                String.class);
        ResponseEntity<String> business = http.getForEntity(
                businessBase + "/actuator/prometheus",
                String.class);

        assertThat(metrics.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(metrics.getHeaders().getContentType().toString())
                .contains("text/plain");
        assertThat(metrics.getBody())
                .contains("jvm_memory_used_bytes")
                .contains("http_server_requests_seconds");
        assertThat(business.getStatusCode()).isNotEqualTo(HttpStatus.OK);
        assertThat(business.getBody()).doesNotContain("jvm_memory_used_bytes");
    }

    @Test
    void httpMetricsDoNotUseRawRequestUrlsOrEntityIdsAsLabels() {
        String businessBase = "http://localhost:" + businessPort;
        http.getForEntity(businessBase + "/api/v1/algorithm/jobs/user-123/task-456", String.class);

        ResponseEntity<String> metrics = http.getForEntity(
                "http://localhost:" + managementPort + "/actuator/prometheus",
                String.class);

        assertThat(metrics.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(metrics.getBody())
                .contains("http_server_requests_seconds")
                .doesNotContain("user-123")
                .doesNotContain("task-456")
                .doesNotContain("uri=\"http");
    }
}
