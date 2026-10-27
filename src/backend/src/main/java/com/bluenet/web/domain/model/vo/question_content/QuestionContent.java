package com.bluenet.web.domain.model.vo.question_content;

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
    /**
     * 正文内容、题目内容或结构化配置内容。
     */
    private String content;

    /**
     * 擦除不应暴露给考生的敏感信息（如正确答案、评测用例等）。 每个子类需实现具体的擦除逻辑。
     */
    public abstract void sanitizeForUser();

    /**
     * 创建包含独立嵌套集合和值对象的深复制。
     *
     * @return 与当前内容互不影响的副本
     */
    public abstract QuestionContent deepCopy();
}
