package com.bluenet.web.application;

import com.bluenet.web.domain.model.enumerate.Direction;
import com.bluenet.web.domain.model.enumerate.SoftwareResourceStatus;

/**
 * 软件资源聚合的应用层结果对象。
 */
public record SoftwareResourceResult(
        Long id,
        String name,
        Direction direction,
        String category,
        String description,
        String externalUrl,
        Integer sortOrder,
        SoftwareResourceStatus status) {
}
