package com.bluenet.web.application.service.impl;

import com.bluenet.web.domain.model.entity.BugReport;
import com.bluenet.web.domain.repository.BugReportRepository;
import com.bluenet.web.infrastructure.config.GitHubAppProperties;
import com.bluenet.web.infrastructure.github.GitHubIssueClient;
import com.bluenet.web.infrastructure.github.GitHubIssueCreateResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@DisplayName("GitHubIssueSyncService 单元测试")
@ExtendWith(MockitoExtension.class)
class GitHubIssueSyncServiceTest {

    @Mock
    private GitHubIssueClient gitHubIssueClient;

    @Mock
    private BugReportRepository bugReportRepository;

    @Mock
    private GitHubAppProperties gitHubAppProperties;

    private GitHubIssueSyncService service;

    @BeforeEach
    void setUp() {
        service = new GitHubIssueSyncService(gitHubIssueClient, bugReportRepository, gitHubAppProperties);
    }

    @Nested
    @DisplayName("sync 方法测试")
    class SyncTest {

        @Test
        @DisplayName("TC-013: 成功同步应回写数据库")
        void sync_success_shouldUpdateRepository() {
            when(gitHubAppProperties.isEnabled()).thenReturn(true);

            BugReport bugReport = BugReport.create(
                    "页面加载缓慢，持续超过5秒",
                    "/dashboard",
                    "{\"browser\":\"Chrome 120\",\"os\":\"Windows 11\"}",
                    "user@example.com",
                    java.util.List.of(101L, 102L));

            GitHubIssueCreateResult mockResult = new GitHubIssueCreateResult(
                    42,
                    "https://github.com/bluenet-team/bluenet-issues/issues/42",
                    "页面加载缓慢，持续超过5秒");
            when(gitHubIssueClient.createIssue(anyString(), anyString())).thenReturn(mockResult);

            service.sync(bugReport);

            ArgumentCaptor<Long> idCaptor = ArgumentCaptor.forClass(Long.class);
            ArgumentCaptor<String> urlCaptor = ArgumentCaptor.forClass(String.class);
            ArgumentCaptor<Integer> numberCaptor = ArgumentCaptor.forClass(Integer.class);
            verify(bugReportRepository)
                    .updateGithubIssueInfo(idCaptor.capture(), urlCaptor.capture(), numberCaptor.capture());
            assertEquals("https://github.com/bluenet-team/bluenet-issues/issues/42", urlCaptor.getValue());
            assertEquals(42, numberCaptor.getValue());
        }

        @Test
        @DisplayName("TC-014: GitHub API 失败应记录异常但不抛出")
        void sync_apiFailure_shouldNotThrowException() {
            when(gitHubAppProperties.isEnabled()).thenReturn(true);

            BugReport bugReport = BugReport.create(
                    "测试描述",
                    "/test",
                    "{}",
                    null,
                    java.util.List.of());

            when(gitHubIssueClient.createIssue(anyString(), anyString()))
                    .thenThrow(new RuntimeException("GitHub API error: 500"));

            assertDoesNotThrow(() -> service.sync(bugReport));
            verify(bugReportRepository, never()).updateGithubIssueInfo(any(), any(), any());
        }

        @Test
        @DisplayName("TC-015: 配置未启用不应调用 GitHub API")
        void sync_disabled_shouldNotCallGitHubApi() {
            when(gitHubAppProperties.isEnabled()).thenReturn(false);

            BugReport bugReport = BugReport.create("desc", "/page", "{}", null, java.util.List.of());

            service.sync(bugReport);

            verifyNoInteractions(gitHubIssueClient);
            verifyNoInteractions(bugReportRepository);
        }

        @Test
        @DisplayName("TC-016: 无截图时 Issue Body 中截图部分应为空")
        void sync_noScreenshots_shouldCreateIssueWithoutImageLinks() {
            when(gitHubAppProperties.isEnabled()).thenReturn(true);

            BugReport bugReport = BugReport.create(
                    "无截图的 Bug",
                    "/home",
                    "{}",
                    null,
                    java.util.List.of());

            GitHubIssueCreateResult mockResult = new GitHubIssueCreateResult(1, "https://github.com/test/issues/1",
                    "无截图的 Bug");
            when(gitHubIssueClient.createIssue(anyString(), anyString())).thenReturn(mockResult);

            service.sync(bugReport);

            ArgumentCaptor<String> bodyCaptor = ArgumentCaptor.forClass(String.class);
            verify(gitHubIssueClient).createIssue(anyString(), bodyCaptor.capture());
            String body = bodyCaptor.getValue();
            assertTrue(body.contains("无截图"));
        }
    }
}
