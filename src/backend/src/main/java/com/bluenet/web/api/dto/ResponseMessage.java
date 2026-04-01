package com.bluenet.web.api.dto;

import org.springframework.http.HttpStatus;

import com.bluenet.web.domain.exception.GlobalException;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * 统一 API 响应体封装。
 *
 * @param <T>
 *            载荷类型
 */
@Schema(description = "统一 API 响应包装，所有接口均返回此结构")
@Setter
@Getter
public class ResponseMessage<T> {
    @Schema(description = "业务/HTTP 状态码，成功时为 200，错误时为对应 HTTP 状态码（如 401、500）", example = "200")
    private Integer code;
    @Schema(description = "提示信息，成功多为 Success，错误时为具体原因", example = "Success")
    private String msg;
    @Schema(description = "业务数据，成功时存在，错误时通常为 null")
    private T data;

    /**
     * 创建空响应。
     */
    public ResponseMessage() {
    }

    /**
     * 创建带完整字段的响应。
     *
     * @param code
     *            响应码
     * @param msg
     *            响应消息
     * @param data
     *            响应数据
     */
    public ResponseMessage(Integer code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    /**
     * 创建默认成功响应。
     *
     * @param data
     *            响应数据
     * @param <T>
     *            载荷类型
     * @return 成功响应
     */
    public static <T> ResponseMessage<T> success(T data) {
        return new ResponseMessage<>(HttpStatus.OK.value(), "Success", data);
    }

    /**
     * 创建自定义消息的成功响应。
     *
     * @param msg
     *            响应消息
     * @param data
     *            响应数据
     * @param <T>
     *            载荷类型
     * @return 成功响应
     */
    public static <T> ResponseMessage<T> success(String msg, T data) {
        return new ResponseMessage<>(HttpStatus.OK.value(), msg, data);
    }

    /**
     * 创建空载荷成功响应。
     *
     * @param <T>
     *            载荷类型
     * @return 成功响应
     */
    public static <T> ResponseMessage<T> success() {
        return success(null);
    }

    /**
     * 使用预定义错误码创建错误响应。
     *
     * @param status
     *            响应状态
     * @param <T>
     *            载荷类型
     * @return 错误响应
     */
    public static <T> ResponseMessage<T> error(HttpStatus status) {
        return new ResponseMessage<>(status.value(), status.getReasonPhrase(), null);
    }

    /**
     * 创建自定义错误响应。
     *
     * @param code
     *            响应码
     * @param msg
     *            响应消息
     * @param <T>
     *            载荷类型
     * @return 错误响应
     */
    public static <T> ResponseMessage<T> error(Integer code, String msg) {
        return new ResponseMessage<>(code, msg, null);
    }

    /**
     * 创建自定义错误响应。
     *
     * @param code
     *            响应码
     * @param msg
     *            响应消息
     * @param data
     *            响应体
     * @param <T>
     *            载荷类型
     * @return 错误响应
     */
    public static <T> ResponseMessage<T> error(Integer code, String msg, T data) {
        return new ResponseMessage<>(code, msg, data);
    }

    public static <T> ResponseMessage<T> error(GlobalException e) {
        return new ResponseMessage<>(e.getCode().value(), e.getMessage(), null);
    }
}
