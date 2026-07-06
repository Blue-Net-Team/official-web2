package com.bluenet.web.infrastructure.security.principal;

import com.bluenet.web.domain.model.entity.User;
import com.bluenet.web.domain.model.enumerate.RoleType;
import com.bluenet.web.infrastructure.security.util.UserCTX;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithSecurityContextFactory;

import java.util.Collections;
import java.util.Set;

/**
 * 测试用：同时设置 Spring Security 的 SecurityContext 和自定义的 UserCTX。
 * <p>
 * 这样使用 {@link WithSecurityPrincipal} 的 MockMvc 测试中，既满足 Spring Security 的认证，业务里
 * {@link UserCTX#getCurrentUser()} 也能拿到同一份 {@link User} 实体。
 * </p>
 */
public class WithSecurityPrincipalContextFactory implements WithSecurityContextFactory<WithSecurityPrincipal> {

    @Override
    public SecurityContext createSecurityContext(WithSecurityPrincipal withUser) {
        User user = User.reconstruct(withUser.userId(), "password");
        user.setStudentId(withUser.studentId());
        user.setUsername(withUser.username());
        user.setRoleId(withUser.roleId());
        user.setDirection(withUser.noDirection() ? null : withUser.direction());

        RoleType roleType = RoleType.fromName(withUser.roleType());
        Set<String> permissions = withUser.permissions().length == 0
                ? Collections.emptySet()
                : Set.of(withUser.permissions());

        SecurityPrincipal principal = new SecurityPrincipal(user, roleType, permissions);

        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                principal, null, Collections.emptyList());

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);

        // 同时设置自定义上下文，与 JwtAuthenticationFilter 行为一致
        UserCTX.setPrincipal(principal);

        return context;
    }
}
