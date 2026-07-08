package com.bluenet.web.application.result.audit;

/**
 * 审计日志聚合的应用层结果对象。
 * <p>
 * 封装了审计日志相关操作返回给 API 层的数据。
 * </p>
 */
public final class AuditResult {

    private AuditResult() {
        // 工具类，禁止实例化
    }

    /**
     * 保存审计日志结果。
     */
    public record SaveAuditResult(
            /** 唯一标识 */
            Long id) {
    }
}
