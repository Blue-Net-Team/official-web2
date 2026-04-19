package com.bluenet.web.api.dto.algorithm_judge;

import com.bluenet.web.domain.model.enumerate.AlgorithmTestcaseType;
import com.bluenet.web.domain.model.enumerate.ProgrammingLanguage;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request body for non-scoring algorithm runs.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "算法题运行请求")
public class AlgorithmRunRequestDTO {
    @Schema(description = "题目ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "题目ID不能为空")
    private Long questionId;

    @Schema(description = "编程语言", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "编程语言不能为空")
    private ProgrammingLanguage language;

    @Schema(description = "源代码", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "源代码不能为空")
    private String sourceCode;

    @Schema(description = "运行类型：DEFAULT_RUN/CUSTOM_RUN")
    private AlgorithmTestcaseType testcaseType;

    @Schema(description = "自定义输入")
    private String customInput;
}
