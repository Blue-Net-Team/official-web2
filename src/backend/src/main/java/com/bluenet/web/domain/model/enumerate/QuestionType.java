package com.bluenet.web.domain.model.enumerate;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum QuestionType {
    SINGLE_CHOICE("single_choice", "单选题"),
    MULTIPLE_CHOICE("multiple_choice", "多选题"),
    FILE_UPLOAD("file_upload", "文件上传"),
    ALGORITHM("algorithm", "算法题");

    @EnumValue
    @JsonValue
    private final String value;
    private final String description;

    QuestionType(String value, String description) {
        this.value = value;
        this.description = description;
    }
}
