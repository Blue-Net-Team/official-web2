package com.bluenet.web.api.controller.v1.venue;

import com.bluenet.web.infrastructure.repository.dataobject.*;

import com.bluenet.web.testsupport.RepositoryTestObjects;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

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

import com.bluenet.web.BaseIntegrationTest;
import com.bluenet.web.api.dto.ResponseMessage;
import com.bluenet.web.api.dto.venue.VenueDTO;
import com.bluenet.web.domain.model.entity.File;
import com.bluenet.web.domain.model.entity.Venue;
import com.bluenet.web.domain.model.enumerate.FileType;
import com.bluenet.web.infrastructure.repository.mapper.FileMapper;
import com.bluenet.web.infrastructure.repository.mapper.VenueMapper;
import com.bluenet.web.testcontainers.TestcontainersConfiguration;

/**
 * VenueController 集成测试
 */
@DisplayName("VenueController 集成测试")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Testcontainers
@Import(TestcontainersConfiguration.class)
class VenueControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private VenueMapper venueMapper;

    @Autowired
    private FileMapper fileMapper;

    private static final Long TEST_FILE_ID = 1L;
    private static final String TEST_FILE_URL = "http://example.com/venue.jpg";
    private static final String TEST_FILE_NAME = "venue.jpg";
    private static final FileType TEST_FILE_TYPE = FileType.NORMAL_IMG;
    private static final String TEST_NAME = "测试场地";
    private static final String TEST_SUBTITLE = "测试副标题";
    private static final String TEST_DESCRIPTION = "这是一个测试场地";

    @BeforeEach
    void setUpTestData() {
        // 创建测试文件
        File file = File.builder()
                .id(TEST_FILE_ID)
                .name(TEST_FILE_NAME)
                .url(TEST_FILE_URL)
                .type(TEST_FILE_TYPE)
                .build();
        RepositoryTestObjects.insert(fileMapper, file, FileDO.class);

        // 创建测试场地1
        Venue venue1 = Venue
                .reconstruct(null, TEST_NAME + "1", TEST_SUBTITLE + "1", TEST_DESCRIPTION, TEST_FILE_ID, 10);
        RepositoryTestObjects.insert(venueMapper, venue1, VenueDO.class);

        // 创建测试场地2
        Venue venue2 = Venue
                .reconstruct(null, TEST_NAME + "2", TEST_SUBTITLE + "2", TEST_DESCRIPTION, TEST_FILE_ID, 20);
        RepositoryTestObjects.insert(venueMapper, venue2, VenueDO.class);
    }

    @Test
    @DisplayName("获取所有场地列表：应返回按排序倒序的场地列表")
    void getAllVenues_shouldReturnSortedList() {
        // 执行
        ResponseEntity<ResponseMessage<List<VenueDTO>>> response = restTemplate.exchange(
                "/api/v1/venues",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<ResponseMessage<List<VenueDTO>>>() {
                });

        // 验证
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(200, response.getBody().getCode());
        assertNotNull(response.getBody().getData());

        List<VenueDTO> venues = response.getBody().getData();
        assertEquals(2, venues.size());

        // 验证按sortOrder倒序排列
        assertEquals(TEST_NAME + "2", venues.get(0).getName());
        assertEquals(TEST_NAME + "1", venues.get(1).getName());
    }

    @Test
    @DisplayName("根据ID获取场地：场地存在时应返回场地详情")
    void getVenueById_existingVenue_shouldReturnVenue() {
        // 先获取列表拿到ID
        ResponseEntity<ResponseMessage<List<VenueDTO>>> listResponse = restTemplate.exchange(
                "/api/v1/venues",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<ResponseMessage<List<VenueDTO>>>() {
                });

        Long venueId = listResponse.getBody().getData().get(0).getId();

        // 执行
        ResponseEntity<ResponseMessage<VenueDTO>> response = restTemplate.exchange(
                "/api/v1/venues/" + venueId,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<ResponseMessage<VenueDTO>>() {
                });

        // 验证
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(200, response.getBody().getCode());
        assertNotNull(response.getBody().getData());

        VenueDTO venue = response.getBody().getData();
        assertEquals(venueId, venue.getId());
        assertNotNull(venue.getName());
        assertNotNull(venue.getSubtitle());
    }

    @Test
    @DisplayName("根据ID获取场地：场地不存在时应返回404")
    void getVenueById_nonExistingVenue_shouldReturn404() {
        // 执行
        ResponseEntity<ResponseMessage<VenueDTO>> response = restTemplate.exchange(
                "/api/v1/venues/99999",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<ResponseMessage<VenueDTO>>() {
                });

        // 验证
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(404, response.getBody().getCode());
        assertEquals("场地不存在", response.getBody().getMsg());
    }
}
