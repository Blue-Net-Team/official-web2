package com.bluenet.web.api.controller.v1.auth;

import com.bluenet.web.BaseIntegrationTest;
import com.bluenet.web.api.dto.auth.EmailLoginRequestDTO;
import com.bluenet.web.api.dto.auth.SendVerificationCodeRequestDTO;
import com.bluenet.web.api.dto.auth.StudentIdLoginRequestDTO;
import com.bluenet.web.application.message.MessageDispatcher;
import com.bluenet.web.application.message.MessageRequest;
import com.bluenet.web.domain.model.entity.User;
import com.bluenet.web.domain.model.entity.VerifyCode;
import com.bluenet.web.domain.model.enumerate.MessageChannel;
import com.bluenet.web.domain.model.enumerate.RoleType;
import com.bluenet.web.domain.repository.UserRepository;
import com.bluenet.web.domain.repository.VerificationCodeRepository;
import com.bluenet.web.domain.service.GitHubOAuthService;
import com.bluenet.web.domain.service.VerificationCodeDomainService;
import com.bluenet.web.infrastructure.repository.mapper.RoleMapper;
import com.bluenet.web.infrastructure.security.principal.WithSecurityPrincipal;
import com.bluenet.web.infrastructure.security.util.UserCTX;
import com.bluenet.web.testconfig.TestSecurityConfig;
import com.bluenet.web.testsupport.fixture.RoleFixture;
import com.bluenet.web.testsupport.fixture.UserFixture;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * AuthController 集成测试。
 * <p>
 * 验证登录、验证码发送、获取登录状态与登出等认证相关接口的完整链路。
 * </p>
 */
