package com.bluenet.web.domain.service.impl;

import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.bluenet.web.domain.model.entity.User;
import com.bluenet.web.domain.model.entity.VerifyCode;
import com.bluenet.web.domain.model.enumerate.LocalLoginType;
import com.bluenet.web.domain.repository.UserRepository;
import com.bluenet.web.domain.repository.VerificationCodeRepository;
import com.bluenet.web.domain.service.AuthDomainService;
import com.bluenet.web.domain.exception.Unauthorized;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 认证领域服务实现类
 * <p>
 * 封装认证相关的领域逻辑，包括： - 本地登录验证（学号/邮箱 + 密码） - Token缓存管理（Redis白名单）
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthDomainServiceImpl implements AuthDomainService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final VerificationCodeRepository verificationCodeRepository;

    @Override
    public Optional<User> checkLocalValid(String userSign, String principal, LocalLoginType localLoginType) {
        // 1. 根据登录类型查询用户
        User user = findUserByLoginType(userSign, localLoginType);

        // 2. 检查账户状态
        if (Boolean.TRUE.equals(user.getDisable())) {
            log.warn("Login failed: account disabled - type={}, sign={}", localLoginType, userSign);
            throw new Unauthorized("账户已被禁用");
        }

        // 3. 验证凭证
        if (!verifyPrincipal(principal, user, localLoginType)) {
            log.warn("Login failed: incorrect password - type={}, sign={}", localLoginType, userSign);
            return Optional.empty();
        }

        log.debug("Local login validation passed - type={}, sign={}", localLoginType, userSign);
        return Optional.of(user);
    }

    /**
     * 根据登录类型查询用户
     *
     * @param userSign
     *            用户标识（学号或邮箱）
     * @param localLoginType
     *            登录类型
     * @return 用户实体，未找到返回null
     */
    private User findUserByLoginType(String userSign, LocalLoginType localLoginType) {
        Optional<User> userOpt = Optional.empty();
        switch (localLoginType) {
            case STUDENT_ID -> userOpt = userRepository.findByStudentId(userSign);
            case EMAIL -> userOpt = userRepository.findByEmail(userSign);
        }
        if (userOpt.isEmpty()) {
            log.error("用户不存在 - type={}, sign={}", localLoginType, userSign);
            throw new Unauthorized("账号或密码错误");
        }
        return userOpt.get();
    }

    /**
     * 验证登录凭证是否合法
     *
     * @param principal
     *            认证凭证（如密码或验证码）
     * @param user
     *            用户实体
     * @param localLoginType
     *            登录类型
     * @return 如果合法返回true，否则返回false
     */
    private boolean verifyPrincipal(String principal, User user, LocalLoginType localLoginType) {
        return switch (localLoginType) {
            case STUDENT_ID -> verifyPassword(principal, user.getPassword());
            case EMAIL -> verifyCode(principal, user.getEmail());
        };
    }

    /**
     * 验证原始密码与加密密码是否匹配
     *
     * @param rawPassword
     *            原始密码，即用户输入的密码
     * @param encodedPassword
     *            加密后的密码，即数据库中存储的密码
     * @return 如果匹配返回true，否则返回false
     */
    private boolean verifyPassword(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }

    /**
     * 验证验证码是否正确
     *
     * @param verifyCode
     *            验证码
     * @param userEmail
     *            用户邮箱
     * @return 如果验证码正确返回true，否则返回false
     */
    private boolean verifyCode(String verifyCode, String userEmail) {
        Optional<VerifyCode> verifyCodeOptional = verificationCodeRepository.findByEmailAndCode(
                userEmail,
                verifyCode);
        if (verifyCodeOptional.isEmpty()) {
            log.warn("验证码无效 未找到 - email={}, code={}", userEmail, verifyCode);
            return false;
        }

        VerifyCode code = verifyCodeOptional.get();
        if (code.isExpired()) {
            log.warn("验证码无效 已过期 - email={}, code={}", userEmail, verifyCode);
            return false;
        }

        if (code.isUsed()) {
            log.warn("验证码无效 已被使用 - email={}, code={}", userEmail, verifyCode);
            return false;
        }

        log.debug("验证码验证通过 - email={}, code={}", userEmail, verifyCode);
        return true;
    }
}
