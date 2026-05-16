package com.bluenet.web.api.controller.v1.admin;

import com.bluenet.web.BaseIntegrationTest;
import com.bluenet.web.api.dto.ResponseMessage;
import com.bluenet.web.api.dto.assessment_judgement.CommentDTO;
import com.bluenet.web.api.dto.assessment_judgement.CommentRequestDTO;
import com.bluenet.web.api.dto.auth.StudentIdLoginRequestDTO;
import com.bluenet.web.api.dto.auth.UserAuthResponseDTO;
import com.bluenet.web.domain.model.entity.AssessmentAnswer;
import com.bluenet.web.domain.model.entity.AssessmentQuestion;
import com.bluenet.web.domain.model.entity.AssessmentTime;
import com.bluenet.web.domain.model.entity.Role;
import com.bluenet.web.domain.model.entity.User;
import com.bluenet.web.domain.model.enumerate.Direction;
import com.bluenet.web.domain.model.enumerate.QuestionType;
import com.bluenet.web.infrastructure.repository.dataobject.AssessmentAnswerDO;
import com.bluenet.web.infrastructure.repository.dataobject.AssessmentQuestionDO;
import com.bluenet.web.infrastructure.repository.dataobject.AssessmentTimeDO;
import com.bluenet.web.infrastructure.repository.dataobject.UserDO;
import com.bluenet.web.infrastructure.repository.mapper.AssessmentAnswerMapper;
import com.bluenet.web.infrastructure.repository.mapper.AssessmentQuestionMapper;
import com.bluenet.web.infrastructure.repository.mapper.AssessmentTimeMapper;
import com.bluenet.web.infrastructure.repository.mapper.RoleMapper;
import com.bluenet.web.infrastructure.repository.mapper.UserMapper;
import com.bluenet.web.testcontainers.TestcontainersConfiguration;
import com.bluenet.web.testsupport.RepositoryTestObjects;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Import;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * AdminCommentController 集成测试。
 */
