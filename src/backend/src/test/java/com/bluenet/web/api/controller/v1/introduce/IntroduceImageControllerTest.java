package com.bluenet.web.api.controller.v1.introduce;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.bluenet.web.api.dto.introduce.IntroduceImageDTO;
import com.bluenet.web.application.service.IntroduceImageService;
import com.bluenet.web.domain.model.enumerate.Direction;
import com.bluenet.web.domain.model.enumerate.ImageType;
import com.bluenet.web.testcontainers.TestcontainersConfiguration;

@DisplayName("IntroduceImageController 单元测试")
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Testcontainers
@Import(TestcontainersConfiguration.class)
class IntroduceImageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private IntroduceImageService introduceImageService;

    private static final Long TEST_ID = 1L;
    private static final Long TEST_FILE_ID = 100L;
    private static final String TEST_DESCRIPTION = "测试图片";
    private static final String TEST_FILE_URL = "http://example.com/image.jpg";
    private static final Direction TEST_DIRECTION = Direction.COMPUTER_VISION;

    private IntroduceImageDTO createTestIntroduceImageDTO() {
        return IntroduceImageDTO.builder()
                .id(TEST_ID)
                .type(ImageType.LABORATORY)
                .description(TEST_DESCRIPTION)
                .fileId(TEST_FILE_ID)
                .fileUrl(TEST_FILE_URL)
                .direction(TEST_DIRECTION)
                .build();
    }

    /**
     * 获取介绍图片列表：应返回200成功响应
     */
    @Test
    @DisplayName("获取介绍图片列表：应返回200成功响应")
    void getIntroduceImages_shouldReturn200Success() throws Exception {
        // 准备
        ImageType type = ImageType.LABORATORY;
        List<IntroduceImageDTO> expectedDTOs = new ArrayList<>();
        expectedDTOs.add(createTestIntroduceImageDTO());

        when(introduceImageService.getIntroduceImages(type, null)).thenReturn(expectedDTOs);

        // 执行 & 验证
        mockMvc.perform(
                get("/api/v1/introduce-images").param("type", type.name()).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.msg").value("Success"))
                .andExpect(jsonPath("$.data[0].id").value(TEST_ID))
                .andExpect(jsonPath("$.data[0].type").value(type.name()))
                .andExpect(jsonPath("$.data[0].description").value(TEST_DESCRIPTION));

        verify(introduceImageService).getIntroduceImages(type, null);
    }

    /**
     * 获取介绍图片列表：带方向参数应返回200成功响应
     */
    @Test
    @DisplayName("获取介绍图片列表：带方向参数应返回200成功响应")
    void getIntroduceImages_withDirection_shouldReturn200Success() throws Exception {
        // 准备
        ImageType type = ImageType.DIRECTION;
        Direction direction = TEST_DIRECTION;
        List<IntroduceImageDTO> expectedDTOs = new ArrayList<>();
        expectedDTOs.add(createTestIntroduceImageDTO());

        when(introduceImageService.getIntroduceImages(type, direction)).thenReturn(expectedDTOs);

        // 执行 & 验证
        mockMvc.perform(
                get("/api/v1/introduce-images").param("type", type.name())
                        .param("direction", direction.name())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.msg").value("Success"))
                .andExpect(jsonPath("$.data[0].direction").value(direction.name()));

        verify(introduceImageService).getIntroduceImages(type, direction);
    }

    /**
     * 获取介绍图片列表：无匹配图片应返回空列表
     */
    @Test
    @DisplayName("获取介绍图片列表：无匹配图片应返回空列表")
    void getIntroduceImages_noMatchingImages_shouldReturnEmptyList() throws Exception {
        // 准备
        ImageType type = ImageType.LABORATORY;
        List<IntroduceImageDTO> expectedDTOs = new ArrayList<>();

        when(introduceImageService.getIntroduceImages(type, null)).thenReturn(expectedDTOs);

        // 执行 & 验证
        mockMvc.perform(
                get("/api/v1/introduce-images").param("type", type.name()).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.msg").value("Success"))
                .andExpect(jsonPath("$.data").isEmpty());

        verify(introduceImageService).getIntroduceImages(type, null);
    }

    /**
     * 获取介绍图片列表：参数错误应返回500错误响应
     */
    @Test
    @DisplayName("获取介绍图片列表：参数错误应返回500错误响应")
    void getIntroduceImages_invalidParameters_shouldReturn500Error() throws Exception {
        // 准备
        String invalidType = "INVALID_TYPE";

        // 执行 & 验证
        mockMvc.perform(
                get("/api/v1/introduce-images").param("type", invalidType).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
    }

}
