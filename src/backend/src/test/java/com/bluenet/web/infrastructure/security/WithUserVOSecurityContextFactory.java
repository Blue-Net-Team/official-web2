package com.bluenet.web.infrastructure.security;

import com.bluenet.web.domain.model.vo.UserVO;
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
 * 这样使用 @WithUserVO 的 MockMvc 测试中，既满足 Spring Security 的认证，业务里
 * UserCTX.getCurrentUser() 也能拿到同一份 UserVO。
 * </p>
 */
public class WithUserVOSecurityContextFactory implements WithSecurityContextFactory<WithUserVO> {

    @Override
    public SecurityContext createSecurityContext(WithUserVO withUser) {
        UserVO userVO = UserVO.builder()
                .id(withUser.userId())
                .studentId(withUser.studentId())
                .username(withUser.username())
                .roleName(withUser.roleName())
                .direction(withUser.noDirection() ? null : withUser.direction())
                .permissions(
                        withUser.permissions().length == 0 ? Collections.emptySet() : Set.of(withUser.permissions()))
                .build();

        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userVO, null,
                Collections.emptyList());

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);

        // 同时设置自定义上下文，与 JwtAuthenticationFilter 行为一致
        UserCTX.setCurrentUser(userVO);

        return context;
    }
}
