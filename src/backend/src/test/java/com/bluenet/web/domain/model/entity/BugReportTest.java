package com.bluenet.web.domain.model.entity;

import com.bluenet.web.domain.model.enumerate.BugReportStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * BugReport 领域实体单元测试。
 */
@DisplayName("BugReport 领域实体测试")
class BugReportTest {

    @Test
    @DisplayName("create: 应创建新的待处理 Bug 报告")
    void create_shouldCreatePendingBugReport() {
        BugReport bugReport = BugReport.create(
                "首页加载缓慢",
                "进入首页需要等待超过5秒",
                "https://example.com/home",
                "{\"browser\":\"Chrome\"}",
                "reporter@example.com",
                List.of(1L, 2L));

        assertThat(bugReport.getId()).isNull();
        assertThat(bugReport.getTitle()).isEqualTo("首页加载缓慢");
        assertThat(bugReport.getDescription()).isEqualTo("进入首页需要等待超过5秒");
        assertThat(bugReport.getPageUrl()).isEqualTo("https://example.com/home");
        assertThat(bugReport.getEnvironmentJson()).isEqualTo("{\"browser\":\"Chrome\"}");
        assertThat(bugReport.getReporterEmail()).isEqualTo("reporter@example.com");
        assertThat(bugReport.getStatus()).isEqualTo(BugReportStatus.PENDING);
        assertThat(bugReport.getGithubIssueUrl()).isNull();
        assertThat(bugReport.getGithubIssueNumber()).isNull();
        assertThat(bugReport.getImages()).hasSize(2);
        assertThat(bugReport.getImages().get(0).getFileId()).isEqualTo(1L);
        assertThat(bugReport.getImages().get(1).getFileId()).isEqualTo(2L);
    }

    @Test
    @DisplayName("create: 标题为空应抛异常")
    void create_withBlankTitle_shouldThrow() {
        assertThatThrownBy(
                () -> BugReport.create(
                        "   ",
                        "描述",
                        null,
                        null,
                        null,
                        null))
                                .isInstanceOf(IllegalArgumentException.class)
                                .hasMessageContaining("标题");
    }

    @Test
    @DisplayName("create: 描述为空应抛异常")
    void create_withBlankDescription_shouldThrow() {
        assertThatThrownBy(
                () -> BugReport.create(
                        "标题",
                        null,
                        null,
                        null,
                        null,
                        null))
                                .isInstanceOf(IllegalArgumentException.class)
                                .hasMessageContaining("描述");
    }

    @Test
    @DisplayName("create: 标题超过最大长度应抛异常")
    void create_withTitleTooLong_shouldThrow() {
        String longTitle = "a".repeat(BugReport.MAX_TITLE_LENGTH + 1);
        assertThatThrownBy(
                () -> BugReport.create(
                        longTitle,
                        "描述",
                        null,
                        null,
                        null,
                        null))
                                .isInstanceOf(IllegalArgumentException.class)
                                .hasMessageContaining("最多");
    }

    @Test
    @DisplayName("create: 描述超过最大长度应抛异常")
    void create_withDescriptionTooLong_shouldThrow() {
        String longDescription = "a".repeat(BugReport.MAX_DESCRIPTION_LENGTH + 1);
        assertThatThrownBy(
                () -> BugReport.create(
                        "标题",
                        longDescription,
                        null,
                        null,
                        null,
                        null))
                                .isInstanceOf(IllegalArgumentException.class)
                                .hasMessageContaining("最多");
    }

    @Test
    @DisplayName("create: 截图超过最大数量应抛异常")
    void create_withTooManyImages_shouldThrow() {
        List<Long> fileIds = List.of(1L, 2L, 3L, 4L);
        assertThatThrownBy(
                () -> BugReport.create(
                        "标题",
                        "描述",
                        null,
                        null,
                        null,
                        fileIds))
                                .isInstanceOf(IllegalArgumentException.class)
                                .hasMessageContaining("最多上传");
    }

