package com.bluenet.web.application.service.impl;

import com.bluenet.web.api.dto.auth.StudentIdLoginRequestDTO;
import com.bluenet.web.api.dto.auth.UserAuthResponseDTO;
import com.bluenet.web.api.dto.user.UserInfo;
import com.bluenet.web.application.converter.UserConverter;
import com.bluenet.web.domain.exception.Unauthorized;
import com.bluenet.web.domain.model.enumerate.Direction;
import com.bluenet.web.domain.model.enumerate.LocalLoginType;
import com.bluenet.web.domain.model.vo.UserVO;
import com.bluenet.web.domain.service.AuthDomainService;
import com.bluenet.web.infrastructure.security.auth.AuthTokenService;
import com.bluenet.web.infrastructure.security.cookie.CookieService;
import com.bluenet.web.infrastructure.security.csrf.CsrfTokenService;
import com.bluenet.web.infrastructure.security.jwt.JwtUtil;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * AuthServiceImpl单元测试
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private AuthDomainService authDomainService;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private AuthTokenService authTokenService;

    @Mock
    private UserConverter userConverter;

    @Mock
    private CookieService cookieService;

    @Mock
    private CsrfTokenService csrfTokenService;

    @Mock
    private HttpServletResponse response;

    @InjectMocks
    private AuthServiceImpl authService;

    private static final Long TEST_USER_ID = 12345L;
    private static final String TEST_STUDENT_ID = "2024001001";
    private static final String TEST_EMAIL = "test@example.com";
    private static final String TEST_USERNAME = "测试用户";
    private static final String TEST_PASSWORD = "password123";
    private static final String TEST_TOKEN = "test.jwt.token";
    private static final String TEST_JTI = "test-jti-123";
    private static final String TEST_CSRF_TOKEN = "test-csrf-token-456";

    private UserVO createTestUserVO() {
        return UserVO.builder()
                .id(TEST_USER_ID)
                .studentId(TEST_STUDENT_ID)
                .email(TEST_EMAIL)
                .username(TEST_USERNAME)
                .disabled(false)
                .direction(Direction.COMPUTER_VISION)
                .build();
    }

    private StudentIdLoginRequestDTO createLoginRequest() {
        StudentIdLoginRequestDTO request = new StudentIdLoginRequestDTO();
        request.setStudentId(TEST_STUDENT_ID);
        request.setPassword(TEST_PASSWORD);
        return request;
    }

    @Test
    void login_withValidCredentials_shouldReturnCsrfToken() {
        // 准备
        UserVO userVO = createTestUserVO();
        StudentIdLoginRequestDTO request = createLoginRequest();

        when(authDomainService.checkLocalValid(TEST_STUDENT_ID, TEST_PASSWORD, LocalLoginType.STUDENT_ID))
                .thenReturn(Optional.of(userVO));
        when(jwtUtil.generateToken(TEST_USER_ID)).thenReturn(TEST_TOKEN);
        when(jwtUtil.getJti(TEST_TOKEN)).thenReturn(TEST_JTI);
        when(csrfTokenService.generateCsrfToken()).thenReturn(TEST_CSRF_TOKEN);
        UserInfo expectedUserInfo = UserInfo.builder().username(TEST_USERNAME).build();
        when(userConverter.convertToUserInfo(userVO)).thenReturn(expectedUserInfo);

        // 执行
        UserAuthResponseDTO result = authService.login(request, response);

        // 验证
        assertNotNull(result);
        assertEquals(TEST_CSRF_TOKEN, result.getCsrfToken());
        assertNotNull(result.getUserInfo());
        assertEquals(TEST_USERNAME, result.getUserInfo().getUsername());

        verify(authDomainService).checkLocalValid(TEST_STUDENT_ID, TEST_PASSWORD, LocalLoginType.STUDENT_ID);
        verify(jwtUtil).generateToken(TEST_USER_ID);
        verify(authTokenService).storeToken(TEST_JTI, TEST_USER_ID);
        verify(csrfTokenService).generateCsrfToken();
        verify(cookieService).setAuthCookies(response, TEST_TOKEN, TEST_CSRF_TOKEN);
        verify(userConverter).convertToUserInfo(userVO);
    }

    @Test
    void login_withInvalidCredentials_shouldThrowUnauthorized() {
        // 准备
        StudentIdLoginRequestDTO request = createLoginRequest();

        when(authDomainService.checkLocalValid(TEST_STUDENT_ID, TEST_PASSWORD, LocalLoginType.STUDENT_ID))
                .thenReturn(Optional.empty());

        // 执行 & 验证
        assertThrows(Unauthorized.class, () -> authService.login(request, response));

        verify(authDomainService).checkLocalValid(TEST_STUDENT_ID, TEST_PASSWORD, LocalLoginType.STUDENT_ID);
        verify(jwtUtil, never()).generateToken(any());
        verify(authTokenService, never()).storeToken(any(), any());
        verify(cookieService, never()).setAuthCookies(any(), any(), any());
    }

    @Test
    void login_withDisabledAccount_shouldThrowUnauthorized() {
        // 准备
        StudentIdLoginRequestDTO request = createLoginRequest();

        when(authDomainService.checkLocalValid(TEST_STUDENT_ID, TEST_PASSWORD, LocalLoginType.STUDENT_ID))
                .thenThrow(new Unauthorized("账户已被禁用"));

        // 执行 & 验证
        Unauthorized exception = assertThrows(Unauthorized.class, () -> authService.login(request, response));
        assertEquals("账户已被禁用", exception.getMessage());
    }

}
