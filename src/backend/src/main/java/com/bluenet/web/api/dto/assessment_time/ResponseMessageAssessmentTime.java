package com.bluenet.web.api.dto.assessment_time;

import com.bluenet.web.api.dto.ResponseMessage;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * ResponseMessage包装类，用于Swagger文档生成单个考核时间响应
 */
@Schema(description = "考核时间响应")
public class ResponseMessageAssessmentTime extends ResponseMessage<AssessmentTimeDTO> {
}
