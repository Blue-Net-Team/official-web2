package com.bluenet.web.api.controller.v1.competition;

import com.bluenet.web.api.dto.PageDTO;
import com.bluenet.web.api.dto.ResponseMessage;
import com.bluenet.web.api.dto.competition.CompetitionResponseDTO;
import com.bluenet.web.api.dto.competition.ResponseMessageCompetitionList;
import com.bluenet.web.application.service.CompetitionService;
import com.bluenet.web.infrastructure.security.annotation.AccessLevel;
import com.bluenet.web.infrastructure.security.annotation.RequiresPermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
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
            @ApiResponse(responseCode = "200", description = "成功，返回竞赛列表", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseMessageCompetitionList.class))) })
    @RequiresPermission(name = "获取竞赛列表", value = "competition:list", access = AccessLevel.PUBLIC)
    @GetMapping
    public ResponseMessage<List<CompetitionResponseDTO>> getCompetitionList(
            @Parameter(description = "返回数量限制，默认10，最大50", example = "10") @RequestParam(defaultValue = "10") int limit) {
        List<CompetitionResponseDTO> competitions = competitionService.getCompetitionResponseList(limit);
        return ResponseMessage.success(competitions);
    }

    @Operation(summary = "分页获取竞赛列表", description = "分页查询竞赛列表，按排序权重降序排列")
    @RequiresPermission(name = "分页获取竞赛列表", value = "competition:page", access = AccessLevel.PUBLIC)
    @GetMapping("/page")
    public ResponseMessage<PageDTO<CompetitionResponseDTO>> getCompetitionPage(
            @Parameter(description = "页码，从0开始，默认0", example = "0") @RequestParam(required = false) Integer page,
            @Parameter(description = "每页数量，默认10，最大50", example = "10") @RequestParam(required = false) Integer size) {
        PageDTO<CompetitionResponseDTO> result = competitionService.getCompetitionPage(page, size);
        return ResponseMessage.success(result);
    }
}
