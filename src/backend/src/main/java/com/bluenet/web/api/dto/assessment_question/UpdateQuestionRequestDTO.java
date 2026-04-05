package com.bluenet.web.api.dto.assessment_question;

import com.bluenet.web.domain.model.enumerate.QuestionType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "更新考题请求")
public class UpdateQuestionRequestDTO {
    @Schema(description = "题号")
    private Integer questionNo;

    @Schema(description = "题型")
    private QuestionType questionType;

    @Schema(description = "题目标题")
    private String title;

    @Schema(description = "题目内容（JSON格式）")
    private Object content;

    @Schema(description = "附件ID")
    private Long attachmentId;

    @Schema(description = "分值")
    private BigDecimal score;
}
