package com.bluenet.web.domain.model.enumerate;

import lombok.Getter;

@Getter
public enum Direction implements ValueEnum {
    COMPUTER_VISION("computer_vision", "计算机视觉"),
    STRUCTURAL_DESIGN("structural_design", "结构设计"),
    EMBEDDED("embedded", "嵌入式开发"),
    GENERAL("general", "通用");

    private final String value;
    private final String description;

    Direction(String value, String description) {
        this.value = value;
        this.description = description;
    }
}
