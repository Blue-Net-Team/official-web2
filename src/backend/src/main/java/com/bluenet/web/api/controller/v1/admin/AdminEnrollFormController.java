package com.bluenet.web.api.controller.v1.admin;

import com.bluenet.web.api.dto.ResponseMessage;
import com.bluenet.web.application.command.enrollform.EnrollFormCommands;
import com.bluenet.web.application.service.EnrollFormAppService;
import com.bluenet.web.infrastructure.security.annotation.AccessLevel;
import com.bluenet.web.infrastructure.security.annotation.RequiresPermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 报名表管理接口
 * <p>
 * 管理员设置/更新与删除报名表
 * </p>
 */
@Tag(name = "报名表管理", description = "管理员管理报名表")
@RestController
@RequestMapping("/api/v1/admin/enroll-form")
@RequiredArgsConstructor
@Slf4j
public class AdminEnrollFormController {

    private final EnrollFormAppService enrollFormAppService;

    @Operation(summary = "设置或更新报名表", description = "通过已上传确认的 fileId 设置当前报名表，文件类型必须为 ENROLL_FORM；设置成功后旧报名表文件将被删除")
    @RequiresPermission(name = "设置报名表", value = "admin:enroll-form:update", access = AccessLevel.PROTECTED)
    @SecurityRequirement(name = "cookie-auth")
    @PostMapping
    public ResponseMessage<Void> setEnrollForm(
            @Parameter(description = "文件ID", required = true) @RequestParam("fileId") Long fileId) {
        enrollFormAppService.setEnrollForm(new EnrollFormCommands.SetEnrollFormCommand(fileId));
        return ResponseMessage.success();
    }

    @Operation(summary = "删除报名表", description = "删除当前报名表（数据库记录与对象存储对象一并删除）")
    @RequiresPermission(name = "删除报名表", value = "admin:enroll-form:delete", access = AccessLevel.PROTECTED)
    @SecurityRequirement(name = "cookie-auth")
    @DeleteMapping
    public ResponseMessage<Void> deleteEnrollForm() {
        enrollFormAppService.deleteEnrollForm();
        return ResponseMessage.success();
    }
}
