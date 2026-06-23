package com.bluenet.web.api.controller.v1.wps;

import com.bluenet.web.api.dto.ResponseMessage;
import com.bluenet.web.api.dto.wps.WpsBindCallbackRequestDTO;
import com.bluenet.web.api.dto.wps.WpsCallbackRequestDTO;
import com.bluenet.web.api.dto.wps.WpsCreateAnswerCallbackRequestDTO;
import com.bluenet.web.api.dto.wps.WpsProbeCallbackRequestDTO;
import com.bluenet.web.api.dto.wps.WpsResponseMessage;
import com.bluenet.web.application.service.WpsFormAppService;
import com.bluenet.web.domain.exception.Unauthorized;
import com.bluenet.web.domain.model.vo.wps.WpsFormField;
import com.bluenet.web.infrastructure.config.properties.WpsProperties;
import com.bluenet.web.infrastructure.security.annotation.AccessLevel;
import com.bluenet.web.infrastructure.security.annotation.RateLimit;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * WPS 智能表单回调控制器。
 * <p>
 * 接收 WPS 表单的数据推送事件（bind / create_answer）， 将表单提交数据解析为系统用户并自动创建账号。
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
    private final WpsProperties wpsProperties;

    @Operation(summary = "WPS表单回调", description = "接收WPS表单数据推送事件（bind/create_answer），bind返回验证码，create_answer自动创建用户")
    @RequiresPermission(value = "wps:callback", name = "WPS表单回调", access = AccessLevel.PUBLIC, audit = true)
    @RateLimit(interval = 1)  // 每秒最多 1 次（WPS 重试频繁）
    @PostMapping(value = "/callback", produces = MediaType.APPLICATION_JSON_VALUE)
    public Object handleCallback(
            @RequestParam(value = "bind_code", required = false) String bindCodeFromQuery,
            @RequestBody WpsCallbackRequestDTO request,
            HttpServletRequest httpRequest) {

        // 1. bind_code 来自查询参数（?bind_code=xxx）
        if (StringUtils.hasText(bindCodeFromQuery)) {
            log.info("WPS 表单绑定验证(查询参数): bind_code={}", bindCodeFromQuery);
            return new WpsResponseMessage(bindCodeFromQuery);
        }

        // 2. bind_code 来自请求体（WPS 绑定验证会 POST: {"bind_code":"..."}），无 event 字段
        if (request instanceof WpsProbeCallbackRequestDTO probe && StringUtils.hasText(probe.getBindCode())) {
            log.info("WPS 表单绑定验证(请求体): bind_code={}", probe.getBindCode());
            return new WpsResponseMessage(probe.getBindCode());
        }

        // 3. 验证 API Secret（如果已配置）
        validateSecret(httpRequest);

        // 4. 按事件类型分发
        return switch (request) {
            case WpsBindCallbackRequestDTO bind -> handleBind(bind);
            case WpsCreateAnswerCallbackRequestDTO create -> {
                handleCreateAnswer(create);
                yield ResponseMessage.success();
            }
            case null, default -> {
                log.warn("WPS 回调收到无效请求（无 event 字段，未配置 bind_code）");
                yield ResponseMessage.error(400, "无效的 WPS 回调请求");
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

    private WpsResponseMessage handleBind(WpsBindCallbackRequestDTO request) {
        String bindCode = StringUtils.hasText(wpsProperties.getBindCode())
                ? wpsProperties.getBindCode()
                : request.getRid();
        log.info("WPS 表单绑定验证(event=bind): bind_code={}", bindCode);
        return new WpsResponseMessage(bindCode);
    }

    /**
     * 将 WPS 返回值转为字符串，处理字符串和数组两种类型（如多选返回 ["a","b"]）。
     */
    private static String valueToString(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof String s) {
            return s;
        }
        if (value instanceof List<?> list) {
            return list.stream().map(Object::toString).collect(Collectors.joining(","));
        }
        return value.toString();
    }

    /**
     * 处理 create_answer 事件：解析表单字段并创建用户。
     */
    private void handleCreateAnswer(WpsCreateAnswerCallbackRequestDTO request) {
        List<WpsCreateAnswerCallbackRequestDTO.AnswerContent> answers = request.getAnswerContents();
        if (answers == null || answers.isEmpty()) {
            log.warn("WPS 表单 create_answer 无回答内容，跳过");
            return;
        }

        Map<String, String> fieldMap = answers.stream()
                .filter(a -> a.getTitle() != null && a.getValue() != null)
                .collect(
                        Collectors.toMap(
                                WpsCreateAnswerCallbackRequestDTO.AnswerContent::getTitle,
                                a -> valueToString(a.getValue()),
                                (a, b) -> a));

        String studentId = fieldMap.get(WpsFormField.STUDENT_ID);
        String username = fieldMap.get(WpsFormField.USERNAME);
        String email = fieldMap.get(WpsFormField.EMAIL);
        String directionText = fieldMap.get(WpsFormField.DIRECTION);
        String major = fieldMap.get(WpsFormField.MAJOR);
        String collegeText = fieldMap.get(WpsFormField.COLLEGE);
        String genderText = fieldMap.get(WpsFormField.GENDER);

        if (!StringUtils.hasText(studentId)) {
            log.warn("WPS 表单缺少必填字段: {}", WpsFormField.STUDENT_ID);
            return;
        }
        if (!StringUtils.hasText(username)) {
            log.warn("WPS 表单缺少必填字段: {}", WpsFormField.USERNAME);
            return;
        }
        if (!StringUtils.hasText(email)) {
            log.warn("WPS 表单缺少必填字段: {}", WpsFormField.EMAIL);
            return;
        }
        if (!StringUtils.hasText(directionText)) {
            log.warn("WPS 表单缺少必填字段: {}", WpsFormField.DIRECTION);
            return;
        }

        wpsFormAppService.createUserFromWpsForm(
                studentId,
                username,
                email,
                directionText,
                major,
                collegeText,
                genderText);
        log.info("WPS 表单创建用户成功: studentId={}, username={}, email={}", studentId, username, email);
    }
}
