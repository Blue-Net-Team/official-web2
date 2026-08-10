package com.bluenet.web.api.controller.v1.learningpath;

import com.bluenet.web.BaseIntegrationTest;
import com.bluenet.web.api.dto.learningpath.DirectionLearningPathDTO;
import com.bluenet.web.api.dto.learningpath.LearningStepDTO;
import com.bluenet.web.api.converter.learningpath.LearningPathResponseConverter;
import com.bluenet.web.application.result.learningpath.LearningPathResult;
import com.bluenet.web.application.service.LearningPathAppService;
import com.bluenet.web.domain.model.enumerate.Direction;
import com.bluenet.web.testconfig.TestSecurityConfig;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestSecurityConfig.class)
@DisplayName("LearningPathController 集成测试")
class LearningPathControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private LearningPathAppService learningPathAppService;

    @MockitoBean
    private LearningPathResponseConverter learningPathResponseConverter;

    @AfterEach
    void tearDown() {
        // 公开接口，不涉及 UserCTX。
    }

    @Test
    @DisplayName("getLearningPath: 有效方向标识应返回学习路径")
    void getLearningPath_validSlug_shouldReturnPath() throws Exception {
        LearningPathResult result = new LearningPathResult(1L, Direction.COMPUTER_VISION, 1, "OpenCV 基础",
                "https://example.com/video");
        DirectionLearningPathDTO dto = DirectionLearningPathDTO.builder()
                .direction("cv")
                .directionName("计算机视觉")
                .steps(List.of(LearningStepDTO.builder().id(1L).stepNumber(1).title("OpenCV 基础").build()))
                .build();
        when(learningPathAppService.getLearningPath("cv")).thenReturn(List.of(result));
        when(learningPathResponseConverter.toDirectionLearningPathDTO("cv", List.of(result))).thenReturn(dto);

        mockMvc.perform(get("/api/v1/directions/cv/learning-path"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.direction").value("cv"))
                .andExpect(jsonPath("$.data.steps[0].title").value("OpenCV 基础"));
    }

    @Test
    @DisplayName("getLearningPath: 服务返回空列表时应返回空步骤")
    void getLearningPath_emptyList_shouldReturnEmptySteps() throws Exception {
        DirectionLearningPathDTO dto = DirectionLearningPathDTO.builder()
                .direction("embed")
                .directionName("嵌入式")
                .steps(List.of())
                .build();
        when(learningPathAppService.getLearningPath("embed")).thenReturn(List.of());
        when(learningPathResponseConverter.toDirectionLearningPathDTO("embed", List.of())).thenReturn(dto);

        mockMvc.perform(get("/api/v1/directions/embed/learning-path"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.steps").isArray())
                .andExpect(jsonPath("$.data.steps").isEmpty());
    }
}
