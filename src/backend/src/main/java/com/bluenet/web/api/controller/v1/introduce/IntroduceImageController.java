package com.bluenet.web.api.controller.v1.introduce;

import com.bluenet.web.api.dto.ResponseMessage;
import com.bluenet.web.api.dto.introduce.IntroduceImageDTO;
import com.bluenet.web.api.dto.introduce.ResponseMessageIntroduceImageList;
import com.bluenet.web.application.service.IntroduceImageService;
import com.bluenet.web.domain.model.enumerate.ImageType;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 介绍图片接口
 * <p>
 * 提供介绍图片查询功能，支持按类型筛选。
 * </p>
 */
@Tag(name = "介绍图片", description = "介绍图片相关接口，公开访问")
@RestController
@RequestMapping("/api/v1/introduce-images")
@RequiredArgsConstructor
public class IntroduceImageController {
    private final IntroduceImageService introduceImageService;

    @Operation(summary = "获取介绍图片列表", description = "根据类型获取介绍图片列表。支持类型：laboratory（实验室介绍）、equipment（设备介绍）、competition（竞赛介绍）。")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "成功，返回介绍图片列表", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseMessageIntroduceImageList.class))) })
    @RequiresPermission(name = "获取介绍图片列表", value = "introduce-image:list", access = AccessLevel.PUBLIC)
    @GetMapping
    public ResponseMessage<List<IntroduceImageDTO>> getIntroduceImages(
            @Parameter(description = "图片类型（必填）：laboratory/equipment/competition", required = true, example = "competition") @RequestParam ImageType type) {
        List<IntroduceImageDTO> images = introduceImageService.getIntroduceImages(type);
        return ResponseMessage.success(images);
    }
}
