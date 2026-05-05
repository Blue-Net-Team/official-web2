package com.bluenet.web.domain.model.enumerate;

import lombok.Getter;

@Getter
public enum BugReportStatus implements ValueEnum {
    PENDING("pending", "待处理"),
    IN_PROGRESS("in_progress", "处理中"),
    RESOLVED("resolved", "已解决");

    private final String value;
    private final String description;

    BugReportStatus(String value, String description) {
        this.value = value;
        this.description = description;
    }
}
