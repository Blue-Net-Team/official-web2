package com.bluenet.web.application.result.softwareresource;

import com.bluenet.web.domain.model.enumerate.SoftwareResourceDirection;
import com.bluenet.web.domain.model.enumerate.SoftwareResourceStatus;

/**
 * 软件资源聚合的应用层结果对象。
 */
public record SoftwareResourceResult(
        Long id,
        String name,
        SoftwareResourceDirection direction,
        String category,
        String description,
        String externalUrl,
        Integer sortOrder,
        SoftwareResourceStatus status) {
}
