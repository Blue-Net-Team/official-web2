package com.bluenet.web.domain.model.entity;

import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 审计日志聚合根
 * <p>
 * 承载审计日志相关的业务规则和行为
 * </p>
 */
@Data
@NoArgsConstructor(access = AccessLevel.PRIVATE)
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

    private Audit(Long id, String requestMethod, String requestUri, String requestUriPattern,
            String actionArg, Long actionUserId, LocalDateTime actionTime,
            String ipAddress, String userAgent, Integer httpStatus,
            String responseMessage, String stackTrace, Long durationMs,
            Boolean successState) {
        this.id = id;
        this.requestMethod = requestMethod;
        this.requestUri = requestUri;
        this.requestUriPattern = requestUriPattern;
        this.actionArg = actionArg;
        this.actionUserId = actionUserId;
        this.actionTime = actionTime;
        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
        this.httpStatus = httpStatus;
        this.responseMessage = responseMessage;
        this.stackTrace = stackTrace;
        this.durationMs = durationMs;
        this.successState = successState;
    }

    /**
     * 构造新审计日志 —— 带领域校验
     *
     * @param requestMethod
     *            HTTP 请求方法
     * @param requestUri
     *            原始请求路径
     * @param requestUriPattern
     *            归一化请求路径
     * @param actionArg
     *            业务参数
     * @param actionUserId
     *            用户标识
     * @param ipAddress
     *            客户端 IP
     * @param userAgent
     *            客户端 User-Agent
     * @return 新的审计日志实体
     */
    public static Audit create(String requestMethod, String requestUri, String requestUriPattern,
            String actionArg, Long actionUserId, String ipAddress, String userAgent) {
        return new Audit(null, requestMethod, requestUri, requestUriPattern,
                actionArg, actionUserId, LocalDateTime.now(),
                ipAddress, userAgent, null, null, null, null, null);
    }

    /**
     * 从数据库重建 —— 跳过创建校验
     *
     * @param id
     *            审计日志ID
     * @param requestMethod
     *            HTTP 请求方法
     * @param requestUri
     *            原始请求路径
     * @param requestUriPattern
     *            归一化请求路径
     * @param actionArg
     *            业务参数
     * @param actionUserId
     *            用户标识
     * @param actionTime
     *            审计时间
     * @param ipAddress
     *            客户端 IP
     * @param userAgent
     *            客户端 User-Agent
     * @param httpStatus
     *            HTTP 状态码
     * @param responseMessage
     *            响应消息
     * @param stackTrace
     *            异常堆栈
     * @param durationMs
     *            耗时
     * @param successState
     *            是否成功
     * @return 重建的审计日志实体
     */
    public static Audit reconstruct(Long id, String requestMethod, String requestUri, String requestUriPattern,
            String actionArg, Long actionUserId, LocalDateTime actionTime,
            String ipAddress, String userAgent, Integer httpStatus,
            String responseMessage, String stackTrace, Long durationMs,
            Boolean successState) {
        return new Audit(id, requestMethod, requestUri, requestUriPattern,
                actionArg, actionUserId, actionTime,
                ipAddress, userAgent, httpStatus,
                responseMessage, stackTrace, durationMs, successState);
    }

    /**
     * 记录成功响应
     *
     * @param httpStatus
     *            HTTP 状态码
     * @param responseMessage
     *            响应消息
     */
    public void recordSuccess(Integer httpStatus, String responseMessage) {
        this.httpStatus = httpStatus;
        this.responseMessage = responseMessage;
        this.successState = true;
    }

    /**
     * 记录失败响应
     *
     * @param httpStatus
     *            HTTP 状态码
     * @param responseMessage
     *            响应消息
     * @param stackTrace
     *            异常堆栈
     */
    public void recordFailure(Integer httpStatus, String responseMessage, String stackTrace) {
        this.httpStatus = httpStatus;
        this.responseMessage = responseMessage;
        this.stackTrace = stackTrace;
        this.successState = false;
    }

    /**
     * 设置请求耗时
     *
     * @param durationMs
     *            耗时（毫秒）
     */
    public void setDuration(Long durationMs) {
        this.durationMs = durationMs;
    }
}
