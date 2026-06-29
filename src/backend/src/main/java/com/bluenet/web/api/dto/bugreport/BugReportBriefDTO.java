package com.bluenet.web.api.dto.bugreport;

import com.bluenet.web.domain.model.enumerate.BugReportStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "Bug 报告列表项响应")
public class BugReportBriefDTO {

    @Schema(description = "报告 ID", example = "1")
    private Long id;

    @Schema(description = "Bug 标题", example = "提交按钮无响应")
    private String title;

    @Schema(description = "Bug 描述", example = "点击提交按钮后页面无响应")
    private String description;

    @Schema(description = "发生页面的 URL", example = "/home")
    private String pageUrl;

    @Schema(description = "报告者邮箱", example = "user@example.com")
    private String reporterEmail;

    @Schema(description = "当前状态", example = "PENDING")
    private BugReportStatus status;

    @Schema(description = "关联图片数量", example = "2")
    private int imageCount;

    @Schema(description = "GitHub Issue URL")
    private String githubIssueUrl;

    @Schema(description = "GitHub Issue 编号")
    private Integer githubIssueNumber;
}
