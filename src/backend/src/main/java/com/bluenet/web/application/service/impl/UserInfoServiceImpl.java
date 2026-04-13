package com.bluenet.web.application.service.impl;

import com.bluenet.web.api.dto.user.ChangeEmailRequestDTO;
import com.bluenet.web.api.dto.user.SendEmailVerificationCodeRequestDTO;
import com.bluenet.web.api.dto.user.TabCountsDTO;
import com.bluenet.web.api.dto.user.UpdateProfileRequestDTO;
import com.bluenet.web.api.dto.user.UserInfo;
import com.bluenet.web.application.converter.UserConverter;
import com.bluenet.web.application.service.UserInfoService;
import com.bluenet.web.domain.exception.BadRequest;
import com.bluenet.web.domain.exception.DataNotFound;
import com.bluenet.web.domain.exception.Forbidden;
import com.bluenet.web.domain.exception.Unauthorized;
import com.bluenet.web.domain.model.enumerate.FileType;
import com.bluenet.web.domain.model.vo.FileVO;
import com.bluenet.web.domain.model.vo.TabCountsVO;
import com.bluenet.web.domain.model.vo.UserVO;
import com.bluenet.web.domain.model.vo.VerifyCodeVO;
import com.bluenet.web.domain.repository.VerificationCodeRepository;
import com.bluenet.web.domain.service.FileDomainService;
import com.bluenet.web.domain.service.UserDomainService;
import com.bluenet.web.domain.service.VerificationCodeDomainService;
import com.bluenet.web.infrastructure.email.EmailSender;
import com.bluenet.web.domain.model.enumerate.RoleType;
import com.bluenet.web.infrastructure.security.auth.AuthTokenService;
import com.bluenet.web.infrastructure.security.change.ChangePasswordStateService;
import com.bluenet.web.infrastructure.security.util.UserCTX;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Service
public class UserInfoServiceImpl implements UserInfoService {
    private final UserConverter userConverter;
    private final UserDomainService userDomainService;
    private final FileDomainService fileDomainService;
    private final VerificationCodeDomainService verificationCodeDomainService;
    private final VerificationCodeRepository verificationCodeRepository;
    private final EmailSender emailSender;
    private final PasswordEncoder passwordEncoder;
    private final ChangePasswordStateService changePasswordStateService;
    private final AuthTokenService authTokenService;

    @Override
    public UserInfo getMyInfo() {
        UserVO userVO = UserCTX.getCurrentUser();

        if (userVO == null) {
            log.warn("用户未认证，无法获取用户信息");
            throw new Unauthorized("未认证");
        }

        return userConverter.convertToUserInfo(userVO);
    }

    @Override
    public void updateProfile(UpdateProfileRequestDTO request) {
        Long userId = getCurrentUserId();
        UserVO currentUser = UserCTX.getCurrentUser();

        validateProfileUpdatePermission(currentUser, request);

        userDomainService.updateProfile(
                userId,
                request.getUsername(),
                request.getNickname(),
                request.getCollege(),
                request.getMajor(),
                request.getDirection(),
                request.getGender(),
                request.getBio());
    }

    @Override
    public TabCountsDTO getTabCounts() {
        Long userId = getCurrentUserId();

        TabCountsVO tabCountsVO = userDomainService.getTabCounts(userId);

        return TabCountsDTO.builder()
                .projects(tabCountsVO.getProjects())
                .competitions(tabCountsVO.getCompetitions())
                .internships(tabCountsVO.getInternships())
                .build();
    }

    private Long getCurrentUserId() {
        UserVO userVO = UserCTX.getCurrentUser();
        if (userVO == null) {
            log.warn("用户未认证");
            throw new Unauthorized("未认证");
        }
        return userVO.getId();
    }

    private void validateProfileUpdatePermission(UserVO user, UpdateProfileRequestDTO request) {
        RoleType role = RoleType.fromName(user.getRoleName());

        if (role == RoleType.CANDIDATE) {
            if (request.getUsername() != null || request.getGender() != null || request.getCollege() != null
                    || request.getMajor() != null || request.getDirection() != null) {
                throw new Forbidden("只有成员及以上角色才能修改用户名、性别、学院、专业和报名方向");
            }
        }
    }

