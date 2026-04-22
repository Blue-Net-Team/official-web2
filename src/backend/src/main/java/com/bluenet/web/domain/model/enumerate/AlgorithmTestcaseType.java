package com.bluenet.web.domain.model.enumerate;

import lombok.Getter;

/**
 * Separates run-only cases from formal submission cases.
 */
@Getter
public enum AlgorithmTestcaseType implements ValueEnum {
    DEFAULT_RUN("DEFAULT_RUN", "默认运行用例"),
    CUSTOM_RUN("CUSTOM_RUN", "自定义运行输入"),
    FORMAL("FORMAL", "正式判题用例");

    private final String value;
    private final String description;

    AlgorithmTestcaseType(String value, String description) {
        this.value = value;
        this.description = description;
    }
}
