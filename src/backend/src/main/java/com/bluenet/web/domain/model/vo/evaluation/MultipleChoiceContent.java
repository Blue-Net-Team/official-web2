package com.bluenet.web.domain.model.vo.evaluation;

import lombok.Data;
import lombok.EqualsAndHashCode;

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
}
