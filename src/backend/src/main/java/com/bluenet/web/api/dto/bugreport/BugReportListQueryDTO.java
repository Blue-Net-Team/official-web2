package com.bluenet.web.api.dto.bugreport;

import com.bluenet.web.domain.model.enumerate.BugReportStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "Bug 报告列表查询参数")
public class BugReportListQueryDTO {

    @Min(value = 0, message = "页码不能小于 0")
    @Schema(description = "页码，从 0 开始", example = "0")
    private Integer page;

    @Min(value = 1, message = "每页大小不能小于 1")
    @Max(value = 100, message = "每页大小不能大于 100")
    @Schema(description = "每页大小", example = "20")
    private Integer size;

    @Schema(description = "状态筛选", example = "PENDING")
    private BugReportStatus status;
}
