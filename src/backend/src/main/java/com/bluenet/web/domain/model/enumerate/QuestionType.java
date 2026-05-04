package com.bluenet.web.domain.model.enumerate;

import lombok.Getter;

@Getter
public enum QuestionType implements ValueEnum {
    SINGLE_CHOICE("single_choice", "单选题"),
    MULTIPLE_CHOICE("multiple_choice", "多选题"),
    FILE_UPLOAD("file_upload", "文件上传"),
    ALGORITHM("algorithm", "算法题");

    private final String value;
    private final String description;

    QuestionType(String value, String description) {
        this.value = value;
        this.description = description;
    }

    public boolean isChoiceQuestion() {
        return this == SINGLE_CHOICE || this == MULTIPLE_CHOICE;
    }
}
