package com.bluenet.web.api.dto.assessment_question;

import com.bluenet.web.api.dto.PageDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "用户考题列表响应，包含考题分页数据和限时考核截止时间")
public class UserQuestionListResponse {
    @Schema(description = "考题列表（分页）")
    private PageDTO<AssessmentQuestionDTO> questions;

    @Schema(description = "限时考核截止时间（ISO格式），非限时考核为null")
    private String deadline;
}
