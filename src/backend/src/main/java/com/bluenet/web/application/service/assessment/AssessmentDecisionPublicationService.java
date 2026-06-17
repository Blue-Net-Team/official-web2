package com.bluenet.web.application.service.assessment;

import com.bluenet.web.application.message.MessageDispatcher;
import com.bluenet.web.application.message.MessageRequest;
import com.bluenet.web.application.message.template.AssessmentDecisionNotificationTemplate;
import com.bluenet.web.domain.exception.DataNotFound;
import com.bluenet.web.domain.model.enumerate.MessageChannel;
import com.bluenet.web.domain.model.enumerate.RoleType;
import com.bluenet.web.domain.model.entity.AssessmentTime;
import com.bluenet.web.domain.model.vo.AssessmentDecisionVO;
import com.bluenet.web.domain.model.vo.UserVO;
import com.bluenet.web.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 考核决策发布服务。
 * <p>
 * 负责单个考生的决策发布，包含邮件通知和录取后的角色升级。 每个考生的处理在独立事务中进行，单个失败不影响其他考生。
 * </p>
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class AssessmentDecisionPublicationService {

    private final UserRepository userRepository;
    private final MessageDispatcher messageDispatcher;
    private final AssessmentDecisionNotificationTemplate notificationTemplate;

    /**
     * 发布单个考生的决策结果。
     * <p>
     * 若该考生通过全局最终考核且当前角色为 CANDIDATE，则自动升级为 MEMBER。 无论是否升级，都会异步发送决策邮件通知。
     * </p>
     *
     * @param decision
     *            决策记录
     * @param assessmentTime
     *            考核时间
     */
    @Transactional
    public void publish(AssessmentDecisionVO decision, AssessmentTime assessmentTime) {
        UserVO user = userRepository.findById(decision.getUserId())
                .orElseThrow(() -> new DataNotFound("用户不存在，ID: " + decision.getUserId()));

        if (shouldPromoteToMember(decision, assessmentTime, user)) {
            userRepository.batchUpdateRole(List.of(user.getId()), RoleType.MEMBER);
            log.info("考生 {} 通过全局最终考核，角色已升级为 MEMBER", user.getId());
        }

        sendDecisionEmail(user, assessmentTime, decision);
    }

    /**
     * 判断是否需要将考生升级为组员。
     */
    private boolean shouldPromoteToMember(AssessmentDecisionVO decision, AssessmentTime assessmentTime, UserVO user) {
        return assessmentTime.isGlobalFinalAssessment()
                && Boolean.TRUE.equals(decision.getPassed())
                && RoleType.CANDIDATE.getName().equals(user.getRoleName());
    }

    /**
     * 发送决策结果邮件。
     */
    private void sendDecisionEmail(UserVO user, AssessmentTime assessmentTime, AssessmentDecisionVO decision) {
        if (user.getEmail() == null || user.getEmail().isBlank()) {
            log.warn("跳过无邮箱用户：userId={}", user.getId());
            return;
        }

        String subject = "[蓝网] 考核结果通知";
        String directionLabel = assessmentTime.getDirection() != null
                ? assessmentTime.getDirection().getDescription()
                : "全局";
        int epoch = assessmentTime.getEpoch() != null ? assessmentTime.getEpoch() : 0;
        boolean isFinalRound = assessmentTime.isGlobalFinalAssessment();

        String resultText;
        if (isFinalRound) {
            resultText = Boolean.TRUE.equals(decision.getPassed()) ? "录取" : "淘汰";
        } else {
            resultText = Boolean.TRUE.equals(decision.getPassed()) ? "通过" : "未通过";
        }

        String nickname = user.getNickname() != null ? user.getNickname() : user.getUsername();
        String htmlContent = notificationTemplate.buildHtml(nickname, directionLabel, epoch, resultText);
        messageDispatcher.dispatchAsync(
                MessageRequest.html(MessageChannel.EMAIL, user.getEmail(), subject, htmlContent));
    }
}
