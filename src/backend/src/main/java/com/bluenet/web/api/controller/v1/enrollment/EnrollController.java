package com.bluenet.web.api.controller.v1.enrollment;

import com.bluenet.web.api.dto.ResponseMessage;
import com.bluenet.web.api.dto.enrollment.*;
import com.bluenet.web.application.service.EnrollService;
import com.bluenet.web.domain.model.vo.EnrollVO;
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
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@Tag(name = "报名", description = "报名相关接口，公开访问")
@RestController
@RequestMapping("/api/v1/enrollments")
@RequiredArgsConstructor
public class EnrollController {
    private final EnrollService enrollService;

    @Operation(summary = "发起报名", description = "外部用户提交报名申请，无需登录。如果学号已存在且forceUpdate为false，返回409冲突")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "报名成功", content = @Content(mediaType = "application/json", schema = @Schema(implementation = EnrollmentBriefDTO.class))),
            @ApiResponse(responseCode = "200", description = "更新成功（forceUpdate=true）", content = @Content(mediaType = "application/json", schema = @Schema(implementation = EnrollmentBriefDTO.class))),
            @ApiResponse(responseCode = "409", description = "学号已存在", content = @Content(mediaType = "application/json", schema = @Schema(implementation = EnrollmentConflictDTO.class))),
            @ApiResponse(responseCode = "400", description = "参数校验失败", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseMessage.class)))
    })
    @RequiresPermission(name = "发起报名", value = "enrollment:create", access = AccessLevel.PUBLIC)
    @PostMapping
    public ResponseMessage<?> createEnrollment(@Valid @RequestBody CreateEnrollmentRequestDTO request) {
        Optional<EnrollVO> existing = enrollService.checkEnrollmentExists(request.getStudentId());

        if (existing.isPresent()) {
            if (Boolean.TRUE.equals(request.getForceUpdate())) {
                EnrollmentBriefDTO result = enrollService.updateEnrollment(request.getStudentId(), request);
                return ResponseMessage.success("报名信息已更新", result);
            } else {
                EnrollmentConflictDTO conflict = EnrollmentConflictDTO.builder()
                        .id(existing.get().getId())
                        .username(existing.get().getUsername())
                        .studentId(existing.get().getStudentId())
                        .status(existing.get().getStatus())
                        .direction(existing.get().getDirection())
                        .build();
                return new ResponseMessage<>(HttpStatus.CONFLICT.value(), "学号已存在，是否更新报名信息？", conflict);
            }
        }

        EnrollmentResultDTO result = enrollService.createEnrollment(request);
        EnrollmentBriefDTO brief = EnrollmentBriefDTO.builder()
                .id(result.getId())
                .username(result.getUsername())
                .studentId(result.getStudentId())
                .direction(result.getDirection())
                .status(result.getStatus())
                .build();
        return new ResponseMessage<>(HttpStatus.CREATED.value(), "报名成功", brief);
    }
}
