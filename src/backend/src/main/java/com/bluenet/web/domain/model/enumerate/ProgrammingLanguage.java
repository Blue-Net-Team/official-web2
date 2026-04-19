package com.bluenet.web.domain.model.enumerate;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum ProgrammingLanguage {
    PYTHON("python",
            "Python"),
    C("c", "C"),
    CPP("cpp", "C++"),
    JAVA("java", "Java"),
    JAVASCRIPT("javascript", "JavaScript");

    @EnumValue
    private final String value;
    private final String description;

    ProgrammingLanguage(String value, String description) {
        this.value = value;
        this.description = description;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static ProgrammingLanguage fromValue(String value) {
        if (value == null) {
            return null;
        }
        // 前端按 starterCode 的 key 提交小写语言值，后端同时兼容枚举名。
        for (ProgrammingLanguage language : values()) {
            if (language.value.equalsIgnoreCase(value) || language.name().equalsIgnoreCase(value)) {
                return language;
            }
        }
        throw new IllegalArgumentException("不支持的编程语言：" + value);
    }
}
