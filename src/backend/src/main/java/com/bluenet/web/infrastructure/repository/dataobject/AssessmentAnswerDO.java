package com.bluenet.web.infrastructure.repository.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.bluenet.web.domain.model.enumerate.ProgrammingLanguage;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Mapper 专用数据对象，只承载数据库表字段，避免持久层依赖领域实体行为。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("tb_assessment_answer")
public class AssessmentAnswerDO {
    /**
     * 当前对象在系统中的唯一标识。
     */
    @TableId(type = IdType.AUTO)
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
