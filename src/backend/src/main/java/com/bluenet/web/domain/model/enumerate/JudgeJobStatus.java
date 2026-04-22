package com.bluenet.web.domain.model.enumerate;

import lombok.Getter;

/**
 * Represents infrastructure-level state for algorithm judge jobs.
 */
@Getter
public enum JudgeJobStatus implements ValueEnum {
    PENDING("PENDING", "等待执行"),
    RUNNING("RUNNING", "执行中"),
    RETRYING("RETRYING", "等待重试"),
    SUCCEEDED("SUCCEEDED", "执行成功"),
    FAILED_REVIEW_REQUIRED("FAILED_REVIEW_REQUIRED", "需要人工排查");

    private final String value;
    private final String description;

    JudgeJobStatus(String value, String description) {
        this.value = value;
        this.description = description;
    }
}
