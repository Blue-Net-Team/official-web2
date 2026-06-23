package com.bluenet.web.domain.model.enumerate;

import lombok.Getter;

@Getter
public enum Gender implements ValueEnum {
    MALE("male", "男"),
    FEMALE("female", "女"),
    UNKNOWN("unknown", "未知");

    private final String value;
    private final String description;

    Gender(String value, String description) {
        this.value = value;
        this.description = description;
    }

    /**
     * 根据中文描述解析性别枚举。
     *
     * @param description
     *            中文描述，如"男"、"女"
     * @return 对应的 Gender 枚举，无法识别时返回 {@link #UNKNOWN}
     */
    public static Gender fromDescription(String description) {
        if (description == null || description.isBlank()) {
            return UNKNOWN;
        }
        return switch (description.trim()) {
            case "男" -> MALE;
            case "女" -> FEMALE;
            default -> UNKNOWN;
        };
    }
}
