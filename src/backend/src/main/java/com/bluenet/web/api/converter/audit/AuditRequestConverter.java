package com.bluenet.web.api.converter.audit;

import org.springframework.stereotype.Component;

/**
 * 审计日志请求转换器
 * <p>
 * 负责将 API 层的 RequestDTO 转换为应用层的 Command
 * </p>
 */
@Component
public class AuditRequestConverter {

    // 审计日志由切面自动创建，暂无需 API 请求转换方法
}
