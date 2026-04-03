package com.bluenet.web.api.controller.v1;

import com.bluenet.web.api.dto.PageDTO;
import com.bluenet.web.api.dto.ResponseMessage;
import com.bluenet.web.api.dto.assessment_time.AssessmentTimeDTO;
import com.bluenet.web.api.dto.assessment_time.ResponseMessageAssessmentTimeList;
import com.bluenet.web.application.service.AssessmentTimeService;
import com.bluenet.web.infrastructure.security.annotation.AccessLevel;
import com.bluenet.web.infrastructure.security.annotation.RequiresPermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 考核时间查询控制器
 * <p>
 * 提供已登录用户查询考核时间接口
 * </p>
 */
@Tag(name = "考核时间查询", description = "考核时间查询接口，已登录用户可访问")
@RestController
@RequestMapping("/api/v1/assessment-times")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearer-jwt")
public class AssessmentTimeController {
    private final AssessmentTimeService assessmentTimeService;

    @Operation(summary = "查询考核时间列表", description = "分页查询当前用户可见的考核时间。考生只能看到自己方向和年级的考核时间，成员可以看到自己方向的全部考核时间，方向管理员及以上可以查看所有考核时间。")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "查询成功", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseMessageAssessmentTimeList.class)))
    })
    @RequiresPermission(name = "查询考核时间", value = "assessment-time:query", access = AccessLevel.AUTHENTICATED)
    @GetMapping
    public ResponseMessage<PageDTO<AssessmentTimeDTO>> listAssessmentTimes(
            @Parameter(description = "页码（从0开始，默认0）") @RequestParam(required = false, defaultValue = "0") Integer page,
            @Parameter(description = "每页大小（默认5）") @RequestParam(required = false, defaultValue = "5") Integer size) {
        PageDTO<AssessmentTimeDTO> result = assessmentTimeService.listAssessmentTimesForUser(page, size);
        return ResponseMessage.success(result);
    }
}
