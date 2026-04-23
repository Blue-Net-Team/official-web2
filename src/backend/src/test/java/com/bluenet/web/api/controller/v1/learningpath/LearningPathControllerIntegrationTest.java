package com.bluenet.web.api.controller.v1.learningpath;

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
import com.bluenet.web.api.dto.learningpath.DirectionLearningPathDTO;
import com.bluenet.web.api.dto.learningpath.LearningStepDTO;
import com.bluenet.web.domain.model.entity.DirectionLearningStep;
import com.bluenet.web.domain.model.enumerate.Direction;
import com.bluenet.web.infrastructure.repository.mapper.LearningPathMapper;
import com.bluenet.web.testcontainers.TestcontainersConfiguration;

/**
 * LearningPathController 集成测试
 * <p>
 * 测试公开接口 GET /api/v1/directions/{slug}/learning-path
 * </p>
 */
@DisplayName("LearningPathController 集成测试")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Testcontainers
@Import(TestcontainersConfiguration.class)
class LearningPathControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private LearningPathMapper learningPathMapper;

    private static final String CV_SLUG = "cv";
    private static final String EMBED_SLUG = "embed";
    private static final String STRUCT_SLUG = "struct";
    private static final String INVALID_SLUG = "invalid";

    private DirectionLearningStep createTestStep(Direction direction, Integer stepNumber, String title,
            String videoUrl) {
        return DirectionLearningStep.create(direction, stepNumber, title, videoUrl);
    }

    @BeforeEach
    void setUpTestData() {
        // 数据库迁移脚本已插入默认数据，无需额外准备测试数据
        // V15__add_direction_learning_step.sql 已包含：
        // - 计算机视觉方向：4个步骤
        // - 嵌入式方向：4个步骤
        // - 结构设计方向：4个步骤
    }

    // ==================== GET /api/v1/directions/{slug}/learning-path
    // ====================

    /**
     * 集成测试：获取计算机视觉方向学习路径应成功
     */
    @Test
    @DisplayName("集成测试：获取计算机视觉方向学习路径应成功")
    void getLearningPath_cvDirection_shouldReturnSuccessfully() {
        // 执行
        ResponseEntity<ResponseMessage<DirectionLearningPathDTO>> response = restTemplate.exchange(
                "/api/v1/directions/" + CV_SLUG + "/learning-path",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<ResponseMessage<DirectionLearningPathDTO>>() {
                });

        // 验证
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(200, response.getBody().getCode());

        DirectionLearningPathDTO learningPath = response.getBody().getData();
        assertNotNull(learningPath);
        assertEquals("cv", learningPath.getDirection());
        assertEquals("计算机视觉", learningPath.getDirectionName());

        List<LearningStepDTO> steps = learningPath.getSteps();
        assertNotNull(steps);
        assertEquals(4, steps.size());

        // 验证步骤按序号排序
        assertEquals(1, steps.get(0).getStepNumber());
        assertEquals("Python基础", steps.get(0).getTitle());
        assertEquals(2, steps.get(1).getStepNumber());
        assertEquals("OpenCV入门", steps.get(1).getTitle());
        assertEquals(3, steps.get(2).getStepNumber());
        assertEquals("深度学习基础", steps.get(2).getTitle());
        assertEquals(4, steps.get(3).getStepNumber());
        assertEquals("项目实战", steps.get(3).getTitle());
    }

    /**
     * 集成测试：获取嵌入式方向学习路径应成功
     */
    @Test
    @DisplayName("集成测试：获取嵌入式方向学习路径应成功")
    void getLearningPath_embedDirection_shouldReturnSuccessfully() {
        // 执行
        ResponseEntity<ResponseMessage<DirectionLearningPathDTO>> response = restTemplate.exchange(
                "/api/v1/directions/" + EMBED_SLUG + "/learning-path",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<ResponseMessage<DirectionLearningPathDTO>>() {
                });

        // 验证
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(200, response.getBody().getCode());

        DirectionLearningPathDTO learningPath = response.getBody().getData();
        assertNotNull(learningPath);
        assertEquals("embed", learningPath.getDirection());
        assertEquals("嵌入式开发", learningPath.getDirectionName());

        List<LearningStepDTO> steps = learningPath.getSteps();
        assertNotNull(steps);
        assertEquals(4, steps.size());
    }

    /**
     * 集成测试：获取结构设计方向学习路径应成功
     */
    @Test
    @DisplayName("集成测试：获取结构设计方向学习路径应成功")
    void getLearningPath_structDirection_shouldReturnSteps() {
        // 执行
        ResponseEntity<ResponseMessage<DirectionLearningPathDTO>> response = restTemplate.exchange(
                "/api/v1/directions/" + STRUCT_SLUG + "/learning-path",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<ResponseMessage<DirectionLearningPathDTO>>() {
                });

        // 验证
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(200, response.getBody().getCode());

        DirectionLearningPathDTO learningPath = response.getBody().getData();
        assertNotNull(learningPath);
        assertEquals("struct", learningPath.getDirection());
        assertEquals("结构设计", learningPath.getDirectionName());

        List<LearningStepDTO> steps = learningPath.getSteps();
        assertNotNull(steps);
        assertEquals(4, steps.size()); // 数据库已有4个步骤
    }

    /**
     * 集成测试：无效的方向标识应返回404
     */
    @Test
    @DisplayName("集成测试：无效的方向标识应返回404")
    void getLearningPath_invalidSlug_shouldReturn404() {
        // 执行
        ResponseEntity<ResponseMessage<DirectionLearningPathDTO>> response = restTemplate.exchange(
                "/api/v1/directions/" + INVALID_SLUG + "/learning-path",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<ResponseMessage<DirectionLearningPathDTO>>() {
                });

        // 验证：实际返回404状态码
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    /**
     * 集成测试：公开接口无需认证即可访问
     */
    @Test
    @DisplayName("集成测试：公开接口无需认证即可访问")
    void getLearningPath_publicEndpoint_shouldNotRequireAuth() {
        // 执行：不携带任何认证信息
        ResponseEntity<ResponseMessage<DirectionLearningPathDTO>> response = restTemplate.exchange(
                "/api/v1/directions/" + CV_SLUG + "/learning-path",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<ResponseMessage<DirectionLearningPathDTO>>() {
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
    void getLearningPath_responseFormat_shouldBeCorrect() {
        // 执行
        ResponseEntity<ResponseMessage<DirectionLearningPathDTO>> response = restTemplate.exchange(
                "/api/v1/directions/" + CV_SLUG + "/learning-path",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<ResponseMessage<DirectionLearningPathDTO>>() {
                });

        // 验证响应结构
        assertNotNull(response.getBody());
        assertNotNull(response.getBody().getCode());
        assertNotNull(response.getBody().getMsg());
        assertNotNull(response.getBody().getData());

        // 验证msg为成功消息
        assertEquals("Success", response.getBody().getMsg());
    }

    /**
     * 集成测试：学习步骤应包含完整信息
     */
    @Test
    @DisplayName("集成测试：学习步骤应包含完整信息")
    void getLearningPath_steps_shouldContainCompleteInfo() {
        // 执行
        ResponseEntity<ResponseMessage<DirectionLearningPathDTO>> response = restTemplate.exchange(
                "/api/v1/directions/" + CV_SLUG + "/learning-path",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<ResponseMessage<DirectionLearningPathDTO>>() {
                });

        // 验证
        assertEquals(HttpStatus.OK, response.getStatusCode());
        DirectionLearningPathDTO learningPath = response.getBody().getData();
        assertNotNull(learningPath);

        for (LearningStepDTO step : learningPath.getSteps()) {
            assertNotNull(step.getId(), "步骤ID不能为空");
            assertNotNull(step.getStepNumber(), "步骤序号不能为空");
            assertNotNull(step.getTitle(), "步骤标题不能为空");
            // 视频链接可以为空（数据库默认值为NULL）
        }
    }
}
