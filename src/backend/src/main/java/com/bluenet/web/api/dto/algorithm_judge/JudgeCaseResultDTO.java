package com.bluenet.web.api.dto.algorithm_judge;

import com.bluenet.web.domain.model.enumerate.AlgorithmTestcaseType;
import com.bluenet.web.domain.model.enumerate.JudgeCaseStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * API response for one testcase result in a judge job.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "算法题用例结果")
public class JudgeCaseResultDTO {
    @Schema(description = "用例序号")
    private Integer caseNo;
    @Schema(description = "用例类型")
    private AlgorithmTestcaseType testcaseType;
    @Schema(description = "结果状态")
    private JudgeCaseStatus status;
    @Schema(description = "输入")
    private String input;
    @Schema(description = "期望输出")
    private String expectedOutput;
    @Schema(description = "实际输出")
    private String actualOutput;
    @Schema(description = "标准输出")
    private String stdout;
    @Schema(description = "标准错误")
    private String stderr;
    @Schema(description = "耗时毫秒")
    private Integer timeUsedMs;
    @Schema(description = "内存KB")
    private Integer memoryUsedKb;
    @Schema(description = "结果说明")
    private String message;
}
