package com.bluenet.web.infrastructure.job;

import com.bluenet.web.domain.model.entity.BugReport;
import com.bluenet.web.domain.model.enumerate.BugReportStatus;
import com.bluenet.web.domain.repository.BugReportRepository;
import com.bluenet.web.infrastructure.config.GitHubAppProperties;
import com.bluenet.web.infrastructure.github.GitHubIssueClient;
import com.bluenet.web.infrastructure.github.GitHubIssueListResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@DisplayName("GitHubIssuePollingJob 单元测试")
@ExtendWith(MockitoExtension.class)
class GitHubIssuePollingJobTest {

    @Mock
    private GitHubIssueClient gitHubIssueClient;

    @Mock
    private BugReportRepository bugReportRepository;

    @Mock
    private GitHubAppProperties gitHubAppProperties;

    private GitHubIssuePollingJob job;

    @BeforeEach
    void setUp() {
        job = new GitHubIssuePollingJob(gitHubIssueClient, bugReportRepository, gitHubAppProperties);
    }

    @Nested
    @DisplayName("配置检查测试")
    class ConfigCheckTest {

        @Test
        @DisplayName("TC-005: 配置禁用时跳过执行")
        void sync_pollingDisabled_shouldSkip() {
            when(gitHubAppProperties.isPollingEnabled()).thenReturn(false);

            job.sync();

            verify(gitHubIssueClient, never()).listIssues(any());
            verify(bugReportRepository, never()).findByGithubIssueNumber(any());
        }

        @Test
        @DisplayName("TC-006: 空列表时直接结束")
        void sync_emptyList_shouldEndImmediately() {
            when(gitHubAppProperties.isPollingEnabled()).thenReturn(true);
            when(gitHubAppProperties.getPollingSinceDays()).thenReturn(7);
            when(gitHubIssueClient.listIssues(any())).thenReturn(List.of());

            job.sync();

            verify(bugReportRepository, never()).findByGithubIssueNumber(any());
        }
    }

    @Nested
    @DisplayName("反向同步创建测试")
    class CreateSyncTest {

        @Test
        @DisplayName("TC-007: 本地不存在 → 调用 save 创建")
        void sync_notExists_shouldCreate() {
            when(gitHubAppProperties.isPollingEnabled()).thenReturn(true);
            when(gitHubAppProperties.getPollingSinceDays()).thenReturn(7);

            GitHubIssueListResult issue = new GitHubIssueListResult(
                    42, "GitHub Bug", "Description", "open", "https://github.com/test/issues/42");
            when(gitHubIssueClient.listIssues(any())).thenReturn(List.of(issue));
            when(bugReportRepository.findByGithubIssueNumber(42)).thenReturn(Optional.empty());

            job.sync();

            ArgumentCaptor<BugReport> captor = ArgumentCaptor.forClass(BugReport.class);
            verify(bugReportRepository).save(captor.capture());
            BugReport saved = captor.getValue();
            assertEquals("GitHub Bug", saved.getTitle());
            assertEquals("Description", saved.getDescription());
            assertEquals(BugReportStatus.PENDING, saved.getStatus());
            assertEquals(42, saved.getGithubIssueNumber());
        }

        @Test
        @DisplayName("TC-011: state=open → 映射为 PENDING")
        void sync_openState_shouldMapToPending() {
            when(gitHubAppProperties.isPollingEnabled()).thenReturn(true);
            when(gitHubAppProperties.getPollingSinceDays()).thenReturn(7);

            GitHubIssueListResult issue = new GitHubIssueListResult(
                    42, "Bug", "Desc", "open", "https://github.com/test/issues/42");
            when(gitHubIssueClient.listIssues(any())).thenReturn(List.of(issue));
            when(bugReportRepository.findByGithubIssueNumber(42)).thenReturn(Optional.empty());

            job.sync();

            ArgumentCaptor<BugReport> captor = ArgumentCaptor.forClass(BugReport.class);
            verify(bugReportRepository).save(captor.capture());
            assertEquals(BugReportStatus.PENDING, captor.getValue().getStatus());
        }

