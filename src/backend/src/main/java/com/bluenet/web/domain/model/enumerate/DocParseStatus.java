package com.bluenet.web.domain.model.enumerate;

import lombok.Getter;

/**
 * 知识库文档解析状态枚举。
 */
@Getter
public enum DocParseStatus implements ValueEnum {
    PENDING("pending", "待解析"),
    PARSING("parsing", "解析中"),
    COMPLETED("completed", "已完成"),
    FAILED("failed", "失败"),
    CANCELING("canceling", "取消中"),
    CANCELED("canceled", "已取消");

    private final String value;
    private final String description;

    DocParseStatus(String value, String description) {
        this.value = value;
        this.description = description;
    }
}
