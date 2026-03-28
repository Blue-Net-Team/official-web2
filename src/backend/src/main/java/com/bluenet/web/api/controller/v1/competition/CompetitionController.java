package com.bluenet.web.api.controller.v1.competition;

import com.bluenet.web.api.dto.ResponseMessage;
import com.bluenet.web.api.dto.competition.CompetitionDetailDTO;
import com.bluenet.web.api.dto.competition.CompetitionResponseDTO;
import com.bluenet.web.api.dto.competition.ResponseMessageCompetitionBriefList;
import com.bluenet.web.api.dto.competition.ResponseMessageCompetitionDetail;
import com.bluenet.web.application.service.CompetitionService;
import com.bluenet.web.infrastructure.security.annotation.AccessLevel;
import com.bluenet.web.infrastructure.security.annotation.RequiresPermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "竞赛", description = "竞赛相关接口，公开访问")
@RestController
@RequestMapping("/api/v1/competitions")
@RequiredArgsConstructor
public class CompetitionController {
    private final CompetitionService competitionService;

    @Operation(summary = "获取竞赛列表", description = "获取启用的竞赛简要信息列表，按排序权重降序排列")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "成功，返回竞赛列表", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseMessageCompetitionBriefList.class))) })
    @RequiresPermission(name = "获取竞赛列表", value = "competition:list", access = AccessLevel.PUBLIC)
    @GetMapping
    public ResponseMessage<List<CompetitionResponseDTO>> getCompetitionList(
            @Parameter(description = "返回数量限制，默认10，最大50", example = "10") @RequestParam(defaultValue = "10") int limit) {
        List<CompetitionResponseDTO> competitions = competitionService.getCompetitionResponseList(limit);
        return ResponseMessage.success(competitions);
    }

    @Operation(summary = "获取竞赛详情", description = "获取单个竞赛的详细信息，包括相关照片")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "成功，返回竞赛详情", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseMessageCompetitionDetail.class))),
            @ApiResponse(responseCode = "404", description = "竞赛不存在", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseMessage.class), examples = @ExampleObject(value = "{\"code\":404,\"msg\":\"竞赛不存在\",\"data\":null}"))) })
    @RequiresPermission(name = "获取竞赛详情", value = "competition:detail", access = AccessLevel.PUBLIC)
    @GetMapping("/{id}")
    public ResponseMessage<CompetitionDetailDTO> getCompetitionDetail(
            @Parameter(description = "竞赛ID", required = true, example = "1") @PathVariable Long id) {
        try {
            CompetitionDetailDTO detail = competitionService.getCompetitionDetail(id);
            return ResponseMessage.success(detail);
        } catch (IllegalArgumentException e) {
            return ResponseMessage.error(404, e.getMessage());
        }
    }
}
