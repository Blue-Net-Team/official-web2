package com.bluenet.web.api.controller.v1.wps;

import com.bluenet.web.api.converter.wpsform.WpsFormRequestConverter;
import com.bluenet.web.api.dto.wps.WpsBindCallbackRequestDTO;
import com.bluenet.web.api.dto.wps.WpsCallbackRequestDTO;
import com.bluenet.web.api.dto.wps.WpsCreateAnswerCallbackRequestDTO;
import com.bluenet.web.api.dto.wps.WpsBindResponseDTO;
import com.bluenet.web.application.command.wpsform.WpsFormCommands;
import com.bluenet.web.application.service.WpsFormAppService;
import com.bluenet.web.domain.exception.BadRequest;
import com.bluenet.web.domain.exception.Unauthorized;
import com.bluenet.web.infrastructure.config.properties.WpsProperties;
import com.bluenet.web.infrastructure.security.annotation.AccessLevel;
import com.bluenet.web.infrastructure.security.annotation.RequiresPermission;
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
import org.springframework.web.bind.annotation.RestController;

/**
 * WPS 智能表单回调控制器。
 * <p>
 * 接收 WPS 表单的绑定验证请求（?bind_code=xxx 或 event=bind）与数据推送事件（create_answer），
 * 将表单提交数据解析为系统用户并自动创建账号。
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

    @Operation(summary = "WPS表单回调", description = "接收WPS表单绑定验证(?bind_code=xxx/event=bind)与数据推送事件(create_answer)，create_answer自动创建用户")
    @RequiresPermission(value = "wps:callback", name = "WPS表单回调", access = AccessLevel.PUBLIC, audit = true)
    @PostMapping(value = "/callback", produces = MediaType.APPLICATION_JSON_VALUE)
    public WpsBindResponseDTO handleCallback(
            @RequestBody() WpsCallbackRequestDTO request,
            HttpServletRequest httpRequest) {

        // 1. request 中没有表单id说明是绑定
        if (request.getFormId().isBlank() || request instanceof WpsBindCallbackRequestDTO) {
            return new WpsBindResponseDTO(wpsFormAppService.resolveBindCode(request.getRid()));
        }

        // 4. 数据推送事件需要校验 API Secret（如果已配置）
        validateSecret(httpRequest);

        // 5. 按事件类型分发
        if (request instanceof WpsCreateAnswerCallbackRequestDTO create) {
            return handleCreateAnswer(create);
        }

        throw new BadRequest("无效的 WPS 回调请求");
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

    private WpsBindResponseDTO handleCreateAnswer(WpsCreateAnswerCallbackRequestDTO request) {
        WpsFormCommands.CreateUserFromWpsFormCommand command = wpsFormRequestConverter.toCreateUserCommand(request);
        wpsFormAppService.createUserFromWpsForm(command);
        log.info("WPS 表单创建用户请求已处理: rid={}", request.getRid());
        return new WpsBindResponseDTO(null);
    }
}
