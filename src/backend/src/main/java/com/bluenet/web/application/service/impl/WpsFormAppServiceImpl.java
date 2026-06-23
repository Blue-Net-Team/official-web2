package com.bluenet.web.application.service.impl;

import com.bluenet.web.application.message.MessageDispatcher;
import com.bluenet.web.application.message.MessageRequest;
import com.bluenet.web.application.message.template.EnrollmentApprovalCredentialTemplate;
import com.bluenet.web.application.service.WpsFormAppService;
import com.bluenet.web.domain.exception.DataConflict;
import com.bluenet.web.domain.model.enumerate.Gender;
import com.bluenet.web.domain.model.enumerate.MessageChannel;
import com.bluenet.web.domain.service.UserOnboardingService;
import com.bluenet.web.domain.service.WpsFormDirectionResolver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    private static final String EMAIL_SUBJECT = "蓝网团队创建成功通知";

    private final UserOnboardingService userOnboardingService;
    private final WpsFormDirectionResolver directionResolver;
    private final MessageDispatcher messageDispatcher;
    private final EnrollmentApprovalCredentialTemplate enrollmentApprovalCredentialTemplate;

    @Override
    @Transactional
    public void createUserFromWpsForm(String studentId, String username, String email,
                                       String directionText, String major,
                                       String collegeText, String genderText) {
        // 学号空值检查由 Controller 完成

        var direction = directionResolver.resolve(directionText);
        if (direction == null) {
            log.warn("WPS 表单方向字段值无效: {}", directionText);
            return;
        }

        Gender gender = resolveGender(genderText);
        Long collegeId = null; // 学院字段目前按文本记录，未建 College 映射

        var role = userOnboardingService.getMemberRole();

        var request = UserOnboardingService.CreateUserRequest.builder()
                .studentId(studentId.trim())
                .username(username.trim())
                .email(email.trim())
                .roleId(role.getId())
                .direction(direction)
                .major(major != null ? major.trim() : null)
                .collegeId(collegeId)
                .gender(gender)
                .build();

        var result = userOnboardingService.createUserWithGeneratedPassword(request);
        log.info("WPS 表单创建新用户 {}, 学号: {}, 内推码: {}", result.userId(), studentId, result.referralCode());

        sendCredentialEmail(username, studentId, email, result.initialPassword());
    }

    private void sendCredentialEmail(String username, String studentId, String email, String initialPassword) {
        try {
            String htmlContent = enrollmentApprovalCredentialTemplate
                    .buildHtml(username, studentId, initialPassword);
            messageDispatcher.dispatchAsync(
                    MessageRequest.html(MessageChannel.EMAIL, email, EMAIL_SUBJECT, htmlContent));
            log.info("WPS 表单创建用户凭据邮件已触发异步分发 - email={}", email);
        } catch (Exception ex) {
            log.warn("WPS 表单创建用户凭据邮件分发触发失败 - email={}", email, ex);
        }
    }

    /**
     * 将中文性别文本解析为 Gender 枚举。
     */
    private static Gender resolveGender(String genderText) {
        if (genderText == null || genderText.isBlank()) {
            return Gender.UNKNOWN;
        }
        return switch (genderText.trim()) {
            case "男" -> Gender.MALE;
            case "女" -> Gender.FEMALE;
            default -> Gender.UNKNOWN;
        };
    }
}
