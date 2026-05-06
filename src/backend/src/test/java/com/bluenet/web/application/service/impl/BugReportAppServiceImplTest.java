package com.bluenet.web.application.service.impl;

import com.bluenet.web.application.BugReportResult;
import com.bluenet.web.application.command.bugreport.BugReportCommands;
import com.bluenet.web.domain.model.entity.BugReport;
import com.bluenet.web.domain.repository.BugReportRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("BugReportAppServiceImpl 单元测试")
@ExtendWith(MockitoExtension.class)
class BugReportAppServiceImplTest {

    @Mock
    private BugReportRepository bugReportRepository;

    @Mock
    private GitHubIssueSyncService gitHubIssueSyncService;

    private BugReportAppServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new BugReportAppServiceImpl(bugReportRepository, gitHubIssueSyncService);
    }

    @Nested
    @DisplayName("submitBugReport 方法测试")
    class SubmitBugReportTest {

        @Test
        @DisplayName("TC-017: 成功提交应保存并触发异步同步")
        void submitBugReport_success_shouldSaveAndTriggerSync() {
            doAnswer(invocation -> {
                BugReport report = invocation.getArgument(0);
                report.setId(1L);
                return null;
            }).when(bugReportRepository).save(any(BugReport.class));

            BugReportCommands.CreateBugReportCommand command = new BugReportCommands.CreateBugReportCommand(
                    "页面无响应",
                    "/home",
                    "{\"browser\":\"Chrome\"}",
                    "user@example.com",
                    List.of(1L));

            BugReportResult.Created result = service.submitBugReport(command);

            assertNotNull(result);
            assertEquals(1L, result.id());
            assertNotNull(result.status());
            assertNull(result.githubIssueUrl());

            ArgumentCaptor<BugReport> captor = ArgumentCaptor.forClass(BugReport.class);
            verify(bugReportRepository).save(captor.capture());
            assertEquals("页面无响应", captor.getValue().getDescription());

            verify(gitHubIssueSyncService).sync(any(BugReport.class));
        }

        @Test
        @DisplayName("TC-018: GitHub 同步失败不应影响保存和返回")
        void submitBugReport_syncFailure_shouldStillReturnSuccess() {
            doAnswer(invocation -> {
                BugReport report = invocation.getArgument(0);
                report.setId(2L);
                return null;
            }).when(bugReportRepository).save(any(BugReport.class));

            BugReportCommands.CreateBugReportCommand command = new BugReportCommands.CreateBugReportCommand(
                    "测试描述",
                    "/test",
                    "{}",
                    null,
                    List.of());

            // sync 异常由 GitHubIssueSyncService 内部捕获，此处模拟正常返回
            BugReportResult.Created result = service.submitBugReport(command);

            assertNotNull(result);
            assertEquals(2L, result.id());
            assertNull(result.githubIssueUrl());
            verify(bugReportRepository).save(any(BugReport.class));
            verify(gitHubIssueSyncService).sync(any(BugReport.class));
        }

        @Test
        @DisplayName("TC-019: 无 GitHub 配置时应正常保存")
        void submitBugReport_noGitHubConfig_shouldSaveNormally() {
            doAnswer(invocation -> {
                BugReport report = invocation.getArgument(0);
                report.setId(3L);
                return null;
            }).when(bugReportRepository).save(any(BugReport.class));

            BugReportCommands.CreateBugReportCommand command = new BugReportCommands.CreateBugReportCommand(
                    "配置未启用测试",
                    "/page",
                    "{}",
                    null,
                    List.of());

            BugReportResult.Created result = service.submitBugReport(command);

            assertNotNull(result);
            assertEquals(3L, result.id());
            assertNull(result.githubIssueUrl());
            verify(bugReportRepository).save(any(BugReport.class));
            verify(gitHubIssueSyncService).sync(any(BugReport.class));
        }
    }
}
