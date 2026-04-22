package com.bluenet.web.application.service.auth.strategy;

/**
 * 认证策略类型，认证门面只依赖该枚举选择具体 provider。
 */
public enum AuthProviderType {
    STUDENT_ID,
    EMAIL_CODE,
    GITHUB
}
