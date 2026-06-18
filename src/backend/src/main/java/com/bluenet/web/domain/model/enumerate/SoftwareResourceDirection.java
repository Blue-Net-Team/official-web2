package com.bluenet.web.domain.model.enumerate;

import lombok.Getter;

/**
 * 软件资源方向枚举。
 * <p>
 * 表示软件资源所属的方向分类，与表示成员技术方向的 {@link Direction} 不同， 软件资源允许“通用”分类，用于跨方向共享的软件工具。
 * </p>
 */
@Getter
public enum SoftwareResourceDirection implements ValueEnum {

    COMPUTER_VISION("computer_vision", "计算机视觉"),
    STRUCTURAL_DESIGN("structural_design", "结构设计"),
    EMBEDDED("embedded", "嵌入式开发"),
    GENERAL("general", "通用");

    private final String value;
    private final String description;

    SoftwareResourceDirection(String value, String description) {
        this.value = value;
        this.description = description;
    }
}
