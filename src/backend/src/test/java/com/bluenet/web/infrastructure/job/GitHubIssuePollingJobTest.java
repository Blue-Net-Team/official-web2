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

            verify(bugReportRepository, never()).save(any());
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

            verify(bugReportRepository).save(
                    argThat(
                            bugReport -> bugReport.getId().equals(1L)
                                    && bugReport.getStatus() == BugReportStatus.RESOLVED));
        }

        @Test
        @DisplayName("TC-020: 状态更新时 bugReport 对象应先持有旧状态再更新")
        void sync_statusMismatch_shouldPreserveOldStatusForLogging() {
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

            // 验证 updateStatus 被调用后，bugReport 的状态已更新
            assertEquals(BugReportStatus.RESOLVED, existing.getStatus());
            // 验证 repository 被传入正确的旧状态目标值
            verify(bugReportRepository).save(
                    argThat(
                            bugReport -> bugReport.getId().equals(1L)
                                    && bugReport.getStatus() == BugReportStatus.RESOLVED));
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

    @Nested
    @DisplayName("防御性处理测试")
    class DefensiveProgrammingTest {

        @Test
        @DisplayName("TC-021: issue number 为 null 时应跳过，不查询数据库")
        void sync_nullIssueNumber_shouldSkip() {
            when(gitHubAppProperties.isPollingEnabled()).thenReturn(true);
            when(gitHubAppProperties.getPollingSinceDays()).thenReturn(7);

            GitHubIssueListResult issue = new GitHubIssueListResult(
                    null, "Bug", "Desc", "open", "https://github.com/test/issues/42");

            when(gitHubIssueClient.listIssues(any())).thenReturn(List.of(issue));

            job.sync();

            verify(bugReportRepository, never()).findByGithubIssueNumber(any());
            verify(bugReportRepository, never()).save(any());
            verify(bugReportRepository, never()).save(any());
        }

        @Test
        @DisplayName("TC-022: 反向同步时标题超长应截断到 100 字符")
        void sync_titleTooLong_shouldTruncate() {
            when(gitHubAppProperties.isPollingEnabled()).thenReturn(true);
            when(gitHubAppProperties.getPollingSinceDays()).thenReturn(7);

            String longTitle = "A".repeat(200);
            GitHubIssueListResult issue = new GitHubIssueListResult(
                    42, longTitle, "Description", "open", "https://github.com/test/issues/42");

            when(gitHubIssueClient.listIssues(any())).thenReturn(List.of(issue));
            when(bugReportRepository.findByGithubIssueNumber(42)).thenReturn(Optional.empty());

            job.sync();

            ArgumentCaptor<BugReport> captor = ArgumentCaptor.forClass(BugReport.class);
            verify(bugReportRepository).save(captor.capture());
            assertEquals(100, captor.getValue().getTitle().length());
        }

        @Test
        @DisplayName("TC-023: 反向同步时描述超长应截断到 2000 字符")
        void sync_descriptionTooLong_shouldTruncate() {
            when(gitHubAppProperties.isPollingEnabled()).thenReturn(true);
            when(gitHubAppProperties.getPollingSinceDays()).thenReturn(7);

            String longDescription = "B".repeat(3000);
            GitHubIssueListResult issue = new GitHubIssueListResult(
                    42, "Bug", longDescription, "open", "https://github.com/test/issues/42");

            when(gitHubIssueClient.listIssues(any())).thenReturn(List.of(issue));
            when(bugReportRepository.findByGithubIssueNumber(42)).thenReturn(Optional.empty());

            job.sync();

            ArgumentCaptor<BugReport> captor = ArgumentCaptor.forClass(BugReport.class);
            verify(bugReportRepository).save(captor.capture());
            assertEquals(2000, captor.getValue().getDescription().length());
        }
    }
}
