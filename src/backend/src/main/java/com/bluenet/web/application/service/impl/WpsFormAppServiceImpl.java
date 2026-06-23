package com.bluenet.web.application.service.impl;

import com.bluenet.web.application.command.wpsform.WpsFormCommands;
import com.bluenet.web.application.message.MessageDispatcher;
import com.bluenet.web.application.message.MessageRequest;
import com.bluenet.web.application.message.template.EnrollmentApprovalCredentialTemplate;
import com.bluenet.web.application.service.WpsFormAppService;
import com.bluenet.web.domain.exception.DataConflict;
import com.bluenet.web.domain.exception.GlobalException;
import com.bluenet.web.domain.model.entity.User;
import com.bluenet.web.domain.model.enumerate.Direction;
import com.bluenet.web.domain.model.enumerate.MessageChannel;
import com.bluenet.web.domain.model.enumerate.RoleType;
import com.bluenet.web.domain.model.vo.RoleVO;
import com.bluenet.web.domain.repository.RoleRepository;
import com.bluenet.web.domain.repository.UserRepository;
import com.bluenet.web.domain.service.ReferralCodeGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Optional;

/**
 * WPS 智能表单应用服务实现。
 * <p>
 * 处理 WPS 表单数据推送的 create_answer 事件，将表单字段映射为系统用户并发送初始凭据邮件。
 * </p>
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class WpsFormAppServiceImpl implements WpsFormAppService {

    private static final int PASSWORD_LENGTH = 10;
    private static final String EMAIL_SUBJECT = "蓝网团队创建成功通知";

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final ReferralCodeGenerator referralCodeGenerator;
    private final PasswordEncoder passwordEncoder;
    private final MessageDispatcher messageDispatcher;
    private final EnrollmentApprovalCredentialTemplate enrollmentApprovalCredentialTemplate;

    @Override
    @Transactional
    public void createUserFromWpsForm(WpsFormCommands.CreateUserFromWpsCommand command) {
        String studentId = command.studentId().trim();
        String email = command.email().trim();

        // 检查学号是否已存在
        Optional<com.bluenet.web.domain.model.vo.UserVO> existingStudent = userRepository.findByStudentId(studentId);
        if (existingStudent.isPresent()) {
            log.warn("WPS 表单创建用户跳过，学号已存在: {}", studentId);
            throw new DataConflict("学号 " + studentId + " 对应的用户已存在");
        }

        // 检查邮箱是否已存在
        Optional<com.bluenet.web.domain.model.vo.UserVO> existingEmail = userRepository.findByEmail(email);
        if (existingEmail.isPresent()) {
            log.warn("WPS 表单创建用户跳过，邮箱已存在: {}", email);
            throw new DataConflict("邮箱 " + email + " 对应的用户已存在");
        }

        // 查找 MEMBER 角色
        RoleVO memberRole = roleRepository.findByName(RoleType.MEMBER.getName())
                .orElseThrow(() -> new GlobalException("MEMBER 角色不存在，请先初始化角色数据"));

        // 生成密码：随机字符串 → SHA256 → BCrypt
        String initialPassword = generateRandomPassword(PASSWORD_LENGTH);
        String hashedPassword = sha256Hash(initialPassword);
        String encodedPassword = passwordEncoder.encode(hashedPassword);
        String referralCode = referralCodeGenerator.generate();

        User user = User.create(
                studentId,
                email,
                memberRole.getId(),
                encodedPassword,
                command.username().trim(),
                null,
                null,
                command.major() != null ? command.major().trim() : null,
                null,
                command.direction(),
                null,
                null,
                null,
                null,
                null,
                null,
                referralCode,
                null);

        userRepository.save(user);
        log.info("WPS 表单创建新用户 {}, 学号: {}, 内推码: {}", user.getId(), studentId, referralCode);

        // 发送初始凭据邮件
        sendCredentialEmail(command, initialPassword);
    }

    private void sendCredentialEmail(WpsFormCommands.CreateUserFromWpsCommand command, String initialPassword) {
        try {
            String htmlContent = enrollmentApprovalCredentialTemplate
                    .buildHtml(command.username(), command.studentId(), initialPassword);
            messageDispatcher.dispatchAsync(
                    MessageRequest.html(MessageChannel.EMAIL, command.email(), EMAIL_SUBJECT, htmlContent));
            log.info("WPS 表单创建用户凭据邮件已触发异步分发 - email={}", command.email());
        } catch (Exception ex) {
            log.warn("WPS 表单创建用户凭据邮件分发触发失败 - email={}", command.email(), ex);
        }
    }

    /**
     * 通过中文描述查找 Direction 枚举。
     * <p>
     * 支持 WPS 表单的短名称（结构、电控、视觉）和完整描述（结构设计、嵌入式开发、计算机视觉）。
     * </p>
     *
     * @param description 中文方向描述
     * @return 匹配的 Direction，若未匹配则返回 null
     */
    public static Direction resolveDirection(String description) {
        if (description == null || description.isBlank()) {
            return null;
        }
        String trimmed = description.trim();
        // 先尝试精确匹配完整描述
        for (Direction d : Direction.values()) {
            if (d.getDescription().equals(trimmed)) {
                return d;
            }
        }
        // 再尝试匹配 WPS 表单短名称
        switch (trimmed) {
            case "结构" -> {
                return Direction.STRUCTURAL_DESIGN;
            }
            case "电控" -> {
                return Direction.EMBEDDED;
            }
            case "视觉" -> {
                return Direction.COMPUTER_VISION;
            }
            default -> {
                log.warn("未找到匹配的方向描述: {}", description);
                return null;
            }
        }
    }

    /**
     * 生成随机密码。
     * <p>
     * 密码包含大小写字母、数字和特殊字符，确保足够强度。
     * </p>
     *
     * @param length 密码长度
     * @return 随机字符串
     */
    public static String generateRandomPassword(int length) {
        // 字符池：大写字母、小写字母、数字、特殊字符
        String lower = "abcdefghijklmnopqrstuvwxyz";
        String upper = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String digits = "0123456789";
        String special = "!@#$%^&*";
        String allChars = lower + upper + digits + special;

        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder(length);

        // 确保至少包含各类字符
        sb.append(lower.charAt(random.nextInt(lower.length())));
        sb.append(upper.charAt(random.nextInt(upper.length())));
        sb.append(digits.charAt(random.nextInt(digits.length())));
        sb.append(special.charAt(random.nextInt(special.length())));

        // 剩余字符从完整字符池随机选取
        for (int i = 4; i < length; i++) {
            sb.append(allChars.charAt(random.nextInt(allChars.length())));
        }

        // 打乱字符顺序
        char[] chars = sb.toString().toCharArray();
        for (int i = chars.length - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            char temp = chars[i];
            chars[i] = chars[j];
            chars[j] = temp;
        }
        return new String(chars);
    }

    /**
     * SHA-256 哈希。
     *
     * @param input 输入字符串
     * @return 十六进制哈希字符串
     */
    public static String sha256Hash(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }
}
