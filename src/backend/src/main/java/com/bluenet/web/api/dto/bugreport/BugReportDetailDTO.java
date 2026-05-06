package com.bluenet.web.api.dto.bugreport;

import com.bluenet.web.domain.model.enumerate.BugReportStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "Bug 报告详情响应")
public class BugReportDetailDTO {

    @Schema(description = "报告 ID", example = "1")
    private Long id;

    @Schema(description = "Bug 描述", example = "点击提交按钮后页面无响应")
    private String description;

    @Schema(description = "发生页面的 URL", example = "/home")
    private String pageUrl;

    @Schema(description = "前端环境信息 JSON")
    private String environmentJson;

    @Schema(description = "报告者邮箱", example = "user@example.com")
    private String reporterEmail;

    @Schema(description = "当前状态", example = "PENDING")
    private BugReportStatus status;

    @Schema(description = "GitHub Issue URL")
    private String githubIssueUrl;

    @Schema(description = "GitHub Issue 编号")
    private Integer githubIssueNumber;

    @Schema(description = "关联图片文件 ID 列表")
    private List<Long> fileIds;
}
