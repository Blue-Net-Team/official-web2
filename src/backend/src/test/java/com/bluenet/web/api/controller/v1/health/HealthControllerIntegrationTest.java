package com.bluenet.web.api.controller.v1.health;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Import;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.bluenet.web.BaseIntegrationTest;
import com.bluenet.web.api.dto.ResponseMessage;
import com.bluenet.web.api.dto.health.HealthStatusDTO;
import com.bluenet.web.testcontainers.TestcontainersConfiguration;

/**
 * 健康检查控制器集成测试。
 */
@DisplayName("HealthController 集成测试")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Testcontainers
@Import(TestcontainersConfiguration.class)
class HealthControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    @DisplayName("健康检查应返回有效状态")
    void health_shouldReturnValidStatus() {
        // When
        ResponseEntity<ResponseMessage<HealthStatusDTO>> response = restTemplate.exchange(
                "/api/v1/health",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<ResponseMessage<HealthStatusDTO>>() {
                });

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());

        ResponseMessage<HealthStatusDTO> body = response.getBody();
        assertEquals(200, body.getCode());
        assertEquals("Success", body.getMsg());
        assertNotNull(body.getData());

        HealthStatusDTO healthStatus = body.getData();
        // 状态应该是 UP 或 DOWN（取决于 MinIO 等服务的可用性)
        assertTrue(
                "UP".equals(healthStatus.getStatus()) || "DOWN".equals(healthStatus.getStatus()),
                "Status should be UP or DOWN");
        assertNotNull(healthStatus.getComponents());
    }

    @Test
    @DisplayName("健康检查应公开可访问")
    void health_shouldBePubliclyAccessible() {
        // When - 不带认证访问
        ResponseEntity<ResponseMessage<HealthStatusDTO>> response = restTemplate.exchange(
                "/api/v1/health",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<ResponseMessage<HealthStatusDTO>>() {
                });

        // Then - 应该成功访问
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(200, response.getBody().getCode());
    }

    @Test
    @DisplayName("健康检查应返回组件状态")
    void health_shouldReturnComponents() {
        // When
        ResponseEntity<ResponseMessage<HealthStatusDTO>> response = restTemplate.exchange(
                "/api/v1/health",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<ResponseMessage<HealthStatusDTO>>() {
                });

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        HealthStatusDTO healthStatus = response.getBody().getData();

        // 验证组件映射存在且非空
        assertNotNull(healthStatus.getComponents());
        assertFalse(healthStatus.getComponents().isEmpty(), "Should have at least one component");

        // 验证组件状态格式正确
        healthStatus.getComponents().forEach((name, component) -> {
            assertTrue(
                    "UP".equals(component.getStatus()) || "DOWN".equals(component.getStatus()),
                    "Component '" + name + "' status should be UP or DOWN");
        });
    }
}
