package com.bluenet.web.api.dto;

import java.util.Objects;

import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import jakarta.servlet.http.HttpServletResponse;

/**
 * 统一包装控制器返回值为 {@link ResponseMessage}，并对特定类型豁免。 不包装 springdoc
 * 文档端点（/v3/api-docs、/swagger-ui）的响应，避免 OpenAPI JSON 被包装导致 500。
 */
@RestControllerAdvice
public class ApiResponseAdvice implements ResponseBodyAdvice<Object> {

    /**
     * 判断是否需要包装响应。
     *
     * @param returnType
     *            方法返回类型
     * @param converterType
     *            转换器类型
     * @return true 表示启用包装
     */
    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        return true;
    }

    /**
     * 在非豁免场景下，将响应体包装为 {@link ResponseMessage}。
     *
     * @param body
     *            响应体
     * @param returnType
     *            方法返回类型
     * @param selectedContentType
     *            内容类型
     * @param selectedConverterType
     *            转换器类型
     * @param request
     *            请求
     * @param response
     *            响应
     * @return 包装后的响应或原始响应
     */
    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType,
            Class<? extends HttpMessageConverter<?>> selectedConverterType, ServerHttpRequest request,
            ServerHttpResponse response) {
        String path = request.getURI().getPath();
        if (path != null && (path.contains("/v3/api-docs") || path.startsWith("/swagger-ui"))) {
            return body;
        }

        if (response instanceof ServletServerHttpResponse servletResponse) {
            HttpServletResponse rawResponse = servletResponse.getServletResponse();
            if (rawResponse.getContentType() != null && rawResponse.getContentType().toLowerCase().contains("stream")) {
                return body;
            }
        }

        if (Objects.equals(selectedContentType, MediaType.APPLICATION_OCTET_STREAM)) {
            return body;
        }

        if (body instanceof ResponseMessage) {
            return body;
        }

        if (body instanceof org.springframework.core.io.Resource) {
            return body;
        }

        return ResponseMessage.success(body);
    }
}
