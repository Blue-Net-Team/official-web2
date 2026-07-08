package com.bluenet.web.infrastructure.job;

import com.bluenet.web.domain.model.entity.BugReport;
import com.bluenet.web.domain.model.enumerate.BugReportStatus;
import com.bluenet.web.domain.repository.BugReportRepository;
import com.bluenet.web.infrastructure.config.GitHubAppProperties;
import com.bluenet.web.infrastructure.github.GitHubIssueClient;
import com.bluenet.web.infrastructure.github.GitHubIssueListResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

/**
 * GitHub Issue 定时轮询同步任务。
 * <p>
 * 作为 Webhook 的兜底机制，每天定时拉取最近更新的 GitHub Issue，与平台 Bug 报告进行状态对账。
 * </p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class GitHubIssuePollingJob {

    private static final String BLUENET_BUG_REPORT_MARKER = "<!-- bluenet-bug-report -->";

    private final GitHubIssueClient gitHubIssueClient;
    private final BugReportRepository bugReportRepository;
    private final GitHubAppProperties gitHubAppProperties;

    @Scheduled(cron = "${job.github-issue-sync.cron:0 0 3 * * *}")
    public void sync() {
        if (!gitHubAppProperties.isPollingEnabled()) {
            log.debug("GitHub Issue 定时同步已禁用，跳过执行");
            return;
        }

        int sinceDays = gitHubAppProperties.getPollingSinceDays();
        Instant since = Instant.now().minus(sinceDays, ChronoUnit.DAYS);

        log.info("开始执行 GitHub Issue 定时同步任务: since={} ({}天前)", since, sinceDays);

        List<GitHubIssueListResult> issues;
        try {
            issues = gitHubIssueClient.listIssues(since);
        } catch (Exception e) {
            log.error("拉取 GitHub Issue 列表失败，同步任务终止", e);
            return;
        }

        if (issues.isEmpty()) {
            log.info("GitHub Issue 列表为空，同步任务结束");
            return;
        }

        log.info("拉取到 {} 个 Issue，开始对账", issues.size());

        int createdCount = 0;
        int updatedCount = 0;
        int skippedCount = 0;
        int failedCount = 0;

        for (GitHubIssueListResult issue : issues) {
            try {
                SyncResult result = processSingleIssue(issue);
                switch (result) {
                    case CREATED -> createdCount++;
                    case UPDATED -> updatedCount++;
                    case SKIPPED -> skippedCount++;
                }
            } catch (Exception e) {
                failedCount++;
                log.error("处理 GitHub Issue 失败，跳过: number={}, title={}", issue.number(), issue.title(), e);
            }
        }

        log.info(
                "GitHub Issue 定时同步任务完成: 创建={}, 更新={}, 跳过={}, 失败={}",
                createdCount,
                updatedCount,
                skippedCount,
                failedCount);
    }

    private SyncResult processSingleIssue(GitHubIssueListResult issue) {
        Integer issueNumber = issue.number();
        if (issueNumber == null) {
            log.warn("GitHub Issue number 为空，跳过处理: title={}", issue.title());
            return SyncResult.SKIPPED;
        }
        Optional<BugReport> existing = bugReportRepository.findByGithubIssueNumber(issueNumber);

        if (existing.isPresent()) {
            return syncExistingBugReport(existing.get(), issue);
        }

        return createBugReportFromIssue(issue);
    }

    private SyncResult syncExistingBugReport(BugReport bugReport, GitHubIssueListResult issue) {
        BugReportStatus expectedStatus = mapStateToStatus(issue.state());
        if (bugReport.getStatus() == expectedStatus) {
            log.debug("Bug 报告 {} 状态与 GitHub Issue {} 一致，跳过更新", bugReport.getId(), issue.number());
            return SyncResult.SKIPPED;
        }

        BugReportStatus oldStatus = bugReport.getStatus();
        bugReport.updateStatus(expectedStatus);
        bugReportRepository.save(bugReport);
        log.info(
                "Bug 报告 {} 状态更新: {} → {} (source=polling, issueNumber={})",
                bugReport.getId(),
                oldStatus,
                expectedStatus,
                issue.number());
        return SyncResult.UPDATED;
    }

    private SyncResult createBugReportFromIssue(GitHubIssueListResult issue) {
        String body = issue.body() != null ? issue.body() : "";

        // 检查是否为平台创建的 Issue（body 中包含隐藏标记）
        if (body.contains(BLUENET_BUG_REPORT_MARKER)) {
            log.debug("Issue {} 由平台创建但本地无记录，跳过反向同步", issue.number());
            return SyncResult.SKIPPED;
        }

        String title = issue.title();
        if (title == null || title.isBlank()) {
            log.warn("GitHub Issue {} 标题为空，无法反向同步", issue.number());
            return SyncResult.SKIPPED;
        }
        if (title.length() > BugReport.MAX_TITLE_LENGTH) {
            log.warn("GitHub Issue {} 标题超长，已截断到 {} 字符", issue.number(), BugReport.MAX_TITLE_LENGTH);
            title = title.substring(0, BugReport.MAX_TITLE_LENGTH);
        }

        String description = body.isBlank() ? title : body;
        if (description.length() > BugReport.MAX_DESCRIPTION_LENGTH) {
            log.warn("GitHub Issue {} 描述超长，已截断到 {} 字符", issue.number(), BugReport.MAX_DESCRIPTION_LENGTH);
            description = description.substring(0, BugReport.MAX_DESCRIPTION_LENGTH);
        }

        BugReportStatus status = mapStateToStatus(issue.state());

        BugReport bugReport = BugReport.reconstruct(
                null,
                title.trim(),
                description.trim(),
                null,
                null,
                null,
                status,
                issue.htmlUrl(),
                issue.number(),
                List.of());

        bugReportRepository.save(bugReport);
        log.info("GitHub Issue {} 反向同步创建成功: bugReportId={}", issue.number(), bugReport.getId());
        return SyncResult.CREATED;
    }

    private BugReportStatus mapStateToStatus(String state) {
        return "closed".equalsIgnoreCase(state) ? BugReportStatus.RESOLVED : BugReportStatus.PENDING;
    }

    private enum SyncResult {
        CREATED,
        UPDATED,
        SKIPPED
    }
}
