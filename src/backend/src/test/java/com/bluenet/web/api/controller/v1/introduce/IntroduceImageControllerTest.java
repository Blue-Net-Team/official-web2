package com.bluenet.web.api.controller.v1.introduce;

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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.bluenet.web.api.dto.introduce.IntroduceImageDTO;
import com.bluenet.web.application.service.IntroduceImageService;
import com.bluenet.web.domain.model.enumerate.ImageType;
import com.bluenet.web.testcontainers.TestcontainersConfiguration;

/**
 * IntroduceImageController单元测试
 */
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

    private static final String BASE_URL = "/api/v1/introduce-images";

    private IntroduceImageDTO createTestDTO() {
        return IntroduceImageDTO.builder()
                .id(1L)
                .type(ImageType.COMPETITION)
                .description("测试图片")
                .fileId(100L)
                .fileUrl("http://example.com/image.jpg")
                .build();
    }

    /**
     * 获取介绍图片列表：应返回成功响应
     */
    @Test
    @DisplayName("获取介绍图片列表：应返回成功响应")
    void getIntroduceImages_shouldReturnSuccessResponse() throws Exception {
        // 准备
        List<IntroduceImageDTO> images = new ArrayList<>();
        images.add(createTestDTO());

        when(introduceImageService.getIntroduceImages(ImageType.COMPETITION)).thenReturn(images);

        // 执行和验证
        mockMvc.perform(
                get(BASE_URL)
                        .param("type", "competition"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].id").value(1))
                .andExpect(jsonPath("$.data[0].type").value("COMPETITION"));
    }

    /**
     * 获取介绍图片列表：无图片时应返回空数组
     */
    @Test
    @DisplayName("获取介绍图片列表：无图片时应返回空数组")
    void getIntroduceImages_noImages_shouldReturnEmptyArray() throws Exception {
        // 准备
        when(introduceImageService.getIntroduceImages(ImageType.COMPETITION)).thenReturn(new ArrayList<>());

        // 执行和验证
        mockMvc.perform(
                get(BASE_URL)
                        .param("type", "competition"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data").isEmpty());
    }

    /**
     * 获取介绍图片列表：缺少type参数应返回400错误
     */
    @Test
    @DisplayName("获取介绍图片列表：缺少type参数应返回400错误")
    void getIntroduceImages_missingType_shouldReturn400() throws Exception {
        // 执行和验证
        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isBadRequest());
    }
}
