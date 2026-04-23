package com.bluenet.web.application.command.audit;

import java.time.LocalDateTime;

/**
 * 审计日志聚合的命令对象集合。
 * <p>
 * 定义了该聚合下所有应用层操作所需的命令参数。
 * </p>
 */
public final class AuditCommands {

    /** 禁止实例化。 */
    private AuditCommands() {
    }

    /**
     * 保存审计日志命令。
     * <p>
     * 用于保存系统操作审计日志。
     * </p>
     */
    public record SaveAuditCommand(
            /** 请求方法 */
            String requestMethod,
            /** 请求URI */
            String requestUri,
            /** 请求URI模式 */
            String requestUriPattern,
            /** 操作参数 */
            String actionArg,
            /** 操作用户ID */
            Long actionUserId,
            /** 操作时间 */
            LocalDateTime actionTime,
            /** IP地址 */
            String ipAddress,
            /** 用户代理 */
            String userAgent,
            /** HTTP状态码 */
            Integer httpStatus,
            /** 响应消息 */
            String responseMessage,
            /** 堆栈跟踪 */
            String stackTrace,
            /** 耗时(毫秒) */
            Long durationMs,
            /** 是否成功 */
            Boolean successState) {
    }
}
