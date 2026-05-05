package com.bluenet.web.api.dto.bugreport;

import com.bluenet.web.domain.model.enumerate.BugReportStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "更新 Bug 报告状态请求")
public class UpdateBugReportStatusRequestDTO {

    @NotNull(message = "状态不能为空")
    @Schema(description = "新状态", required = true, example = "RESOLVED")
    private BugReportStatus status;
}
