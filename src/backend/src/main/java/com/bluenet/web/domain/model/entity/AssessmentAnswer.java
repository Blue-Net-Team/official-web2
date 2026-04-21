package com.bluenet.web.domain.model.entity;

import com.bluenet.web.domain.model.enumerate.ProgrammingLanguage;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AssessmentAnswer {
    /**
     * 当前对象在系统中的唯一标识。
     */
    private Long id;
    /**
     * 关联用户标识。
     */
    private Long userId;
    /**
     * 考核题目标识。
     */
    private Long questionId;
    /**
     * 正文内容、题目内容或结构化配置内容。
     */
    private String content;
    /**
     * 提交代码使用的编程语言。
     */
    private ProgrammingLanguage language;
    /**
     * 关联文件记录标识。
     */
    private Long fileId;
    /**
     * 答案提交时间。
     */
    private LocalDateTime submitTime;
}
