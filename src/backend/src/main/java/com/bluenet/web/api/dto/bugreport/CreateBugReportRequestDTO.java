package com.bluenet.web.api.dto.bugreport;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "提交 Bug 报告请求")
public class CreateBugReportRequestDTO {

    @NotBlank(message = "Bug 标题不能为空")
    @Size(max = 100, message = "Bug 标题最多 100 字符")
    @Schema(description = "Bug 标题", required = true, example = "提交按钮无响应")
    private String title;

    @NotBlank(message = "Bug 描述不能为空")
    @Size(max = 2000, message = "Bug 描述最多 2000 字符")
    @Schema(description = "Bug 描述", required = true, example = "点击提交按钮后页面无响应")
    private String description;

    @Size(max = 2048, message = "页面 URL 最多 2048 字符")
    @Schema(description = "发生页面的 URL", example = "/home")
    private String pageUrl;

    @Schema(description = "前端环境信息 JSON", example = "{\"userAgent\":\"Mozilla/5.0...\",\"resolution\":\"1920x1080\"}")
    private String environmentJson;

    @Email(message = "邮箱格式不正确")
    @Size(max = 255, message = "邮箱最多 255 字符")
    @Schema(description = "报告者邮箱（选填）", example = "user@example.com")
    private String reporterEmail;

    @Size(max = 3, message = "最多上传 3 张截图")
    @Schema(description = "关联图片文件 ID 列表（最多 3 个）", example = "[1, 2, 3]")
    private List<Long> fileIds;
}
