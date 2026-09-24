package com.bluenet.web.api.dto.assessment_time;

import io.github.ivencn.infra.web.response.PageDTO;
import io.github.ivencn.infra.web.response.ResponseMessage;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * ResponseMessage包装类，用于Swagger文档生成分页列表响应
 */
@Schema(description = "考核时间分页列表响应")
public class ResponseMessageAssessmentTimeList extends ResponseMessage<PageDTO<AssessmentTimeDTO>> {
}
