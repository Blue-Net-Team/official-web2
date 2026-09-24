package com.bluenet.web.api.dto.assessment_question;

import io.github.ivencn.infra.web.response.PageDTO;
import io.github.ivencn.infra.web.response.ResponseMessage;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "考题分页列表响应")
public class ResponseMessageAssessmentQuestionList extends ResponseMessage<PageDTO<AssessmentQuestionDTO>> {
}
