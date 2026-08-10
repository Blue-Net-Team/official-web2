package com.bluenet.web.application.service.auth.provider;

import java.util.Optional;

import com.bluenet.web.application.command.auth.AuthCommands;
import com.bluenet.web.application.service.auth.strategy.AbstractAuthProvider;
import com.bluenet.web.application.service.auth.strategy.AuthProviderType;
import com.bluenet.web.domain.exception.Unauthorized;
import com.bluenet.web.domain.model.entity.User;
import com.bluenet.web.domain.model.enumerate.LocalLoginType;
import com.bluenet.web.domain.service.AuthDomainService;

import lombok.extern.slf4j.Slf4j;

/**
 * 学号密码登录凭证校验 provider。
 */
@Slf4j
public class StudentIdLoginProvider extends AbstractAuthProvider<AuthCommands.StudentIdLoginCommand, User> {
    private final AuthDomainService authDomainService;

    public StudentIdLoginProvider(AuthDomainService authDomainService) {
        super(AuthProviderType.STUDENT_ID);
        this.authDomainService = authDomainService;
    }

    /**
     * 校验学号密码并返回用户。
     *
     * @param command
     *            登录命令。
     * @return 通过校验的用户实体。
     */
    public User authenticate(AuthCommands.StudentIdLoginCommand command) {
        Optional<User> userOptional = authDomainService.checkLocalValid(
                command.studentId(),
                command.password(),
                LocalLoginType.STUDENT_ID);
        return userOptional.orElseThrow(() -> {
            log.warn("Login failed: invalid credentials - {}", command.studentId());
            return new Unauthorized("学号或密码错误");
        });
    }
}
