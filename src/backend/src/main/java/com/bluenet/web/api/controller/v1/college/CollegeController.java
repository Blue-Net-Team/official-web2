package com.bluenet.web.api.controller.v1.college;

import com.bluenet.web.api.dto.ResponseMessage;
import com.bluenet.web.api.dto.college.CollegeDTO;
import com.bluenet.web.api.dto.college.ResponseMessageCollegeList;
import com.bluenet.web.application.result.college.CollegeResult;
import com.bluenet.web.api.converter.college.CollegeResponseConverter;
import com.bluenet.web.application.service.CollegeAppService;
import com.bluenet.web.infrastructure.security.annotation.AccessLevel;
import com.bluenet.web.infrastructure.security.annotation.RequiresPermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 学院公开接口控制器
 * <p>
 * 提供学院相关的公开接口，无需登录即可访问
 * </p>
 */
@Tag(name = "学院信息", description = "学院公开接口，无需登录即可访问")
@RestController
@RequestMapping("/api/v1/colleges")
@RequiredArgsConstructor
public class CollegeController {
    private final CollegeAppService collegeAppService;
    private final CollegeResponseConverter collegeResponseConverter;

    @Operation(summary = "获取学院列表", description = "获取所有学院列表，公开接口")
    @ApiResponse(responseCode = "200", description = "获取成功", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseMessageCollegeList.class)))
    @RequiresPermission(name = "获取学院列表", value = "college:list", access = AccessLevel.PUBLIC)
    @GetMapping
    public ResponseMessage<List<CollegeDTO>> getAllColleges() {
        List<CollegeResult> results = collegeAppService.getAllColleges();
        return ResponseMessage.success(collegeResponseConverter.toDTOList(results));
    }
}
