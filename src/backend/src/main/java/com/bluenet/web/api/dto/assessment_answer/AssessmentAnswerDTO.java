package com.bluenet.web.api.dto.assessment_answer;

import com.bluenet.web.api.dto.assessment_judgement.AssessmentJudgementDTO;
import com.bluenet.web.api.dto.assessment_judgement.CommentDTO;
import com.bluenet.web.domain.model.enumerate.ProgrammingLanguage;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

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

    @Schema(description = "编程语言（算法题答案）")
    private ProgrammingLanguage language;

    @Schema(description = "提交时间")
    private LocalDateTime submitTime;

    @Schema(description = "最新评判结果（客观题提交后同步返回）")
    private AssessmentJudgementDTO judgement;

    @Schema(description = "成员评论列表")
    private List<CommentDTO> comments;
}
