package com.bluenet.web.api.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

/**
 * ResponseMessage 工厂方法测试。
 */
class ResponseMessageTest {

    /**
     * 验证默认成功响应。
     */
    @Test
    void successFactoryUsesDefaultCodeAndMessage() {
        ResponseMessage<String> response = ResponseMessage.success("payload");
        assertEquals(HttpStatus.OK.value(), response.getCode());
        assertEquals("Success", response.getMsg());
        assertEquals("payload", response.getData());
    }

    /**
     * 验证自定义错误响应。
     */
    @Test
    void errorFactoryUsesProvidedCodeAndMessage() {
        ResponseMessage<Void> response = ResponseMessage.error(HttpStatus.BAD_REQUEST.value(), "Bad");
        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getCode());
        assertEquals("Bad", response.getMsg());
        assertNull(response.getData());
    }
}