@DisplayName("AdminCommentController 集成测试")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Testcontainers
@Import(TestcontainersConfiguration.class)
class AdminCommentControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private RoleMapper roleMapper;

    @Autowired
    private AssessmentTimeMapper assessmentTimeMapper;

    @Autowired
    private AssessmentQuestionMapper assessmentQuestionMapper;

    @Autowired
    private AssessmentAnswerMapper assessmentAnswerMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @MockBean
    private com.bluenet.web.infrastructure.security.scanner.PermissionScanner permissionScanner;

    private static final String ADMIN_STUDENT_ID = "admcom001";
    private static final String ADMIN_PASSWORD = "adminPassword123";
    private static final String CANDIDATE_STUDENT_ID = "cand001";
    private static final String CANDIDATE_PASSWORD = "candidatePassword123";

    private List<String> authCookies;
    private String csrfToken;
    private Long answerId;

    @BeforeEach
    void setUpTestData() {
        // 查找 SUPER_ADMIN 角色
        Role superAdminRole = RepositoryTestObjects.toDomain(roleMapper.selectByName("SUPER_ADMIN"), Role.class);
        if (superAdminRole == null) {
            throw new IllegalStateException("SUPER_ADMIN 角色不存在");
        }

        // 查找 CANDIDATE 角色
        Role candidateRole = RepositoryTestObjects.toDomain(roleMapper.selectByName("CANDIDATE"), Role.class);
        if (candidateRole == null) {
            throw new IllegalStateException("CANDIDATE 角色不存在");
        }

        // 创建 SUPER_ADMIN 用户
        User adminUser = User.reconstruct(
                null,
                ADMIN_STUDENT_ID,
                "admincomment@test.com",
                superAdminRole.getId(),
                passwordEncoder.encode(ADMIN_PASSWORD),
                "评论管理员",
                null,
                null,
                null,
                null,
                Direction.COMPUTER_VISION,
                null,
                null,
                null,
                false,
                null,
                null,
                null,
                null,
                null);
        RepositoryTestObjects.insert(userMapper, adminUser, UserDO.class);

        // 创建 CANDIDATE 用户（用于提交答案）
        User candidateUser = User.reconstruct(
                null,
                CANDIDATE_STUDENT_ID,
                "candidate@test.com",
                candidateRole.getId(),
                passwordEncoder.encode(CANDIDATE_PASSWORD),
                "考生用户",
                null,
                null,
                null,
                null,
                Direction.COMPUTER_VISION,
                null,
                null,
                null,
                false,
                null,
                null,
                null,
                null,
                null);
        RepositoryTestObjects.insert(userMapper, candidateUser, UserDO.class);

        // 创建考核时间
        AssessmentTime assessmentTime = AssessmentTime.create(
                Direction.COMPUTER_VISION,
                1,
                2026,
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now().plusDays(7),
                false,
                null,
                false);
        RepositoryTestObjects.insert(assessmentTimeMapper, assessmentTime, AssessmentTimeDO.class);

        // 创建文件上传题
        AssessmentQuestion question = AssessmentQuestion.create(
                assessmentTime.getId(),
                1,
                QuestionType.FILE_UPLOAD,
                "文件上传测试题",
                null,
                null,
                BigDecimal.valueOf(30));
        RepositoryTestObjects.insert(assessmentQuestionMapper, question, AssessmentQuestionDO.class);

        // 创建答案
        AssessmentAnswer answer = AssessmentAnswer.create(
                candidateUser.getId(),
                question.getId(),
                null,
                null,
                null);
        RepositoryTestObjects.insert(assessmentAnswerMapper, answer, AssessmentAnswerDO.class);
        answerId = answer.getId();

        // 登录获取认证信息
        loginAndGetCookies();
    }

    @Test
    @DisplayName("添加评论：成功创建评论")
    void addComment_shouldCreateComment() {
        CommentRequestDTO request = new CommentRequestDTO();
        request.setAnswerId(answerId);
        request.setContent("设计思路清晰，架构合理");
        request.setScore(BigDecimal.valueOf(25));

        HttpHeaders headers = createAuthHeadersWithCsrf();
        HttpEntity<CommentRequestDTO> httpEntity = new HttpEntity<>(request, headers);

        ResponseEntity<ResponseMessage<CommentDTO>> response = restTemplate.exchange(
                "/api/v1/admin/comments",
                HttpMethod.POST,
                httpEntity,
                new ParameterizedTypeReference<ResponseMessage<CommentDTO>>() {
                });

        assertEquals(HttpStatus.OK, response.getStatusCode());
        ResponseMessage<CommentDTO> body = response.getBody();
        assertNotNull(body);
        assertEquals(HttpStatus.OK.value(), body.getCode());

        CommentDTO comment = body.getData();
        assertNotNull(comment);
        assertEquals(answerId, comment.getAnswerId());
        assertEquals("设计思路清晰，架构合理", comment.getContent());
        assertEquals(BigDecimal.valueOf(25), comment.getScore());
    }

    @Test
    @DisplayName("查询评论列表：应返回指定答案的评论")
    void listComments_shouldReturnCommentsForAnswer() {
        // 先添加一条评论
        addCommentInternal("测试评论", BigDecimal.valueOf(20));

        HttpHeaders headers = createAuthHeadersWithCsrf();
        HttpEntity<Void> request = new HttpEntity<>(headers);

        ResponseEntity<ResponseMessage<List<CommentDTO>>> response = restTemplate.exchange(
                "/api/v1/admin/comments?answerId=" + answerId,
                HttpMethod.GET,
                request,
                new ParameterizedTypeReference<ResponseMessage<List<CommentDTO>>>() {
                });

        assertEquals(HttpStatus.OK, response.getStatusCode());
        ResponseMessage<List<CommentDTO>> body = response.getBody();
        assertNotNull(body);
        assertEquals(HttpStatus.OK.value(), body.getCode());

        List<CommentDTO> comments = body.getData();
        assertNotNull(comments);
        assertEquals(1, comments.size());
        assertEquals("测试评论", comments.get(0).getContent());
        assertEquals(0, BigDecimal.valueOf(20).compareTo(comments.get(0).getScore()));
    }

    @Test
    @DisplayName("更新评论：评论所有者更新成功")
    void updateComment_owner_shouldUpdate() {
        // 先添加一条评论
        CommentDTO created = addCommentInternal("旧内容", BigDecimal.valueOf(20));

        CommentRequestDTO request = new CommentRequestDTO();
        request.setAnswerId(answerId);
        request.setContent("更新后的内容");
        request.setScore(BigDecimal.valueOf(28));

        HttpHeaders headers = createAuthHeadersWithCsrf();
        HttpEntity<CommentRequestDTO> httpEntity = new HttpEntity<>(request, headers);

        ResponseEntity<ResponseMessage<CommentDTO>> response = restTemplate.exchange(
                "/api/v1/admin/comments/" + created.getId(),
                HttpMethod.PUT,
                httpEntity,
                new ParameterizedTypeReference<ResponseMessage<CommentDTO>>() {
                });

        assertEquals(HttpStatus.OK, response.getStatusCode());
        ResponseMessage<CommentDTO> body = response.getBody();
        assertNotNull(body);
        assertEquals(HttpStatus.OK.value(), body.getCode());

        CommentDTO updated = body.getData();
        assertNotNull(updated);
        assertEquals("更新后的内容", updated.getContent());
        assertEquals(BigDecimal.valueOf(28), updated.getScore());
    }

    @Test
    @DisplayName("删除评论：评论所有者删除成功")
    void deleteComment_owner_shouldDelete() {
        // 先添加一条评论
        CommentDTO created = addCommentInternal("待删除", BigDecimal.valueOf(20));

        HttpHeaders headers = createAuthHeadersWithCsrf();
        HttpEntity<Void> request = new HttpEntity<>(headers);

        ResponseEntity<ResponseMessage<Void>> response = restTemplate.exchange(
                "/api/v1/admin/comments/" + created.getId(),
                HttpMethod.DELETE,
                request,
                new ParameterizedTypeReference<ResponseMessage<Void>>() {
                });

        assertEquals(HttpStatus.OK, response.getStatusCode());
        ResponseMessage<Void> body = response.getBody();
        assertNotNull(body);
        assertEquals(HttpStatus.OK.value(), body.getCode());

        // 验证删除后查询为空
        ResponseEntity<ResponseMessage<List<CommentDTO>>> listResponse = restTemplate.exchange(
                "/api/v1/admin/comments?answerId=" + answerId,
                HttpMethod.GET,
                request,
                new ParameterizedTypeReference<ResponseMessage<List<CommentDTO>>>() {
                });

        assertEquals(HttpStatus.OK, listResponse.getStatusCode());
        assertTrue(listResponse.getBody().getData().isEmpty());
    }

    @Test
    @DisplayName("添加评论：未登录应返回401")
    void addComment_unauthenticated_shouldReturnUnauthorized() {
        CommentRequestDTO request = new CommentRequestDTO();
        request.setAnswerId(answerId);
        request.setContent("评论");
        request.setScore(BigDecimal.valueOf(20));

        HttpEntity<CommentRequestDTO> httpEntity = new HttpEntity<>(request);

        ResponseEntity<ResponseMessage> response = restTemplate.exchange(
                "/api/v1/admin/comments",
                HttpMethod.POST,
                httpEntity,
                ResponseMessage.class);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    // ========== 辅助方法 ==========

    private void loginAndGetCookies() {
        StudentIdLoginRequestDTO loginRequest = new StudentIdLoginRequestDTO();
        loginRequest.setStudentId(ADMIN_STUDENT_ID);
        loginRequest.setPassword(ADMIN_PASSWORD);

        ResponseEntity<ResponseMessage<UserAuthResponseDTO>> response = restTemplate.exchange(
                "/api/v1/auth/login/student-id",
                HttpMethod.POST,
                new HttpEntity<>(loginRequest),
                new ParameterizedTypeReference<ResponseMessage<UserAuthResponseDTO>>() {
                });

        assertEquals(HttpStatus.OK, response.getStatusCode());
        authCookies = response.getHeaders().get(HttpHeaders.SET_COOKIE);
        csrfToken = response.getBody().getData().getCsrfToken();
        assertNotNull(authCookies, "登录响应应包含 Set-Cookie");
        assertNotNull(csrfToken, "登录响应应包含 CSRF Token");
    }

    private HttpHeaders createAuthHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.put(HttpHeaders.COOKIE, authCookies);
        return headers;
    }

    private HttpHeaders createAuthHeadersWithCsrf() {
        HttpHeaders headers = createAuthHeaders();
        headers.set("X-CSRF-Token", csrfToken);
        return headers;
    }

    private CommentDTO addCommentInternal(String content, BigDecimal score) {
        CommentRequestDTO request = new CommentRequestDTO();
        request.setAnswerId(answerId);
        request.setContent(content);
        request.setScore(score);

        HttpHeaders headers = createAuthHeadersWithCsrf();
        HttpEntity<CommentRequestDTO> httpEntity = new HttpEntity<>(request, headers);

        ResponseEntity<ResponseMessage<CommentDTO>> response = restTemplate.exchange(
                "/api/v1/admin/comments",
                HttpMethod.POST,
                httpEntity,
                new ParameterizedTypeReference<ResponseMessage<CommentDTO>>() {
                });

        assertEquals(HttpStatus.OK, response.getStatusCode());
        return response.getBody().getData();
    }
}
