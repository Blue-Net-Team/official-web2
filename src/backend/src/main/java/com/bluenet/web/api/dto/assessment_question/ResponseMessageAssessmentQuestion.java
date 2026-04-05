package com.bluenet.web.api.dto.assessment_question;

import com.bluenet.web.api.dto.ResponseMessage;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "考题响应")
public class ResponseMessageAssessmentQuestion extends ResponseMessage<AssessmentQuestionDTO> {
}
