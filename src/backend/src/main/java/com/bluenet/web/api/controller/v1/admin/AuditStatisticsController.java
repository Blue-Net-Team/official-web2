package com.bluenet.web.api.controller.v1.admin;

import com.bluenet.web.api.dto.ResponseMessage;
import com.bluenet.web.api.dto.audit.EndpointLatencyDTO;
import com.bluenet.web.api.dto.audit.EndpointRankingDTO;
import com.bluenet.web.api.dto.audit.TrendPointDTO;
import com.bluenet.web.api.converter.auditstatistics.AuditStatisticsRequestConverter;
import com.bluenet.web.api.converter.auditstatistics.AuditStatisticsResponseConverter;
import com.bluenet.web.application.service.AuditStatisticsAppService;
import com.bluenet.web.domain.model.enumerate.AuditStatisticsPeriod;
import com.bluenet.web.infrastructure.security.annotation.AccessLevel;
import com.bluenet.web.infrastructure.security.annotation.RequiresPermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 审计统计控制器
 * <p>
 * 提供 API 监控统计接口，包括请求量趋势、接口访问排名、接口响应时间排名。 需要管理员权限。
 * </p>
 */
@Tag(name = "审计统计", description = "API 监控统计接口，提供请求量趋势和接口性能排名")
@RestController
@RequestMapping("/api/v1/admin/audit/statistics")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearer-jwt")
public class AuditStatisticsController {
    private final AuditStatisticsAppService auditStatisticsAppService;
    private final AuditStatisticsRequestConverter requestConverter;
    private final AuditStatisticsResponseConverter responseConverter;

    @Operation(summary = "请求量趋势", description = "返回指定时间段内的请求量聚合数据")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "查询成功"),
            @ApiResponse(responseCode = "401", description = "未认证"),
            @ApiResponse(responseCode = "403", description = "无权限")
    })
    @RequiresPermission(name = "查看请求量趋势", value = "audit:statistics:trends", access = AccessLevel.PROTECTED)
    @GetMapping("/trends")
    public ResponseMessage<List<TrendPointDTO>> getTrends(
            @Parameter(description = "时间范围：24h、7d、30d，默认 7d") @RequestParam(defaultValue = "7d") AuditStatisticsPeriod period) {
        return ResponseMessage.success(
                responseConverter.toTrendPointDTOList(
                        auditStatisticsAppService.getTrends(requestConverter.toTrendsCommand(period))));
    }

    @Operation(summary = "接口访问排名", description = "返回按请求量排序的接口排名")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "查询成功"),
            @ApiResponse(responseCode = "401", description = "未认证"),
            @ApiResponse(responseCode = "403", description = "无权限")
    })
    @RequiresPermission(name = "查看接口访问排名", value = "audit:statistics:endpoints", access = AccessLevel.PROTECTED)
    @GetMapping("/endpoints")
    public ResponseMessage<List<EndpointRankingDTO>> getEndpointRanking(
            @Parameter(description = "时间范围：24h、7d、30d，默认 7d") @RequestParam(defaultValue = "7d") AuditStatisticsPeriod period,
            @Parameter(description = "返回条数，默认 20") @RequestParam(defaultValue = "20") int limit) {
        return ResponseMessage.success(
                responseConverter.toEndpointRankingDTOList(
                        auditStatisticsAppService
                                .getEndpointRanking(requestConverter.toEndpointRankingCommand(period, limit))));
    }

    @Operation(summary = "接口响应时间排名", description = "返回按平均响应时间排序的接口排名")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "查询成功"),
            @ApiResponse(responseCode = "401", description = "未认证"),
            @ApiResponse(responseCode = "403", description = "无权限")
    })
    @RequiresPermission(name = "查看接口响应时间排名", value = "audit:statistics:latency", access = AccessLevel.PROTECTED)
    @GetMapping("/latency")
    public ResponseMessage<List<EndpointLatencyDTO>> getEndpointLatencyRanking(
            @Parameter(description = "时间范围：24h、7d、30d，默认 7d") @RequestParam(defaultValue = "7d") AuditStatisticsPeriod period,
            @Parameter(description = "返回条数，默认 20") @RequestParam(defaultValue = "20") int limit) {
        return ResponseMessage.success(
                responseConverter.toEndpointLatencyDTOList(
                        auditStatisticsAppService.getEndpointLatencyRanking(
                                requestConverter.toEndpointLatencyRankingCommand(period, limit))));
    }
}
