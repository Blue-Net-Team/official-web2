package com.bluenet.web.domain.model.enumerate;

import lombok.Getter;

/**
 * 软件资源状态。
 */
@Getter
public enum SoftwareResourceStatus implements ValueEnum {
    ACTIVE("active", "已启用"),
    DISABLED("disabled", "已禁用");

    private final String value;
    private final String description;

    SoftwareResourceStatus(String value, String description) {
        this.value = value;
        this.description = description;
    }
}
