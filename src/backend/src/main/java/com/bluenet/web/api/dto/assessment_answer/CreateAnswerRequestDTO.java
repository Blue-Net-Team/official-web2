package com.bluenet.web.api.dto.assessment_answer;

import com.bluenet.web.domain.model.enumerate.ProgrammingLanguage;
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
@Schema(description = "创建答案请求")
public class CreateAnswerRequestDTO {

    @Schema(description = "题目ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "题目ID不能为空")
    private Long questionId;

    @Schema(description = "上传的文件ID（文件上传题必填）")
    private Long fileId;

    @Schema(description = "答案内容（选择题/算法题）")
    private String content;

    @Schema(description = "编程语言（算法题提交时使用）")
    private ProgrammingLanguage language;
}
