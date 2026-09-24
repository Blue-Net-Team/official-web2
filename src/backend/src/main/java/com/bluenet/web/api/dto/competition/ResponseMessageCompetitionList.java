package com.bluenet.web.api.dto.competition;

import io.github.ivencn.infra.web.response.ResponseMessage;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "竞赛列表响应")
public class ResponseMessageCompetitionList extends ResponseMessage<List<CompetitionResponseDTO>> {
}
