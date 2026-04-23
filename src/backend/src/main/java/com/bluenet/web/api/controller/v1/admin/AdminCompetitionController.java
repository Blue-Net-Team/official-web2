package com.bluenet.web.api.controller.v1.admin;

import com.bluenet.web.api.dto.ResponseMessage;
import com.bluenet.web.api.dto.competition.*;
import com.bluenet.web.api.converter.competition.CompetitionRequestConverter;
import com.bluenet.web.application.CompetitionResult;
import com.bluenet.web.application.service.CompetitionAppService;
import com.bluenet.web.infrastructure.security.annotation.AccessLevel;
import com.bluenet.web.infrastructure.security.annotation.RequiresPermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "竞赛管理", description = "竞赛管理接口，需要管理员权限")
@RestController
@RequestMapping("/api/v1/admin/competitions")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearer-jwt")
public class AdminCompetitionController {
    private final CompetitionAppService competitionAppService;
    private final CompetitionRequestConverter competitionRequestConverter;

    @Operation(summary = "创建竞赛", description = "创建新的竞赛")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "创建成功", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseMessageCompetitionResponse.class))),
            @ApiResponse(responseCode = "400", description = "参数错误", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseMessage.class))) })
    @RequiresPermission(name = "创建竞赛", value = "competition:create", access = AccessLevel.PROTECTED)
    @PostMapping
    public ResponseMessage<CompetitionResponseDTO> createCompetition(
            @Valid @RequestBody CompetitionRequestDTO request) {
        try {
            CompetitionResult result = competitionAppService.createCompetition(
                    competitionRequestConverter.toCreateCommand(request));
            return ResponseMessage.success(toResponseDTO(result));
        } catch (IllegalArgumentException e) {
            return ResponseMessage.error(400, e.getMessage());
        }
    }

    @Operation(summary = "更新竞赛", description = "更新竞赛信息")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "更新成功", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseMessageCompetitionResponse.class))),
            @ApiResponse(responseCode = "404", description = "竞赛不存在", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseMessage.class), examples = @ExampleObject(value = "{\"code\":404,\"msg\":\"竞赛不存在\",\"data\":null}"))) })
    @RequiresPermission(name = "更新竞赛", value = "competition:update", access = AccessLevel.PROTECTED)
    @PutMapping("/{id}")
    public ResponseMessage<CompetitionResponseDTO> updateCompetition(
            @Parameter(description = "竞赛ID", required = true) @PathVariable Long id,
            @Valid @RequestBody CompetitionRequestDTO request) {
        try {
            CompetitionResult result = competitionAppService.updateCompetition(
                    competitionRequestConverter.toUpdateCommand(id, request));
            return ResponseMessage.success(toResponseDTO(result));
        } catch (IllegalArgumentException e) {
            return ResponseMessage.error(404, e.getMessage());
        }
    }

    @Operation(summary = "删除竞赛", description = "删除竞赛")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "删除成功", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseMessage.class))),
            @ApiResponse(responseCode = "404", description = "竞赛不存在", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseMessage.class))) })
    @RequiresPermission(name = "删除竞赛", value = "competition:delete", access = AccessLevel.PROTECTED)
    @DeleteMapping("/{id}")
    public ResponseMessage<Void> deleteCompetition(
            @Parameter(description = "竞赛ID", required = true) @PathVariable Long id) {
        try {
            competitionAppService.deleteCompetition(id);
            return ResponseMessage.success(null);
        } catch (IllegalArgumentException e) {
            return ResponseMessage.error(404, e.getMessage());
        }
    }

    @Operation(summary = "批量调整竞赛排序", description = "批量更新竞赛的排序号，数值越小越靠前")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "调整成功", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseMessage.class))),
            @ApiResponse(responseCode = "404", description = "竞赛不存在", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseMessage.class))) })
    @RequiresPermission(name = "调整竞赛排序", value = "competition:sort", access = AccessLevel.PROTECTED)
    @PutMapping("/sort")
    public ResponseMessage<Void> batchUpdateSortOrder(
            @Valid @RequestBody BatchSortRequestDTO request) {
        try {
            competitionAppService.batchUpdateSortOrder(request);
            return ResponseMessage.success(null);
        } catch (IllegalArgumentException e) {
            return ResponseMessage.error(404, e.getMessage());
        }
    }

    @Operation(summary = "移动竞赛排序", description = "将竞赛上移或下移一位，与相邻竞赛交换排序号")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "移动成功", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseMessage.class))),
            @ApiResponse(responseCode = "400", description = "已在最前/最后或参数错误", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseMessage.class))),
            @ApiResponse(responseCode = "404", description = "竞赛不存在", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseMessage.class))) })
    @RequiresPermission(name = "调整竞赛排序", value = "competition:move", access = AccessLevel.PROTECTED)
    @PutMapping("/{id}/move")
    public ResponseMessage<Void> moveCompetition(
            @Parameter(description = "竞赛ID", required = true) @PathVariable Long id,
            @Valid @RequestBody MoveCompetitionRequestDTO request) {
        try {
            competitionAppService.moveCompetition(competitionRequestConverter.toCommand(id, request));
            return ResponseMessage.success(null);
        } catch (IllegalArgumentException e) {
            String msg = e.getMessage();
            if ("竞赛不存在".equals(msg)) {
                return ResponseMessage.error(404, msg);
            }
            return ResponseMessage.error(400, msg);
        }
    }

    private CompetitionResponseDTO toResponseDTO(CompetitionResult result) {
        return CompetitionResponseDTO.builder()
                .id(result.id())
                .name(result.name())
                .shortName(result.shortName())
                .level(result.level())
                .month(result.month())
                .organizer(result.organizer())
                .summary(result.summary())
                .logoFileId(result.logoFileId())
                .coverFileId(result.coverFileId())
                .sortOrder(result.sortOrder())
                .build();
    }
}
