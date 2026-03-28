package com.bluenet.web.api.controller.v1.learningpath;

import com.bluenet.web.api.dto.ResponseMessage;
import com.bluenet.web.api.dto.learningpath.DirectionLearningPathDTO;
import com.bluenet.web.application.service.LearningPathService;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 学习路径公开接口Controller
 * <p>
 * 提供学习路径的公开访问接口，无需认证
 * </p>
 */
@Tag(name = "学习路径", description = "方向学习路径相关接口，公开访问")
@RestController
@RequestMapping("/api/v1/directions")
@RequiredArgsConstructor
public class LearningPathController {
    private final LearningPathService learningPathService;

    @Operation(summary = "获取方向学习路径", description = "获取指定方向的学习路径步骤列表，公开访问无需认证")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "成功，返回学习路径数据", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseMessage.class))),
            @ApiResponse(responseCode = "404", description = "方向标识无效", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseMessage.class), examples = @ExampleObject(value = "{\"code\":404,\"msg\":\"无效的方向标识: invalid\",\"data\":null}")))
    })
    @RequiresPermission(name = "获取方向学习路径", value = "direction-learning-path:view", access = AccessLevel.PUBLIC)
    @GetMapping("/{slug}/learning-path")
    public ResponseMessage<DirectionLearningPathDTO> getLearningPath(
            @Parameter(description = "方向标识（cv/embed/struct）", required = true, example = "cv") @PathVariable String slug) {
        try {
            DirectionLearningPathDTO learningPath = learningPathService.getLearningPath(slug);
            return ResponseMessage.success(learningPath);
        } catch (IllegalArgumentException e) {
            return ResponseMessage.error(404, e.getMessage());
        }
    }
}
