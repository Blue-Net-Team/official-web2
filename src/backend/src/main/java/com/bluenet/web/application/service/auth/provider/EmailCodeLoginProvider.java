package com.bluenet.web.application.service.auth.provider;

import java.util.Optional;

import com.bluenet.web.application.service.auth.credential.EmailCodeCredential;
import com.bluenet.web.application.service.auth.strategy.AbstractAuthProvider;
import com.bluenet.web.application.service.auth.strategy.AuthProviderType;
import com.bluenet.web.domain.exception.Unauthorized;
import com.bluenet.web.domain.model.enumerate.LocalLoginType;
import com.bluenet.web.domain.model.vo.UserVO;
import com.bluenet.web.domain.repository.VerificationCodeRepository;
import com.bluenet.web.domain.service.AuthDomainService;

import lombok.extern.slf4j.Slf4j;

/**
 * 邮箱验证码登录 provider，封装验证码凭证校验和消费逻辑。
 */
@Slf4j
public class EmailCodeLoginProvider extends AbstractAuthProvider<EmailCodeCredential, UserVO> {
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
     * @param credential
     *            邮箱验证码登录凭证。
     * @return 通过校验的用户。
     */
    public UserVO authenticate(EmailCodeCredential credential) {
        String email = credential.email();
        String verifyCode = credential.verifyCode();
        Optional<UserVO> userVOOptional = authDomainService.checkLocalValid(email, verifyCode, LocalLoginType.EMAIL);
        UserVO userVO = userVOOptional.orElseThrow(() -> {
            log.warn("Email login failed: invalid credentials - {}", email);
            return new Unauthorized("邮箱或验证码错误");
        });
        verificationCodeRepository.markAsUsed(email, verifyCode);
        return userVO;
    }
}
