package com.bluenet.web.domain.model.vo.evaluation;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Data;

@Data
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({ @JsonSubTypes.Type(value = FileUploadContent.class, name = "file_upload"),
        @JsonSubTypes.Type(value = SingleChoiceContent.class, name = "single_choice"),
        @JsonSubTypes.Type(value = MultipleChoiceContent.class, name = "multiple_choice"),
        @JsonSubTypes.Type(value = AlgorithmContent.class, name = "algorithm") })
public abstract class QuestionContent {
    private String content;
}
