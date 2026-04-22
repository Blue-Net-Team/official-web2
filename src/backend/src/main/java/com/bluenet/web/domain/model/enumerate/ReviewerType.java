package com.bluenet.web.domain.model.enumerate;

import lombok.Getter;

/**
 * Classifies the actor that produced or owns a judgement.
 */
@Getter
public enum ReviewerType implements ValueEnum {
    SYSTEM("SYSTEM", "系统"),
    MEMBER("MEMBER", "团队成员"),
    DIRECTION_ADMIN("DIRECTION_ADMIN", "方向管理员"),
    SUPER_ADMIN("SUPER_ADMIN", "超级管理员");

    private final String value;
    private final String description;

    ReviewerType(String value, String description) {
        this.value = value;
        this.description = description;
    }
}
