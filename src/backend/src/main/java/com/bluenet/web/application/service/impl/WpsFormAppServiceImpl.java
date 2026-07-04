package com.bluenet.web.application.service.impl;

import com.bluenet.web.application.command.wpsform.WpsFormCommands;
import com.bluenet.web.application.message.MessageDispatcher;
import com.bluenet.web.application.message.MessageRequest;
import com.bluenet.web.application.message.template.EnrollmentApprovalCredentialTemplate;
import com.bluenet.web.application.service.WpsFormAppService;
import com.bluenet.web.domain.exception.BadRequest;
import com.bluenet.web.domain.model.entity.College;
import com.bluenet.web.domain.model.enumerate.Direction;
import com.bluenet.web.domain.model.enumerate.Gender;
import com.bluenet.web.domain.model.enumerate.MessageChannel;
import com.bluenet.web.domain.model.vo.RoleVO;
import com.bluenet.web.domain.model.vo.UserOnboardingCreateUserRequest;
import com.bluenet.web.domain.model.vo.UserOnboardingResult;
import com.bluenet.web.domain.repository.CollegeRepository;
import com.bluenet.web.domain.service.UserOnboardingService;
import com.bluenet.web.domain.service.WpsFormDirectionResolver;
import com.bluenet.web.infrastructure.config.properties.WpsProperties;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;

import java.util.Optional;

/**
 * WPS 智能表单应用服务实现。
 * <p>
 * 处理 WPS 表单数据推送的 create_answer 事件，将表单字段映射为系统用户并发送初始凭据邮件。
 * </p>
 */
@Service
@Slf4j
@Validated
@RequiredArgsConstructor
public class WpsFormAppServiceImpl implements WpsFormAppService {

    private static final String EMAIL_SUBJECT = "蓝网团队创建成功通知";

    private final WpsProperties wpsProperties;
    private final UserOnboardingService userOnboardingService;
    private final WpsFormDirectionResolver directionResolver;
    private final MessageDispatcher messageDispatcher;
    private final CollegeRepository collegeRepository;
    private final EnrollmentApprovalCredentialTemplate enrollmentApprovalCredentialTemplate;

    @Override
    public String resolveBindCode(String rid) {
        return StringUtils.hasText(wpsProperties.getBindCode()) ? wpsProperties.getBindCode() : rid;
    }

    @Override
    @Transactional
    public void createUserFromWpsForm(@Valid WpsFormCommands.CreateUserFromWpsFormCommand command) {
        Direction direction = Optional.ofNullable(directionResolver.resolve(command.directionText()))
                .orElseThrow(() -> new BadRequest("方向字段值无效: " + command.directionText()));

        Gender gender = Gender.fromDescription(command.genderText());
        // 从文本找collegeId
        College college = collegeRepository.findByName(command.collegeText())
                .orElseThrow(() -> new BadRequest("学院" + command.collegeText() + "不存在"));

        RoleVO role = userOnboardingService.getMemberRole();

        UserOnboardingCreateUserRequest request = UserOnboardingCreateUserRequest.builder()
                .studentId(command.studentId())
                .username(command.username())
                .email(command.email())
                .roleId(role.getId())
                .direction(direction)
                .major(command.major())
                .collegeId(college.getId())
                .gender(gender)
                .build();

        UserOnboardingResult result = userOnboardingService
                .createUserWithGeneratedPassword(request);
        log.info("WPS 表单创建新用户 {}, 学号: {}, 内推码: {}", result.userId(), command.studentId(), result.referralCode());

        sendCredentialEmail(command.username(), command.studentId(), command.email(), result.initialPassword());
    }

    private void sendCredentialEmail(String username, String studentId, String email, String initialPassword) {
        try {
            String htmlContent = enrollmentApprovalCredentialTemplate.buildHtml(username, studentId, initialPassword);
            messageDispatcher.dispatchAsync(
                    MessageRequest.html(MessageChannel.EMAIL, email, EMAIL_SUBJECT, htmlContent));
            log.info("WPS 表单创建用户凭据邮件已触发异步分发 - email={}", email);
        } catch (Exception ex) {
            log.warn("WPS 表单创建用户凭据邮件分发触发失败 - email={}", email, ex);
        }
    }
}
