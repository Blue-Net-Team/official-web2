package com.bluenet.web.domain.model.entity;

import com.bluenet.web.domain.model.enumerate.ProgrammingLanguage;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class AssessmentAnswer {
    private Long id;
    private Long userId;
    private Long questionId;
    private String content;
    private ProgrammingLanguage language;
    private Long fileId;
    private LocalDateTime submitTime;
    private Long teamId;

    private AssessmentAnswer(Long id, Long userId, Long questionId, String content,
            ProgrammingLanguage language, Long fileId, LocalDateTime submitTime, Long teamId) {
        this.id = id;
        this.userId = userId;
        this.questionId = questionId;
        this.content = content;
        this.language = language;
        this.fileId = fileId;
        this.submitTime = submitTime;
        this.teamId = teamId;
    }

    public static AssessmentAnswer create(Long userId, Long questionId, String content,
            ProgrammingLanguage language, Long fileId) {
        return new AssessmentAnswer(null, userId, questionId, content, language, fileId, LocalDateTime.now(), null);
    }

    public static AssessmentAnswer create(Long userId, Long questionId, String content,
            ProgrammingLanguage language, Long fileId, Long teamId) {
        return new AssessmentAnswer(null, userId, questionId, content, language, fileId, LocalDateTime.now(), teamId);
    }

    public static AssessmentAnswer reconstruct(Long id, Long userId, Long questionId, String content,
            ProgrammingLanguage language, Long fileId, LocalDateTime submitTime, Long teamId) {
        return new AssessmentAnswer(id, userId, questionId, content, language, fileId, submitTime, teamId);
    }
}
