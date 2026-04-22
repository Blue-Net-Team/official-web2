package com.bluenet.web.application.service.assessment;

import org.springframework.stereotype.Component;

/**
 * 考核结果通知模板，只负责内容生成，不承担消息发送职责。
 */
@Component
public class AssessmentDecisionNotificationTemplate {

    /**
     * 构建决策通知 HTML 邮件内容。
     *
     * @param nickname
     *            考生昵称。
     * @param directionLabel
     *            考核方向名称。
     * @param epoch
     *            轮次。
     * @param resultText
     *            结果文本。
     * @return HTML 邮件内容。
     */
    public String buildHtml(String nickname, String directionLabel, int epoch, String resultText) {
        return """
                <div style="font-family: 'Microsoft YaHei', sans-serif; max-width: 600px; margin: 0 auto; padding: 20px;">
                  <h2 style="color: #1890ff;">考核结果通知</h2>
                  <p>%s 你好，</p>
                  <p>你参加的 <strong>%s方向第%d轮</strong> 考核结果已公布：</p>
                  <p style="font-size: 18px; font-weight: bold; color: %s;">%s</p>
                  <hr style="border: none; border-top: 1px solid #eee; margin: 20px 0;">
                  <p style="color: #999; font-size: 12px;">此邮件由系统自动发送，请勿回复。</p>
                </div>
                """
                .formatted(
                        nickname,
                        directionLabel,
                        epoch,
                        "通过".equals(resultText) ? "#52c41a" : "#ff4d4f",
                        resultText);
    }
}
