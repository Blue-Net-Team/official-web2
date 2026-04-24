package com.bluenet.web.application.service.impl;

import com.bluenet.web.application.ResetPasswordResult;
import com.bluenet.web.application.command.resetpassword.ResetPasswordCommands;
import com.bluenet.web.application.message.MessageDispatcher;
import com.bluenet.web.application.message.MessageRequest;
import com.bluenet.web.application.message.template.EmailVerificationCodeTemplate;
import com.bluenet.web.application.message.template.VerificationCodeScene;
import com.bluenet.web.application.service.ResetPasswordAppService;
import com.bluenet.web.domain.exception.BadRequest;
import com.bluenet.web.domain.model.entity.User;
import com.bluenet.web.domain.model.enumerate.MessageChannel;
import com.bluenet.web.domain.model.vo.UserVO;
import com.bluenet.web.domain.model.vo.VerifyCodeVO;
import com.bluenet.web.domain.repository.UserRepository;
import com.bluenet.web.domain.repository.VerificationCodeRepository;
import com.bluenet.web.domain.service.VerificationCodeDomainService;
import com.bluenet.web.infrastructure.security.auth.AuthTokenService;
import com.bluenet.web.infrastructure.security.reset.ResetPasswordStateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * 密码重置应用服务实现。
 * <p>
 * 实现密码重置聚合在应用层的业务逻辑编排。
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ResetPasswordAppServiceImpl implements ResetPasswordAppService {

    private static final String SCENE = "reset_password";
    private static final String FIELD_STEP = "step";
    private static final String FIELD_EMAIL = "email";
    private static final String FIELD_USER_ID = "userId";

    private final ResetPasswordStateService stateService;
    private final UserRepository userRepository;
    private final VerificationCodeDomainService verificationCodeDomainService;
    private final VerificationCodeRepository verificationCodeRepository;
    private final MessageDispatcher messageDispatcher;
    private final EmailVerificationCodeTemplate emailVerificationCodeTemplate;
    private final PasswordEncoder passwordEncoder;
    private final AuthTokenService authTokenService;

    /**
     * 校验学生身份。
     *
     * @param command
     *            校验学生命令
     * @return 校验结果
     */
    @Override
    public ResetPasswordResult.VerifyStudent verifyStudent(ResetPasswordCommands.VerifyStudentCommand command) {
        Optional<UserVO> userOpt = userRepository.findByStudentId(command.studentId());
        if (userOpt.isEmpty()) {
            throw new BadRequest("学号不存在");
        }

        UserVO user = userOpt.get();
        String resetToken = stateService.create(command.studentId(), user.getId());
        log.info("Password reset initiated for student: {}", command.studentId());
        return new ResetPasswordResult.VerifyStudent(resetToken);
    }

    /**
     * 校验邮箱。
     *
     * @param command
     *            校验邮箱命令
     * @return 校验结果
     */
    @Override
    public ResetPasswordResult.VerifyEmail verifyEmail(ResetPasswordCommands.VerifyEmailCommand command) {
        validateToken(command.resetToken(), 1);

        String studentId = stateService.getField(command.resetToken(), "studentId");
        Optional<UserVO> userOpt = userRepository.findByStudentId(studentId);
        if (userOpt.isEmpty()) {
            throw new BadRequest("用户不存在");
        }

        UserVO user = userOpt.get();
        if (!command.email().equals(user.getEmail())) {
            throw new BadRequest("邮箱与学号不匹配");
        }

        Map<String, String> updates = new HashMap<>();
        updates.put(FIELD_STEP, "2");
        updates.put(FIELD_EMAIL, command.email());
        stateService.update(command.resetToken(), updates);

        log.info("Email verified for password reset: student={}, email={}", studentId, command.email());
        return new ResetPasswordResult.VerifyEmail(command.resetToken());
    }

    /**
     * 发送验证码。
     *
     * @param command
     *            发送验证码命令
     */
    @Override
    public void sendCode(ResetPasswordCommands.SendCodeCommand command) {
        validateToken(command.resetToken(), 2);

        String email = stateService.getField(command.resetToken(), FIELD_EMAIL);
        if (email == null) {
            throw new BadRequest("重置流程状态异常，请重新开始");
        }

        VerifyCodeVO verifyCodeVO = verificationCodeDomainService.generateCode(email, SCENE);
        verificationCodeRepository.save(verifyCodeVO);

        String subject = "蓝网密码重置验证码";
        String htmlContent = emailVerificationCodeTemplate
                .buildHtml(VerificationCodeScene.RESET_PASSWORD, verifyCodeVO.getCode());
        messageDispatcher.dispatchAsync(MessageRequest.html(MessageChannel.EMAIL, email, subject, htmlContent));

        Map<String, String> updates = new HashMap<>();
        updates.put(FIELD_STEP, "3");
        stateService.update(command.resetToken(), updates);

        log.info("Reset code sent to email: {}", email);
    }

    /**
     * 校验验证码。
     *
     * @param command
     *            校验验证码命令
     */
    @Override
    public void verifyCode(ResetPasswordCommands.VerifyCodeCommand command) {
        validateToken(command.resetToken(), 3);

        String email = stateService.getField(command.resetToken(), FIELD_EMAIL);
        if (email == null) {
            throw new BadRequest("重置流程状态异常，请重新开始");
        }

        Optional<VerifyCodeVO> codeOpt = verificationCodeRepository
                .findByEmailAndCodeAndScene(email, command.code(), SCENE);
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

        verificationCodeRepository.markAsUsed(email, command.code(), SCENE);

        Map<String, String> updates = new HashMap<>();
        updates.put(FIELD_STEP, "4");
        stateService.update(command.resetToken(), updates);

        log.info("Reset code verified for email: {}", email);
    }

    /**
     * 重置密码。
     *
     * @param command
     *            重置密码命令
     */
    @Override
    @Transactional
    public void resetPassword(ResetPasswordCommands.ResetPasswordCommand command) {
        validateToken(command.resetToken(), 4);

        String userIdStr = stateService.getField(command.resetToken(), FIELD_USER_ID);
        if (userIdStr == null) {
            throw new BadRequest("重置流程状态异常，请重新开始");
        }

        Long userId = Long.parseLong(userIdStr);
        UserVO userVO = userRepository.findById(userId)
                .orElseThrow(() -> new BadRequest("用户不存在"));
        User user = User.builder()
                .id(userVO.getId())
                .password(userVO.getPassword())
                .build();
        user.changePassword(passwordEncoder.encode(command.newPassword()));
        userRepository.updatePassword(user.getId(), user.getPassword());

        authTokenService.revokeAllUserTokens(userId);
        stateService.delete(command.resetToken());

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

}
