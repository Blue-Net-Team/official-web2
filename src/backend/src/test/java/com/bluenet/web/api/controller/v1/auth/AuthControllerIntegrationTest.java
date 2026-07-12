package com.bluenet.web.api.controller.v1.auth;

import com.bluenet.web.BaseIntegrationTest;
import com.bluenet.web.api.dto.auth.AuthMeResponseDTO;
import com.bluenet.web.api.dto.auth.EmailLoginRequestDTO;
import com.bluenet.web.api.dto.auth.SendVerificationCodeRequestDTO;
import com.bluenet.web.api.dto.auth.StudentIdLoginRequestDTO;
import com.bluenet.web.api.dto.auth.UserAuthResponseDTO;
import com.bluenet.web.api.dto.user.UserInfo;
import com.bluenet.web.api.converter.auth.AuthResponseConverter;
import com.bluenet.web.application.result.auth.AuthResult;
import com.bluenet.web.application.result.user.UserInfoResult;
import com.bluenet.web.application.service.AuthAppService;
import com.bluenet.web.application.service.UserInfoAppService;
import com.bluenet.web.domain.exception.Unauthorized;
import com.bluenet.web.infrastructure.security.principal.WithSecurityPrincipal;
import com.bluenet.web.infrastructure.security.util.UserCTX;
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
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Objects;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * AuthController 集成测试。
 * <p>
 * 验证登录、验证码发送、获取登录状态与登出等认证相关接口的 HTTP 契约、权限切面与响应格式。
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
    private StringRedisTemplate redisTemplate;

    @MockitoBean
    private AuthAppService authAppService;

    @MockitoBean
    private UserInfoAppService userInfoAppService;

    @MockitoBean
    private AuthResponseConverter authResponseConverter;

    @AfterEach
    void tearDown() {
        UserCTX.clear();
        redisTemplate.delete(Objects.requireNonNull(redisTemplate.keys("rate_limit:*")));
    }

    @Test
    @DisplayName("学号登录：正确密码应返回 200 并携带 CSRF Token 与用户信息")
    void studentIdLogin_withCorrectPassword_shouldReturnUserAuthResponse() throws Exception {
        Long userId = 1L;
        AuthResult.Login loginResult = new AuthResult.Login(userId, "csrf-token");
        UserInfoResult userInfoResult = new UserInfoResult(
                userId,
                "测试用户",
                null,
                null,
                null,
                null,
                "test@example.com",
                null,
                "MEMBER",
                null,
                null,
                null,
                null,
                null);
        UserAuthResponseDTO responseDTO = new UserAuthResponseDTO();
        responseDTO.setCsrfToken("csrf-token");
        UserInfo userInfo = UserInfo.builder()
                .id(userId)
                .username("测试用户")
                .email("test@example.com")
                .roleName("MEMBER")
                .build();
        responseDTO.setUserInfo(userInfo);

        when(authAppService.login(any(), any())).thenReturn(loginResult);
        when(userInfoAppService.getMyInfo(userId)).thenReturn(userInfoResult);
        when(authResponseConverter.toDTO(any(AuthResult.Login.class), any(UserInfoResult.class)))
                .thenReturn(responseDTO);

        StudentIdLoginRequestDTO requestDTO = new StudentIdLoginRequestDTO();
        requestDTO.setStudentId("2024005001");
        requestDTO.setPassword("password");

        mockMvc.perform(
                post("/api/v1/auth/login/student-id")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.csrfToken").exists())
                .andExpect(jsonPath("$.data.userInfo").exists())
                .andExpect(jsonPath("$.data.userInfo.id").value(userId));
    }

    @Test
    @DisplayName("学号登录：错误密码应返回 401")
    void studentIdLogin_withWrongPassword_shouldReturn401() throws Exception {
        when(authAppService.login(any(), any())).thenThrow(new Unauthorized("学号或密码错误"));

        StudentIdLoginRequestDTO requestDTO = new StudentIdLoginRequestDTO();
        requestDTO.setStudentId("2024005002");
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
        Long userId = 1L;
        AuthResult.Login loginResult = new AuthResult.Login(userId, "csrf-token");
        UserInfoResult userInfoResult = new UserInfoResult(
                userId,
                "测试用户",
                null,
                null,
                null,
                null,
                "test@example.com",
                null,
                "MEMBER",
                null,
                null,
                null,
                null,
                null);
        UserAuthResponseDTO responseDTO = new UserAuthResponseDTO();
        responseDTO.setCsrfToken("csrf-token");
        UserInfo userInfo = UserInfo.builder()
                .id(userId)
                .username("测试用户")
                .email("test@example.com")
                .roleName("MEMBER")
                .build();
        responseDTO.setUserInfo(userInfo);

        when(authAppService.loginWithEmail(any(), any())).thenReturn(loginResult);
        when(userInfoAppService.getMyInfo(userId)).thenReturn(userInfoResult);
        when(authResponseConverter.toDTO(any(AuthResult.Login.class), any(UserInfoResult.class)))
                .thenReturn(responseDTO);

        EmailLoginRequestDTO requestDTO = new EmailLoginRequestDTO();
        requestDTO.setEmail("test@example.com");
        requestDTO.setVerifyCode("123456");

        mockMvc.perform(
                post("/api/v1/auth/login/email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.csrfToken").exists())
                .andExpect(jsonPath("$.data.userInfo.id").value(userId));
    }

    @Test
    @DisplayName("邮箱登录：错误验证码应返回 401")
    void emailLogin_withWrongCode_shouldReturn401() throws Exception {
        when(authAppService.loginWithEmail(any(), any())).thenThrow(new Unauthorized("邮箱或验证码错误"));

        EmailLoginRequestDTO requestDTO = new EmailLoginRequestDTO();
        requestDTO.setEmail("test@example.com");
        requestDTO.setVerifyCode("000000");

        mockMvc.perform(
                post("/api/v1/auth/login/email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401));
    }

    @Test
    @DisplayName("发送验证码：应返回 200")
    void sendVerificationCode_withValidEmail_shouldReturnOk() throws Exception {
        doNothing().when(authAppService).sendVerificationCode(any());

        SendVerificationCodeRequestDTO requestDTO = new SendVerificationCodeRequestDTO();
        requestDTO.setEmail("verify-code@example.com");
        requestDTO.setScene("login");

        mockMvc.perform(
                post("/api/v1/auth/verification-code/send")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("获取登录状态：未认证时应返回 authenticated=false")
    @WithAnonymousUser
    void getAuthMe_unauthenticated_shouldReturnNotAuthenticated() throws Exception {
        when(authAppService.getAuthMe(any())).thenReturn(new AuthResult.AuthMe(false, null));
        AuthMeResponseDTO responseDTO = new AuthMeResponseDTO();
        responseDTO.setAuthenticated(false);
        responseDTO.setCsrfToken(null);
        responseDTO.setUserInfo(null);
        when(authResponseConverter.toDTO(any(AuthResult.AuthMe.class), nullable(UserInfoResult.class)))
                .thenReturn(responseDTO);

        mockMvc.perform(get("/api/v1/auth/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.authenticated").value(false));
    }

    @Test
    @DisplayName("获取登录状态：已认证时应返回 authenticated=true")
    @WithSecurityPrincipal(userId = 1L, roleId = 3L, roleType = "MEMBER")
    void getAuthMe_authenticated_shouldReturnAuthenticated() throws Exception {
        Long userId = 1L;
        AuthResult.AuthMe authMeResult = new AuthResult.AuthMe(true, "csrf-token");
        UserInfoResult userInfoResult = new UserInfoResult(
                userId,
                "测试用户",
                null,
                null,
                null,
                null,
                "test@example.com",
                null,
                "MEMBER",
                null,
                null,
                null,
                null,
                null);
        AuthMeResponseDTO responseDTO = new AuthMeResponseDTO();
        responseDTO.setAuthenticated(true);
        responseDTO.setCsrfToken("csrf-token");
        UserInfo userInfo = UserInfo.builder()
                .id(userId)
                .username("测试用户")
                .email("test@example.com")
                .roleName("MEMBER")
                .build();
        responseDTO.setUserInfo(userInfo);

        when(authAppService.getAuthMe(any())).thenReturn(authMeResult);
        when(userInfoAppService.getMyInfo(userId)).thenReturn(userInfoResult);
        when(authResponseConverter.toDTO(any(AuthResult.AuthMe.class), nullable(UserInfoResult.class)))
                .thenReturn(responseDTO);

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
        doNothing().when(authAppService).logout(any());

        mockMvc.perform(post("/api/v1/auth/logout"))
                .andExpect(status().isOk());
    }
}
