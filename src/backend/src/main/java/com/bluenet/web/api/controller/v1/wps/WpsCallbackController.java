package com.bluenet.web.api.controller.v1.wps;

import com.bluenet.web.api.dto.ResponseMessage;
import com.bluenet.web.api.dto.wps.WpsCallbackRequestDTO;
import com.bluenet.web.api.dto.wps.WpsCallbackResponseDTO;
import com.bluenet.web.application.command.wpsform.WpsFormCommands;
import com.bluenet.web.application.service.WpsFormAppService;
import com.bluenet.web.domain.exception.DataConflict;
import com.bluenet.web.domain.model.enumerate.Direction;
import com.bluenet.web.infrastructure.security.annotation.AccessLevel;
import com.bluenet.web.infrastructure.security.annotation.RateLimit;
import com.bluenet.web.infrastructure.security.annotation.RequiresPermission;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.bluenet.web.application.service.impl.WpsFormAppServiceImpl.resolveDirection;

/**
 * WPS 智能表单回调控制器。
 * <p>
 * 接收 WPS 表单的数据推送事件（bind / create_answer），
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
    private final ObjectMapper objectMapper;

    @Value("${wps.webhook.secret:}")
    private String wpsSecret;

    @Value("${wps.bind-code:}")
    private String wpsBindCode;

    @Operation(summary = "WPS表单回调", description = "接收WPS表单数据推送事件（bind/create_answer），bind返回验证码，create_answer自动创建用户")
    @RequiresPermission(value = "wps:callback", name = "WPS表单回调", access = AccessLevel.PUBLIC, audit = false)
    @RateLimit(interval = 1)  // 每秒最多 1 次（WPS 重试频繁）
    @PostMapping(value = "/callback", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> handleCallback(
            @RequestParam(value = "bind_code", required = false) String bindCodeFromQuery,
            @RequestBody(required = false) Map<String, Object> rawBody,
            HttpServletRequest httpRequest) {

        // 1. bind_code 来自查询参数（?bind_code=xxx）
        if (bindCodeFromQuery != null && !bindCodeFromQuery.isBlank()) {
            log.info("WPS 表单绑定验证(查询参数): bind_code={}", bindCodeFromQuery);
            return ResponseEntity.ok(Collections.singletonMap("bind_code", bindCodeFromQuery));
        }

        // 2. bind_code 来自请求体（WPS 绑定验证会 POST: {"bind_code":"..."}）
        if (rawBody != null && rawBody.containsKey("bind_code")) {
            String bindCode = rawBody.get("bind_code").toString();
            log.info("WPS 表单绑定验证(请求体): bind_code={}", bindCode);
            return ResponseEntity.ok(Collections.singletonMap("bind_code", bindCode));
        }

        // 3. 验证 API Secret（如果已配置）
        String providedSecret = httpRequest.getHeader(SECRET_HEADER);
        if (wpsSecret != null && !wpsSecret.isBlank()) {
            if (!wpsSecret.equals(providedSecret)) {
                log.warn("WPS 回调 Secret 验证失败");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ResponseMessage.error(HttpStatus.UNAUTHORIZED));
            }
        }

        // 4. 有 event 字段，按事件类型处理
        if (rawBody != null && rawBody.containsKey("event")) {
            WpsCallbackRequestDTO request = objectMapper.convertValue(rawBody, WpsCallbackRequestDTO.class);
            log.info("收到 WPS 表单回调: event={}, rid={}, formTitle={}", request.getEvent(), request.getRid(), request.getFormTitle());

            switch (request.getEvent()) {
                case "bind" -> {
                    // 优先使用配置的 bind_code（WPS 可能不发送 bind_code，只发 event=bind + rid）
                    String bindCode = (wpsBindCode != null && !wpsBindCode.isBlank())
                            ? wpsBindCode
                            : request.getRid();
                    log.info("WPS 表单绑定验证(event=bind): bind_code={}", bindCode);
                    return ResponseEntity.ok(Collections.singletonMap("bind_code", bindCode));
                }
                case "create_answer" -> {
                    handleCreateAnswer(request);
                    return ResponseEntity.ok(ResponseMessage.success(
                            WpsCallbackResponseDTO.builder().build()));
                }
                default -> {
                    log.info("忽略 WPS 表单非目标事件类型: {}", request.getEvent());
                    return ResponseEntity.ok(ResponseMessage.success(
                            WpsCallbackResponseDTO.builder().build()));
                }
            }
        }

        // 5. 无 event 字段 → 返回配置的绑定验证码（WPS 探针测试）
        if (wpsBindCode != null && !wpsBindCode.isBlank()) {
            log.info("WPS 表单绑定验证(默认配置): bind_code={}", wpsBindCode);
            return ResponseEntity.ok(Collections.singletonMap("bind_code", wpsBindCode));
        }

        // 6. 没有 event 也没有配置 → 恶意请求
        log.warn("WPS 回调收到无效请求（无 event 字段，未配置 bind_code）");
        return ResponseEntity.badRequest().body(ResponseMessage.error(HttpStatus.BAD_REQUEST));
    }

    /**
     * 将 WPS 返回值转为字符串，处理字符串和数组两种类型（如多选返回 ["a","b"]）。
     */
    private static String valueToString(Object value) {
        if (value == null) return null;
        if (value instanceof String s) return s;
        if (value instanceof List<?> list) {
            return list.stream().map(Object::toString).collect(Collectors.joining(","));
        }
        return value.toString();
    }

    /**
     * 处理 create_answer 事件：解析表单字段并创建用户。
     */
    private void handleCreateAnswer(WpsCallbackRequestDTO request) {
        List<WpsCallbackRequestDTO.AnswerContent> answers = request.getAnswerContents();
        if (answers == null || answers.isEmpty()) {
            log.warn("WPS 表单 create_answer 无回答内容，跳过");
            return;
        }

        // 将 answerContents 按 title 转成 Map
        Map<String, String> fieldMap = answers.stream()
                .filter(a -> a.getTitle() != null && a.getValue() != null)
                .collect(Collectors.toMap(
                        WpsCallbackRequestDTO.AnswerContent::getTitle,
                        a -> valueToString(a.getValue()),
                        (a, b) -> a));

        String studentId = fieldMap.get(WpsFormCommands.FIELD_STUDENT_ID);
        String username = fieldMap.get(WpsFormCommands.FIELD_USERNAME);
        String email = fieldMap.get(WpsFormCommands.FIELD_EMAIL);
        String directionText = fieldMap.get(WpsFormCommands.FIELD_DIRECTION);
        String major = fieldMap.get(WpsFormCommands.FIELD_MAJOR);

        // 校验必填字段
        if (studentId == null || studentId.isBlank()) {
            log.warn("WPS 表单缺少必填字段: {}", WpsFormCommands.FIELD_STUDENT_ID);
            return;
        }
        if (username == null || username.isBlank()) {
            log.warn("WPS 表单缺少必填字段: {}", WpsFormCommands.FIELD_USERNAME);
            return;
        }
        if (email == null || email.isBlank()) {
            log.warn("WPS 表单缺少必填字段: {}", WpsFormCommands.FIELD_EMAIL);
            return;
        }
        if (directionText == null || directionText.isBlank()) {
            log.warn("WPS 表单缺少必填字段: {}", WpsFormCommands.FIELD_DIRECTION);
            return;
        }

        Direction direction = resolveDirection(directionText);
        if (direction == null) {
            log.warn("WPS 表单方向字段值无效: {}", directionText);
            return;
        }

        WpsFormCommands.CreateUserFromWpsCommand command = WpsFormCommands.CreateUserFromWpsCommand.builder()
                .studentId(studentId)
                .username(username)
                .email(email)
                .direction(direction)
                .major(major)
                .build();

        try {
            wpsFormAppService.createUserFromWpsForm(command);
            log.info("WPS 表单创建用户成功: studentId={}, username={}, email={}", studentId, username, email);
        } catch (DataConflict e) {
            log.warn("WPS 表单创建用户跳过（已有用户）: {}", e.getMessage());
        }
    }
}
