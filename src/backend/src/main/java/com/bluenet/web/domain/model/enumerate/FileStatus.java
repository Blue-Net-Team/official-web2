package com.bluenet.web.domain.model.enumerate;

import lombok.Getter;

@Getter
public enum FileStatus implements ValueEnum {
    PENDING("pending", "待上传"),
    ACTIVE("active", "已激活"),
    REJECTED("rejected", "已拒绝");

    private final String value;
    private final String description;

    FileStatus(String value, String description) {
        this.value = value;
        this.description = description;
    }
}
