package com.bluenet.web.domain.model.entity;

import com.bluenet.web.domain.model.enumerate.BugReportStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("BugReport 领域单元测试")
class BugReportTest {

    @Test
    @DisplayName("创建成功时应包含正确的基础字段")
    void create_success_shouldContainCorrectFields() {
        BugReport report = BugReport.create(
                "提交按钮无响应",
                "点击提交按钮后页面无响应",
                "/home",
                "{\"browser\":\"Chrome\"}",
                "user@example.com",
                List.of(1L, 2L));

        assertNotNull(report);
        assertEquals("提交按钮无响应", report.getTitle());
        assertEquals("点击提交按钮后页面无响应", report.getDescription());
        assertEquals("/home", report.getPageUrl());
        assertEquals("{\"browser\":\"Chrome\"}", report.getEnvironmentJson());
        assertEquals("user@example.com", report.getReporterEmail());
        assertEquals(BugReportStatus.PENDING, report.getStatus());
        assertEquals(2, report.getImages().size());
    }

    @Test
    @DisplayName("创建时应 trim 标题和描述")
    void create_shouldTrimTitleAndDescription() {
        BugReport report = BugReport.create(
                "  标题  ",
                "  描述内容  ",
                "/page",
                null,
                null,
                null);

        assertEquals("标题", report.getTitle());
        assertEquals("描述内容", report.getDescription());
    }

    @Test
    @DisplayName("标题为 null 时应抛出异常")
    void create_nullTitle_shouldThrow() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> BugReport.create(null, "描述", "/page", null, null, null));
        assertEquals("Bug 标题不能为空", exception.getMessage());
    }

    @Test
    @DisplayName("标题为空白时应抛出异常")
    void create_blankTitle_shouldThrow() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> BugReport.create("   ", "描述", "/page", null, null, null));
        assertEquals("Bug 标题不能为空", exception.getMessage());
    }

    @Test
    @DisplayName("标题超长时应抛出异常")
    void create_titleTooLong_shouldThrow() {
        String longTitle = Stream.generate(() -> "a")
                .limit(BugReport.MAX_TITLE_LENGTH + 1)
                .collect(Collectors.joining());
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> BugReport.create(longTitle, "描述", "/page", null, null, null));
        assertEquals("Bug 标题最多 " + BugReport.MAX_TITLE_LENGTH + " 字符", exception.getMessage());
    }

    @Test
    @DisplayName("描述为 null 时应抛出异常")
    void create_nullDescription_shouldThrow() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> BugReport.create("标题", null, "/page", null, null, null));
        assertEquals("Bug 描述不能为空", exception.getMessage());
    }

    @Test
    @DisplayName("描述为空白时应抛出异常")
    void create_blankDescription_shouldThrow() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> BugReport.create("标题", "   ", "/page", null, null, null));
        assertEquals("Bug 描述不能为空", exception.getMessage());
    }

    @Test
    @DisplayName("描述超长时应抛出异常")
    void create_descriptionTooLong_shouldThrow() {
        String longDescription = Stream.generate(() -> "a")
                .limit(BugReport.MAX_DESCRIPTION_LENGTH + 1)
                .collect(Collectors.joining());
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> BugReport.create("标题", longDescription, "/page", null, null, null));
        assertEquals("Bug 描述最多 " + BugReport.MAX_DESCRIPTION_LENGTH + " 字符", exception.getMessage());
    }

    @Test
    @DisplayName("图片超过 3 张时应抛出异常")
    void create_tooManyImages_shouldThrow() {
        List<Long> fileIds = List.of(1L, 2L, 3L, 4L);
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> BugReport.create("标题", "描述", "/page", null, null, fileIds));
        assertEquals("最多上传 3 张截图", exception.getMessage());
    }

    @Test
    @DisplayName("trim 后标题长度合规时应通过校验")
    void create_titleTrimmedToValidLength_shouldPass() {
        String titleWithSpaces = "a" + " ".repeat(BugReport.MAX_TITLE_LENGTH - 1);
        BugReport report = BugReport.create(titleWithSpaces, "描述", "/page", null, null, null);
        assertEquals("a", report.getTitle());
    }

    @Test
    @DisplayName("trim 后标题仍超长时应抛出异常")
    void create_titleStillTooLongAfterTrim_shouldThrow() {
        String title = "a".repeat(BugReport.MAX_TITLE_LENGTH) + " a";
        assertTrue(title.length() > BugReport.MAX_TITLE_LENGTH);
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> BugReport.create(title, "描述", "/page", null, null, null));
        assertEquals("Bug 标题最多 " + BugReport.MAX_TITLE_LENGTH + " 字符", exception.getMessage());
    }
}
