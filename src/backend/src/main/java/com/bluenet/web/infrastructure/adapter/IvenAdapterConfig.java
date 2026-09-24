package com.bluenet.web.infrastructure.adapter;

import io.github.ivencn.infra.security.cookie.CookieProperties;
import io.github.ivencn.infra.security.csrf.CsrfProperties;
import io.github.ivencn.infra.security.csrf.CsrfTokenFilter;
import io.github.ivencn.infra.security.csrf.CsrfTokenService;
import io.github.ivencn.infra.security.http.FailAuthEntryPoint;
import io.github.ivencn.infra.security.jwt.JwtAuthenticationFilter;
import io.github.ivencn.infra.security.jwt.JwtService;
import io.github.ivencn.infra.security.auth.AuthTokenService;
import io.github.ivencn.infra.security.spi.UserDetailsLoader;
import com.bluenet.web.domain.repository.PermissionRepository;
import com.bluenet.web.domain.repository.UserRepository;
import com.bluenet.web.infrastructure.repository.converter.PermissionRepositoryConverter;
import com.bluenet.web.infrastructure.repository.mapper.PermissionMapper;
import com.bluenet.web.infrastructure.repository.mapper.RolePermissionMapper;
import com.bluenet.web.infrastructure.security.principal.RoleTypeResolver;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * iven 框架 SPI 适配器装配。
 */
@Configuration
public class IvenAdapterConfig {

    /**
     * 认证过滤器：框架过滤器 + 应用侧用户详情加载。
     */
    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter(JwtService jwtService,
            AuthTokenService authTokenService,
            UserDetailsLoader userDetailsLoader,
            FailAuthEntryPoint failAuthEntryPoint,
            CookieProperties cookieProperties) {
        return new JwtAuthenticationFilter(jwtService, authTokenService, userDetailsLoader, failAuthEntryPoint,
                cookieProperties);
    }

    @Bean
    public CsrfTokenFilter csrfTokenFilter(CsrfTokenService csrfTokenService, CookieProperties cookieProperties,
            CsrfProperties csrfProperties) {
        return new CsrfTokenFilter(csrfTokenService, cookieProperties, csrfProperties);
    }

    @Bean
    public UserDetailsLoader userDetailsLoader(UserRepository userRepository, RoleTypeResolver roleTypeResolver,
            PermissionRepository permissionRepository) {
        return new IvenUserDetailsLoader(userRepository, roleTypeResolver, permissionRepository);
    }

    @Bean
    public io.github.ivencn.infra.rbac.spi.PermissionRegistry permissionRegistry(PermissionMapper permissionMapper,
            RolePermissionMapper rolePermissionMapper,
            PermissionRepositoryConverter permissionRepositoryConverter) {
        return new DatabasePermissionRegistry(permissionMapper, rolePermissionMapper,
                permissionRepositoryConverter);
    }

    @Bean
    public io.github.ivencn.infra.rbac.spi.SuperAdminPolicy superAdminPolicy() {
        return new RoleHierarchySuperAdminPolicy();
    }

    @Bean
    public LoginContextCleanupFilter loginContextCleanupFilter() {
        return new LoginContextCleanupFilter();
    }
}
