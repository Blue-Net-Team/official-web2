package com.bluenet.web.application.service.impl;

import com.bluenet.web.domain.model.entity.BugReport;
import com.bluenet.web.domain.repository.BugReportRepository;
import com.bluenet.web.infrastructure.config.GitHubAppProperties;
import com.bluenet.web.infrastructure.github.GitHubIssueClient;
import com.bluenet.web.infrastructure.github.GitHubIssueCreateResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class GitHubIssueSyncService {

    private static final int MAX_TITLE_LENGTH = 100;

    private final GitHubIssueClient gitHubIssueClient;
    private final BugReportRepository bugReportRepository;
    private final GitHubAppProperties gitHubAppProperties;

    /**
     * 将 Bug 报告同步到 GitHub Issue（异步执行）。
     *
     * @param bugReport
     *            已保存的 Bug 报告实体
     */
    @Async("githubIssueExecutor")
    public void sync(BugReport bugReport) {
        if (!gitHubAppProperties.isEnabled()) {
            log.debug("GitHub App 配置未启用，跳过同步: bugReportId={}", bugReport.getId());
            return;
        }

        String title = buildTitle(bugReport);
        String body = buildBody(bugReport);

        try {
            GitHubIssueCreateResult result = gitHubIssueClient.createIssue(title, body);
            bugReportRepository.updateGithubIssueInfo(
                    bugReport.getId(),
                    result.htmlUrl(),
                    result.number());
            log.info(
                    "Bug 报告同步到 GitHub Issue 成功: bugReportId={}, issueNumber={}, issueUrl={}",
                    bugReport.getId(),
                    result.number(),
                    result.htmlUrl());
        } catch (Exception e) {
            log.error(
                    "Bug 报告同步到 GitHub Issue 失败: bugReportId={}, title={}",
                    bugReport.getId(),
                    title,
                    e);
        }
    }

    private String buildTitle(BugReport bugReport) {
        String title = bugReport.getTitle();
        if (title == null || title.isBlank()) {
            title = bugReport.getDescription();
        }
        if (title == null || title.isBlank()) {
            return "Bug Report";
        }
        if (title.length() <= MAX_TITLE_LENGTH) {
            return title;
        }
        return title.substring(0, MAX_TITLE_LENGTH) + "...";
    }

    private String buildBody(BugReport bugReport) {
        StringBuilder sb = new StringBuilder();
        sb.append("## 描述\n\n");
        sb.append(bugReport.getDescription()).append("\n\n");

        sb.append("## 页面 URL\n\n");
        sb.append(bugReport.getPageUrl() != null ? bugReport.getPageUrl() : "未提供").append("\n\n");

        sb.append("## 环境信息\n\n");
        if (bugReport.getEnvironmentJson() != null && !bugReport.getEnvironmentJson().isBlank()) {
            sb.append("```json\n");
            sb.append(bugReport.getEnvironmentJson()).append("\n");
            sb.append("```\n\n");
        } else {
            sb.append("未提供\n\n");
        }

        sb.append("## 报告者邮箱\n\n");
        sb.append(bugReport.getReporterEmail() != null ? bugReport.getReporterEmail() : "未提供").append("\n\n");

        sb.append("## 截图\n\n");
        List<Long> fileIds = bugReport.getImages()
                .stream()
                .map(img -> img.getFileId())
                .toList();
        if (fileIds.isEmpty()) {
            sb.append("无截图\n");
        } else {
            String baseUrl = gitHubAppProperties.getAppBaseUrl();
            // 移除末尾的斜杠，避免拼接出双斜杠
            if (baseUrl.endsWith("/")) {
                baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
            }
            for (Long fileId : fileIds) {
                String downloadUrl = baseUrl + "/api/v1/file/download/" + fileId;
                // 使用 Markdown 图片嵌入语法，确保在 GitHub 中直接展示图片
                sb.append("- ![截图 ").append(fileId).append("](").append(downloadUrl).append(")\n");
            }
        }

        return sb.toString();
    }
}
