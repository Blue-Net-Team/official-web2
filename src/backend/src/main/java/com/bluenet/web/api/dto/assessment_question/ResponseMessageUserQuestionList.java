package com.bluenet.web.api.dto.assessment_question;

import io.github.ivencn.infra.web.response.ResponseMessage;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "用户考题列表响应（含限时考核截止时间）")
public class ResponseMessageUserQuestionList extends ResponseMessage<UserQuestionListResponse> {
}
