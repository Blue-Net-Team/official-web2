package com.bluenet.web.api.dto.assessment_answer;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "答案信息")
public class AssessmentAnswerDTO {

    @Schema(description = "答案ID")
    private Long id;

    @Schema(description = "题目ID")
    private Long questionId;

    @Schema(description = "上传的文件ID")
    private Long fileId;

    @Schema(description = "答案内容")
    private String content;

    @Schema(description = "提交时间")
    private LocalDateTime submitTime;
}
