package com.bluenet.web.application.service.impl;

import com.bluenet.web.domain.model.entity.BugReport;
import com.bluenet.web.domain.model.enumerate.BugReportStatus;
import com.bluenet.web.domain.repository.BugReportRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * GitHub Webhook 业务处理服务。
 * <p>
 * 接收 GitHub issues 事件，处理状态自动更新和反向同步。
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GitHubWebhookService {

    private static final String BLUENET_BUG_REPORT_MARKER = "<!-- bluenet-bug-report -->";

    private final BugReportRepository bugReportRepository;
    private final ObjectMapper objectMapper;

    /**
     * 处理 GitHub issues 事件 payload。
     *
     * @param payload
     *            GitHub Webhook 请求体 JSON 字符串
     */
    public void processIssuesEvent(String payload) {
        try {
            JsonNode root = objectMapper.readTree(payload);
            String action = root.path("action").asText(null);

            if (action == null) {
                log.warn("GitHub Webhook payload 缺少 action 字段");
                return;
            }

            JsonNode issueNode = root.path("issue");
            if (issueNode.isMissingNode()) {
                log.warn("GitHub Webhook payload 缺少 issue 字段");
                return;
            }

            Integer issueNumber = issueNode.path("number").asInt(0);
            if (issueNumber == 0) {
                log.warn("GitHub Webhook payload 中 issue number 无效");
                return;
            }

            log.info("处理 GitHub issues.{} 事件: issueNumber={}", action, issueNumber);

            switch (action) {
                case "opened" :
                    handleIssueOpened(issueNode, issueNumber);
                    break;
                case "assigned" :
                    handleIssueStatusChange(issueNumber, BugReportStatus.IN_PROGRESS, action);
                    break;
                case "closed" :
                    handleIssueStatusChange(issueNumber, BugReportStatus.RESOLVED, action);
                    break;
                case "reopened" :
                    handleIssueStatusChange(issueNumber, BugReportStatus.PENDING, action);
                    break;
                default :
                    log.debug("忽略不支持的 issues 事件类型: action={}", action);
                    break;
            }
        } catch (Exception e) {
            log.error("处理 GitHub Webhook 事件失败", e);
        }
    }

    /**
     * 处理 Issue opened 事件。
     * <p>
     * 检查 Issue 是否由平台创建（通过 body 中的标记）， 如果不是，则反向同步到平台创建新的 Bug 报告。
     * </p>
     */
    void handleIssueOpened(JsonNode issueNode, Integer issueNumber) {
        String body = issueNode.path("body").asText("");

        // 检查是否为平台创建的 Issue（body 中包含隐藏标记）
        if (body.contains(BLUENET_BUG_REPORT_MARKER)) {
            log.debug("Issue {} 由平台创建，跳过反向同步", issueNumber);
            return;
        }

        // 检查是否已存在（避免重复创建）
        if (bugReportRepository.findByGithubIssueNumber(issueNumber).isPresent()) {
            log.debug("Issue {} 已存在对应的 Bug 报告，跳过反向同步", issueNumber);
            return;
        }

        String title = issueNode.path("title").asText(null);
        if (title == null || title.isBlank()) {
            log.warn("GitHub Issue {} 标题为空，无法反向同步", issueNumber);
            return;
        }

        String description = issueNode.path("body").asText(null);
        String htmlUrl = issueNode.path("html_url").asText(null);

        // 如果 description 为空，使用 title 作为降级
        if (description == null || description.isBlank()) {
            description = title;
        }

        BugReport bugReport = BugReport.reconstruct(
                null,
                title.trim(),
                description.trim(),
                null,
                null,
                null,
                BugReportStatus.PENDING,
                htmlUrl,
                issueNumber,
                List.of());

        bugReportRepository.save(bugReport);
        log.info("GitHub Issue {} 反向同步到平台成功: bugReportId={}", issueNumber, bugReport.getId());
    }

    /**
     * 处理 Issue 状态变更事件（assigned / closed / reopened）。
     */
    void handleIssueStatusChange(Integer issueNumber, BugReportStatus newStatus, String action) {
        BugReport bugReport = bugReportRepository.findByGithubIssueNumber(issueNumber)
                .orElse(null);

        if (bugReport == null) {
            log.warn("GitHub issues.{} 事件: 未找到 issueNumber={} 对应的 Bug 报告", action, issueNumber);
            return;
        }

        // 如果状态没有变化，跳过
        if (bugReport.getStatus() == newStatus) {
            log.debug("Bug 报告 {} 状态已经是 {}，无需更新", bugReport.getId(), newStatus);
            return;
        }

        bugReport.updateStatus(newStatus);
        bugReportRepository.updateStatus(bugReport.getId(), newStatus);
        log.info(
                "Bug 报告 {} 状态自动更新: {} → {} (triggered by GitHub issues.{})",
                bugReport.getId(),
                bugReport.getStatus(),
                newStatus,
                action);
    }
}
