package com.bluenet.web.domain.model.enumerate;

import com.baomidou.mybatisplus.annotation.EnumValue;
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
}
