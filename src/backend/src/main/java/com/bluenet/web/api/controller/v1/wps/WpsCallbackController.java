package com.bluenet.web.api.controller.v1.wps;

import com.bluenet.web.api.converter.wpsform.WpsFormRequestConverter;
import com.bluenet.web.api.dto.ResponseMessage;
import com.bluenet.web.api.dto.wps.WpsBindCallbackRequestDTO;
import com.bluenet.web.api.dto.wps.WpsCreateAnswerCallbackRequestDTO;
import com.bluenet.web.api.dto.wps.WpsProbeCallbackRequestDTO;
import com.bluenet.web.api.dto.wps.WpsResponseMessage;
import com.bluenet.web.application.command.wpsform.WpsFormCommands;
import com.bluenet.web.application.service.WpsFormAppService;
import com.bluenet.web.domain.exception.Unauthorized;
import com.bluenet.web.infrastructure.config.properties.WpsProperties;
import com.bluenet.web.infrastructure.security.annotation.AccessLevel;
import com.bluenet.web.infrastructure.security.annotation.RequiresPermission;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * WPS 智能表单回调控制器。
 * <p>
 * 接收 WPS 表单的数据推送事件（bind / create_answer），将表单提交数据解析为系统用户并自动创建账号。
 * </p>
 */
@Tag(name = "WPS表单回调", description = "WPS智能表单数据推送回调接口，公开访问")
@RestController
@RequestMapping("/api/v1/wps")
@RequiredArgsConstructor
@Slf4j
public class WpsCallbackController {

    private static final String SECRET_HEADER = "X-WPS-Secret";

    private final WpsFormAppService wpsFormAppService;
    private final WpsFormRequestConverter wpsFormRequestConverter;
    private final WpsProperties wpsProperties;
    private final ObjectMapper objectMapper;

    @Operation(summary = "WPS表单回调", description = "接收WPS表单数据推送事件（bind/create_answer），bind返回验证码，create_answer自动创建用户")
    @RequiresPermission(value = "wps:callback", name = "WPS表单回调", access = AccessLevel.PUBLIC, audit = true)
    @PostMapping(value = "/callback", produces = MediaType.APPLICATION_JSON_VALUE)
    public Object handleCallback(
            @RequestParam(value = "bind_code", required = false) String bindCodeFromQuery,
            HttpServletRequest httpRequest) {

        // 1. bind_code 来自查询参数（?bind_code=xxx）——WPS 可能用 GET 或 POST 无 body 发送
        if (StringUtils.hasText(bindCodeFromQuery)) {
            log.info("WPS 表单绑定验证(查询参数): bind_code={}", bindCodeFromQuery);
            return new WpsResponseMessage(bindCodeFromQuery);
        }

        // 2. 直接读取原始请求体
        String rawBody;
        try {
            rawBody = httpRequest.getReader().lines().collect(java.util.stream.Collectors.joining(System.lineSeparator()));
        } catch (Exception e) {
            log.warn("WPS 回调请求体读取失败: {}", e.getMessage());
            return ResponseMessage.error(400, "读取请求体失败");
        }
        if (!StringUtils.hasText(rawBody)) {
            log.warn("WPS 回调请求体为空");
            return ResponseMessage.error(400, "无效的 WPS 回调请求");
        }

        JsonNode bodyNode;
        try {
            bodyNode = objectMapper.readTree(rawBody);
        } catch (Exception e) {
            log.warn("WPS 回调请求体 JSON 解析失败: {} -> rawBody=[{}]", e.getMessage(), rawBody);
            return ResponseMessage.error(400, "无效的请求体 JSON");
        }

        // 2. bind_code 来自请求体（WPS 绑定验证会 POST: {"bind_code":"..."}），无 event 字段
        if (bodyNode.has("bind_code") && !bodyNode.has("event")) {
            String bindCode = bodyNode.get("bind_code").asText();
            if (StringUtils.hasText(bindCode)) {
                log.info("WPS 表单绑定验证(请求体): bind_code={}", bindCode);
                return new WpsResponseMessage(bindCode);
            }
        }

        // 3. 验证 API Secret（如果已配置）
        validateSecret(httpRequest);

        // 4. 没有 event 字段 → 无效请求
        if (!bodyNode.has("event")) {
            log.warn("WPS 回调请求缺少 event 字段");
            return ResponseMessage.error(400, "无效的 WPS 回调请求");
        }

        String event = bodyNode.get("event").asText();
        return switch (event) {
            case "bind" -> {
                WpsBindCallbackRequestDTO bind;
                try {
                    bind = objectMapper.treeToValue(bodyNode, WpsBindCallbackRequestDTO.class);
                } catch (Exception e) {
                    log.warn("WPS bind 事件解析失败: {}", e.getMessage());
                    yield ResponseMessage.error(400, "无效的 bind 请求体");
                }
                yield new WpsResponseMessage(wpsFormAppService.resolveBindCode(bind.getRid()));
            }
            case "create_answer" -> {
                WpsCreateAnswerCallbackRequestDTO create;
                try {
                    create = objectMapper.treeToValue(bodyNode, WpsCreateAnswerCallbackRequestDTO.class);
                } catch (Exception e) {
                    log.warn("WPS create_answer 事件解析失败: {}", e.getMessage());
                    yield ResponseMessage.error(400, "无效的 create_answer 请求体");
                }
                yield handleCreateAnswer(create);
            }
            default -> {
                log.info("忽略 WPS 表单非目标事件类型: {}", event);
                yield ResponseMessage.success();
            }
        };
    }

    private void validateSecret(HttpServletRequest httpRequest) {
        String secret = wpsProperties.getWebhook().getSecret();
        if (!StringUtils.hasText(secret)) {
            return;
        }
        String providedSecret = httpRequest.getHeader(SECRET_HEADER);
        if (!secret.equals(providedSecret)) {
            log.warn("WPS 回调 Secret 验证失败");
            throw new Unauthorized("WPS 回调 Secret 验证失败");
        }
    }

    private ResponseMessage<Void> handleCreateAnswer(WpsCreateAnswerCallbackRequestDTO request) {
        WpsFormCommands.CreateUserFromWpsFormCommand command = wpsFormRequestConverter.toCreateUserCommand(request);
        wpsFormAppService.createUserFromWpsForm(command);
        log.info("WPS 表单创建用户请求已处理: rid={}", request.getRid());
        return ResponseMessage.success();
    }
}
