package com.bluenet.web.api.dto.assessment_question;

import com.bluenet.web.api.dto.PageDTO;
import com.bluenet.web.api.dto.ResponseMessage;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "考题分页列表响应")
public class ResponseMessageAssessmentQuestionList extends ResponseMessage<PageDTO<AssessmentQuestionDTO>> {
}
