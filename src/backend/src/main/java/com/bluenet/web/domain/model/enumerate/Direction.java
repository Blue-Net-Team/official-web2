package com.bluenet.web.domain.model.enumerate;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;

@Getter
public enum Direction {
    COMPUTER_VISION("computer_vision", "计算机视觉"),
    STRUCTURAL_DESIGN("structural_design", "结构设计"),
    EMBEDDED("embedded", "嵌入式开发");

    @EnumValue
    private final String value;
    private final String description;

    Direction(String value, String description) {
        this.value = value;
        this.description = description;
    }
}
