package com.bluenet.web.testsupport.fixture;

import com.bluenet.web.domain.model.entity.User;
import com.bluenet.web.domain.model.enumerate.Direction;
import com.bluenet.web.domain.model.enumerate.RoleType;
import com.bluenet.web.domain.repository.UserRepository;
import com.bluenet.web.infrastructure.adapter.LoginContext;
import io.github.ivencn.infra.security.principal.SecurityPrincipal;
import io.github.ivencn.infra.security.principal.UserCTX;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Collections;
import java.util.Set;

/**
 * 安全上下文测试夹具，用于在测试中设置/清理 {@link UserCTX}。
 */
public final class SecurityContextFixture {

    private SecurityContextFixture() {
    }

    public static SecurityPrincipal principal(User user, RoleType roleType) {
        return new SecurityPrincipal(user.getId(), roleType, Collections.emptySet());
    }

    public static SecurityPrincipal principal(User user, RoleType roleType, Set<String> permissions) {
        return new SecurityPrincipal(user.getId(), roleType, permissions);
    }

    public static void asMember(User user) {
        as(user, RoleType.MEMBER);
    }

    public static void asCandidate(User user) {
        as(user, RoleType.CANDIDATE);
    }

    public static void asDirectionAdmin(User user, Direction direction) {
        as(user, RoleType.DIRECTION_ADMIN);
    }

    public static void asSuperAdmin(User user) {
        as(user, RoleType.SUPER_ADMIN);
    }

    /**
     * 同时写入框架主体与业务侧登录上下文。
     *
     * <p>
     * 应用服务读取的是 {@link LoginContext}（由 {@code IvenUserDetailsLoader} 在真实认证时写入）， 只设框架
     * {@code UserCTX} 会让服务层报 「未登录」，所以夹具必须两边都写。
     * </p>
     */
    public static void as(User user, RoleType roleType) {
        UserCTX.setPrincipal(principal(user, roleType));
        LoginContext.set(user, roleType);
    }

    public static void as(User user, RoleType roleType, Set<String> permissions) {
        UserCTX.setPrincipal(principal(user, roleType, permissions));
        LoginContext.set(user, roleType);
    }

    public static void clear() {
        UserCTX.clear();
        LoginContext.clear();
    }

    /**
     * 保存用户并设置为当前安全主体。
     */
    public static User saveAndAuthenticate(UserRepository userRepository, PasswordEncoder passwordEncoder,
            User user, RoleType roleType) {
        User saved = save(userRepository, passwordEncoder, user);
        as(saved, roleType);
        return saved;
    }

    private static User save(UserRepository userRepository, PasswordEncoder passwordEncoder, User user) {
        if (!user.getPassword().startsWith("$2a$") && !user.getPassword().startsWith("$2b$")
                && !user.getPassword().startsWith("$2y$")) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }
        userRepository.save(user);
        return user;
    }
}
