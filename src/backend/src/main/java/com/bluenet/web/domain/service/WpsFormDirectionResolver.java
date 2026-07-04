package com.bluenet.web.domain.service;

import com.bluenet.web.domain.model.enumerate.Direction;

/**
 * WPS 表单方向字段解析器。
 * <p>
 * 将 WPS 表单中的方向题标题映射为系统 {@link Direction} 枚举，支持短名称（如"结构"）和完整描述匹配。
 * </p>
 */
public interface WpsFormDirectionResolver {

    /**
     * 通过中文描述查找对应的 Direction 枚举。
     *
     * @param description
     *            中文方向描述
     * @return 匹配的 Direction，若未匹配则返回 null
     */
    Direction resolve(String description);
}
