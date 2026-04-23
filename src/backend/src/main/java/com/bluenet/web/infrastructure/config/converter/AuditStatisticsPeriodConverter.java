package com.bluenet.web.infrastructure.config.converter;

import com.bluenet.web.domain.model.enumerate.AuditStatisticsPeriod;
import org.springframework.core.convert.converter.Converter;

/**
 * 审计统计周期枚举转换器
 * <p>
 * 将 API 层的友好字符串（如 24h、7d、30d）转换为领域枚举， 同时也兼容枚举常量名（H24、D7、D30）。
 * </p>
 */
public class AuditStatisticsPeriodConverter implements Converter<String, AuditStatisticsPeriod> {

    @Override
    public AuditStatisticsPeriod convert(String source) {
        if (source == null || source.isEmpty()) {
            return null;
        }
        return AuditStatisticsPeriod.fromString(source);
    }
}
