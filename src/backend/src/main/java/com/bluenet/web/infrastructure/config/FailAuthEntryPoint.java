package com.bluenet.web.infrastructure.config;

import com.bluenet.web.api.dto.ResponseMessage;
import com.bluenet.web.domain.exception.GlobalException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class FailAuthEntryPoint implements AuthenticationEntryPoint {
    ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
            AuthenticationException authException) throws IOException, ServletException {
        response.setStatus(403);
        response.setContentType("application/json;charset=UTF-8");
        ResponseMessage<Object> responseMessage = ResponseMessage.error(403, authException.getMessage());
        response.getWriter().write(objectMapper.writeValueAsString(responseMessage));
    }

    public void commence(HttpServletRequest request, HttpServletResponse response, GlobalException exception)
            throws IOException, ServletException {
        response.setStatus(exception.getCode().value());
        response.setContentType("application/json;charset=UTF-8");
        ResponseMessage<Object> responseMessage = ResponseMessage.error(exception);
        response.getWriter().write(objectMapper.writeValueAsString(responseMessage));
    }
}
