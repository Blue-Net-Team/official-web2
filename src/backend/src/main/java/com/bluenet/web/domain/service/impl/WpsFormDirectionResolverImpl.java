package com.bluenet.web.domain.service.impl;

import com.bluenet.web.domain.model.enumerate.Direction;
import com.bluenet.web.domain.service.WpsFormDirectionResolver;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * WPS 表单方向字段解析器实现。
 * <p>
 * 将 WPS 表单中的方向题标题映射为系统 {@link Direction} 枚举，支持短名称（如"结构"）和完整描述匹配。
 * </p>
 */
@Slf4j
@Service
public class WpsFormDirectionResolverImpl implements WpsFormDirectionResolver {

    @Override
    public Direction resolve(String description) {
        if (description == null || description.isBlank()) {
            return null;
        }
        String trimmed = description.trim();

        // 先尝试精确匹配完整描述
        for (Direction d : Direction.values()) {
            if (d.getDescription().equals(trimmed)) {
                return d;
            }
        }
        log.warn("未找到匹配的方向描述: {}", description);
        return null;
    }
}
