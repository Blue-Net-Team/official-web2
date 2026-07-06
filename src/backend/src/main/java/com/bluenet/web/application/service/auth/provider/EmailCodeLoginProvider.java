package com.bluenet.web.application.service.auth.provider;

import java.util.Optional;

import com.bluenet.web.application.command.auth.AuthCommands;
import com.bluenet.web.application.service.auth.strategy.AbstractAuthProvider;
import com.bluenet.web.application.service.auth.strategy.AuthProviderType;
import com.bluenet.web.domain.exception.Unauthorized;
import com.bluenet.web.domain.model.entity.User;
import com.bluenet.web.domain.model.enumerate.LocalLoginType;
import com.bluenet.web.domain.repository.VerificationCodeRepository;
import com.bluenet.web.domain.service.AuthDomainService;

import lombok.extern.slf4j.Slf4j;

/**
 * 邮箱验证码登录 provider，封装验证码凭证校验和消费逻辑。
 */
@Slf4j
public class EmailCodeLoginProvider extends AbstractAuthProvider<AuthCommands.EmailLoginCommand, User> {
    private final AuthDomainService authDomainService;
    private final VerificationCodeRepository verificationCodeRepository;

    public EmailCodeLoginProvider(AuthDomainService authDomainService,
            VerificationCodeRepository verificationCodeRepository) {
        super(AuthProviderType.EMAIL_CODE);
        this.authDomainService = authDomainService;
        this.verificationCodeRepository = verificationCodeRepository;
    }

    /**
     * 校验邮箱验证码并返回用户。
     *
     * @param command
     *            邮箱验证码登录命令。
     * @return 通过校验的用户实体。
     */
    public User authenticate(AuthCommands.EmailLoginCommand command) {
        String email = command.email();
        String verifyCode = command.verifyCode();
        Optional<User> userOptional = authDomainService.checkLocalValid(email, verifyCode, LocalLoginType.EMAIL);
        User user = userOptional.orElseThrow(() -> {
            log.warn("Email login failed: invalid credentials - {}", email);
            return new Unauthorized("邮箱或验证码错误");
        });
        verificationCodeRepository.markAsUsed(email, verifyCode);
        return user;
    }
}
