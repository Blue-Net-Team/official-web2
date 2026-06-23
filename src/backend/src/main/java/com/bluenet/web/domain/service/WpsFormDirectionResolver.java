package com.bluenet.web.domain.service;

import com.bluenet.web.domain.model.enumerate.Direction;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * WPS 表单方向字段解析器。
 * <p>
 * 将 WPS 表单中的方向题标题映射为系统 {@link Direction} 枚举，
 * 支持短名称（如"结构"）和完整描述匹配。
 * </p>
 */
@Slf4j
@Service
public class WpsFormDirectionResolver {

    /**
     * 通过中文描述查找对应的 Direction 枚举。
     *
     * @param description 中文方向描述
     * @return 匹配的 Direction，若未匹配则返回 null
     */
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

        // 再尝试匹配 WPS 表单短名称
        return switch (trimmed) {
            case "结构" -> Direction.STRUCTURAL_DESIGN;
            case "电控" -> Direction.EMBEDDED;
            case "视觉" -> Direction.COMPUTER_VISION;
            default -> {
                log.warn("未找到匹配的方向描述: {}", description);
                yield null;
            }
        };
    }
}
