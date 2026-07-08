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

    /**
     * 更新答案内容、编程语言或文件，并刷新提交时间。
     *
     * @param content
     *            答案内容，非 null 时更新
     * @param language
     *            编程语言，非 null 时更新
     * @param fileId
     *            文件 ID，非 null 时更新
     */
    public void update(String content, ProgrammingLanguage language, Long fileId) {
        if (content != null) {
            this.content = content;
        }
        if (language != null) {
            this.language = language;
        }
        if (fileId != null) {
            this.fileId = fileId;
        }
        this.submitTime = LocalDateTime.now();
    }
}
