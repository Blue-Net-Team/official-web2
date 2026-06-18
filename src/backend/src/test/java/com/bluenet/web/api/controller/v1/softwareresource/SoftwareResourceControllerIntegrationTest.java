package com.bluenet.web.api.controller.v1.softwareresource;

import com.bluenet.web.BaseIntegrationTest;
import com.bluenet.web.api.dto.PageDTO;
import com.bluenet.web.api.dto.ResponseMessage;
import com.bluenet.web.api.dto.softwareresource.SoftwareResourceDTO;
import com.bluenet.web.domain.model.enumerate.Direction;
import com.bluenet.web.domain.model.enumerate.SoftwareResourceStatus;
import com.bluenet.web.infrastructure.repository.dataobject.SoftwareResourceDO;
import com.bluenet.web.infrastructure.repository.mapper.SoftwareResourceMapper;
import org.junit.jupiter.api.BeforeEach;
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
import com.bluenet.web.testcontainers.TestcontainersConfiguration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("SoftwareResourceController 集成测试")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Testcontainers
@Import(TestcontainersConfiguration.class)
class SoftwareResourceControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private SoftwareResourceMapper softwareResourceMapper;

    @BeforeEach
    void setUpTestData() {
        insertResource("CV Tool", Direction.COMPUTER_VISION, 1, SoftwareResourceStatus.ACTIVE);
        insertResource("CV Disabled", Direction.COMPUTER_VISION, 2, SoftwareResourceStatus.DISABLED);
        insertResource("General Tool", Direction.GENERAL, 3, SoftwareResourceStatus.ACTIVE);
        insertResource("Embed Tool", Direction.EMBEDDED, 4, SoftwareResourceStatus.ACTIVE);
    }

    private void insertResource(String name, Direction direction, int sortOrder, SoftwareResourceStatus status) {
        SoftwareResourceDO resource = SoftwareResourceDO.builder()
                .name(name)
                .direction(direction)
                .category("tool")
                .description("desc")
                .externalUrl("https://example.com/" + name)
                .sortOrder(sortOrder)
                .status(status)
                .build();
        softwareResourceMapper.insert(resource);
    }

    @Test
    @DisplayName("集成测试：按方向查询已启用资源")
    void list_byDirection_shouldReturnActiveResources() {
        ResponseEntity<ResponseMessage<PageDTO<SoftwareResourceDTO>>> response = restTemplate.exchange(
                "/api/v1/software-resources?direction=COMPUTER_VISION&page=0&size=10",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<ResponseMessage<PageDTO<SoftwareResourceDTO>>>() {
                });

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(200, response.getBody().getCode());
        PageDTO<SoftwareResourceDTO> page = response.getBody().getData();
        assertEquals(1, page.getTotalElements());
        assertEquals("CV Tool", page.getContent().get(0).getName());
    }

    @Test
    @DisplayName("集成测试：不指定方向时返回所有已启用资源")
    void list_withoutDirection_shouldReturnAllActiveResources() {
        ResponseEntity<ResponseMessage<PageDTO<SoftwareResourceDTO>>> response = restTemplate.exchange(
                "/api/v1/software-resources?page=0&size=10",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<ResponseMessage<PageDTO<SoftwareResourceDTO>>>() {
                });

        assertEquals(HttpStatus.OK, response.getStatusCode());
        PageDTO<SoftwareResourceDTO> page = response.getBody().getData();
        assertEquals(3, page.getTotalElements());
    }

    @Test
    @DisplayName("集成测试：公开接口无需认证")
    void list_public_shouldNotRequireAuth() {
        ResponseEntity<ResponseMessage<PageDTO<SoftwareResourceDTO>>> response = restTemplate.exchange(
                "/api/v1/software-resources?page=0&size=10",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<ResponseMessage<PageDTO<SoftwareResourceDTO>>>() {
                });

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(200, response.getBody().getCode());
    }

    @Test
    @DisplayName("集成测试：已禁用资源不返回")
    void list_shouldExcludeDisabled() {
        ResponseEntity<ResponseMessage<PageDTO<SoftwareResourceDTO>>> response = restTemplate.exchange(
                "/api/v1/software-resources?direction=COMPUTER_VISION&page=0&size=10",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<ResponseMessage<PageDTO<SoftwareResourceDTO>>>() {
                });

        PageDTO<SoftwareResourceDTO> page = response.getBody().getData();
        assertTrue(page.getContent().stream().noneMatch(r -> "CV Disabled".equals(r.getName())));
    }
}
