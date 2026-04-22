package com.bluenet.web.application.service.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.bluenet.web.application.service.ResetPasswordService;
import com.bluenet.web.domain.exception.BadRequest;
import com.bluenet.web.domain.model.entity.User;
import com.bluenet.web.domain.model.vo.UserVO;
import com.bluenet.web.domain.model.vo.VerifyCodeVO;
import com.bluenet.web.domain.repository.UserRepository;
import com.bluenet.web.domain.repository.VerificationCodeRepository;
import com.bluenet.web.domain.service.VerificationCodeDomainService;
import com.bluenet.web.domain.model.enumerate.MessageChannel;
import com.bluenet.web.application.message.MessageDispatcher;
import com.bluenet.web.application.message.MessageRequest;
import com.bluenet.web.infrastructure.security.auth.AuthTokenService;
import com.bluenet.web.infrastructure.security.reset.ResetPasswordStateService;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 密码重置应用服务实现
 * <p>
 * 编排4步密码重置流程，使用 Redis 管理中间状态
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ResetPasswordServiceImpl implements ResetPasswordService {

    private static final String SCENE = "reset_password";
    private static final String FIELD_STEP = "step";
    private static final String FIELD_EMAIL = "email";
    private static final String FIELD_USER_ID = "userId";

    private final ResetPasswordStateService stateService;
    private final UserRepository userRepository;
    private final VerificationCodeDomainService verificationCodeDomainService;
    private final VerificationCodeRepository verificationCodeRepository;
    private final MessageDispatcher messageDispatcher;
    private final PasswordEncoder passwordEncoder;
    private final AuthTokenService authTokenService;

    @Override
    public String verifyStudent(String studentId) {
        Optional<UserVO> userOpt = userRepository.findByStudentId(studentId);
        if (userOpt.isEmpty()) {
            throw new BadRequest("学号不存在");
        }

        UserVO user = userOpt.get();
        String resetToken = stateService.create(studentId, user.getId());
        log.info("Password reset initiated for student: {}", studentId);
        return resetToken;
    }

    @Override
    public String verifyEmail(String resetToken, String email) {
        validateToken(resetToken, 1);

        String studentId = stateService.getField(resetToken, "studentId");
        Optional<UserVO> userOpt = userRepository.findByStudentId(studentId);
        if (userOpt.isEmpty()) {
            throw new BadRequest("用户不存在");
        }

        UserVO user = userOpt.get();
        if (!email.equals(user.getEmail())) {
            throw new BadRequest("邮箱与学号不匹配");
        }

        Map<String, String> updates = new HashMap<>();
        updates.put(FIELD_STEP, "2");
        updates.put(FIELD_EMAIL, email);
        stateService.update(resetToken, updates);

        log.info("Email verified for password reset: student={}, email={}", studentId, email);
        return resetToken;
    }

    @Override
    public void sendCode(String resetToken) {
        validateToken(resetToken, 2);

        String email = stateService.getField(resetToken, FIELD_EMAIL);
        if (email == null) {
            throw new BadRequest("重置流程状态异常，请重新开始");
        }

        // 生成验证码
        VerifyCodeVO verifyCodeVO = verificationCodeDomainService.generateCode(email, SCENE);
        verificationCodeRepository.save(verifyCodeVO);

        // 发送邮件
        String subject = "蓝网密码重置验证码";
        String htmlContent = buildResetCodeEmail(verifyCodeVO.getCode());
        messageDispatcher.dispatchAsync(MessageRequest.html(MessageChannel.EMAIL, email, subject, htmlContent));

        // 更新步骤
        Map<String, String> updates = new HashMap<>();
        updates.put(FIELD_STEP, "3");
        stateService.update(resetToken, updates);

        log.info("Reset code sent to email: {}", email);
    }

    @Override
    public void verifyCode(String resetToken, String code) {
        validateToken(resetToken, 3);

        String email = stateService.getField(resetToken, FIELD_EMAIL);
        if (email == null) {
            throw new BadRequest("重置流程状态异常，请重新开始");
        }

        Optional<VerifyCodeVO> codeOpt = verificationCodeRepository.findByEmailAndCodeAndScene(email, code, SCENE);
        if (codeOpt.isEmpty()) {
            throw new BadRequest("验证码错误");
        }
        VerifyCodeVO verifyCodeVO = codeOpt.get();
        if (verifyCodeVO.isUsed()) {
            throw new BadRequest("验证码已使用");
        }
        if (verifyCodeVO.getExpireAt() != null && verifyCodeVO.getExpireAt().isBefore(java.time.LocalDateTime.now())) {
            throw new BadRequest("验证码已过期");
        }

        // 标记验证码已使用
        verificationCodeRepository.markAsUsed(email, code, SCENE);

        // 更新步骤到4
        Map<String, String> updates = new HashMap<>();
        updates.put(FIELD_STEP, "4");
        stateService.update(resetToken, updates);

        log.info("Reset code verified for email: {}", email);
    }

    @Override
    @Transactional
    public void resetPassword(String resetToken, String newPassword) {
        validateToken(resetToken, 4);

        String userIdStr = stateService.getField(resetToken, FIELD_USER_ID);
        if (userIdStr == null) {
            throw new BadRequest("重置流程状态异常，请重新开始");
        }

        // 编码新密码
        Long userId = Long.parseLong(userIdStr);
        UserVO userVO = userRepository.findById(userId)
                .orElseThrow(() -> new BadRequest("用户不存在"));
        User user = User.builder()
                .id(userVO.getId())
                .password(userVO.getPassword())
                .build();
        user.changePassword(passwordEncoder.encode(newPassword));
        userRepository.updatePassword(user.getId(), user.getPassword());

        // 吊销该用户所有已登录设备的 Token
        authTokenService.revokeAllUserTokens(userId);

        // 删除流程状态
        stateService.delete(resetToken);

        log.info("Password reset successful for userId: {}", userId);
    }

    private void validateToken(String resetToken, int requiredStep) {
        if (!stateService.exists(resetToken)) {
            throw new BadRequest("重置流程已过期，请重新开始");
        }
        int currentStep = stateService.getStep(resetToken);
        if (currentStep < requiredStep) {
            throw new BadRequest("请先完成上一步验证");
        }
    }

    private String buildResetCodeEmail(String code) {
        return """
                <div style="max-width:400px;margin:0 auto;padding:20px;font-family:sans-serif;">
                    <h2 style="color:#fa8c16;text-align:center;">蓝网密码重置验证码</h2>
                    <p style="text-align:center;font-size:14px;color:#666;">您正在重置密码，验证码为：</p>
                    <p style="text-align:center;font-size:32px;font-weight:bold;letter-spacing:8px;color:#fa8c16;">%s</p>
                    <p style="text-align:center;font-size:12px;color:#999;">验证码5分钟内有效，如非本人操作请忽略此邮件</p>
                </div>
                """
                .formatted(code);
    }
}
