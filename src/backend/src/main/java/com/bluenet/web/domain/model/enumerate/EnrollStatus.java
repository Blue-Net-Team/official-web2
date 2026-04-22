package com.bluenet.web.domain.model.enumerate;

import lombok.Getter;

@Getter
public enum EnrollStatus implements ValueEnum {
    PENDING("pending", "待审核"),
    APPROVED("approved", "已通过"),
    REJECTED("rejected", "已拒绝");

    private final String value;
    private final String description;

    EnrollStatus(String value, String description) {
        this.value = value;
        this.description = description;
    }
}
