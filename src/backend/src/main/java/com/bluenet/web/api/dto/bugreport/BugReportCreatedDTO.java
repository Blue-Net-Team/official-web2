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
@Schema(description = "Bug 报告创建响应")
public class BugReportCreatedDTO {

    @Schema(description = "报告 ID", example = "1")
    private Long id;

    @Schema(description = "当前状态", example = "PENDING")
    private BugReportStatus status;
}