@SpringBootTest
@AutoConfigureMockMvc
@Import(TestSecurityConfig.class)
@DisplayName("AuthController 集成测试")
class AuthControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleMapper roleMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private VerificationCodeRepository verificationCodeRepository;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @MockBean
    private MessageDispatcher messageDispatcher;

    @MockBean
    private VerificationCodeDomainService verificationCodeDomainService;

    @MockBean
    private GitHubOAuthService gitHubOAuthService;

    @AfterEach
    void tearDown() {
        UserCTX.clear();
        redisTemplate.delete(Objects.requireNonNull(redisTemplate.keys("rate_limit:*")));
    }

    private User createMemberUser(String studentId) {
        return UserFixture.builder()
                .withStudentId(studentId)
                .withRoleId(RoleFixture.roleId(roleMapper, RoleType.MEMBER))
                .save(userRepository, passwordEncoder);
    }

    private VerifyCode createVerifyCode(String email, String code, LocalDateTime expireAt, String scene) {
        VerifyCode verifyCode = VerifyCode.create(email, code, expireAt, scene);
        verificationCodeRepository.save(verifyCode);
        return verifyCode;
    }

    @Test
    @DisplayName("学号登录：正确密码应返回 200 并携带 CSRF Token 与用户信息")
    void studentIdLogin_withCorrectPassword_shouldReturnUserAuthResponse() throws Exception {
        User user = createMemberUser("2024005001");
        StudentIdLoginRequestDTO requestDTO = new StudentIdLoginRequestDTO();
        requestDTO.setStudentId(user.getStudentId());
        requestDTO.setPassword("password");

        mockMvc.perform(
                post("/api/v1/auth/login/student-id")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.csrfToken").exists())
                .andExpect(jsonPath("$.data.userInfo").exists())
                .andExpect(jsonPath("$.data.userInfo.id").value(user.getId()));
    }

    @Test
    @DisplayName("学号登录：错误密码应返回 401")
    void studentIdLogin_withWrongPassword_shouldReturn401() throws Exception {
        User user = createMemberUser("2024005002");
        StudentIdLoginRequestDTO requestDTO = new StudentIdLoginRequestDTO();
        requestDTO.setStudentId(user.getStudentId());
        requestDTO.setPassword("wrong-password");

        mockMvc.perform(
                post("/api/v1/auth/login/student-id")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401));
    }

    @Test
    @DisplayName("邮箱登录：正确验证码应返回 200")
    void emailLogin_withValidCode_shouldReturnUserAuthResponse() throws Exception {
        User user = createMemberUser("2024005003");
        String code = "123456";
        createVerifyCode(user.getEmail(), code, LocalDateTime.now().plusMinutes(5), "login");
        EmailLoginRequestDTO requestDTO = new EmailLoginRequestDTO();
        requestDTO.setEmail(user.getEmail());
        requestDTO.setVerifyCode(code);

        mockMvc.perform(
                post("/api/v1/auth/login/email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.csrfToken").exists())
                .andExpect(jsonPath("$.data.userInfo.id").value(user.getId()));
    }

    @Test
    @DisplayName("邮箱登录：错误验证码应返回 401")
    void emailLogin_withWrongCode_shouldReturn401() throws Exception {
        User user = createMemberUser("2024005004");
        createVerifyCode(user.getEmail(), "123456", LocalDateTime.now().plusMinutes(5), "login");
        EmailLoginRequestDTO requestDTO = new EmailLoginRequestDTO();
        requestDTO.setEmail(user.getEmail());
        requestDTO.setVerifyCode("000000");

        mockMvc.perform(
                post("/api/v1/auth/login/email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401));
    }

    @Test
    @DisplayName("发送验证码：应返回 200 并触发邮件分发")
    void sendVerificationCode_withValidEmail_shouldReturnOkAndDispatchEmail() throws Exception {
        String email = "verify-code@example.com";
        String code = "123456";
        String scene = "login";
        VerifyCode verifyCode = VerifyCode.create(email, code, LocalDateTime.now().plusMinutes(5), scene);
        when(verificationCodeDomainService.generateCode(email, scene)).thenReturn(verifyCode);

        SendVerificationCodeRequestDTO requestDTO = new SendVerificationCodeRequestDTO();
        requestDTO.setEmail(email);
        requestDTO.setScene(scene);

        mockMvc.perform(
                post("/api/v1/auth/verification-code/send")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isOk());

        ArgumentCaptor<MessageRequest> captor = ArgumentCaptor.forClass(MessageRequest.class);
        verify(messageDispatcher).dispatchAsync(captor.capture());
        MessageRequest request = captor.getValue();
        assertEquals(MessageChannel.EMAIL, request.channel());
        assertEquals(email, request.recipient());
    }

    @Test
    @DisplayName("获取登录状态：未认证时应返回 authenticated=false")
    @WithAnonymousUser
    void getAuthMe_unauthenticated_shouldReturnNotAuthenticated() throws Exception {
        mockMvc.perform(get("/api/v1/auth/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.authenticated").value(false));
    }

    @Test
    @DisplayName("获取登录状态：已认证时应返回 authenticated=true")
    @WithSecurityPrincipal(userId = 1L, roleId = 3L, roleType = "MEMBER")
    void getAuthMe_authenticated_shouldReturnAuthenticated() throws Exception {
        createMemberUser("2024005005");

        mockMvc.perform(get("/api/v1/auth/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.authenticated").value(true))
                .andExpect(jsonPath("$.data.csrfToken").exists())
                .andExpect(jsonPath("$.data.userInfo").exists());
    }

    @Test
    @DisplayName("登出：未认证时应返回 401")
    @WithAnonymousUser
    void logout_unauthenticated_shouldReturn401() throws Exception {
        mockMvc.perform(post("/api/v1/auth/logout"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("登出：已认证时应返回 200")
    @WithSecurityPrincipal(userId = 1L, roleId = 3L, roleType = "MEMBER")
    void logout_authenticated_shouldReturnOk() throws Exception {
        createMemberUser("2024005006");

        mockMvc.perform(post("/api/v1/auth/logout"))
                .andExpect(status().isOk());
    }
}
