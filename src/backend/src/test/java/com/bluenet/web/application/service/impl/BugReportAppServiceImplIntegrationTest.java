package com.bluenet.web.application.service.impl;

import com.bluenet.web.BaseIntegrationTest;
import com.bluenet.web.application.command.bugreport.BugReportCommands;
import com.bluenet.web.application.result.bugreport.BugReportResult;
import com.bluenet.web.application.service.BugReportAppService;
import com.bluenet.web.domain.model.entity.BugReport;
import com.bluenet.web.domain.model.enumerate.BugReportStatus;
import com.bluenet.web.domain.repository.BugReportRepository;
import com.bluenet.web.infrastructure.security.principal.WithSecurityPrincipal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.mockito.ArgumentCaptor;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;

/**
 * BugReportAppServiceImpl 集成测试。
 *
 * <p>
 * 验证公开端 Bug 报告提交、参数校验及与 GitHub Issue 同步服务的协作逻辑。 为避免测试期间调用真实的 GitHub
 * 接口，{@link GitHubIssueSyncService} 通过 {@code @MockitoBean} 替换为 mock。
 * </p>
 */
@DisplayName("BugReportAppServiceImpl 集成测试")
@WithSecurityPrincipal(userId = 1L, roleType = "MEMBER")
class BugReportAppServiceImplIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private BugReportAppService bugReportAppService;

    @Autowired
    private BugReportRepository bugReportRepository;

    @MockitoBean
    private GitHubIssueSyncService gitHubIssueSyncService;

    @Test
    @DisplayName("submitBugReport: 应创建 Bug 报告并持久化图片关联")
    void submitBugReport_shouldCreateAndPersistImages() {
        BugReportCommands.CreateBugReportCommand command = new BugReportCommands.CreateBugReportCommand(
                "登录按钮无响应",
                "点击登录按钮后页面没有任何反应",
                "https://example.com/login",
                "{\"browser\":\"Chrome\"}",
                "reporter@example.com",
                List.of(1L, 2L));

        BugReportResult.Created result = bugReportAppService.submitBugReport(command);

        assertThat(result).isNotNull();
        assertThat(result.id()).isNotNull();
        assertThat(result.status()).isEqualTo(BugReportStatus.PENDING);
        assertThat(result.githubIssueUrl()).isNull();

        BugReport saved = bugReportRepository.findById(result.id()).orElseThrow();
        assertThat(saved.getTitle()).isEqualTo("登录按钮无响应");
        assertThat(saved.getDescription()).isEqualTo("点击登录按钮后页面没有任何反应");
        assertThat(saved.getPageUrl()).isEqualTo("https://example.com/login");
        assertThat(saved.getEnvironmentJson()).isEqualTo("{\"browser\":\"Chrome\"}");
        assertThat(saved.getReporterEmail()).isEqualTo("reporter@example.com");
        assertThat(saved.getStatus()).isEqualTo(BugReportStatus.PENDING);
        assertThat(saved.getImages())
                .hasSize(2)
                .extracting(image -> image.getFileId())
                .containsExactly(1L, 2L);

        ArgumentCaptor<BugReport> syncCaptor = ArgumentCaptor.forClass(BugReport.class);
        verify(gitHubIssueSyncService).sync(syncCaptor.capture());
        BugReport synced = syncCaptor.getValue();
        assertThat(synced.getId()).isEqualTo(result.id());
        assertThat(synced.getTitle()).isEqualTo("登录按钮无响应");
        assertThat(synced.getImages()).hasSize(2);
    }

    @Test
    @DisplayName("submitBugReport: 不带图片和可选字段时应创建成功")
    void submitBugReport_withoutOptionalFields_shouldCreate() {
        BugReportCommands.CreateBugReportCommand command = new BugReportCommands.CreateBugReportCommand(
                "页面样式错乱",
                "页面样式错乱详情",
                null,
                null,
                null,
                null);

        BugReportResult.Created result = bugReportAppService.submitBugReport(command);

        assertThat(result).isNotNull();
        assertThat(result.id()).isNotNull();

        BugReport saved = bugReportRepository.findById(result.id()).orElseThrow();
        assertThat(saved.getTitle()).isEqualTo("页面样式错乱");
        assertThat(saved.getImages()).isEmpty();
        assertThat(saved.getPageUrl()).isNull();
        assertThat(saved.getEnvironmentJson()).isNull();
        assertThat(saved.getReporterEmail()).isNull();

        ArgumentCaptor<BugReport> syncCaptor = ArgumentCaptor.forClass(BugReport.class);
        verify(gitHubIssueSyncService).sync(syncCaptor.capture());
        BugReport synced = syncCaptor.getValue();
        assertThat(synced.getId()).isEqualTo(result.id());
        assertThat(synced.getImages()).isEmpty();
    }

    @Test
    @DisplayName("submitBugReport: 标题为空应抛 IllegalArgumentException")
    void submitBugReport_blankTitle_shouldThrowIllegalArgumentException() {
        BugReportCommands.CreateBugReportCommand command = new BugReportCommands.CreateBugReportCommand(
                "   ",
                "描述",
                null,
                null,
                null,
                null);

        assertThatThrownBy(() -> bugReportAppService.submitBugReport(command))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("标题不能为空");
    }

    @Test
    @DisplayName("submitBugReport: 描述为空应抛 IllegalArgumentException")
    void submitBugReport_nullDescription_shouldThrowIllegalArgumentException() {
        BugReportCommands.CreateBugReportCommand command = new BugReportCommands.CreateBugReportCommand(
                "标题",
                null,
                null,
                null,
                null,
                null);

        assertThatThrownBy(() -> bugReportAppService.submitBugReport(command))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("描述不能为空");
    }

    @Test
    @DisplayName("submitBugReport: 标题超长应抛 IllegalArgumentException")
    void submitBugReport_titleTooLong_shouldThrowIllegalArgumentException() {
        String title = "a".repeat(BugReport.MAX_TITLE_LENGTH + 1);
        BugReportCommands.CreateBugReportCommand command = new BugReportCommands.CreateBugReportCommand(
                title,
                "描述",
                null,
                null,
                null,
                null);

        assertThatThrownBy(() -> bugReportAppService.submitBugReport(command))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("标题最多");
    }

    @Test
    @DisplayName("submitBugReport: 描述超长应抛 IllegalArgumentException")
    void submitBugReport_descriptionTooLong_shouldThrowIllegalArgumentException() {
        String description = "a".repeat(BugReport.MAX_DESCRIPTION_LENGTH + 1);
        BugReportCommands.CreateBugReportCommand command = new BugReportCommands.CreateBugReportCommand(
                "标题",
                description,
                null,
                null,
                null,
                null);

        assertThatThrownBy(() -> bugReportAppService.submitBugReport(command))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("描述最多");
    }

    @Test
    @DisplayName("submitBugReport: 图片超过 3 张应抛 IllegalArgumentException")
    void submitBugReport_tooManyImages_shouldThrowIllegalArgumentException() {
        BugReportCommands.CreateBugReportCommand command = new BugReportCommands.CreateBugReportCommand(
                "标题",
                "描述",
                null,
                null,
                null,
                List.of(1L, 2L, 3L, 4L));

        assertThatThrownBy(() -> bugReportAppService.submitBugReport(command))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("最多上传");
    }
}
