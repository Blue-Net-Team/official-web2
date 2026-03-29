package com.bluenet.web.api.controller.v1.equipment;

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
        File file = File.builder()
                .id(TEST_FILE_ID)
                .name(TEST_FILE_NAME)
                .url(TEST_FILE_URL)
                .type(TEST_FILE_TYPE)
                .build();
        fileMapper.insert(file);

        // 创建测试设备1
        Equipment equipment1 = new Equipment();
        equipment1.setName(TEST_NAME + "1");
        equipment1.setBrand(TEST_BRAND + "1");
        equipment1.setDescription(TEST_DESCRIPTION);
        equipment1.setImageFileId(TEST_FILE_ID);
        equipment1.setSortOrder(10);
        equipmentMapper.insert(equipment1);

        // 创建测试设备2
        Equipment equipment2 = new Equipment();
        equipment2.setName(TEST_NAME + "2");
        equipment2.setBrand(TEST_BRAND + "2");
        equipment2.setDescription(TEST_DESCRIPTION);
        equipment2.setImageFileId(TEST_FILE_ID);
        equipment2.setSortOrder(20);
        equipmentMapper.insert(equipment2);

        // 创建测试设备3
        Equipment equipment3 = new Equipment();
        equipment3.setName(TEST_NAME + "3");
        equipment3.setBrand(TEST_BRAND + "3");
        equipment3.setDescription(TEST_DESCRIPTION);
        equipment3.setImageFileId(TEST_FILE_ID);
        equipment3.setSortOrder(30);
        equipmentMapper.insert(equipment3);
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
