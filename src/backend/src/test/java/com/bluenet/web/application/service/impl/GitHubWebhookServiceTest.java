package com.bluenet.web.application.service.impl;

import com.bluenet.web.domain.model.entity.BugReport;
import com.bluenet.web.domain.model.enumerate.BugReportStatus;
import com.bluenet.web.domain.repository.BugReportRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@DisplayName("GitHubWebhookService 单元测试")
@ExtendWith(MockitoExtension.class)
class GitHubWebhookServiceTest {

    @Mock
    private BugReportRepository bugReportRepository;

    private GitHubWebhookService service;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        service = new GitHubWebhookService(bugReportRepository, objectMapper);
    }

    @Nested
    @DisplayName("processIssuesEvent - opened 事件测试")
    class OpenedEventTest {

        @Test
        @DisplayName("TC-005: opened + 无标记 → 创建新 BugReport（PENDING）")
        void opened_withoutMarker_shouldCreateBugReport() {
            String payload = createIssuePayload(
                    "opened",
                    42,
                    "GitHub Bug",
                    "This is a bug from GitHub",
                    "https://github.com/test/issues/42");
            when(bugReportRepository.findByGithubIssueNumber(42)).thenReturn(Optional.empty());

            service.processIssuesEvent(payload);

            ArgumentCaptor<BugReport> captor = ArgumentCaptor.forClass(BugReport.class);
            verify(bugReportRepository).save(captor.capture());
            BugReport saved = captor.getValue();
            assertEquals("GitHub Bug", saved.getTitle());
            assertEquals("This is a bug from GitHub", saved.getDescription());
            assertEquals(BugReportStatus.PENDING, saved.getStatus());
            assertEquals(42, saved.getGithubIssueNumber());
            assertEquals("https://github.com/test/issues/42", saved.getGithubIssueUrl());
        }

        @Test
        @DisplayName("TC-006: opened + 有标记 → 忽略不处理")
        void opened_withMarker_shouldIgnore() {
            String payload = createIssuePayload(
                    "opened",
                    42,
                    "Platform Bug",
                    "Description\n\n<!-- bluenet-bug-report -->",
                    "https://github.com/test/issues/42");

            service.processIssuesEvent(payload);

            verify(bugReportRepository, never()).save(any());
            verify(bugReportRepository, never()).findByGithubIssueNumber(any());
        }

        @Test
        @DisplayName("TC-011: opened + description 为空 → 使用 title 作为 description")
        void opened_emptyDescription_shouldUseTitleAsDescription() {
            String payload = createIssuePayload("opened", 42, "Title Only", "", "https://github.com/test/issues/42");
            when(bugReportRepository.findByGithubIssueNumber(42)).thenReturn(Optional.empty());

            service.processIssuesEvent(payload);

            ArgumentCaptor<BugReport> captor = ArgumentCaptor.forClass(BugReport.class);
            verify(bugReportRepository).save(captor.capture());
            assertEquals("Title Only", captor.getValue().getDescription());
        }

        @Test
        @DisplayName("TC-012: opened + title 为空 → 记录日志不创建")
        void opened_emptyTitle_shouldNotCreate() {
            String payload = createIssuePayload("opened", 42, "", "Some body", "https://github.com/test/issues/42");

            service.processIssuesEvent(payload);

            verify(bugReportRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("processIssuesEvent - 状态变更事件测试")
    class StatusChangeEventTest {

        @Test
        @DisplayName("TC-007: assigned → 更新状态为 IN_PROGRESS")
        void assigned_shouldUpdateToInProgress() {
            String payload = createIssuePayload("assigned", 42, "Test", "Body", "https://github.com/test/issues/42");
            BugReport bugReport = BugReport.reconstruct(
                    1L,
                    "Test",
                    "Body",
                    null,
                    null,
                    null,
                    BugReportStatus.PENDING,
                    "https://github.com/test/issues/42",
                    42,
                    java.util.List.of());
            when(bugReportRepository.findByGithubIssueNumber(42)).thenReturn(Optional.of(bugReport));

            service.processIssuesEvent(payload);

            verify(bugReportRepository).updateStatus(1L, BugReportStatus.IN_PROGRESS);
        }

        @Test
        @DisplayName("TC-008: closed → 更新状态为 RESOLVED")
        void closed_shouldUpdateToResolved() {
            String payload = createIssuePayload("closed", 42, "Test", "Body", "https://github.com/test/issues/42");
            BugReport bugReport = BugReport.reconstruct(
                    1L,
                    "Test",
                    "Body",
                    null,
                    null,
                    null,
                    BugReportStatus.IN_PROGRESS,
                    "https://github.com/test/issues/42",
                    42,
                    java.util.List.of());
            when(bugReportRepository.findByGithubIssueNumber(42)).thenReturn(Optional.of(bugReport));

            service.processIssuesEvent(payload);

            verify(bugReportRepository).updateStatus(1L, BugReportStatus.RESOLVED);
        }

        @Test
        @DisplayName("TC-009: reopened → 更新状态为 PENDING")
        void reopened_shouldUpdateToPending() {
            String payload = createIssuePayload("reopened", 42, "Test", "Body", "https://github.com/test/issues/42");
            BugReport bugReport = BugReport.reconstruct(
                    1L,
                    "Test",
                    "Body",
                    null,
                    null,
                    null,
                    BugReportStatus.RESOLVED,
                    "https://github.com/test/issues/42",
                    42,
                    java.util.List.of());
            when(bugReportRepository.findByGithubIssueNumber(42)).thenReturn(Optional.of(bugReport));

            service.processIssuesEvent(payload);

            verify(bugReportRepository).updateStatus(1L, BugReportStatus.PENDING);
        }
    }

    @Nested
    @DisplayName("processIssuesEvent - 边界情况测试")
    class EdgeCaseTest {

        @Test
        @DisplayName("TC-010: 事件对应的 BugReport 不存在 → 记录日志不抛异常")
        void event_bugReportNotFound_shouldNotThrow() {
            String payload = createIssuePayload("closed", 999, "Test", "Body", "https://github.com/test/issues/999");
            when(bugReportRepository.findByGithubIssueNumber(999)).thenReturn(Optional.empty());

            assertDoesNotThrow(() -> service.processIssuesEvent(payload));
            verify(bugReportRepository, never()).updateStatus(any(), any());
        }

        @Test
        @DisplayName("TC-013: 不支持的事件类型（如 labeled）→ 忽略")
        void unsupportedEventType_shouldIgnore() {
            String payload = createIssuePayload("labeled", 42, "Test", "Body", "https://github.com/test/issues/42");

            assertDoesNotThrow(() -> service.processIssuesEvent(payload));
            verify(bugReportRepository, never()).findByGithubIssueNumber(any());
            verify(bugReportRepository, never()).save(any());
            verify(bugReportRepository, never()).updateStatus(any(), any());
        }

        @Test
        @DisplayName("缺少 action 字段 → 忽略不抛异常")
        void missingAction_shouldIgnore() {
            String payload = "{\"issue\":{\"number\":42}}";

            assertDoesNotThrow(() -> service.processIssuesEvent(payload));
            verifyNoInteractions(bugReportRepository);
        }

        @Test
        @DisplayName("缺少 issue 字段 → 忽略不抛异常")
        void missingIssue_shouldIgnore() {
            String payload = "{\"action\":\"closed\"}";

            assertDoesNotThrow(() -> service.processIssuesEvent(payload));
            verifyNoInteractions(bugReportRepository);
        }

        @Test
        @DisplayName("状态无变化 → 跳过更新")
        void statusNoChange_shouldSkipUpdate() {
            String payload = createIssuePayload("closed", 42, "Test", "Body", "https://github.com/test/issues/42");
            BugReport bugReport = BugReport.reconstruct(
                    1L,
                    "Test",
                    "Body",
                    null,
                    null,
                    null,
                    BugReportStatus.RESOLVED,
                    "https://github.com/test/issues/42",
                    42,
                    java.util.List.of());
            when(bugReportRepository.findByGithubIssueNumber(42)).thenReturn(Optional.of(bugReport));

            service.processIssuesEvent(payload);

            verify(bugReportRepository, never()).updateStatus(any(), any());
        }
    }

    private String createIssuePayload(String action, int number, String title, String body, String htmlUrl) {
        try {
            return "{"
                    + "\"action\":\"" + action + "\","
                    + "\"issue\":{"
                    + "\"number\":" + number + ","
                    + "\"title\":\"" + title + "\","
                    + "\"body\":\"" + body + "\","
                    + "\"html_url\":\"" + htmlUrl + "\""
                    + "}"
                    + "}";
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
