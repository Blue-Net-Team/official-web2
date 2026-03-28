package com.bluenet.web.domain.model.vo.evaluation;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class MultipleChoiceContent extends QuestionContent {
    private List<String> options;
    private List<String> correctAnswers;
}
