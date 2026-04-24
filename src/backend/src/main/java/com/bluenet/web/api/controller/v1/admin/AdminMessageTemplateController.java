package com.bluenet.web.api.controller.v1.admin;

import com.bluenet.web.api.dto.ResponseMessage;
import com.bluenet.web.application.message.MessageTemplateInfo;
import com.bluenet.web.application.service.MessageTemplateAppService;
import com.bluenet.web.infrastructure.security.annotation.AccessLevel;
import com.bluenet.web.infrastructure.security.annotation.RequiresPermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 消息模板管理控制器。
 */
@Tag(name = "消息模板管理", description = "消息模板查询、编辑、启禁用和预览接口")
@RestController
@RequestMapping("/api/v1/admin/message-templates")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearer-jwt")
public class AdminMessageTemplateController {

    private final MessageTemplateAppService messageTemplateAppService;

    /**
     * 查询模板列表。
     */
    @Operation(summary = "查询模板列表", description = "返回系统中所有消息模板的元数据列表")
    @RequiresPermission(name = "查询消息模板列表", value = "message-template:list", access = AccessLevel.PROTECTED)
    @GetMapping
    public ResponseMessage<List<MessageTemplateInfo>> listTemplates() {
        return ResponseMessage.success(messageTemplateAppService.listTemplates());
    }

    /**
     * 查询模板详情。
     */
    @Operation(summary = "查询模板详情", description = "按模板编码查询详情，包含当前内容和默认内容")
    @RequiresPermission(name = "查询消息模板详情", value = "message-template:detail", access = AccessLevel.PROTECTED)
    @GetMapping("/{code}")
    public ResponseMessage<MessageTemplateInfo> getTemplate(
            @Parameter(description = "模板编码", required = true) @PathVariable String code) {
        return ResponseMessage.success(messageTemplateAppService.getTemplate(code));
    }

    /**
     * 更新模板内容。
     */
    @Operation(summary = "更新模板内容", description = "更新指定模板的 HTML 内容，会进行变量校验")
    @RequiresPermission(name = "更新消息模板", value = "message-template:update", access = AccessLevel.PROTECTED)
    @PutMapping("/{code}")
    public ResponseMessage<Void> updateTemplate(
            @Parameter(description = "模板编码", required = true) @PathVariable String code,
            @RequestBody UpdateTemplateRequest request) {
        messageTemplateAppService.updateTemplate(code, request.subject(), request.content());
        return ResponseMessage.success();
    }

    /**
     * 切换模板启禁用状态。
     */
    @Operation(summary = "切换模板启禁用", description = "启用或禁用指定模板")
    @RequiresPermission(name = "切换消息模板状态", value = "message-template:toggle", access = AccessLevel.PROTECTED)
    @PostMapping("/{code}/toggle")
    public ResponseMessage<Boolean> toggleTemplate(
            @Parameter(description = "模板编码", required = true) @PathVariable String code,
            @Parameter(description = "是否启用", required = true) @RequestParam boolean enabled) {
        messageTemplateAppService.toggleTemplate(code, enabled);
        return ResponseMessage.success(enabled);
    }

    /**
     * 预览模板渲染效果。
     */
    @Operation(summary = "预览模板", description = "使用测试变量渲染模板，返回 HTML 内容")
    @RequiresPermission(name = "预览消息模板", value = "message-template:preview", access = AccessLevel.PROTECTED)
    @PostMapping("/{code}/preview")
    public ResponseMessage<String> previewTemplate(
            @Parameter(description = "模板编码", required = true) @PathVariable String code,
            @RequestBody Map<String, String> variables) {
        String html = messageTemplateAppService.previewTemplate(code, variables);
        return ResponseMessage.success(html);
    }

    public record UpdateTemplateRequest(String subject, String content) {
    }
}
