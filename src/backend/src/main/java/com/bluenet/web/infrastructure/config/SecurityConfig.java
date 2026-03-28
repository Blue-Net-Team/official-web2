package com.bluenet.web.infrastructure.config;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.bluenet.web.infrastructure.security.csrf.CsrfTokenFilter;
import com.bluenet.web.infrastructure.security.jwt.JwtAuthenticationFilter;

import lombok.RequiredArgsConstructor;

/**
 * Spring Security配置类 配置JWT认证过滤器和安全规则
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final CsrfTokenFilter csrfTokenFilter;

    @Value("${cors.allowed-origins:*}")
    private String allowedOrigins;

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // 1. 允许的前端域名
        // 从环境变量读取，支持逗号分隔的多个域名
        // 生产环境应配置具体域名，如: https://example.com,https://www.example.com
        // 开发环境可使用 * 允许所有域名
        List<String> origins = Arrays.asList(allowedOrigins.split(","));
        if (origins.size() == 1 && "*".equals(origins.get(0).trim())) {
            // 使用 Pattern 允许所有域名（开发环境）
            configuration.setAllowedOriginPatterns(Collections.singletonList("*"));
        } else {
            // 使用具体域名列表（生产环境）
            configuration.setAllowedOriginPatterns(origins);
        }

        // 2. 允许的方法
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));

        // 3. 允许的头信息
        configuration.setAllowedHeaders(Collections.singletonList("*"));

        // 4. 允许暴露的头信息（前端需要读取 CSRF Token）
        configuration.setExposedHeaders(Arrays.asList("X-CSRF-Token"));

        // 5. 允许凭证（Cookie 必须开启）
        configuration.setAllowCredentials(true);

        // 6. 预检请求缓存时间（秒）
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    /**
     * 配置安全过滤器链
     *
     * @param http
     *            HttpSecurity
     * @return SecurityFilterChain
     * @throws Exception
     *             配置异常
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, FailAuthEntryPoint failAuthEntryPoint) throws Exception {
        http.cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // 禁用 Spring 内置 CSRF（使用自定义 CsrfTokenFilter）
                .csrf(AbstractHttpConfigurer::disable)

                // 配置会话管理为无状态（JWT不需要session）
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // 配置认证失败处理
                .exceptionHandling(ex -> ex.authenticationEntryPoint(failAuthEntryPoint))

                // 配置授权规则
                .authorizeHttpRequests(
                        auth -> auth
                                // 允许公开访问的端点
                                .requestMatchers("/api/v1/**")
                                .permitAll()
                                // 允许 OpenAPI 文档与 Swagger UI 无需认证
                                .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html")
                                .permitAll()
                                // 其余需要认证的接口（如 /v1/user/info/me）
                                .anyRequest()
                                .authenticated())

                // 添加JWT认证过滤器
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)

                // 添加 CSRF Token 验证过滤器（在 JWT 认证之后）
                .addFilterAfter(csrfTokenFilter, JwtAuthenticationFilter.class);

        return http.build();
    }

    /**
     * 密码编码器 使用BCrypt进行密码哈希
     *
     * @return PasswordEncoder
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
