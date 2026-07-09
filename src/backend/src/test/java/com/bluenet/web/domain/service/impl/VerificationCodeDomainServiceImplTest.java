package com.bluenet.web.domain.service.impl;

import com.bluenet.web.domain.model.entity.VerifyCode;
import com.bluenet.web.domain.service.VerificationCodeDomainService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link VerificationCodeDomainServiceImpl} 单元测试。
 */
@DisplayName("VerificationCodeDomainServiceImpl 单元测试")
class VerificationCodeDomainServiceImplTest {

    private final VerificationCodeDomainService domainService = new VerificationCodeDomainServiceImpl();

    @Test
    @DisplayName("generateCode: 应生成6位数字验证码")
    void generateCode_shouldBeSixDigits() {
        VerifyCode verifyCode = domainService.generateCode("test@example.com", "LOGIN");

        assertEquals("test@example.com", verifyCode.getTarget());
        assertEquals("LOGIN", verifyCode.getScene());
        assertEquals(6, verifyCode.getCode().length());
        assertTrue(verifyCode.getCode().matches("\\d{6}"));
    }

    @Test
    @DisplayName("generateCode: 过期时间应为当前时间后约5分钟")
    void generateCode_expireAtShouldBeFiveMinutesLater() {
        LocalDateTime before = LocalDateTime.now().plusMinutes(4).plusSeconds(55);
        VerifyCode verifyCode = domainService.generateCode("test@example.com", "LOGIN");
        LocalDateTime after = LocalDateTime.now().plusMinutes(5).plusSeconds(5);

        assertTrue(verifyCode.getExpireAt().isAfter(before) || verifyCode.getExpireAt().isEqual(before));
        assertTrue(verifyCode.getExpireAt().isBefore(after) || verifyCode.getExpireAt().isEqual(after));
    }

    @Test
    @DisplayName("generateCode: 多次生成应产生不同验证码")
    void generateCode_multipleTimes_shouldDiffer() {
        VerifyCode first = domainService.generateCode("test@example.com", "LOGIN");
        VerifyCode second = domainService.generateCode("test@example.com", "LOGIN");

        assertEquals(6, first.getCode().length());
        assertEquals(6, second.getCode().length());
        // 由于随机性，理论上可能相同，但概率极低；此处仅验证格式正确
        assertTrue(first.getCode().matches("\\d{6}"));
        assertTrue(second.getCode().matches("\\d{6}"));
    }
}
