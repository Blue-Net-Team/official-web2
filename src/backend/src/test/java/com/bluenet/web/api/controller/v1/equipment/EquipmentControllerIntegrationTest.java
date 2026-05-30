package com.bluenet.web.api.controller.v1.equipment;

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
import com.bluenet.web.api.dto.equipment.EquipmentDTO;
import com.bluenet.web.domain.model.entity.Equipment;
import com.bluenet.web.domain.model.entity.File;
import com.bluenet.web.domain.model.enumerate.FileStatus;
import com.bluenet.web.domain.model.enumerate.FileType;
import com.bluenet.web.infrastructure.repository.mapper.EquipmentMapper;
import com.bluenet.web.infrastructure.repository.mapper.FileMapper;
import com.bluenet.web.testcontainers.TestcontainersConfiguration;

/**
 * EquipmentController 集成测试
 */
@DisplayName("EquipmentController 集成测试")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Testcontainers
@Import(TestcontainersConfiguration.class)
class EquipmentControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private EquipmentMapper equipmentMapper;

    @Autowired
    private FileMapper fileMapper;

    private static final Long TEST_FILE_ID = 1L;
    private static final String TEST_FILE_URL = "http://example.com/equipment.jpg";
    private static final String TEST_FILE_NAME = "equipment.jpg";
    private static final FileType TEST_FILE_TYPE = FileType.NORMAL_IMG;
    private static final String TEST_NAME = "测试设备";
    private static final String TEST_BRAND = "测试品牌";
    private static final String TEST_DESCRIPTION = "这是一个测试设备";

    @BeforeEach
    void setUpTestData() {
        // 创建测试文件
        File file = File
                .reconstruct(TEST_FILE_ID, TEST_FILE_NAME, TEST_FILE_TYPE, TEST_FILE_URL, FileStatus.ACTIVE, null);
        RepositoryTestObjects.insert(fileMapper, file, FileDO.class);

        // 创建测试设备1
        Equipment equipment1 = Equipment
                .reconstruct(null, TEST_NAME + "1", TEST_BRAND + "1", TEST_DESCRIPTION, TEST_FILE_ID, 10);
        RepositoryTestObjects.insert(equipmentMapper, equipment1, EquipmentDO.class);

        // 创建测试设备2
        Equipment equipment2 = Equipment
                .reconstruct(null, TEST_NAME + "2", TEST_BRAND + "2", TEST_DESCRIPTION, TEST_FILE_ID, 20);
        RepositoryTestObjects.insert(equipmentMapper, equipment2, EquipmentDO.class);

        // 创建测试设备3
        Equipment equipment3 = Equipment
                .reconstruct(null, TEST_NAME + "3", TEST_BRAND + "3", TEST_DESCRIPTION, TEST_FILE_ID, 30);
        RepositoryTestObjects.insert(equipmentMapper, equipment3, EquipmentDO.class);
    }

    @Test
    @DisplayName("获取所有设备列表：应返回按排序倒序的设备列表")
    void getAllEquipments_shouldReturnSortedList() {
        // 执行
        ResponseEntity<ResponseMessage<List<EquipmentDTO>>> response = restTemplate.exchange(
                "/api/v1/equipments",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<ResponseMessage<List<EquipmentDTO>>>() {
                });

        // 验证
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(200, response.getBody().getCode());
        assertNotNull(response.getBody().getData());

        List<EquipmentDTO> equipments = response.getBody().getData();
        assertEquals(3, equipments.size());

        // 验证按sortOrder倒序排列
        assertEquals(TEST_NAME + "3", equipments.get(0).getName());
        assertEquals(TEST_NAME + "2", equipments.get(1).getName());
        assertEquals(TEST_NAME + "1", equipments.get(2).getName());
    }

    @Test
    @DisplayName("根据ID获取设备：设备存在时应返回设备详情")
    void getEquipmentById_existingEquipment_shouldReturnEquipment() {
        // 先获取列表拿到ID
        ResponseEntity<ResponseMessage<List<EquipmentDTO>>> listResponse = restTemplate.exchange(
                "/api/v1/equipments",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<ResponseMessage<List<EquipmentDTO>>>() {
                });

        Long equipmentId = listResponse.getBody().getData().get(0).getId();

        // 执行
        ResponseEntity<ResponseMessage<EquipmentDTO>> response = restTemplate.exchange(
                "/api/v1/equipments/" + equipmentId,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<ResponseMessage<EquipmentDTO>>() {
                });

        // 验证
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(200, response.getBody().getCode());
        assertNotNull(response.getBody().getData());

        EquipmentDTO equipment = response.getBody().getData();
        assertEquals(equipmentId, equipment.getId());
        assertNotNull(equipment.getName());
        assertNotNull(equipment.getBrand());
    }

    @Test
    @DisplayName("根据ID获取设备：设备不存在时应返回404")
    void getEquipmentById_nonExistingEquipment_shouldReturn404() {
        // 执行
        ResponseEntity<ResponseMessage<EquipmentDTO>> response = restTemplate.exchange(
                "/api/v1/equipments/99999",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<ResponseMessage<EquipmentDTO>>() {
                });

        // 验证
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(404, response.getBody().getCode());
        assertEquals("设备不存在", response.getBody().getMsg());
    }
}
