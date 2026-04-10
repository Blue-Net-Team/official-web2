package com.bluenet.web.domain.service.impl;

import java.security.SecureRandom;
import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import com.bluenet.web.domain.model.vo.VerifyCodeVO;
import com.bluenet.web.domain.service.VerificationCodeDomainService;

import lombok.extern.slf4j.Slf4j;

/**
 * 验证码领域服务实现
 * <p>
 * 负责生成6位数字验证码，有效期5分钟
 * </p>
 */
@Slf4j
@Service
public class VerificationCodeDomainServiceImpl implements VerificationCodeDomainService {

    private static final int CODE_LENGTH = 6;
    private static final int CODE_VALIDITY_MINUTES = 5;
    private static final int CODE_BOUND = 1_000_000;

    private final SecureRandom secureRandom = new SecureRandom();

    @Override
    public VerifyCodeVO generateCode(String email, String scene) {
        String code = generateSixDigitCode();
        LocalDateTime expireAt = LocalDateTime.now().plusMinutes(CODE_VALIDITY_MINUTES);

        log.debug("验证码已生成 - target={}, scene={}, code={}, expireAt={}", email, scene, code, expireAt);

        return VerifyCodeVO.builder()
                .target(email)
                .code(code)
                .expireAt(expireAt)
                .used(false)
                .scene(scene)
                .build();
    }

    private String generateSixDigitCode() {
        int code = secureRandom.nextInt(CODE_BOUND);
        return String.format("%0" + CODE_LENGTH + "d", code);
    }
}
