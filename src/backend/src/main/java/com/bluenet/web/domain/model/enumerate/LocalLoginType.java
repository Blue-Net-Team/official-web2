package com.bluenet.web.domain.model.enumerate;

import lombok.Getter;

/**
 * 本地登录类型
 */
@Getter
public enum LocalLoginType implements ValueEnum {
    STUDENT_ID("student_id", "学号登录"), // 学生信息系统登录
    EMAIL("email", "邮箱登录"); // 邮箱登录;

    private final String value;
    private final String description;

    LocalLoginType(String value, String description) {
        this.value = value;
        this.description = description;
    }
}
