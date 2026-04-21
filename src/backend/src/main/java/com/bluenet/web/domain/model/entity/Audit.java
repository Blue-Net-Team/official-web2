package com.bluenet.web.domain.model.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Audit {
    /**
     * 当前对象在系统中的唯一标识。
     */
    private Long id;
    /**
     * 审计记录中的 HTTP 请求方法。
     */
    private String requestMethod;
    /**
     * 审计记录中的原始请求路径。
     */
    private String requestUri;
    /**
     * 审计统计使用的归一化请求路径。
     */
    private String requestUriPattern;
    /**
     * 审计动作携带的业务参数或补充上下文。
     */
    private String actionArg;
    /**
     * 执行审计动作的用户标识。
     */
    private Long actionUserId;
    /**
     * 审计动作发生的时间。
     */
    private LocalDateTime actionTime;
    /**
     * 发起请求的客户端 IP 地址。
     */
    private String ipAddress;
    /**
     * 发起请求的客户端 User-Agent。
     */
    private String userAgent;
    /**
     * 接口响应的 HTTP 状态码。
     */
    private Integer httpStatus;
    /**
     * 接口返回的业务消息。
     */
    private String responseMessage;
    /**
     * 异常堆栈信息，用于问题排查。
     */
    private String stackTrace;
    /**
     * 本次请求或操作耗时，单位毫秒。
     */
    private Long durationMs;
    /**
     * 审计动作是否执行成功。
     */
    private Boolean successState;
}
