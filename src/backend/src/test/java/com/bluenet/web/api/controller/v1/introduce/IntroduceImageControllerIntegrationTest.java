package com.bluenet.web.api.controller.v1.introduce;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.bluenet.web.testcontainers.TestcontainersConfiguration;

/**
 * IntroduceImageController集成测试
 * <p>
 * 使用Testcontainers进行真实数据库集成测试
 * </p>
 */
@DisplayName("IntroduceImageController 集成测试")
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Testcontainers
@Import(TestcontainersConfiguration.class)
class IntroduceImageControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private static final String BASE_URL = "/api/v1/introduce-images";

    /**
     * 获取介绍图片列表：应返回成功响应
     */
    @Test
    @DisplayName("获取介绍图片列表：应返回成功响应")
    void getIntroduceImages_shouldReturnSuccessResponse() throws Exception {
        mockMvc.perform(
                get(BASE_URL)
                        .param("type", "competition"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray());
    }

    /**
     * 获取介绍图片列表：缺少type参数应返回400错误
     */
    @Test
    @DisplayName("获取介绍图片列表：缺少type参数应返回400错误")
    void getIntroduceImages_missingType_shouldReturn400() throws Exception {
        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isBadRequest());
    }

    /**
     * 获取介绍图片列表：无效type参数应返回400错误
     */
    @Test
    @DisplayName("获取介绍图片列表：无效type参数应返回400错误")
    void getIntroduceImages_invalidType_shouldReturn400() throws Exception {
        mockMvc.perform(
                get(BASE_URL)
                        .param("type", "invalid_type"))
                .andExpect(status().isBadRequest());
    }
}
