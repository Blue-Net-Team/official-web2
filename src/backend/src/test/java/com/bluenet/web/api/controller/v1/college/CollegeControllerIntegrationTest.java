package com.bluenet.web.api.controller.v1.college;

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
import com.bluenet.web.api.dto.college.CollegeDTO;
import com.bluenet.web.domain.model.entity.College;
import com.bluenet.web.infrastructure.repository.mapper.CollegeMapper;
import com.bluenet.web.testcontainers.TestcontainersConfiguration;

/**
 * CollegeController 集成测试
 * <p>
 * 测试公开接口 GET /api/v1/colleges
 * </p>
 */
@DisplayName("CollegeController 集成测试")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Testcontainers
@Import(TestcontainersConfiguration.class)
class CollegeControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private CollegeMapper collegeMapper;

    private static final String TEST_NAME_1 = "计算机科学与技术学院";
    private static final String TEST_NAME_2 = "软件学院";
    private static final String TEST_NAME_3 = "人工智能学院";

    private College createTestCollege(String name) {
        return College.create(name);
    }

    @BeforeEach
    void setUpTestData() {
        // 创建测试学院数据
        RepositoryTestObjects.insert(collegeMapper, createTestCollege(TEST_NAME_1), CollegeDO.class);
        RepositoryTestObjects.insert(collegeMapper, createTestCollege(TEST_NAME_2), CollegeDO.class);
        RepositoryTestObjects.insert(collegeMapper, createTestCollege(TEST_NAME_3), CollegeDO.class);
    }

    // ==================== GET /api/v1/colleges ====================

    /**
     * 集成测试：获取学院列表应成功
     */
    @Test
    @DisplayName("集成测试：获取学院列表应成功")
    void getAllColleges_shouldReturnListSuccessfully() {
        // 执行
        ResponseEntity<ResponseMessage<List<CollegeDTO>>> response = restTemplate.exchange(
                "/api/v1/colleges",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<ResponseMessage<List<CollegeDTO>>>() {
                });

        // 验证
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(200, response.getBody().getCode());

        List<CollegeDTO> colleges = response.getBody().getData();
        assertNotNull(colleges);
        assertEquals(3, colleges.size());

        // 验证学院名称存在
        List<String> names = colleges.stream().map(CollegeDTO::getName).toList();
        assertTrue(names.contains(TEST_NAME_1));
        assertTrue(names.contains(TEST_NAME_2));
        assertTrue(names.contains(TEST_NAME_3));

        // 验证每个学院都有ID和名称
        for (CollegeDTO college : colleges) {
            assertNotNull(college.getId());
            assertNotNull(college.getName());
        }
    }

    /**
     * 集成测试：无学院数据时应返回空列表
     */
    @Test
    @DisplayName("集成测试：无学院数据时应返回空列表")
    void getAllColleges_noColleges_shouldReturnEmptyList() {
        // 准备：清空学院数据（通过BaseIntegrationTest的setUp已经清空）
        // 由于setUp在每个测试前都会清空并重新插入数据，这里需要手动清空
        collegeMapper.delete(null);

        // 执行
        ResponseEntity<ResponseMessage<List<CollegeDTO>>> response = restTemplate.exchange(
                "/api/v1/colleges",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<ResponseMessage<List<CollegeDTO>>>() {
                });

        // 验证
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(200, response.getBody().getCode());

        List<CollegeDTO> colleges = response.getBody().getData();
        assertNotNull(colleges);
        assertTrue(colleges.isEmpty());
    }

    /**
     * 集成测试：公开接口无需认证即可访问
     */
    @Test
    @DisplayName("集成测试：公开接口无需认证即可访问")
    void getAllColleges_publicEndpoint_shouldNotRequireAuth() {
        // 执行：不携带任何认证信息
        ResponseEntity<ResponseMessage<List<CollegeDTO>>> response = restTemplate.exchange(
                "/api/v1/colleges",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<ResponseMessage<List<CollegeDTO>>>() {
                });

        // 验证：应返回200而非401
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(200, response.getBody().getCode());
    }

    /**
     * 集成测试：响应格式应正确
     */
    @Test
    @DisplayName("集成测试：响应格式应正确")
    void getAllColleges_responseFormat_shouldBeCorrect() {
        // 执行
        ResponseEntity<ResponseMessage<List<CollegeDTO>>> response = restTemplate.exchange(
                "/api/v1/colleges",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<ResponseMessage<List<CollegeDTO>>>() {
                });

        // 验证响应结构
        assertNotNull(response.getBody());
        assertNotNull(response.getBody().getCode());
        assertNotNull(response.getBody().getMsg());
        assertNotNull(response.getBody().getData());

        // 验证msg为成功消息
        assertEquals("Success", response.getBody().getMsg());
    }
}
