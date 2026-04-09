package com.bluenet.web.domain.service.impl;

import com.bluenet.web.domain.exception.GlobalException;
import com.bluenet.web.domain.repository.UserRepository;
import com.bluenet.web.domain.service.ReferralCodeGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;

/**
 * 内推码生成服务实现
 * <p>
 * 生成8位大写字母+数字的唯一内推码，确保数据库中不重复
 * </p>
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ReferralCodeGeneratorImpl implements ReferralCodeGenerator {

    private final UserRepository userRepository;

    private static final int CODE_LENGTH = 8;
    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int MAX_RETRIES = 10;
    private final SecureRandom random = new SecureRandom();

    @Override
    public String generate() {
        for (int attempt = 0; attempt < MAX_RETRIES; attempt++) {
            String code = generateRandomCode();
            if (isUnique(code)) {
                log.debug("Generated unique referral code: {}", code);
                return code;
            }
            log.debug("Referral code collision detected, retrying (attempt {}/{})", attempt + 1, MAX_RETRIES);
        }
        throw new GlobalException("Failed to generate unique referral code after " + MAX_RETRIES + " attempts");
    }

    @Override
    public boolean isValidFormat(String code) {
        if (code == null || code.length() != CODE_LENGTH) {
            return false;
        }
        for (char c : code.toCharArray()) {
            if (!CHARACTERS.contains(String.valueOf(c))) {
                return false;
            }
        }
        return true;
    }

    private String generateRandomCode() {
        StringBuilder sb = new StringBuilder(CODE_LENGTH);
        for (int i = 0; i < CODE_LENGTH; i++) {
            int index = random.nextInt(CHARACTERS.length());
            sb.append(CHARACTERS.charAt(index));
        }
        return sb.toString();
    }

    private boolean isUnique(String code) {
        return !userRepository.existsByInternalReferralCode(code);
    }
}