    @Override
    public void sendEmailVerificationCode(SendEmailVerificationCodeRequestDTO request) {
        String email = request.getEmail();
        String scene = request.getScene();

        if (!"change-email-original".equals(scene) && !"change-email-new".equals(scene)) {
            throw new BadRequest("无效的验证码场景");
        }

        VerifyCodeVO verifyCodeVO = verificationCodeDomainService.generateCode(email, scene);
        verificationCodeRepository.save(verifyCodeVO);

        String subject = "change-email-original".equals(scene) ? "蓝网修改邮箱 - 验证原邮箱" : "蓝网修改邮箱 - 验证新邮箱";
        String htmlContent = buildChangeEmailVerificationCodeHtml(verifyCodeVO.getCode(), scene);
        emailSender.sendHtmlAsync(email, subject, htmlContent);

        log.info("修改邮箱验证码已发送 - email={}, scene={}", email, scene);
    }

    private String buildChangeEmailVerificationCodeHtml(String code, String scene) {
        String action = "change-email-original".equals(scene) ? "验证原邮箱" : "验证新邮箱";
        return """
                <div style="max-width:400px;margin:0 auto;padding:20px;font-family:sans-serif;">
                    <h2 style="color:#fa8c16;text-align:center;">蓝网修改邮箱 - %s</h2>
                    <p style="text-align:center;font-size:14px;color:#666;">您的验证码为：</p>
                    <p style="text-align:center;font-size:32px;font-weight:bold;letter-spacing:8px;color:#fa8c16;">%s</p>
                    <p style="text-align:center;font-size:12px;color:#999;">验证码5分钟内有效，请勿泄露给他人。</p>
                </div>
                """
                .formatted(action, code);
    }

    @Override
    public void changeEmail(ChangeEmailRequestDTO request) {
        Long userId = getCurrentUserId();
        UserVO currentUser = UserCTX.getCurrentUser();

        userDomainService.changeEmail(
                userId,
                currentUser.getEmail(),
                request.getOriginalEmailVerifyCode(),
                request.getNewEmail(),
                request.getNewEmailVerifyCode());

        log.info("用户邮箱修改成功 - userId={}", userId);
    }

    @Override
    public String verifyCurrentPassword(Long userId, String currentPassword) {
        UserVO user = UserCTX.getCurrentUser();
        if (user == null) {
            throw new Unauthorized("未认证");
        }

        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new BadRequest("当前密码不正确");
        }

        String token = changePasswordStateService.create(userId);
        log.info("修改密码 - 原密码验证通过, userId={}", userId);
        return token;
    }

    @Override
    @Transactional
    public void changePassword(Long userId, String token, String newPassword, String confirmPassword) {
        if (!changePasswordStateService.exists(token)) {
            throw new BadRequest("验证已过期，请重新开始");
        }

        int step = changePasswordStateService.getStep(token);
        if (step < 1) {
            throw new BadRequest("请先验证当前密码");
        }

        String storedUserId = changePasswordStateService.getField(token, "userId");
        if (!userId.toString().equals(storedUserId)) {
            throw new BadRequest("验证信息不匹配");
        }

        if (!newPassword.equals(confirmPassword)) {
            throw new BadRequest("两次输入的密码不一致");
        }

        userDomainService.changePassword(userId, newPassword);
        authTokenService.revokeAllUserTokens(userId);
        changePasswordStateService.delete(token);

        log.info("密码修改成功 - userId={}", userId);
    }

    @Override
    @Transactional
    public void updateAvatar(Long fileId) {
        Long userId = getCurrentUserId();

        // 校验文件存在
        FileVO fileVO = fileDomainService.getFileById(fileId);
        if (fileVO == null) {
            throw new DataNotFound("文件不存在");
        }

        // 校验文件类型为 AVATAR
        if (fileVO.getType() != FileType.AVATAR) {
            throw new BadRequest("文件类型不匹配，期望 AVATAR");
        }

        // 调用领域服务更新头像
        userDomainService.updateUserAvatar(userId, fileVO);

        log.info("用户头像更新成功 - userId={}, fileId={}", userId, fileId);
    }
}
