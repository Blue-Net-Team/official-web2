package com.bluenet.web.api.controller.v1.admin;

import com.bluenet.web.api.dto.ResponseMessage;
import com.bluenet.web.api.dto.competition.*;
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
    private final CompetitionService competitionService;

    @Operation(summary = "创建竞赛", description = "创建新的竞赛")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "创建成功", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseMessageCompetition.class))),
            @ApiResponse(responseCode = "400", description = "参数错误", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseMessage.class))) })
    @RequiresPermission(name = "创建竞赛", value = "competition:create", access = AccessLevel.PROTECTED)
    @PostMapping
    public ResponseMessage<CompetitionBriefDTO> createCompetition(
            @Valid @RequestBody CreateCompetitionRequestDTO request) {
        try {
            CompetitionBriefDTO created = competitionService.createCompetition(request);
            return ResponseMessage.success(created);
        } catch (IllegalArgumentException e) {
            return ResponseMessage.error(400, e.getMessage());
        }
    }

    @Operation(summary = "更新竞赛", description = "更新竞赛信息")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "更新成功", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseMessageCompetition.class))),
            @ApiResponse(responseCode = "404", description = "竞赛不存在", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseMessage.class), examples = @ExampleObject(value = "{\"code\":404,\"msg\":\"竞赛不存在\",\"data\":null}"))) })
    @RequiresPermission(name = "更新竞赛", value = "competition:update", access = AccessLevel.PROTECTED)
    @PutMapping("/{id}")
    public ResponseMessage<CompetitionBriefDTO> updateCompetition(
            @Parameter(description = "竞赛ID", required = true) @PathVariable Long id,
            @Valid @RequestBody UpdateCompetitionRequestDTO request) {
        try {
            CompetitionBriefDTO updated = competitionService.updateCompetition(id, request);
            return ResponseMessage.success(updated);
        } catch (IllegalArgumentException e) {
            return ResponseMessage.error(404, e.getMessage());
        }
    }

    @Operation(summary = "删除竞赛", description = "删除竞赛及其关联的照片")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "删除成功", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseMessage.class))),
            @ApiResponse(responseCode = "404", description = "竞赛不存在", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseMessage.class))) })
    @RequiresPermission(name = "删除竞赛", value = "competition:delete", access = AccessLevel.PROTECTED)
    @DeleteMapping("/{id}")
    public ResponseMessage<Void> deleteCompetition(
            @Parameter(description = "竞赛ID", required = true) @PathVariable Long id) {
        try {
            competitionService.deleteCompetition(id);
            return ResponseMessage.success(null);
        } catch (IllegalArgumentException e) {
            return ResponseMessage.error(404, e.getMessage());
        }
    }

    @Operation(summary = "调整竞赛排序", description = "调整竞赛的排序权重")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "调整成功", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseMessage.class))),
            @ApiResponse(responseCode = "404", description = "竞赛不存在", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseMessage.class))) })
    @RequiresPermission(name = "调整竞赛排序", value = "competition:sort", access = AccessLevel.PROTECTED)
    @PutMapping("/{id}/sort")
    public ResponseMessage<Void> updateSortOrder(
            @Parameter(description = "竞赛ID", required = true) @PathVariable Long id,
            @Valid @RequestBody UpdateSortOrderRequestDTO request) {
        try {
            competitionService.updateSortOrder(id, request);
            return ResponseMessage.success(null);
        } catch (IllegalArgumentException e) {
            return ResponseMessage.error(404, e.getMessage());
        }
    }

    @Operation(summary = "添加竞赛照片", description = "为竞赛添加照片")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "添加成功", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseMessageCompetitionImage.class))),
            @ApiResponse(responseCode = "400", description = "参数错误或超过限制", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseMessage.class), examples = @ExampleObject(value = "{\"code\":400,\"msg\":\"每个竞赛最多关联20张照片\",\"data\":null}"))),
            @ApiResponse(responseCode = "404", description = "竞赛不存在", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseMessage.class))) })
    @RequiresPermission(name = "添加竞赛照片", value = "competition:image:add", access = AccessLevel.PROTECTED)
    @PostMapping("/{id}/images")
    public ResponseMessage<CompetitionImageDTO> addCompetitionImage(
            @Parameter(description = "竞赛ID", required = true) @PathVariable Long id,
            @Valid @RequestBody AddCompetitionImageRequestDTO request) {
        try {
            CompetitionImageDTO image = competitionService.addCompetitionImage(id, request);
            return ResponseMessage.success(image);
        } catch (IllegalArgumentException e) {
            if (e.getMessage().contains("不存在")) {
                return ResponseMessage.error(404, e.getMessage());
            }
            return ResponseMessage.error(400, e.getMessage());
        }
    }

    @Operation(summary = "删除竞赛照片", description = "删除竞赛的指定照片")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "删除成功", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseMessage.class))),
            @ApiResponse(responseCode = "404", description = "竞赛或图片不存在", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseMessage.class))) })
    @RequiresPermission(name = "删除竞赛照片", value = "competition:image:delete", access = AccessLevel.PROTECTED)
    @DeleteMapping("/{id}/images/{imageId}")
    public ResponseMessage<Void> removeCompetitionImage(
            @Parameter(description = "竞赛ID", required = true) @PathVariable Long id,
            @Parameter(description = "图片ID", required = true) @PathVariable Long imageId) {
        try {
            competitionService.removeCompetitionImage(id, imageId);
            return ResponseMessage.success(null);
        } catch (IllegalArgumentException e) {
            return ResponseMessage.error(404, e.getMessage());
        }
    }
}
