package com.bluenet.web.api.dto.competition;

import com.bluenet.web.api.dto.ResponseMessage;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "竞赛响应")
public class ResponseMessageCompetitionResponse extends ResponseMessage<CompetitionResponseDTO> {
}
