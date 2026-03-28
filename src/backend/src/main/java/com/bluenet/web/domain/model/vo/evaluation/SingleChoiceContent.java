package com.bluenet.web.domain.model.vo.evaluation;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class SingleChoiceContent extends QuestionContent {
    private List<String> options;
    private String correctAnswer;
}
