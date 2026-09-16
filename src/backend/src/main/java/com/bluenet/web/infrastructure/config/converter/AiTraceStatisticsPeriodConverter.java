package com.bluenet.web.infrastructure.config.converter;

import com.bluenet.web.domain.model.enumerate.AiTraceStatisticsPeriod;
import org.springframework.core.convert.converter.Converter;

/**
 * AI 轨迹统计周期枚举转换器。
 *
 * <p>
 * 将 API 层的友好字符串（如 24h、7d、30d）转换为领域枚举，同时也兼容枚举常量名。
 * </p>
 */
public class AiTraceStatisticsPeriodConverter implements Converter<String, AiTraceStatisticsPeriod> {

    @Override
    public AiTraceStatisticsPeriod convert(String source) {
        if (source == null || source.isEmpty()) {
            return null;
        }
        return AiTraceStatisticsPeriod.fromString(source);
    }
}
