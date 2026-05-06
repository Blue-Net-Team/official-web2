package com.bluenet.web.api.controller.v1.achievement;

import com.bluenet.web.api.dto.PageDTO;
import com.bluenet.web.api.dto.ResponseMessage;
import com.bluenet.web.api.dto.achievement.AchievementDTO;
import com.bluenet.web.api.dto.achievement.AchievementStatsDTO;
import com.bluenet.web.api.converter.achievement.AchievementResponseConverter;
import com.bluenet.web.application.AchievementResult;
import com.bluenet.web.application.service.AchievementAppService;
import com.bluenet.web.domain.model.enumerate.AchievementType;
import com.bluenet.web.domain.model.enumerate.AwardLevel;
import com.bluenet.web.domain.model.vo.AchievementStatsVO;
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
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

@Tag(name = "成就", description = "团队成就相关接口，公开访问")
@RestController
@RequestMapping("/api/v1/achievements")
@RequiredArgsConstructor
public class AchievementController {
    private final AchievementAppService achievementAppService;
    private final AchievementResponseConverter achievementResponseConverter;

    @Operation(summary = "获取成就列表", description = "分页获取团队成就列表，支持按类型、奖项级别、年份筛选")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "成功，返回成就列表", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseMessage.class)))
    })
    @RequiresPermission(name = "获取成就列表", value = "achievement:list", access = AccessLevel.PUBLIC)
    @GetMapping
    public ResponseMessage<PageDTO<AchievementDTO>> getAchievements(
            @Parameter(description = "页码，从0开始") @RequestParam(defaultValue = "0") Integer page,
            @Parameter(description = "每页数量，默认12") @RequestParam(defaultValue = "12") Integer size,
            @Parameter(description = "成就类型，默认返回全部") @RequestParam(required = false) AchievementType type,
            @Parameter(description = "奖项级别，仅对竞赛成就有效") @RequestParam(required = false) AwardLevel awardLevel,
            @Parameter(description = "获奖年份") @RequestParam(required = false) Integer year) {
        Page<AchievementResult> resultPage = achievementAppService
                .getAchievements(page, size, type, awardLevel, year);
        PageDTO<AchievementDTO> achievements = achievementResponseConverter.toDTOPage(resultPage);
        return ResponseMessage.success(achievements);
    }

    @Operation(summary = "获取成就统计", description = "获取团队成就统计数据，包括各级别奖项数量")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "成功，返回成就统计", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseMessage.class)))
    })
    @RequiresPermission(name = "获取成就统计", value = "achievement:stats", access = AccessLevel.PUBLIC)
    @GetMapping("/stats")
    public ResponseMessage<AchievementStatsDTO> getAchievementStats() {
        AchievementStatsVO statsVO = achievementAppService.getAchievementStats();
        AchievementStatsDTO stats = achievementResponseConverter.toStatsDTO(statsVO);
        return ResponseMessage.success(stats);
    }
}
