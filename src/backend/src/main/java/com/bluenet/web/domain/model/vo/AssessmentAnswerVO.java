package com.bluenet.web.domain.model.vo;

import com.bluenet.web.domain.model.enumerate.ProgrammingLanguage;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class AssessmentAnswerVO {
    private Long id;
    private Long userId;
    private Long questionId;
    private String content;
    private ProgrammingLanguage language;
    private Long fileId;
    private LocalDateTime submitTime;
}
