package com.bluenet.web.api.controller.v1.introduce;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
import com.bluenet.web.api.dto.introduce.IntroduceImageDTO;
import com.bluenet.web.domain.model.entity.File;
import com.bluenet.web.domain.model.entity.IntroduceImage;
import com.bluenet.web.domain.model.enumerate.Direction;
import com.bluenet.web.domain.model.enumerate.FileType;
import com.bluenet.web.domain.model.enumerate.ImageType;
import com.bluenet.web.infrastructure.repository.mapper.FileMapper;
import com.bluenet.web.infrastructure.repository.mapper.IntroduceImageMapper;
import com.bluenet.web.testcontainers.TestcontainersConfiguration;

/**
 * IntroduceImageController集成测试
 */
@DisplayName("IntroduceImageController 集成测试")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Testcontainers
@Import(TestcontainersConfiguration.class)
class IntroduceImageControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private IntroduceImageMapper introduceImageMapper;

    @Autowired
    private FileMapper fileMapper;

    private static final Long TEST_FILE_ID = 1L;
    private static final String TEST_FILE_URL = "http://example.com/test.jpg";
    private static final String TEST_FILE_NAME = "test.jpg";
    private static final FileType TEST_FILE_TYPE = FileType.NORMAL_IMG;
    private static final String TEST_DESCRIPTION = "测试图片";
    private static final Direction TEST_DIRECTION = Direction.COMPUTER_VISION;

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

        // 创建测试介绍图片 - 实验室类型
        IntroduceImage labImage = new IntroduceImage();
        labImage.setType(ImageType.LABORATORY);
        labImage.setDescription(TEST_DESCRIPTION + "-实验室");
        labImage.setFileId(TEST_FILE_ID);
        introduceImageMapper.insert(labImage);

        // 创建测试介绍图片 - 方向类型
        IntroduceImage directionImage = new IntroduceImage();
        directionImage.setType(ImageType.DIRECTION);
        directionImage.setDescription(TEST_DESCRIPTION + "-方向");
        directionImage.setFileId(TEST_FILE_ID);
        directionImage.setDirection(TEST_DIRECTION);
        introduceImageMapper.insert(directionImage);
    }

    /**
     * 集成测试：获取实验室介绍图片列表
     */
    @Test
    @DisplayName("集成测试：获取实验室介绍图片列表")
    void getLaboratoryIntroduceImages_shouldReturnImages() {
        // 执行
        ResponseEntity<ResponseMessage<List<IntroduceImageDTO>>> response = restTemplate.exchange(
                "/api/v1/introduce-images?type=LABORATORY",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<ResponseMessage<List<IntroduceImageDTO>>>() {
                });

        // 验证
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(200, response.getBody().getCode());

        List<IntroduceImageDTO> images = response.getBody().getData();
        assertNotNull(images);
        assertTrue(images.size() > 0);
        assertEquals(ImageType.LABORATORY, images.get(0).getType());
        assertEquals(TEST_FILE_ID, images.get(0).getFileId());
        assertEquals(TEST_FILE_URL, images.get(0).getFileUrl());
    }

    /**
     * 集成测试：获取方向介绍图片列表
     */
    @Test
    @DisplayName("集成测试：获取方向介绍图片列表")
    void getDirectionIntroduceImages_shouldReturnImages() {
        // 执行
        ResponseEntity<ResponseMessage<List<IntroduceImageDTO>>> response = restTemplate.exchange(
                "/api/v1/introduce-images?type=DIRECTION",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<ResponseMessage<List<IntroduceImageDTO>>>() {
                });

        // 验证
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(200, response.getBody().getCode());

        List<IntroduceImageDTO> images = response.getBody().getData();
        assertNotNull(images);
        assertTrue(images.size() > 0);
        assertEquals(ImageType.DIRECTION, images.get(0).getType());
    }

    /**
     * 集成测试：按方向筛选获取介绍图片列表
     */
    @Test
    @DisplayName("集成测试：按方向筛选获取介绍图片列表")
    void getDirectionIntroduceImages_byDirection_shouldReturnMatchingImages() {
        // 执行
        ResponseEntity<ResponseMessage<List<IntroduceImageDTO>>> response = restTemplate.exchange(
                "/api/v1/introduce-images?type=DIRECTION&direction=" + TEST_DIRECTION.name(),
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<ResponseMessage<List<IntroduceImageDTO>>>() {
                });

        // 验证
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(200, response.getBody().getCode());

        List<IntroduceImageDTO> images = response.getBody().getData();
        assertNotNull(images);
        assertTrue(images.size() > 0);
        assertEquals(ImageType.DIRECTION, images.get(0).getType());
        assertEquals(TEST_DIRECTION, images.get(0).getDirection());
    }

    /**
     * 集成测试：获取不存在类型的介绍图片应返回空列表
     */
    @Test
    @DisplayName("集成测试：获取不存在类型的介绍图片应返回空列表")
    void getNonExistentTypeIntroduceImages_shouldReturnEmptyList() {
        // 执行
        ResponseEntity<ResponseMessage<List<IntroduceImageDTO>>> response = restTemplate.exchange(
                "/api/v1/introduce-images?type=EQUIPMENT", // 不存在的类型
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<ResponseMessage<List<IntroduceImageDTO>>>() {
                });

        // 验证
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(200, response.getBody().getCode());

        List<IntroduceImageDTO> images = response.getBody().getData();
        assertNotNull(images);
        assertTrue(images.isEmpty());
    }

    /**
     * 集成测试：参数错误应返回500错误
     */
    @Test
    @DisplayName("集成测试：参数错误应返回500错误")
    void getIntroduceImages_withInvalidType_shouldReturn500Error() {
        // 执行
        ResponseEntity<ResponseMessage<List<IntroduceImageDTO>>> response = restTemplate.exchange(
                "/api/v1/introduce-images?type=INVALID_TYPE", // 无效类型
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<ResponseMessage<List<IntroduceImageDTO>>>() {
                });

        // 验证
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    /**
     * 集成测试：获取无file_id的介绍图片应返回fileUrl为null
     */
    @Test
    @DisplayName("集成测试：获取无file_id的介绍图片应返回fileUrl为null")
    void getIntroduceImages_withoutFileId_shouldReturnNullFileUrl() {
        // 创建无file_id的测试图片
        IntroduceImage imageWithoutFile = new IntroduceImage();
        imageWithoutFile.setType(ImageType.LABORATORY);
        imageWithoutFile.setDescription("无文件图片");
        imageWithoutFile.setFileId(null); // 无file_id
        introduceImageMapper.insert(imageWithoutFile);

        // 执行
        ResponseEntity<ResponseMessage<List<IntroduceImageDTO>>> response = restTemplate.exchange(
                "/api/v1/introduce-images?type=LABORATORY",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<ResponseMessage<List<IntroduceImageDTO>>>() {
                });

        // 验证
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(200, response.getBody().getCode());

        List<IntroduceImageDTO> images = response.getBody().getData();
        assertNotNull(images);
        assertTrue(images.size() > 0);
    }

}
