package com.bluenet.web.domain.model.vo.question_content;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class MultipleChoiceContent extends QuestionContent {
    /**
     * 选择题选项列表。
     */
    private List<String> options;
    /**
     * 多选题的正确选项标识集合。
     */
    private List<String> correctAnswers;

    @Override
    public void sanitizeForUser() {
        this.correctAnswers = null;
    }

    @Override
    public MultipleChoiceContent deepCopy() {
        MultipleChoiceContent copy = new MultipleChoiceContent();
        copy.setContent(getContent());
        copy.setOptions(options == null ? null : new ArrayList<>(options));
        copy.setCorrectAnswers(correctAnswers == null ? null : new ArrayList<>(correctAnswers));
        return copy;
    }
}
