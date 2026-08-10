package com.bluenet.web.testsupport.fixture;

import com.bluenet.web.domain.model.entity.User;
import com.bluenet.web.domain.model.enumerate.Direction;
import com.bluenet.web.domain.model.enumerate.RoleType;
import com.bluenet.web.domain.repository.UserRepository;
import com.bluenet.web.infrastructure.security.principal.SecurityPrincipal;
import com.bluenet.web.infrastructure.security.util.UserCTX;
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
        return new SecurityPrincipal(user, roleType, Collections.emptySet());
    }

    public static SecurityPrincipal principal(User user, RoleType roleType, Set<String> permissions) {
        return new SecurityPrincipal(user, roleType, permissions);
    }

    public static void asMember(User user) {
        UserCTX.setPrincipal(principal(user, RoleType.MEMBER));
    }

    public static void asCandidate(User user) {
        UserCTX.setPrincipal(principal(user, RoleType.CANDIDATE));
    }

    public static void asDirectionAdmin(User user, Direction direction) {
        UserCTX.setPrincipal(principal(user, RoleType.DIRECTION_ADMIN));
    }

    public static void asSuperAdmin(User user) {
        UserCTX.setPrincipal(principal(user, RoleType.SUPER_ADMIN));
    }

    public static void as(User user, RoleType roleType) {
        UserCTX.setPrincipal(principal(user, roleType));
    }

    public static void as(User user, RoleType roleType, Set<String> permissions) {
        UserCTX.setPrincipal(principal(user, roleType, permissions));
    }

    public static void clear() {
        UserCTX.clear();
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
