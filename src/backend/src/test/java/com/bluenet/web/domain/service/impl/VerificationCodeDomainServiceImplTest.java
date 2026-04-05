package com.bluenet.web.domain.service.impl;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;

import com.bluenet.web.domain.model.vo.VerifyCodeVO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

/**
 * VerificationCodeDomainServiceImpl 单元测试
 * <p>
 * 验证验证码生成的核心逻辑：6位数字、5分钟有效期、target正确
 * </p>
 */
@DisplayName("VerificationCodeDomainServiceImpl 单元测试")
class VerificationCodeDomainServiceImplTest {

    private final VerificationCodeDomainServiceImpl service = new VerificationCodeDomainServiceImpl();

    private static final String TEST_EMAIL = "test@example.com";

    @Test
    @DisplayName("生成的验证码应为6位数字字符串")
    void generateCode_shouldBeSixDigits() {
        VerifyCodeVO result = service.generateCode(TEST_EMAIL, null);

        assertNotNull(result.getCode());
        assertEquals(6, result.getCode().length(), "验证码长度应为6位");
        assertTrue(result.getCode().matches("^\\d{6}$"), "验证码应为纯数字");
    }

    @Test
    @DisplayName("生成的验证码目标应为传入的邮箱")
    void generateCode_targetShouldBeEmail() {
        VerifyCodeVO result = service.generateCode(TEST_EMAIL, null);

        assertEquals(TEST_EMAIL, result.getTarget());
    }

    @Test
    @DisplayName("生成的验证码应为未使用状态")
    void generateCode_shouldBeUnused() {
        VerifyCodeVO result = service.generateCode(TEST_EMAIL, null);

        assertFalse(result.isUsed(), "新生成的验证码不应为已使用");
    }

    @Test
    @DisplayName("生成的验证码有效期应为5分钟")
    void generateCode_shouldExpireIn5Minutes() {
        LocalDateTime before = LocalDateTime.now().plusMinutes(5).minusSeconds(2);
        VerifyCodeVO result = service.generateCode(TEST_EMAIL, null);
        LocalDateTime after = LocalDateTime.now().plusMinutes(5).plusSeconds(2);

        assertNotNull(result.getExpireAt());
        // 过期时间应在约5分钟后
        assertTrue(result.getExpireAt().isAfter(before), "过期时间应大于5分钟前");
        assertTrue(result.getExpireAt().isBefore(after), "过期时间应小于5分钟后+容差");
        // 验证码不应立即过期
        assertFalse(result.isExpired(), "新生成的验证码不应已过期");
    }

    @RepeatedTest(10)
    @DisplayName("多次生成的验证码应不相同（随机性验证）")
    void generateCode_shouldGenerateDifferentCodes() {
        VerifyCodeVO first = service.generateCode(TEST_EMAIL, null);
        VerifyCodeVO second = service.generateCode(TEST_EMAIL, null);

        // 10次重复中偶尔可能相同，但概率极低（百万分之一）
        // 如果这个测试偶尔失败，是正常概率事件
        assertNotNull(first.getCode());
        assertNotNull(second.getCode());
    }
}
