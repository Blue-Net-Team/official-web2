package com.bluenet.web.api.dto.algorithm_judge;

import com.bluenet.web.api.dto.assessment_judgement.AssessmentJudgementDTO;
import com.bluenet.web.domain.model.enumerate.AlgorithmTestcaseType;
import com.bluenet.web.domain.model.enumerate.JudgeJobStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Polling response for algorithm run and submit jobs.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "算法题判题轮询响应")
public class JudgeJobPollingResponseDTO {
    @Schema(description = "判题任务ID")
    private Long judgeJobId;
    @Schema(description = "判题用例类型")
    private AlgorithmTestcaseType testcaseType;
    @Schema(description = "任务状态")
    private JudgeJobStatus status;
    @Schema(description = "状态说明")
    private String statusMessage;
    @Schema(description = "用例结果")
    private List<JudgeCaseResultDTO> caseResults;
    @Schema(description = "正式提交评判结果")
    private AssessmentJudgementDTO judgement;
}
