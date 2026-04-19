package com.bluenet.web.api.dto.algorithm_judge;

import com.bluenet.web.domain.model.enumerate.AlgorithmTestcaseType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response returned after creating a formal algorithm judge job.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "算法题提交响应")
public class AlgorithmSubmitResponseDTO {
    @Schema(description = "答案ID")
    private Long answerId;
    @Schema(description = "判题任务ID")
    private Long judgeJobId;
    @Schema(description = "判题用例类型")
    private AlgorithmTestcaseType testcaseType;
}
