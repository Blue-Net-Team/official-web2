package com.bluenet.web.application.service.impl;

import com.bluenet.web.application.UserInfoResult;
import com.bluenet.web.application.command.userinfo.UserInfoCommands;
import com.bluenet.web.application.service.UserInfoAppService;
import com.bluenet.web.domain.exception.BadRequest;
import com.bluenet.web.domain.exception.DataNotFound;
import com.bluenet.web.domain.exception.Forbidden;
import com.bluenet.web.domain.exception.Unauthorized;
import com.bluenet.web.domain.model.entity.College;
import com.bluenet.web.domain.model.entity.User;
import com.bluenet.web.domain.model.enumerate.FileType;
import com.bluenet.web.domain.model.enumerate.MessageChannel;
import com.bluenet.web.domain.model.enumerate.RoleType;
import com.bluenet.web.domain.model.vo.FileVO;
import com.bluenet.web.domain.model.vo.TabCountsVO;
import com.bluenet.web.domain.model.vo.VerifyCodeVO;
import com.bluenet.web.domain.repository.CollegeRepository;
import com.bluenet.web.domain.repository.UserRepository;
import com.bluenet.web.domain.repository.VerificationCodeRepository;
import com.bluenet.web.domain.service.FileDomainService;
import com.bluenet.web.domain.service.VerificationCodeDomainService;
import com.bluenet.web.domain.util.GradeCalculator;
import com.bluenet.web.application.message.MessageDispatcher;
import com.bluenet.web.application.message.MessageRequest;
import com.bluenet.web.application.message.template.EmailVerificationCodeTemplate;
import com.bluenet.web.application.message.template.VerificationCodeScene;
import com.bluenet.web.infrastructure.security.auth.AuthTokenService;
import com.bluenet.web.infrastructure.security.change.ChangePasswordStateService;
import com.bluenet.web.infrastructure.security.principal.RoleTypeResolver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 用户信息应用服务实现。
 * <p>
 * 实现用户信息聚合在应用层的业务逻辑编排。
 * </p>
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class UserInfoAppServiceImpl implements UserInfoAppService {

    private final UserRepository userRepository;
    private final FileDomainService fileDomainService;
    private final VerificationCodeDomainService verificationCodeDomainService;
    private final VerificationCodeRepository verificationCodeRepository;
    private final MessageDispatcher messageDispatcher;
    private final EmailVerificationCodeTemplate emailVerificationCodeTemplate;
    private final PasswordEncoder passwordEncoder;
    private final ChangePasswordStateService changePasswordStateService;
    private final AuthTokenService authTokenService;
    private final CollegeRepository collegeRepository;
    private final RoleTypeResolver roleTypeResolver;

    @Override
    public UserInfoResult getMyInfo(Long userId) {
        if (userId == null) {
            throw new Unauthorized("用户不存在");
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new Unauthorized("用户不存在"));
        String gradeLabel = GradeCalculator.getGradeLabel(user.getStudentId(), user.getAssessmentGradeYear());
        String collegeName = resolveCollegeName(user.getCollegeId());
        String roleName = resolveRoleName(user.getRoleId());
        return new UserInfoResult(
                user.getId(),
                user.getUsername(),
                user.getNickname(),
                collegeName,
                user.getMajor(),
                gradeLabel,
                user.getEmail(),
                user.getAvatarId(),
                roleName,
                user.getDirection(),
                user.getGender(),
                user.getBio(),
                user.getGithubUsername(),
                user.getQrcodeId());
    }

    private String resolveCollegeName(Long collegeId) {
        if (collegeId == null) {
            return null;
        }
        return collegeRepository.findById(collegeId)
                .map(College::getName)
                .orElse(null);
    }

    private String resolveRoleName(Long roleId) {
        RoleType roleType = roleTypeResolver.resolve(roleId);
        return roleType != null ? roleType.getName() : null;
    }

    @Override
    @Transactional
    public void updateProfile(Long userId, UserInfoCommands.UpdateProfileCommand command) {
        User currentUser = userRepository.findById(userId)
                .orElseThrow(() -> new Unauthorized("用户不存在"));
        validateProfileUpdatePermission(currentUser, command);
        currentUser.updateProfile(
                command.username(),
                command.nickname(),
                command.major(),
                command.direction(),
                command.gender(),
                command.bio(),
                command.qrcodeFileId());
        userRepository.save(currentUser);
    }

    @Override
    public UserInfoResult.TabCounts getTabCounts(Long userId) {
        TabCountsVO tabCountsVO = userRepository.getTabCounts(userId);
        return new UserInfoResult.TabCounts(
                tabCountsVO.getProjects(),
                tabCountsVO.getCompetitions(),
                tabCountsVO.getInternships());
    }

    @Override
    public void sendEmailVerificationCode(UserInfoCommands.SendEmailVerificationCodeCommand command) {
        String email = command.email();
        String scene = command.scene();
        if (!"change-email-original".equals(scene) && !"change-email-new".equals(scene)) {
            throw new BadRequest("无效的验证码场景");
        }
        VerifyCodeVO verifyCodeVO = verificationCodeDomainService.generateCode(email, scene);
        verificationCodeRepository.save(verifyCodeVO);
        String subject = "change-email-original".equals(scene) ? "蓝网修改邮箱 - 验证原邮箱" : "蓝网修改邮箱 - 验证新邮箱";
        VerificationCodeScene codeScene = "change-email-original".equals(scene)
                ? VerificationCodeScene.CHANGE_EMAIL_ORIGINAL
                : VerificationCodeScene.CHANGE_EMAIL_NEW;
        String htmlContent = emailVerificationCodeTemplate.buildHtml(codeScene, verifyCodeVO.getCode());
        messageDispatcher.dispatchAsync(MessageRequest.html(MessageChannel.EMAIL, email, subject, htmlContent));
        log.info("修改邮箱验证码已发送 - email={}, scene={}", email, scene);
    }

    @Override
    @Transactional
    public void changeEmail(Long userId, UserInfoCommands.ChangeEmailCommand command) {
        User currentUser = userRepository.findById(userId)
                .orElseThrow(() -> new Unauthorized("用户不存在"));
        verifyCode(currentUser.getEmail(), command.originalEmailVerifyCode(), "change-email-original");
        verifyCode(command.newEmail(), command.newEmailVerifyCode(), "change-email-new");
        if (userRepository.findByEmail(command.newEmail()).isPresent()) {
            throw new BadRequest("该邮箱已被其他账号绑定");
        }
        currentUser.changeEmail(command.newEmail());
        userRepository.save(currentUser);
        verificationCodeRepository.markAsUsed(
                currentUser.getEmail(),
                command.originalEmailVerifyCode(),
                "change-email-original");
        verificationCodeRepository.markAsUsed(command.newEmail(), command.newEmailVerifyCode(), "change-email-new");
        log.info("用户邮箱修改成功 - userId={}", userId);
    }

    @Override
    public String verifyCurrentPassword(UserInfoCommands.VerifyCurrentPasswordCommand command) {
        User user = userRepository.findById(command.userId())
                .orElseThrow(() -> new Unauthorized("用户不存在"));
        if (!passwordEncoder.matches(command.currentPassword(), user.getPassword())) {
            throw new BadRequest("当前密码不正确");
        }
        String token = changePasswordStateService.create(command.userId());
        log.info("修改密码 - 原密码验证通过, userId={}", command.userId());
        return token;
    }

    @Override
    @Transactional
    public void changePassword(UserInfoCommands.ChangePasswordCommand command) {
        if (!changePasswordStateService.exists(command.token())) {
            throw new BadRequest("验证已过期，请重新开始");
        }
        int step = changePasswordStateService.getStep(command.token());
        if (step < 1) {
            throw new BadRequest("请先验证当前密码");
        }
        String storedUserId = changePasswordStateService.getField(command.token(), "userId");
        if (!command.userId().toString().equals(storedUserId)) {
            throw new BadRequest("验证信息不匹配");
        }
        if (!command.newPassword().equals(command.confirmPassword())) {
            throw new BadRequest("两次输入的密码不一致");
        }
        User user = userRepository.findById(command.userId())
                .orElseThrow(() -> new Unauthorized("用户不存在"));
        user.changePassword(passwordEncoder.encode(command.newPassword()));
        userRepository.save(user);
        authTokenService.revokeAllUserTokens(command.userId());
        changePasswordStateService.delete(command.token());
        log.info("密码修改成功 - userId={}", command.userId());
    }

    @Override
    @Transactional
    public void updateAvatar(Long userId, UserInfoCommands.UpdateAvatarCommand command) {
        FileVO fileVO = fileDomainService.getFileById(command.fileId());
        if (fileVO == null) {
            throw new DataNotFound("文件不存在");
        }
        if (fileVO.getType() != FileType.AVATAR) {
            throw new BadRequest("文件类型不匹配，期望 AVATAR");
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new Unauthorized("用户不存在"));
        user.updateAvatar(command.fileId());
        userRepository.save(user);
        log.info("用户头像更新成功 - userId={}, fileId={}", userId, command.fileId());
    }

    private void validateProfileUpdatePermission(User user, UserInfoCommands.UpdateProfileCommand command) {
        RoleType role = roleTypeResolver.resolve(user.getRoleId());
        if (role == RoleType.CANDIDATE) {
            if (command.username() != null || command.gender() != null || command.college() != null
                    || command.major() != null || command.direction() != null) {
                throw new Forbidden("只有成员及以上角色才能修改用户名、性别、学院、专业和报名方向");
            }
        }
    }

    private void verifyCode(String email, String code, String scene) {
        VerifyCodeVO verifyCodeVO = verificationCodeRepository.findByEmailAndCodeAndScene(email, code, scene)
                .orElseThrow(() -> new BadRequest("验证码错误"));
        if (verifyCodeVO.isExpired()) {
            throw new BadRequest("验证码已过期");
        }
        if (verifyCodeVO.isUsed()) {
            throw new BadRequest("验证码已被使用");
        }
    }

}
