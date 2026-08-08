package com.bluenet.web.api.controller.v1.enrollform;

import com.bluenet.web.api.converter.enrollform.EnrollFormResponseConverter;
import com.bluenet.web.api.dto.ResponseMessage;
import com.bluenet.web.api.dto.enrollform.EnrollFormDTO;
import com.bluenet.web.application.result.enrollform.EnrollFormResult;
import com.bluenet.web.application.service.EnrollFormAppService;
import com.bluenet.web.infrastructure.security.annotation.AccessLevel;
import com.bluenet.web.infrastructure.security.annotation.RequiresPermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

/**
 * 报名表公开接口控制器
 * <p>
 * 提供报名表查询的公开接口，无需登录即可访问
 * </p>
 */
@Tag(name = "报名表", description = "报名表公开接口，无需登录即可访问")
@RestController
@RequestMapping("/api/v1/enroll-form")
@RequiredArgsConstructor
public class EnrollFormController {

    private final EnrollFormAppService enrollFormAppService;
    private final EnrollFormResponseConverter enrollFormResponseConverter;

    @Operation(summary = "获取当前报名表", description = "返回当前报名表的文件ID与上传时间，无报名表时 data 为 null")
    @RequiresPermission(name = "获取当前报名表", value = "enroll-form:read", access = AccessLevel.PUBLIC)
    @GetMapping
    public ResponseMessage<EnrollFormDTO> getCurrentEnrollForm() {
        Optional<EnrollFormResult> result = enrollFormAppService.getCurrentEnrollForm();
        EnrollFormDTO dto = result.map(enrollFormResponseConverter::toDTO).orElse(null);
        return ResponseMessage.success(dto);
    }
}
