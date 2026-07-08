package com.bluenet.web.domain.model.vo.question_content;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class SingleChoiceContent extends QuestionContent {
    /**
     * 选择题选项列表。
     */
    private List<String> options;
    /**
     * 单选题的正确选项标识。
     */
    private String correctAnswer;

    @Override
    public void sanitizeForUser() {
        this.correctAnswer = null;
    }
}