    @Test
    @DisplayName("create: 无截图时应创建空图片列表")
    void create_withNullFileIds_shouldCreateEmptyImageList() {
        BugReport bugReport = BugReport.create(
                "标题",
                "描述",
                null,
                null,
                null,
                null);

        assertThat(bugReport.getImages()).isEmpty();
    }

    @Test
    @DisplayName("create: 应去除标题和描述前后空白")
    void create_shouldTrimTitleAndDescription() {
        BugReport bugReport = BugReport.create(
                "  标题  ",
                "  描述  ",
                null,
                null,
                null,
                null);

        assertThat(bugReport.getTitle()).isEqualTo("标题");
        assertThat(bugReport.getDescription()).isEqualTo("描述");
    }

    @Test
    @DisplayName("updateStatus: 应更新状态")
    void updateStatus_shouldUpdateStatus() {
        BugReport bugReport = BugReport.create("标题", "描述", null, null, null, null);

        bugReport.updateStatus(BugReportStatus.IN_PROGRESS);

        assertThat(bugReport.getStatus()).isEqualTo(BugReportStatus.IN_PROGRESS);
    }

    @Test
    @DisplayName("updateStatus: 状态为空应抛异常")
    void updateStatus_withNullStatus_shouldThrow() {
        BugReport bugReport = BugReport.create("标题", "描述", null, null, null, null);

        assertThatThrownBy(() -> bugReport.updateStatus(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("状态不能为空");
    }

    @Test
    @DisplayName("updateGithubIssueInfo: 应更新 GitHub Issue 信息")
    void updateGithubIssueInfo_shouldUpdateIssueInfo() {
        BugReport bugReport = BugReport.create("标题", "描述", null, null, null, null);

        bugReport.updateGithubIssueInfo("https://github.com/org/repo/issues/1", 1);

        assertThat(bugReport.getGithubIssueUrl()).isEqualTo("https://github.com/org/repo/issues/1");
        assertThat(bugReport.getGithubIssueNumber()).isEqualTo(1);
    }

    @Test
    @DisplayName("updateGithubIssueInfo: URL 为空应抛异常")
    void updateGithubIssueInfo_withBlankUrl_shouldThrow() {
        BugReport bugReport = BugReport.create("标题", "描述", null, null, null, null);

        assertThatThrownBy(() -> bugReport.updateGithubIssueInfo("   ", 1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("URL");
    }

    @Test
    @DisplayName("updateGithubIssueInfo: 编号不合法应抛异常")
    void updateGithubIssueInfo_withInvalidNumber_shouldThrow() {
        BugReport bugReport = BugReport.create("标题", "描述", null, null, null, null);

        assertThatThrownBy(() -> bugReport.updateGithubIssueInfo("https://github.com/org/repo/issues/1", 0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("编号");
    }

    @Test
    @DisplayName("reconstruct: 应保留所有字段")
    void reconstruct_shouldPreserveAllFields() {
        BugReportImage image = BugReportImage.reconstruct(1L, 10L, 100L);
        BugReport bugReport = BugReport.reconstruct(
                10L,
                "标题",
                "描述",
                "https://example.com",
                "{}",
                "email@example.com",
                BugReportStatus.RESOLVED,
                "https://github.com/org/repo/issues/1",
                1,
                List.of(image));

        assertThat(bugReport.getId()).isEqualTo(10L);
        assertThat(bugReport.getTitle()).isEqualTo("标题");
        assertThat(bugReport.getDescription()).isEqualTo("描述");
        assertThat(bugReport.getPageUrl()).isEqualTo("https://example.com");
        assertThat(bugReport.getEnvironmentJson()).isEqualTo("{}");
        assertThat(bugReport.getReporterEmail()).isEqualTo("email@example.com");
        assertThat(bugReport.getStatus()).isEqualTo(BugReportStatus.RESOLVED);
        assertThat(bugReport.getGithubIssueUrl()).isEqualTo("https://github.com/org/repo/issues/1");
        assertThat(bugReport.getGithubIssueNumber()).isEqualTo(1);
        assertThat(bugReport.getImages()).containsExactly(image);
    }
}
