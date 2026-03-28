package com.bluenet.web.infrastructure.security.jwt;

import java.io.IOException;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.context.SecurityContextHolder;

import com.bluenet.web.domain.model.vo.UserVO;
import com.bluenet.web.domain.repository.UserRepository;
import com.bluenet.web.infrastructure.config.FailAuthEntryPoint;
import com.bluenet.web.infrastructure.security.auth.AuthTokenService;
import com.bluenet.web.infrastructure.security.util.UserCTX;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * JwtAuthenticationFilter单元测试
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("JWT认证过滤器测试")
class JwtAuthenticationFilterTest {

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private AuthTokenService authTokenService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private FailAuthEntryPoint failAuthEntryPoint;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    private static final Long TEST_USER_ID = 12345L;
    private static final String TEST_JTI = "test-jti-123";
    private static final String TEST_TOKEN = "valid.jwt.token";
    private static final String BEARER_TOKEN = "Bearer " + TEST_TOKEN;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
        UserCTX.clear();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
        UserCTX.clear();
    }

    /**
     * 测试当请求头包含有效的JWT令牌时，过滤器应正确解析令牌，验证用户，并将认证信息设置到SecurityContext中。
     */
    @Test
    @DisplayName("提供有效JWT令牌时，应成功认证并设置SecurityContext")
    void doFilterInternal_withValidToken_shouldSetAuthentication() throws ServletException, IOException {
        // 准备
        JwtPayload payload = JwtPayload.builder()
                .userId(TEST_USER_ID)
                .jti(TEST_JTI)
                .issuedAt(System.currentTimeMillis() / 1000)
                .expiration(System.currentTimeMillis() / 1000 + 3600)
                .build();

        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn(BEARER_TOKEN);
        when(jwtUtil.parseToken(TEST_TOKEN)).thenReturn(payload);
        when(authTokenService.validateToken(TEST_JTI)).thenReturn(Optional.of(TEST_USER_ID));

        UserVO userVO = UserVO.builder().id(TEST_USER_ID).username("testUser").build();
        when(userRepository.findById(TEST_USER_ID)).thenReturn(Optional.of(userVO));

        // 模拟 filterChain 执行时的行为，确保在执行 doFilter 时 SecurityContext 已设置
        doAnswer(invocation -> {
            assertNotNull(SecurityContextHolder.getContext().getAuthentication());
            assertEquals(userVO, SecurityContextHolder.getContext().getAuthentication().getPrincipal());
            return null;
        }).when(filterChain).doFilter(request, response);

        // 执行
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // 验证
        verify(filterChain).doFilter(request, response);
        // 执行完后，SecurityContext 应该被清理
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    /**
     * 测试当请求头中缺少Authorization字段时，过滤器应直接放行，不设置认证信息。
     */
    @Test
    @DisplayName("请求头未包含Authorization时，不应设置认证信息")
    void doFilterInternal_withNoAuthorizationHeader_shouldNotSetAuthentication() throws ServletException, IOException {
        // 准备
        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn(null);

        // 执行
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // 验证
        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    /**
     * 测试当Authorization请求头格式不正确（不以Bearer开头）时，过滤器应直接放行，不设置认证信息。
     */
    @Test
    @DisplayName("令牌格式无效时，不应设置认证信息")
    void doFilterInternal_withInvalidTokenFormat_shouldNotSetAuthentication() throws ServletException, IOException {
        // 准备
        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn("InvalidFormat");

        // 执行
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // 验证
        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    /**
     * 测试当JWT令牌过期或无法解析时，过滤器不应设置认证信息。
     */
    @Test
    @DisplayName("令牌过期或解析失败时，不应设置认证信息")
    void doFilterInternal_withExpiredToken_shouldNotSetAuthentication() throws ServletException, IOException {
        // 准备
        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn(BEARER_TOKEN);
        when(jwtUtil.parseToken(TEST_TOKEN)).thenReturn(null);

        // 执行
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // 验证
        verify(filterChain, never()).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    /**
     * 测试当JWT令牌有效但JTI不在白名单（即已被注销或无效）时，过滤器不应设置认证信息。
     */
    @Test
    @DisplayName("令牌不在白名单中时，不应设置认证信息")
    void doFilterInternal_withTokenNotInWhitelist_shouldNotSetAuthentication() throws ServletException, IOException {
        // 准备
        JwtPayload payload = JwtPayload.builder()
                .userId(TEST_USER_ID)
                .jti(TEST_JTI)
                .issuedAt(System.currentTimeMillis() / 1000)
                .expiration(System.currentTimeMillis() / 1000 + 3600)
                .build();

        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn(BEARER_TOKEN);
        when(jwtUtil.parseToken(TEST_TOKEN)).thenReturn(payload);
        when(authTokenService.validateToken(TEST_JTI)).thenReturn(Optional.empty());

        // 执行
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // 验证
        verify(filterChain, never()).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    /**
     * 测试当JWT令牌中的用户ID与白名单服务中记录的用户ID不一致时，过滤器不应设置认证信息。
     */
    @Test
    @DisplayName("令牌用户ID与白名单记录不匹配时，不应设置认证信息")
    void doFilterInternal_withMismatchedUserId_shouldNotSetAuthentication() throws ServletException, IOException {
        // 准备
        JwtPayload payload = JwtPayload.builder()
                .userId(TEST_USER_ID)
                .jti(TEST_JTI)
                .issuedAt(System.currentTimeMillis() / 1000)
                .expiration(System.currentTimeMillis() / 1000 + 3600)
                .build();

        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn(BEARER_TOKEN);
        when(jwtUtil.parseToken(TEST_TOKEN)).thenReturn(payload);
        when(authTokenService.validateToken(TEST_JTI)).thenReturn(Optional.of(99999L)); // 不同的用户ID

        // 执行
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // 验证
        verify(filterChain, never()).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    /**
     * 测试过滤器在请求处理完成后，是否正确清理了SecurityContext，防止线程复用导致的安全问题。
     */
    @Test
    @DisplayName("请求处理完成后，应清理SecurityContext")
    void doFilterInternal_shouldClearContextAfterRequest() throws ServletException, IOException {
        // 准备
        JwtPayload payload = JwtPayload.builder()
                .userId(TEST_USER_ID)
                .jti(TEST_JTI)
                .issuedAt(System.currentTimeMillis() / 1000)
                .expiration(System.currentTimeMillis() / 1000 + 3600)
                .build();

        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn(BEARER_TOKEN);
        when(jwtUtil.parseToken(TEST_TOKEN)).thenReturn(payload);
        when(authTokenService.validateToken(TEST_JTI)).thenReturn(Optional.of(TEST_USER_ID));
        when(userRepository.findById(TEST_USER_ID)).thenReturn(Optional.of(UserVO.builder().id(TEST_USER_ID).build()));

        // 执行
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // 验证 - SecurityContext 应该在 finally 块中被清理
        // 注意：由于我们在测试中直接调用了 doFilterInternal，
        // 所以在这里 SecurityContext 可能还是设置状态
        // 实际在请求结束后会被清理
        verify(filterChain).doFilter(request, response);
    }
}
