package com.bluenet.web.api.controller.v1.enrollment;

import com.bluenet.web.BaseIntegrationTest;
import com.bluenet.web.api.dto.enrollment.CreateEnrollmentRequestDTO;
import com.bluenet.web.api.dto.enrollment.EnrollmentResultDTO;
import com.bluenet.web.api.converter.enroll.EnrollRequestConverter;
import com.bluenet.web.api.converter.enroll.EnrollResponseConverter;
import com.bluenet.web.application.command.enroll.EnrollCommands;
import com.bluenet.web.application.result.enroll.EnrollResult;
import com.bluenet.web.application.service.EnrollAppService;
import com.bluenet.web.domain.model.enumerate.Direction;
import com.bluenet.web.domain.model.enumerate.EnrollStatus;
import com.bluenet.web.domain.model.enumerate.Gender;
import com.bluenet.web.testconfig.TestSecurityConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestSecurityConfig.class)
@DisplayName("EnrollController 集成测试")
class EnrollControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private EnrollAppService enrollAppService;

    @MockitoBean
    private EnrollRequestConverter enrollRequestConverter;

    @MockitoBean
    private EnrollResponseConverter enrollResponseConverter;

    @AfterEach
    void tearDown() {
        // 公开接口，不涉及 UserCTX。
    }

    private CreateEnrollmentRequestDTO buildValidRequest() {
        return CreateEnrollmentRequestDTO.builder()
                .username("张三")
                .studentId("202100010001")
                .email("zhangsan@example.com")
                .collegeId(1L)
                .major("计算机科学与技术")
                .gender(Gender.MALE)
                .direction(Direction.COMPUTER_VISION)
                .introduction(
                        "我是张三，来自计算机科学与技术专业。在校期间系统学习了数据结构、算法、操作系统等核心课程，并积极参与课外技术实践。我对计算机视觉方向充满热情，曾自学 OpenCV 完成多个小项目，希望加入团队后继续深入学习并贡献自己的力量。")
                .build();
    }

    @Test
    @DisplayName("createEnrollment: 新报名应返回 201")
    void createEnrollment_newEnrollment_shouldReturnCreated() throws Exception {
        CreateEnrollmentRequestDTO request = buildValidRequest();
        EnrollResult.Enrollment result = new EnrollResult.Enrollment(
                1L,
                "张三",
                "202100010001",
                "zhangsan@example.com",
                1L,
                "计算机学院",
                "计算机科学与技术",
                Gender.MALE,
                Direction.COMPUTER_VISION,
                null,
                EnrollStatus.PENDING,
                "我是张三...",
                null,
                null,
                null,
                true);
        EnrollmentResultDTO dto = EnrollmentResultDTO.builder()
                .id(1L)
                .username("张三")
                .studentId("202100010001")
                .status(EnrollStatus.PENDING)
                .created(true)
                .build();
        when(enrollRequestConverter.toCommand(any(CreateEnrollmentRequestDTO.class))).thenReturn(
                new EnrollCommands.CreateEnrollmentCommand(
                        "张三",
                        "202100010001",
                        "zhangsan@example.com",
                        1L,
                        "计算机科学与技术",
                        Gender.MALE,
                        Direction.COMPUTER_VISION,
                        null,
                        "我是张三...",
                        null,
                        false));
        when(enrollAppService.createEnrollment(any())).thenReturn(result);
        when(enrollResponseConverter.toEnrollmentResultDTO(result)).thenReturn(dto);

        mockMvc.perform(
                post("/api/v1/enrollments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value(201))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.created").value(true));
    }

    @Test
    @DisplayName("createEnrollment: 更新已有报名应返回 200")
    void createEnrollment_updateExisting_shouldReturnOk() throws Exception {
        CreateEnrollmentRequestDTO request = buildValidRequest();
        request.setForceUpdate(true);
        EnrollResult.Enrollment result = new EnrollResult.Enrollment(
                1L,
                "张三",
                "202100010001",
                "zhangsan@example.com",
                1L,
                "计算机学院",
                "计算机科学与技术",
                Gender.MALE,
                Direction.COMPUTER_VISION,
                null,
                EnrollStatus.PENDING,
                "我是张三...",
                null,
                null,
                null,
                false);
        EnrollmentResultDTO dto = EnrollmentResultDTO.builder()
                .id(1L)
                .created(false)
                .build();
        when(enrollRequestConverter.toCommand(any(CreateEnrollmentRequestDTO.class))).thenReturn(
                new EnrollCommands.CreateEnrollmentCommand(
                        "张三",
                        "202100010001",
                        "zhangsan@example.com",
                        1L,
                        "计算机科学与技术",
                        Gender.MALE,
                        Direction.COMPUTER_VISION,
                        null,
                        "我是张三...",
                        null,
                        true));
        when(enrollAppService.createEnrollment(any())).thenReturn(result);
        when(enrollResponseConverter.toEnrollmentResultDTO(result)).thenReturn(dto);

        mockMvc.perform(
                post("/api/v1/enrollments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.created").value(false));
    }

    @Test
    @DisplayName("createEnrollment: 必填字段为空时应返回 400")
    void createEnrollment_blankField_shouldReturnBadRequest() throws Exception {
        CreateEnrollmentRequestDTO request = buildValidRequest();
        request.setUsername("");

        mockMvc.perform(
                post("/api/v1/enrollments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    @DisplayName("createEnrollment: 学号格式非法时应返回 400")
    void createEnrollment_invalidStudentId_shouldReturnBadRequest() throws Exception {
        CreateEnrollmentRequestDTO request = buildValidRequest();
        request.setStudentId("123");

        mockMvc.perform(
                post("/api/v1/enrollments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400));
    }
}
