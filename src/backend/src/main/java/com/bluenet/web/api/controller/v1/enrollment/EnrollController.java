package com.bluenet.web.api.controller.v1.enrollment;

import com.bluenet.web.api.dto.ResponseMessage;
import com.bluenet.web.api.dto.enrollment.CreateEnrollmentRequestDTO;
import com.bluenet.web.api.dto.enrollment.EnrollmentConflictDTO;
import com.bluenet.web.api.dto.enrollment.EnrollmentResultDTO;
import com.bluenet.web.api.converter.enroll.EnrollRequestConverter;
import com.bluenet.web.api.converter.enroll.EnrollResponseConverter;
import com.bluenet.web.application.result.enroll.EnrollResult;
import com.bluenet.web.application.service.EnrollAppService;
import com.bluenet.web.infrastructure.security.annotation.AccessLevel;
import com.bluenet.web.infrastructure.security.annotation.RequiresPermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "报名", description = "报名相关接口，公开访问")
@RestController
@RequestMapping("/api/v1/enrollments")
@RequiredArgsConstructor
public class EnrollController {
    private final EnrollAppService enrollAppService;
    private final EnrollRequestConverter enrollRequestConverter;
    private final EnrollResponseConverter enrollResponseConverter;

    @Operation(summary = "发起报名", description = "外部用户提交报名申请，无需登录。如果学号已存在且 forceUpdate 为 false，返回 409 冲突")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "报名成功", content = @Content(mediaType = "application/json", schema = @Schema(implementation = EnrollmentResultDTO.class))),
            @ApiResponse(responseCode = "200", description = "更新成功（forceUpdate=true）", content = @Content(mediaType = "application/json", schema = @Schema(implementation = EnrollmentResultDTO.class))),
            @ApiResponse(responseCode = "409", description = "学号已存在", content = @Content(mediaType = "application/json", schema = @Schema(implementation = EnrollmentConflictDTO.class))),
            @ApiResponse(responseCode = "400", description = "参数校验失败", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseMessage.class)))
    })
    @RequiresPermission(name = "发起报名", value = "enrollment:create", access = AccessLevel.PUBLIC)
    @PostMapping
    public ResponseMessage<?> createEnrollment(@Valid @RequestBody CreateEnrollmentRequestDTO request) {
        EnrollResult.Enrollment result = enrollAppService.createEnrollment(enrollRequestConverter.toCommand(request));
        EnrollmentResultDTO dto = enrollResponseConverter.toEnrollmentResultDTO(result);
        HttpStatus status = dto.isCreated() ? HttpStatus.CREATED : HttpStatus.OK;
        String message = dto.isCreated() ? "报名成功" : "报名信息已更新";
        return new ResponseMessage<>(status.value(), message, dto);
    }
}
