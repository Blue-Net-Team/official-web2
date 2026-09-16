package com.bluenet.web.api.controller.v1.admin;

import com.bluenet.web.api.converter.aitrace.AiTraceResponseConverter;
import com.bluenet.web.api.dto.PageDTO;
import com.bluenet.web.api.dto.ResponseMessage;
import com.bluenet.web.api.dto.aitrace.AiConversationDetailDTO;
import com.bluenet.web.api.dto.aitrace.AiConversationSummaryDTO;
import com.bluenet.web.api.dto.aitrace.AiTraceStatisticsDTO;
import com.bluenet.web.api.dto.aitrace.AiTurnSummaryDTO;
import com.bluenet.web.application.result.aitrace.AiConversationDetail;
import com.bluenet.web.application.query.aitrace.AiTraceGapQuery;
import com.bluenet.web.application.query.aitrace.AiTraceListQuery;
import com.bluenet.web.application.query.aitrace.AiTraceStatisticsQuery;
import com.bluenet.web.application.service.AiTraceQueryService;
import com.bluenet.web.domain.model.enumerate.AiTraceStatisticsPeriod;
import com.bluenet.web.infrastructure.security.annotation.AccessLevel;
import com.bluenet.web.infrastructure.security.annotation.RequiresPermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

/**
 * AI 对话轨迹后台控制器。
 *
 * <p>
 * 提供用户提问、检索过程与作答依据的只读回看能力。 全部接口为 GET，不提供任何写入、修改或删除轨迹的能力：轨迹由 ai-service 采集写入。
 * </p>
 */
@Tag(name = "AI 对话轨迹", description = "AI 对话会话、轨迹详情与统计的只读查询接口")
@RestController
@RequestMapping("/api/v1/admin/ai-traces")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearer-jwt")
public class AdminAiTraceController {

    private final AiTraceQueryService aiTraceQueryService;
    private final AiTraceResponseConverter responseConverter;

    @Operation(summary = "查询会话列表", description = "按会话组织的分页列表，内嵌各会话的提问摘要")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "查询成功"),
            @ApiResponse(responseCode = "401", description = "未认证"),
            @ApiResponse(responseCode = "403", description = "无权限")
    })
    @RequiresPermission(name = "查询 AI 对话会话列表", value = "ai-trace:list", access = AccessLevel.PROTECTED)
    @GetMapping("/conversations")
    public ResponseMessage<PageDTO<AiConversationSummaryDTO>> listConversations(
            @Parameter(description = "意图筛选") @RequestParam(required = false) String intent,
            @Parameter(description = "动作筛选：RETRIEVE / REFUSE / DIRECT") @RequestParam(required = false) String action,
            @Parameter(description = "用户提问关键词") @RequestParam(required = false) String keyword,
            @Parameter(description = "最后活跃时间下界（ISO-8601）") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @Parameter(description = "最后活跃时间上界（ISO-8601）") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime,
            @PageableDefault(size = 20) Pageable pageable) {
        AiTraceListQuery query = new AiTraceListQuery(intent, action, keyword, startTime, endTime, pageable);
        return ResponseMessage.success(
                responseConverter.toConversationPageDTO(
                        aiTraceQueryService.listConversations(query)));
    }

    @Operation(summary = "查询会话详情", description = "返回会话全部提问的完整原始事件序列与 prompt 快照")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "查询成功；会话不存在时 data 为 null"),
            @ApiResponse(responseCode = "401", description = "未认证"),
            @ApiResponse(responseCode = "403", description = "无权限")
    })
    @RequiresPermission(name = "查询 AI 对话会话详情", value = "ai-trace:detail", access = AccessLevel.PROTECTED)
    @GetMapping("/conversations/{id}")
    public ResponseMessage<AiConversationDetailDTO> getConversationDetail(
            @Parameter(description = "会话标识", required = true) @PathVariable String id) {
        AiConversationDetail detail = aiTraceQueryService.getConversationDetail(id);
        if (detail == null) {
            return ResponseMessage.success(null);
        }
        return ResponseMessage.success(responseConverter.toDetailDTO(detail));
    }

    @Operation(summary = "统计聚合", description = "概览指标、会话量趋势与各类分布；趋势按会话聚合，分布按提问聚合")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "查询成功；区间内无数据时返回零值与空列表"),
            @ApiResponse(responseCode = "401", description = "未认证"),
            @ApiResponse(responseCode = "403", description = "无权限")
    })
    @RequiresPermission(name = "查询 AI 对话统计", value = "ai-trace:statistics", access = AccessLevel.PROTECTED)
    @GetMapping("/statistics")
    public ResponseMessage<AiTraceStatisticsDTO> getStatistics(
            @Parameter(description = "统计周期：24h、7d、30d，默认 7d") @RequestParam(defaultValue = "7d") AiTraceStatisticsPeriod period) {
        return ResponseMessage.success(
                responseConverter.toStatisticsDTO(
                        aiTraceQueryService.getStatistics(new AiTraceStatisticsQuery(period))));
    }

    @Operation(summary = "查询缺口提问", description = "按缺口类型筛选需要补文档的提问：未听懂 / 被拒答 / 标签未覆盖")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "查询成功；无数据时返回空分页"),
            @ApiResponse(responseCode = "401", description = "未认证"),
            @ApiResponse(responseCode = "403", description = "无权限")
    })
    @RequiresPermission(name = "查询 AI 对话缺口", value = "ai-trace:gap", access = AccessLevel.PROTECTED)
    @GetMapping("/gaps")
    public ResponseMessage<PageDTO<AiTurnSummaryDTO>> listGapTurns(
            @Parameter(description = "缺口类型：CLARIFY、REFUSE、FALLBACK", required = true) @RequestParam String gapType,
            @PageableDefault(size = 20) Pageable pageable) {
        AiTraceGapQuery query = new AiTraceGapQuery(gapType, pageable);
        return ResponseMessage.success(
                responseConverter.toTurnSummaryPageDTO(
                        aiTraceQueryService.listGapTurns(query)));
    }
}