        @Test
        @DisplayName("TC-012: state=closed → 映射为 RESOLVED")
        void sync_closedState_shouldMapToResolved() {
            when(gitHubAppProperties.isPollingEnabled()).thenReturn(true);
            when(gitHubAppProperties.getPollingSinceDays()).thenReturn(7);

            GitHubIssueListResult issue = new GitHubIssueListResult(
                    42, "Bug", "Desc", "closed", "https://github.com/test/issues/42");
            when(gitHubIssueClient.listIssues(any())).thenReturn(List.of(issue));
            when(bugReportRepository.findByGithubIssueNumber(42)).thenReturn(Optional.empty());

            job.sync();

            ArgumentCaptor<BugReport> captor = ArgumentCaptor.forClass(BugReport.class);
            verify(bugReportRepository).save(captor.capture());
            assertEquals(BugReportStatus.RESOLVED, captor.getValue().getStatus());
        }
    }

    @Nested
    @DisplayName("状态对账测试")
    class StatusReconciliationTest {

        @Test
        @DisplayName("TC-008: 本地存在且状态一致 → 跳过更新")
        void sync_statusMatch_shouldSkip() {
            when(gitHubAppProperties.isPollingEnabled()).thenReturn(true);
            when(gitHubAppProperties.getPollingSinceDays()).thenReturn(7);

            GitHubIssueListResult issue = new GitHubIssueListResult(
                    42, "Bug", "Desc", "open", "https://github.com/test/issues/42");
            BugReport existing = BugReport.reconstruct(
                    1L,
                    "Bug",
                    "Desc",
                    null,
                    null,
                    null,
                    BugReportStatus.PENDING,
                    "https://github.com/test/issues/42",
                    42,
                    List.of());

            when(gitHubIssueClient.listIssues(any())).thenReturn(List.of(issue));
            when(bugReportRepository.findByGithubIssueNumber(42)).thenReturn(Optional.of(existing));

            job.sync();

            verify(bugReportRepository, never()).updateStatus(any(), any());
        }

        @Test
        @DisplayName("TC-009: 本地存在且状态不一致 → 调用 updateStatus")
        void sync_statusMismatch_shouldUpdate() {
            when(gitHubAppProperties.isPollingEnabled()).thenReturn(true);
            when(gitHubAppProperties.getPollingSinceDays()).thenReturn(7);

            GitHubIssueListResult issue = new GitHubIssueListResult(
                    42, "Bug", "Desc", "closed", "https://github.com/test/issues/42");
            BugReport existing = BugReport.reconstruct(
                    1L,
                    "Bug",
                    "Desc",
                    null,
                    null,
                    null,
                    BugReportStatus.PENDING,
                    "https://github.com/test/issues/42",
                    42,
                    List.of());

            when(gitHubIssueClient.listIssues(any())).thenReturn(List.of(issue));
            when(bugReportRepository.findByGithubIssueNumber(42)).thenReturn(Optional.of(existing));

            job.sync();

            verify(bugReportRepository).updateStatus(1L, BugReportStatus.RESOLVED);
        }
    }

    @Nested
    @DisplayName("异常处理测试")
    class ExceptionHandlingTest {

        @Test
        @DisplayName("TC-010: 单条处理异常 → 继续处理后续条目")
        void sync_singleFailure_shouldContinue() {
            when(gitHubAppProperties.isPollingEnabled()).thenReturn(true);
            when(gitHubAppProperties.getPollingSinceDays()).thenReturn(7);

            GitHubIssueListResult issue1 = new GitHubIssueListResult(
                    42, "Bug 1", "Desc", "open", "https://github.com/test/issues/42");
            GitHubIssueListResult issue2 = new GitHubIssueListResult(
                    43, "Bug 2", "Desc", "open", "https://github.com/test/issues/43");

            when(gitHubIssueClient.listIssues(any())).thenReturn(List.of(issue1, issue2));
            when(bugReportRepository.findByGithubIssueNumber(42)).thenThrow(new RuntimeException("DB error"));
            when(bugReportRepository.findByGithubIssueNumber(43)).thenReturn(Optional.empty());

            job.sync();

            verify(bugReportRepository).findByGithubIssueNumber(42);
            verify(bugReportRepository).findByGithubIssueNumber(43);
            verify(bugReportRepository).save(any());
        }
    }
}
