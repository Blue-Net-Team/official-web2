package com.bluenet.web.api.controller.v1.enrollment;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Import;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;

import com.bluenet.web.BaseIntegrationTest;
import com.bluenet.web.api.dto.ResponseMessage;
import com.bluenet.web.api.dto.enrollment.*;
import com.bluenet.web.domain.model.entity.College;
import com.bluenet.web.domain.model.entity.Enroll;
import com.bluenet.web.domain.model.enumerate.Direction;
import com.bluenet.web.domain.model.enumerate.EnrollStatus;
import com.bluenet.web.infrastructure.repository.mapper.CollegeMapper;
import com.bluenet.web.infrastructure.repository.mapper.EnrollMapper;
import com.bluenet.web.testcontainers.TestcontainersConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Testcontainers;

@Slf4j
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Testcontainers
@Import(TestcontainersConfiguration.class)
class EnrollControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private EnrollMapper enrollMapper;

    @Autowired
    private CollegeMapper collegeMapper;

    private static final String TEST_STUDENT_ID = "202311548105";
    private static final String TEST_USERNAME = "张三";
    private static final String TEST_MAJOR = "计算机科学与技术";
    private static final Integer TEST_GRADE = 2;
    private static final String TEST_EMAIL = "test@example.com";
    private static final String TEST_INTRODUCTION = "我是测试用户，来自计算机科学与技术专业，对计算机视觉方向非常感兴趣。我热爱编程，熟悉Python、Java等多种编程语言，曾参与多个项目开发，希望能够加入蓝网团队学习更多知识，提升自己的技术能力，为团队做出贡献。";

    private Long testCollegeId;

    @BeforeEach
    void setUp() {
        College college = College.builder()
                .name("计算机学院")
                .build();
        collegeMapper.insert(college);
        testCollegeId = college.getId();
    }

    private CreateEnrollmentRequestDTO createTestRequest() {
        return CreateEnrollmentRequestDTO.builder()
                .username(TEST_USERNAME)
                .studentId(TEST_STUDENT_ID)
                .email(TEST_EMAIL)
                .collegeId(testCollegeId)
                .major(TEST_MAJOR)
                .grade(TEST_GRADE)
                .direction(Direction.COMPUTER_VISION)
                .introduction(TEST_INTRODUCTION)
                .build();
    }

    @Nested
    @DisplayName("POST /api/v1/enrollments - 发起报名")
    class CreateEnrollmentTests {

        @Test
        @DisplayName("正常报名：应返回201创建成功")
        void createEnrollment_validRequest_shouldReturn201() {
            CreateEnrollmentRequestDTO request = createTestRequest();

            ResponseEntity<ResponseMessage<EnrollmentBriefDTO>> response = restTemplate.exchange(
                    "/api/v1/enrollments",
                    HttpMethod.POST,
                    new HttpEntity<>(request),
                    new ParameterizedTypeReference<ResponseMessage<EnrollmentBriefDTO>>() {
                    });

            assertEquals(HttpStatus.CREATED, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(HttpStatus.CREATED.value(), response.getBody().getCode());
            assertEquals("报名成功", response.getBody().getMsg());

            EnrollmentBriefDTO data = response.getBody().getData();
            assertNotNull(data);
            assertNotNull(data.getId());
            assertEquals(TEST_USERNAME, data.getUsername());
            assertEquals(TEST_STUDENT_ID, data.getStudentId());
            assertEquals(Direction.COMPUTER_VISION, data.getDirection());
            assertEquals(EnrollStatus.PENDING, data.getStatus());
        }

        @Test
        @DisplayName("参数校验失败：缺少必填字段应返回400")
        void createEnrollment_missingRequiredFields_shouldReturn400() {
            CreateEnrollmentRequestDTO request = CreateEnrollmentRequestDTO.builder()
                    .username("")
                    .studentId("")
                    .build();

            ResponseEntity<ResponseMessage> response = restTemplate.postForEntity(
                    "/api/v1/enrollments",
                    request,
                    ResponseMessage.class);

            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        }

        @Test
        @DisplayName("学号格式错误：应返回400")
        void createEnrollment_invalidStudentIdFormat_shouldReturn400() {
            CreateEnrollmentRequestDTO request = createTestRequest();
            request.setStudentId("invalid");

            ResponseEntity<ResponseMessage> response = restTemplate.postForEntity(
                    "/api/v1/enrollments",
                    request,
                    ResponseMessage.class);

            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        }

        @Test
        @DisplayName("年级超出范围：应返回400")
        void createEnrollment_gradeOutOfRange_shouldReturn400() {
            CreateEnrollmentRequestDTO request = createTestRequest();
            request.setGrade(10);

            ResponseEntity<ResponseMessage> response = restTemplate.postForEntity(
                    "/api/v1/enrollments",
                    request,
                    ResponseMessage.class);

            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        }

        @Test
        @DisplayName("学院不存在：应正常创建（学院名称为空）")
        void createEnrollment_nonExistingCollege_shouldCreateWithNullCollegeName() {
            CreateEnrollmentRequestDTO request = createTestRequest();
            request.setCollegeId(99999L);

            ResponseEntity<ResponseMessage<EnrollmentBriefDTO>> response = restTemplate.exchange(
                    "/api/v1/enrollments",
                    HttpMethod.POST,
                    new HttpEntity<>(request),
                    new ParameterizedTypeReference<ResponseMessage<EnrollmentBriefDTO>>() {
                    });

            assertEquals(HttpStatus.CREATED, response.getStatusCode());
        }
    }

    @Nested
    @DisplayName("学号冲突处理")
    class ConflictHandlingTests {

        @Test
        @DisplayName("学号已存在且forceUpdate为false：应返回409冲突")
        void createEnrollment_duplicateStudentIdNoForceUpdate_shouldReturn409() {
            CreateEnrollmentRequestDTO request1 = createTestRequest();
            restTemplate.postForEntity("/api/v1/enrollments", request1, ResponseMessage.class);

            CreateEnrollmentRequestDTO request2 = createTestRequest();
            request2.setUsername("李四");
            request2.setForceUpdate(false);

            ResponseEntity<ResponseMessage<EnrollmentConflictDTO>> response = restTemplate.exchange(
                    "/api/v1/enrollments",
                    HttpMethod.POST,
                    new HttpEntity<>(request2),
                    new ParameterizedTypeReference<ResponseMessage<EnrollmentConflictDTO>>() {
                    });

            assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(HttpStatus.CONFLICT.value(), response.getBody().getCode());

            EnrollmentConflictDTO conflict = response.getBody().getData();
            assertNotNull(conflict);
            assertEquals(TEST_STUDENT_ID, conflict.getStudentId());
            assertEquals(TEST_USERNAME, conflict.getUsername());
        }

        @Test
        @DisplayName("学号已存在且forceUpdate为true：应返回200更新成功")
        void createEnrollment_duplicateStudentIdWithForceUpdate_shouldReturn200() {
            CreateEnrollmentRequestDTO request1 = createTestRequest();
            restTemplate.postForEntity("/api/v1/enrollments", request1, ResponseMessage.class);

            CreateEnrollmentRequestDTO request2 = createTestRequest();
            request2.setUsername("李四");
            request2.setMajor("软件工程");
            request2.setForceUpdate(true);

            ResponseEntity<ResponseMessage<EnrollmentBriefDTO>> response = restTemplate.exchange(
                    "/api/v1/enrollments",
                    HttpMethod.POST,
                    new HttpEntity<>(request2),
                    new ParameterizedTypeReference<ResponseMessage<EnrollmentBriefDTO>>() {
                    });

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals("报名信息已更新", response.getBody().getMsg());

            EnrollmentBriefDTO data = response.getBody().getData();
            assertNotNull(data);
            assertEquals("李四", data.getUsername());
            assertEquals("软件工程", data.getMajor());
        }

        @Test
        @DisplayName("被拒绝的报名更新后：状态应保持REJECTED")
        void createEnrollment_updateRejectedEnrollment_shouldKeepRejected() {
            CreateEnrollmentRequestDTO request1 = createTestRequest();
            ResponseEntity<ResponseMessage<EnrollmentBriefDTO>> createResponse = restTemplate.exchange(
                    "/api/v1/enrollments",
                    HttpMethod.POST,
                    new HttpEntity<>(request1),
                    new ParameterizedTypeReference<ResponseMessage<EnrollmentBriefDTO>>() {
                    });
            Long enrollId = createResponse.getBody().getData().getId();

            Enroll enroll = enrollMapper.selectById(enrollId);
            enroll.setStatus(EnrollStatus.REJECTED);
            enrollMapper.updateById(enroll);

            CreateEnrollmentRequestDTO request2 = createTestRequest();
            request2.setUsername("李四");
            request2.setForceUpdate(true);

            ResponseEntity<ResponseMessage<EnrollmentBriefDTO>> response = restTemplate.exchange(
                    "/api/v1/enrollments",
                    HttpMethod.POST,
                    new HttpEntity<>(request2),
                    new ParameterizedTypeReference<ResponseMessage<EnrollmentBriefDTO>>() {
                    });

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertEquals(EnrollStatus.REJECTED, response.getBody().getData().getStatus());
        }
    }

    @Nested
    @DisplayName("边界值测试")
    class BoundaryTests {

        @Test
        @DisplayName("学号最小长度12位：应正常创建")
        void createEnrollment_minStudentIdLength_shouldCreate() {
            CreateEnrollmentRequestDTO request = createTestRequest();
            request.setStudentId("123456789012");

            ResponseEntity<ResponseMessage<EnrollmentBriefDTO>> response = restTemplate.exchange(
                    "/api/v1/enrollments",
                    HttpMethod.POST,
                    new HttpEntity<>(request),
                    new ParameterizedTypeReference<ResponseMessage<EnrollmentBriefDTO>>() {
                    });

            assertEquals(HttpStatus.CREATED, response.getStatusCode());
        }

        @Test
        @DisplayName("学号最大长度13位：应正常创建")
        void createEnrollment_maxStudentIdLength_shouldCreate() {
            CreateEnrollmentRequestDTO request = createTestRequest();
            request.setStudentId("1234567890123");

            ResponseEntity<ResponseMessage<EnrollmentBriefDTO>> response = restTemplate.exchange(
                    "/api/v1/enrollments",
                    HttpMethod.POST,
                    new HttpEntity<>(request),
                    new ParameterizedTypeReference<ResponseMessage<EnrollmentBriefDTO>>() {
                    });

            assertEquals(HttpStatus.CREATED, response.getStatusCode());
        }

        @Test
        @DisplayName("学号长度不足：应返回400")
        void createEnrollment_studentIdTooShort_shouldReturn400() {
            CreateEnrollmentRequestDTO request = createTestRequest();
            request.setStudentId("12345678901");

            ResponseEntity<ResponseMessage> response = restTemplate.postForEntity(
                    "/api/v1/enrollments",
                    request,
                    ResponseMessage.class);

            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        }

        @Test
        @DisplayName("年级边界值1：应正常创建")
        void createEnrollment_minGrade_shouldCreate() {
            CreateEnrollmentRequestDTO request = createTestRequest();
            request.setGrade(1);

            ResponseEntity<ResponseMessage<EnrollmentBriefDTO>> response = restTemplate.exchange(
                    "/api/v1/enrollments",
                    HttpMethod.POST,
                    new HttpEntity<>(request),
                    new ParameterizedTypeReference<ResponseMessage<EnrollmentBriefDTO>>() {
                    });

            assertEquals(HttpStatus.CREATED, response.getStatusCode());
        }

        @Test
        @DisplayName("年级边界值6：应正常创建")
        void createEnrollment_maxGrade_shouldCreate() {
            CreateEnrollmentRequestDTO request = createTestRequest();
            request.setGrade(6);

            ResponseEntity<ResponseMessage<EnrollmentBriefDTO>> response = restTemplate.exchange(
                    "/api/v1/enrollments",
                    HttpMethod.POST,
                    new HttpEntity<>(request),
                    new ParameterizedTypeReference<ResponseMessage<EnrollmentBriefDTO>>() {
                    });

            assertEquals(HttpStatus.CREATED, response.getStatusCode());
        }

        @Test
        @DisplayName("内推码格式正确：应正常创建")
        void createEnrollment_validReferralCode_shouldCreate() {
            CreateEnrollmentRequestDTO request = createTestRequest();
            request.setInternalReferralCode("ABC12345");

            ResponseEntity<ResponseMessage<EnrollmentBriefDTO>> response = restTemplate.exchange(
                    "/api/v1/enrollments",
                    HttpMethod.POST,
                    new HttpEntity<>(request),
                    new ParameterizedTypeReference<ResponseMessage<EnrollmentBriefDTO>>() {
                    });

            assertEquals(HttpStatus.CREATED, response.getStatusCode());
        }

        @Test
        @DisplayName("内推码格式错误：应返回400")
        void createEnrollment_invalidReferralCode_shouldReturn400() {
            CreateEnrollmentRequestDTO request = createTestRequest();
            request.setInternalReferralCode("abc12345");

            ResponseEntity<ResponseMessage> response = restTemplate.postForEntity(
                    "/api/v1/enrollments",
                    request,
                    ResponseMessage.class);

            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        }
    }

    @Nested
    @DisplayName("不同方向测试")
    class DirectionTests {

        @Test
        @DisplayName("结构设计方向：应正常创建")
        void createEnrollment_structureDirection_shouldCreate() {
            CreateEnrollmentRequestDTO request = createTestRequest();
            request.setDirection(Direction.STRUCTURAL_DESIGN);

            ResponseEntity<ResponseMessage<EnrollmentBriefDTO>> response = restTemplate.exchange(
                    "/api/v1/enrollments",
                    HttpMethod.POST,
                    new HttpEntity<>(request),
                    new ParameterizedTypeReference<ResponseMessage<EnrollmentBriefDTO>>() {
                    });

            assertEquals(HttpStatus.CREATED, response.getStatusCode());
            assertEquals(Direction.STRUCTURAL_DESIGN, response.getBody().getData().getDirection());
        }

        @Test
        @DisplayName("嵌入式方向：应正常创建")
        void createEnrollment_embeddedDirection_shouldCreate() {
            CreateEnrollmentRequestDTO request = createTestRequest();
            request.setDirection(Direction.EMBEDDED);

            ResponseEntity<ResponseMessage<EnrollmentBriefDTO>> response = restTemplate.exchange(
                    "/api/v1/enrollments",
                    HttpMethod.POST,
                    new HttpEntity<>(request),
                    new ParameterizedTypeReference<ResponseMessage<EnrollmentBriefDTO>>() {
                    });

            assertEquals(HttpStatus.CREATED, response.getStatusCode());
            assertEquals(Direction.EMBEDDED, response.getBody().getData().getDirection());
        }
    }
}
